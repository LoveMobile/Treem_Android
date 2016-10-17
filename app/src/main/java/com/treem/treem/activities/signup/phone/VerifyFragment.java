package com.treem.treem.activities.signup.phone;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.treem.treem.R;
import com.treem.treem.application.ApplicationMain;

/**
 * Code verify fragment
 */
public class VerifyFragment extends PhoneBaseFragment {
    private static final String TAG = VerifyFragment.class.getSimpleName();

    //Widgets
    private TextView textPhone; //phone number
    private TextView textStatus; //last resend code time
    private EditText editCode;
    private Button buttonNext;
    private Button buttonResendCode;
    private Button buttonEditPhone;
    private Button buttonClose;

    public VerifyFragment() {
        // Required empty public constructor
    }

    /**
     * Create new instance of this fragment
     * @return new instance of the fragment
     */
    public static VerifyFragment newInstance() {
        VerifyFragment fragment = new VerifyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Get fragment taf
     * @return fragment tag
     */
    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection StatementWithEmptyBody
        if (getArguments() != null) {
            //noting
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        textPhone = (TextView) view.findViewById(R.id.signupVerificationNumberTextView);
        editCode = (EditText) view.findViewById(R.id.signupVerificationEditText);
        buttonNext = (Button) view.findViewById(R.id.signupVerificationNextButton);
        buttonResendCode = (Button) view.findViewById(R.id.signupVerificationResendCode);
        buttonEditPhone = (Button) view.findViewById(R.id.signupVerificationEditPhone);
        textStatus = (TextView)view.findViewById(R.id.signupVerificationLastSentTextView);
        buttonClose = (Button)view.findViewById(R.id.buttonClose);
        if (isEditMode) //show close button if app in edit phone number mode
            buttonClose.setVisibility(View.VISIBLE);
        editCode.requestFocus();
        setWidgetsActions();
    }

    /**
     * Set actions for widgets
     */
    private void setWidgetsActions() {
                /* Events */
        textPhone.setText(newPhone!=null?newPhone:"");
        // Phone number text field changed event
        editCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s==null) {
                    buttonNext.setEnabled(false);
                    return;
                }
                // check if continue button should be enabled (code is at least 3 characters)
                buttonNext.setEnabled(isValidCode(s.toString()));
            }
        });

        // On focus text field changed event
        editCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    ApplicationMain.SHARED_INSTANCE.hideKeyboard(view);
                }
            }
        });
        hideKeyboardOnEnterClick(editCode);

        // Edit phone, take to previous activity
        buttonEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        // Next button tap
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyUserDevice(editCode.getText().toString());
            }
        });

        // Resend verification code button tap
        buttonResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(buttonResendCode, textStatus);
            }
        });
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    /**
     * Check is entered code valid
     * @param code entered code
     * @return true if code valid
     */
    private boolean isValidCode(String code) {
        return code != null && code.length() > 2;
    }

    /**
     * Called when user click done at the keyboard
     * @param v the view in which done clicked
     * @return true if view handle this click
     */
    @Override
    protected boolean onEnterClick(@SuppressWarnings("UnusedParameters") View v) {
        String code = editCode.getText().toString();
        if (isValidCode(code)) {
            verifyUserDevice(code);
            return true;
        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getContext()!=null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}
