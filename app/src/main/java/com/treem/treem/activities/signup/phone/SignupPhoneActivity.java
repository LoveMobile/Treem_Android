package com.treem.treem.activities.signup.phone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.activities.main.MainActivity;
import com.treem.treem.activities.signup.SignupRegisterUserActivity;
import com.treem.treem.application.ApplicationMain;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.ProfileHelper;
import com.treem.treem.helpers.security.Phone.PhoneUtil;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.signup.SignupVerificationResponse;
import com.treem.treem.services.Treem.TreemAuthenticationService;
import com.treem.treem.services.Treem.TreemOAuthUserToken;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Activity to enter new or edit old phone number
 */
public class SignupPhoneActivity extends AppCompatActivity implements PhoneBaseFragment.OnPhoneFragmentInteractionListener{
    private static final String ARG_OLD_PHONE = "arg.old.phone";
    public static final String EXTRA_PHONE_NUMBER = "extra.phone.number";

    /**
     * Setup new phone number for a user
     * @param context base context
     */
    public static void setupPhoneNumber(Context context){
        Intent intent= new Intent(context,SignupPhoneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Edit previously entered phone number
     * @param f parent fragment
     * @param requestEditPhone request code
     * @param oldPhone old phone number
     */
    public static void editPhoneNumber(Fragment f, int requestEditPhone, String oldPhone){
        Intent intent= new Intent(f.getContext(),SignupPhoneActivity.class);
        intent.putExtra(ARG_OLD_PHONE,oldPhone);
        f.startActivityForResult(intent,requestEditPhone);
    }

    //old phone number
    private String oldPhone = null;
    //new phone number
    private String newPhone = null;
    //is activity in edit phone mode or in new phone number
    private boolean isEditMode = false;

    //Fragment manager
    private FragmentManager fragmentManager;

    //Waiting progress bar
    private LoadingProgressBar waitingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_phone);
        ProfileHelper.getInstance(this).clearProfile();

        //get fragment manager
        fragmentManager = getSupportFragmentManager();

        //parse incoming intent to get phone number for edit
        parseIntent();
        //find signup header and hide it for edit activity
        View signupHeader = findViewById(R.id.signupHeader);
        if (isEditMode&& signupHeader !=null)
            signupHeader.setVisibility(View.GONE);

        //Create wating progress bar
        waitingProgressBar = new LoadingProgressBar((ViewGroup)findViewById(R.id.signupPhoneFrameLayout));
        if (savedInstanceState==null)
            toPhoneScreen();
    }

    /**
     * Open enter phone screen
     */
    private void toPhoneScreen() {
        PhoneFragment f = PhoneFragment.newInstance();
        switchFragment(f,true);
    }

    /**
     * Switch settings fragment
     * @param f new fragment
     * @param isMainFragment is this a main fragment (for fragment animation)
     */
    private void switchFragment(PhoneBaseFragment f, boolean isMainFragment){
        FragmentTransaction ft = fragmentManager.beginTransaction();
        //set custom animation
        ft.setCustomAnimations(isMainFragment?0:R.anim.enter,R.anim.exit,R.anim.pop_enter,isMainFragment?0:R.anim.pop_exit);
        ft.replace(R.id.containerPhone,f,f.getFragmentTag());
        ft.addToBackStack(f.getFragmentTag());
        ft.commit();
    }

