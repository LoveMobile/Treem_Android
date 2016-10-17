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

import com.treem.treem.R;
import com.treem.treem.application.ApplicationMain;
import com.treem.treem.helpers.security.Phone.PhoneUtil;

/**
 * Phone entering fragment
 */
public class PhoneFragment extends PhoneBaseFragment {
    private static final String TAG = PhoneFragment.class.getSimpleName();

    //View Widgets
    private EditText editPhone;
    private Button buttonNext;
    private Button buttonClose;

    //entered formatted phone number
    private String formattedPhone;

    public PhoneFragment() {
        // Required empty public constructor
    }

    /**
     * Create new instance of this fragment
     * @return new instance
     */
    public static PhoneFragment newInstance() {
        PhoneFragment fragment = new PhoneFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Get fragment tag
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
        return inflater.inflate(R.layout.fragment_phone, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        editPhone    = (EditText) view.findViewById(R.id.phoneNumberEditText);
        buttonNext     = (Button) view.findViewById(R.id.nextButton);
        buttonClose = (Button)view.findViewById(R.id.buttonClose);
        if (isEditMode) //show close button for edit phone mode
            buttonClose.setVisibility(View.VISIBLE);
        setWidgetsActions();
        String phone = PhoneUtil.getPhone();
        if (phone!=null)
            editPhone.setText(phone);
    }

    /**
     * Set widget actions
     */
    private void setWidgetsActions() {
        // Phone number text field changed event
        editPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean enabled = isValidPhoneNumber(s.toString());

                // check if continue button should be enabled
                buttonNext.setEnabled(enabled);
            }
        });

        // On focus text field changed event
        editPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    ApplicationMain.SHARED_INSTANCE.hideKeyboard(view);
                }
            }
        });
        hideKeyboardOnEnterClick(editPhone);

        // Next button tap
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ApplicationMain.SHARED_INSTANCE.hideKeyboard(view);
                checkPhoneNumber(formattedPhone);
            }
        });

        //close button click
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected boolean onEnterClick(@SuppressWarnings("UnusedParameters") View v) {
        if (isValidPhoneNumber(editPhone.getText().toString())) {
            checkPhoneNumber(formattedPhone);
            return true;
        }
        return false;
    }

    /**
     * Check is entered phone valid
     * @param phone the phone number
     * @return true if valid
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null) {
            return false;
        }
        else if (phone.trim().length() < 1) {
            return false;
        }

        phone = PhoneUtil.getE164FormattedString(phone);

        this.formattedPhone = phone;

        return (phone != null);
    }

    @Override
    public void onResume() {
        super.onResume();
        // set focus for keyboard appearance
        // Note: placed in onResume as the layout is completed at this point
        editPhone.setFocusable(true);
        editPhone.requestFocus();
        if (getContext()!=null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}
