package com.treem.treem.activities.signup;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.treem.treem.R;
import com.treem.treem.activities.main.MainActivity;
import com.treem.treem.activities.signup.phone.SignupPhoneActivity;
import com.treem.treem.application.AppConstants;
import com.treem.treem.application.ApplicationMain;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.TimestampUtils;
import com.treem.treem.helpers.Utils;
import com.treem.treem.helpers.security.Phone.PhoneUtil;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.services.Treem.TreemAuthenticationService;
import com.treem.treem.services.Treem.TreemOAuthUserToken;
import com.treem.treem.services.Treem.TreemService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class SignupRegisterUserActivity extends Activity {

    private static final int MESSAGE_TEXT_CHANGED = 1;
    private static final long DELAY = 500;
    private static final int MIN_USERNAME_LENGTH = 3;
    /**
     * Progress bar
     */
    private LoadingProgressBar saveProgressBar;

    //Activity views
    //Edit fields
    private EditText editUsername;
    private EditText editFirstName;
    private EditText editLastName;
    private EditText editEmail;

    /**
     * Error fields
     */
    private TextView textErrorUsername;
    private TextView textErrorFirstName;
    private TextView textErrorLastName;
    private TextView textErrorEmail;

    /**
     * Birthday fields
     */
    private TextView textBirthday;
    private TextView textErrorBirthday;

    //Buttons
    private Button btnSelectBirthday;
    private Button btnEditPhoneNumber;
    private Button btnSave;

    //is username valid flag
    private boolean isUsernameValid = false;
    //is user name validated and not changed
    private boolean isUsernameValidated = false;
    //is user name valid flag
    private boolean isFirstNameValid = false;
    //is last name valid flag
    private boolean isLastNameValid = false;
    //is age valid flag
    private boolean isAgeValid = false;
    //is email valid flag
    private boolean isEmailValid = false;

    //store selected birthday date
    private Calendar selectedBirthday = GregorianCalendar.getInstance();

    //Check username task
    private TreemService.NetworkRequestTask checkTask;

    //Check username waiting progressbar
    private ProgressBar progressUsernameCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_register_user);
        handleViews();
        setWidgetActions();
    }

    /**
     * Set widgets listeners
     */
    private void setWidgetActions() {
        //handle edit phone button
        btnEditPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to phone edit screen
                toPhoneEnterScreen();
            }
        });

        //handle select birthday button
        btnSelectBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open date selection dialog
                selectBirthday();
            }
        });
        //handle edit text fields
        editUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isUsernameValidated = false; //username changed - unvalidated
                isUsernameValid = !TextUtils.isEmpty(s)&&s.length()>= AppConstants.MIN_USERNAME_LENGTH; //not empty username - valid
                clearUsernameError(); //clear user name error
                updateSaveButton(); //update save button enable
                String username = (s!=null?s.toString():null);
                if (username!=null&&username.trim().length()>=MIN_USERNAME_LENGTH)
                    usernameUpdated(s.toString());
                else
                    showUsernameError();
            }
        });
        editFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isFirstNameValid = !TextUtils.isEmpty(s); //not empty field - name is valid
                clearFirstNameError(); //clear first name error
                updateSaveButton(); //update save button

            }
        });
        editLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isLastNameValid = !TextUtils.isEmpty(s); //not empty field - name is valid
                clearLastNameError(); //clear last name error
                updateSaveButton(); //update save button

            }
        });
        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                clearEmailError();
                isEmailValid = true;
                if (!TextUtils.isEmpty(s)){
                    if (!Utils.isValidEmail(s.toString())) {
                        showEmailError();
                        isEmailValid = false;
                    }
                }
                updateSaveButton();
            }
        });

        //handle save button click
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save(); //save fields on button click
            }
        });

    }
    private Handler waitHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            checkUserNameOnEnter((String)msg.obj);
        }
    };

    private void usernameUpdated(String username){
        stopChecking();
        waitHandler.sendMessageDelayed(waitHandler.obtainMessage(MESSAGE_TEXT_CHANGED, username), DELAY);
    }

    private void stopChecking() {
        waitHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        if (checkTask !=null)
            checkTask.cancel(true);
    }

    private void checkUserNameOnEnter(String username) {
        progressUsernameCheck.setVisibility(View.VISIBLE);
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                checkTask = null;
                isUsernameValidated = true; //user name validated on success
                clearUsernameError();
                progressUsernameCheck.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                progressUsernameCheck.setVisibility(View.GONE);
                if (error!=TreemServiceResponseCode.CANCELED) {
                    showUsernameError();
                    checkTask = null;
                    isUsernameValidated = false;
                    isUsernameValid = false;
                    updateSaveButton();
                }
            }
        };
        //start network request
        checkTask = TreemAuthenticationService.checkUserName(request,username);

    }

    /**
     * Register the user
     */
    private void save() {
        if (!validateFields()){ //is not all entered fields valid?
            updateSaveButton(); //update save button
            return;
        }
        if (isUsernameValidated){ //is username validated and not changed?
            tryToRegister(); //register the user
        }
        else{
            checkUserName(); //check is user name not exists
        }
    }

    /**
     * Clear email field error
     */
    private void clearEmailError() {
        textErrorEmail.setText("");
    }
    /**
     * Show email field error
     */
    private void showEmailError() {
        textErrorEmail.setText(R.string.profile_error_invalid_email);
    }

    /**
     * Check is username not exists
     */
    private void checkUserName() {
        stopChecking();
        saveProgressBar.toggleProgressBar(true); //show progress bar

        //create request
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                isUsernameValidated = true; //user name validated on success
                tryToRegister(); //try to register the user
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                saveProgressBar.toggleProgressBar(false); //hide progress bar
                //show error info
                if (error!=null){
                    switch (error){
                        case GENERIC_RESPONSE_CODE_1:
                            NotificationHelper.showError(SignupRegisterUserActivity.this,R.string.treem_response_code_registration_invalid_request_data);
                            break;
                        case GENERIC_RESPONSE_CODE_2:
                            NotificationHelper.showError(SignupRegisterUserActivity.this,R.string.treem_response_code_registration_user_already_exists);
                            break;
                        default:
                            NotificationHelper.showError(SignupRegisterUserActivity.this,R.string.treem_response_code_other_error);
                    }
                }
                else{
                    NotificationHelper.showError(SignupRegisterUserActivity.this,R.string.treem_response_code_other_error);
                }

            }
        };
        //start network request
        TreemAuthenticationService.checkUserName(request,editUsername.getText().toString());
    }

    /**
     * Try to register the user
     */
    private void tryToRegister() {
        if (!saveProgressBar.isShown()) //show the progress bar if it was not shown
            saveProgressBar.toggleProgressBar(true);

        //create server request
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                //update user status and go to main activity
                TreemOAuthUserToken.SHARED_INSTANCE.setUserStatus(TreemOAuthUserToken.UserStatusFullUser);
                toMainActivity();
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                saveProgressBar.toggleProgressBar(false);
                //show error info
                if (error!=null){
                    switch (error){
                        case GENERIC_RESPONSE_CODE_1:
                            NotificationHelper.showError(SignupRegisterUserActivity.this,R.string.treem_response_code_registration_invalid_request_data_register);
                            break;
                        case GENERIC_RESPONSE_CODE_2:
                            NotificationHelper.showError(SignupRegisterUserActivity.this,R.string.treem_response_code_registration_cant_be_called);
                            break;
                        case GENERIC_RESPONSE_CODE_3:
                            isUsernameValid = false;
                            isUsernameValidated = false;
                            NotificationHelper.showError(SignupRegisterUserActivity.this,R.string.treem_response_code_registration_user_name_alredy_exists_register);
                            showUsernameError();
                            break;
                        case GENERIC_RESPONSE_CODE_4:
                            isEmailValid = false;
                            NotificationHelper.showError(SignupRegisterUserActivity.this, R.string.profile_set_failed_email_exists);
                            showEmailError();
                            break;
                        default:
                            NotificationHelper.showError(SignupRegisterUserActivity.this,R.string.treem_response_code_other_error);
                    }
                }
                else{
                    NotificationHelper.showError(SignupRegisterUserActivity.this,R.string.treem_response_code_other_error);
                }
            }
        };
        //start network request
        TreemAuthenticationService.registerUser(request,
                editUsername.getText().toString(),
                editFirstName.getText().toString(),
                editLastName.getText().toString(),
                editEmail.getText().toString(),
                TimestampUtils.getISO8601StringForDate(selectedBirthday.getTime()));
    }

    /**
     * Clear last name field error
     */
    private void clearLastNameError() {
        textErrorLastName.setText("");
    }

    /**
     * Show user name already exists error
     */
    private void showUsernameError() {
        if (editUsername.length()>AppConstants.MIN_USERNAME_LENGTH)
            textErrorUsername.setText(R.string.profile_error_invalid_username_exists);
        else
            textErrorUsername.setText(R.string.profile_error_invalid_username);
    }

    /**
     * Clear first name field error
     */
    private void clearFirstNameError() {
        textErrorFirstName.setText("");
    }

    /**
     * Clear username field error
     */
    private void clearUsernameError() {
        textErrorUsername.setText("");
    }

    /**
     * Select birthday
     */
    private void selectBirthday() {
        //create date picker dialog
        DatePickerDialog dlg = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedBirthday.set(Calendar.YEAR,year);
                        selectedBirthday.set(Calendar.MONTH,monthOfYear);
                        selectedBirthday.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        Calendar now = GregorianCalendar.getInstance();
                        now.add(Calendar.YEAR,-ApplicationMain.MIN_AGE_YEARS);
                        if (selectedBirthday.getTimeInMillis()>now.getTimeInMillis()){ //check is valid age
                            showAgeError(); //show error if age not valid
                        }
                        else{
                            clearAgeError(); //clear age error on valid age
                        }
                        //update text field with selected age
                        DateFormat df = SimpleDateFormat.getDateInstance();
                        textBirthday.setText(df.format(selectedBirthday.getTime()));
                        textBirthday.setEnabled(true);
                    }
                }
                ,selectedBirthday.get(Calendar.YEAR)
                ,selectedBirthday.get(Calendar.MONTH)
                ,selectedBirthday.get(Calendar.DAY_OF_MONTH)
        );
        dlg.show();
    }

    /**
     * Clear age field error
     */
    private void clearAgeError() {
        textErrorBirthday.setText("");
        isAgeValid = true;
        updateSaveButton();
    }

    /**
     * Update enable of the save button
     */
    private void updateSaveButton() {
        btnSave.setEnabled(validateFields());
    }

    /**
     * Check is all fields valid
     * @return true if all fields valid
     */
    private boolean validateFields() {
        return isAgeValid&&isUsernameValid&&isFirstNameValid&&isLastNameValid&&isEmailValid;
    }

    /**
     * Show age error
     */
    private void showAgeError() {
        textErrorBirthday.setText(getString(R.string.signup_register_to_young_age,ApplicationMain.MIN_AGE_YEARS));
        isAgeValid = false;
        updateSaveButton();
    }

    /**
     * Go to phone edit screen
     */
    private void toPhoneEnterScreen() {
        Intent intent = new Intent(this,SignupPhoneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Handle fragment views
     */
    private void handleViews() {
        editUsername = (EditText)findViewById(R.id.editUserName);
        //limit field max length
        Utils.setMaxSymbols(editUsername,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);
        editFirstName = (EditText)findViewById(R.id.editFirstName);
        //limit field max length
        Utils.setMaxSymbols(editFirstName,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);
        editLastName = (EditText)findViewById(R.id.editLastName);
        //limit field max length
        Utils.setMaxSymbols(editLastName,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);

        editEmail = (EditText)findViewById(R.id.editEmail);
        Utils.setMaxSymbols(editEmail,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);

        textErrorUsername = (TextView)findViewById(R.id.errUserName);
        textErrorFirstName = (TextView)findViewById(R.id.errFirstName);
        textErrorLastName = (TextView)findViewById(R.id.errLastName);
        textErrorEmail = (TextView)findViewById(R.id.errEmail);

        textBirthday = (TextView)findViewById(R.id.textBirthday);
        textErrorBirthday = (TextView)findViewById(R.id.errBirthday);


        btnSelectBirthday = (Button)findViewById(R.id.btnSelectBirthday);
        btnEditPhoneNumber = (Button)findViewById(R.id.btnEditPhoneNumber);

        btnSave = (Button)findViewById(R.id.signupRegisterSaveButton);

        saveProgressBar = new LoadingProgressBar((ViewGroup) findViewById(R.id.signupRegisterUserFrameLayout));

        progressUsernameCheck = (ProgressBar)findViewById(R.id.progressUsername);

        //fill phone number field
        String phone = PhoneUtil.getPhone();

        TextView textPhoneNumber = (TextView) findViewById(R.id.textPhoneNumber);
        if (phone!=null){
            textPhoneNumber.setText(phone);
        }
        else {
            toPhoneEnterScreen();
        }
    }


    /**
     * Open main activity
     */
    private void toMainActivity(){
        // load 'main' activity
        ApplicationMain.SHARED_INSTANCE.hideKeyboard(editUsername);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