    /**
     * Parse activity intent
     */
    private void parseIntent() {
        if (getIntent()!=null)
            oldPhone = getIntent().getStringExtra(ARG_OLD_PHONE); //get old phone
        if (oldPhone!=null) //activity in edit mode if old phone not null
            isEditMode = true;
    }


    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount()>1)
            fragmentManager.popBackStack();
        else
            finish();
    }

    /**
     * Get is edit mode
     * @return true if activity in edit phone mode
     */
    @Override
    public boolean isEditMode() {
        return isEditMode;
    }


    /**
     * Get old phone number
     * @return old phone number
     */
    @Override
    public String getOldPhone() {
        return oldPhone;
    }

    /**
     * Get new phone number
     * @return new phone number
     */
    @Override
    public String getNewPhone() {
        return newPhone;
    }

    /**
     * Check phone number at server
     * @param phone phone number
     */
    @Override
    public void checkPhoneNumber(final String phone) {
        // load question to verify user is human
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                // if successful go to the signup verification view
                waitingProgressBar.toggleProgressBar(false);
                newPhone = phone;
                toVerification(); //go to verification screen
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                // show error
                if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_1){
                    NotificationHelper.showError(SignupPhoneActivity.this,R.string.signup_phone_invalid_phone_number);
                }
                else{
                    NotificationHelper.showError(SignupPhoneActivity.this,error.getDescription(SignupPhoneActivity.this));
                }
                waitingProgressBar.toggleProgressBar(false);

            }
        };

        // show loading progress bar indicator
        waitingProgressBar.toggleProgressBar(true);

        TreemAuthenticationService.checkPhoneNumber(request, phone);
    }


    /**
     * Open phone verification screen
     */
    private void toVerification() {
        VerifyFragment f = VerifyFragment.newInstance();
        switchFragment(f,false);
    }

    /**
     * Return success result to parent activity
     * set phone number to EXTRA_PHONE_NUMBER
     */
    private void returnSuccess(){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PHONE_NUMBER,newPhone);
        setResult(RESULT_OK,intent);
        finish();
    }
    /**
     * Save phone number to registry
     */
    private void savePhoneNumber() {
        PhoneUtil.savePhone(this.newPhone);
    }

    /**
     * Open user registration activity
     */
    private void toSignupProfile(){
        Intent intent = new Intent(getApplicationContext(), SignupRegisterUserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Open main activity
     */
    private void toMainActivity(){
        // load 'main' activity
        ApplicationMain.SHARED_INSTANCE.hideKeyboard(findViewById(R.id.containerPhone));
        ApplicationMain.setExpiredRequested(false);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Request to resend phone verification number from server
     * @param resendButton send button
     * @param lastResponse status view
     */
    @Override
    public void resendVerificationCode(final Button resendButton, final TextView lastResponse) {
        if (resendButton!=null)
            resendButton.setEnabled(false);

        waitingProgressBar.toggleProgressBar(true);

        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                String status = getResources().getString(R.string.signup_verification_last_requested) + " " +
                        SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
                if (lastResponse!=null) {
                    lastResponse.setText(status);
                    lastResponse.setVisibility(View.VISIBLE);
                }

                // show loading progress bar indicator
                waitingProgressBar.toggleProgressBar(false);
                if (resendButton!=null)
                    resendButton.setEnabled(true);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {

                // show error
                if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_1){
                    NotificationHelper.showError(SignupPhoneActivity.this,R.string.signup_phone_invalid_phone_number);
                }
                else{
                    NotificationHelper.showError(SignupPhoneActivity.this,error.getDescription(SignupPhoneActivity.this));
                }
                waitingProgressBar.toggleProgressBar(false);
                if (resendButton!=null)
                    resendButton.setEnabled(true);
            }
        };

        TreemAuthenticationService.resendVerificationCode(request, newPhone);
    }

    /**
     * Verify code entered is correct
      * @param code the sent sms code
     */
    @Override
    public void verifyUserDevice(String code) {
        waitingProgressBar.toggleProgressBar(true);

        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                // success store user oauth tokens
                // store the response
                final Gson gson = new Gson();

                SignupVerificationResponse response;
                try{
                    response = gson.fromJson(data, SignupVerificationResponse.class);
                }
                catch (JsonSyntaxException e){
                    e.printStackTrace();
                    NotificationHelper.showError(SignupPhoneActivity.this,R.string.failed_parse_server_answer);
                    return;
                }
                savePhoneNumber();
                // store the device specific credentials
                TreemOAuthUserToken.SHARED_INSTANCE.setUserStatus(response.user_status);
                if (isEditMode){
                    returnSuccess();
                }
                else {
                    switch (response.user_status) {
                        case TreemOAuthUserToken.UserStatusFullUser:
                            TreemOAuthUserToken.SHARED_INSTANCE.setAccessTokens(response);
                            toMainActivity();
                            break;
                        case TreemOAuthUserToken.UserStatusTempVerified:
                            TreemOAuthUserToken.SHARED_INSTANCE.setAccessTokens(response);
                            toSignupProfile();
                            break;
                        case TreemOAuthUserToken.UserStatusTempUnverified:
                        default:
                            NotificationHelper.showError(SignupPhoneActivity.this, R.string.signup_verification_failed_phone_unverified);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                waitingProgressBar.toggleProgressBar(false);
                if (error!=null) {
                    if (error == TreemServiceResponseCode.GENERIC_RESPONSE_CODE_1) {
                        NotificationHelper.showError(SignupPhoneActivity.this, R.string.treem_response_code_verification_invalid_phone_number);
                    } else if (error == TreemServiceResponseCode.GENERIC_RESPONSE_CODE_2) {
                        NotificationHelper.showError(SignupPhoneActivity.this, R.string.treem_response_code_verification_invalid_device_os);
                    } else if (error == TreemServiceResponseCode.GENERIC_RESPONSE_CODE_3) {
                        NotificationHelper.showError(SignupPhoneActivity.this, R.string.treem_response_code_verification_invalid_signup_code);
                    }
                    else {
                        NotificationHelper.showError(SignupPhoneActivity.this, error.getDescription(SignupPhoneActivity.this));
                    }
                }
                else{
                    NotificationHelper.showError(SignupPhoneActivity.this,R.string.treem_response_code_other_error);
                }

            }
        };

        TreemAuthenticationService.verifyUserDevice(request, newPhone, code);
    }

}
