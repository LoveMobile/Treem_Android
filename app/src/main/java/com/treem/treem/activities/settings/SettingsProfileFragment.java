package com.treem.treem.activities.settings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;
import com.treem.treem.R;
import com.treem.treem.activities.image.ImageSelectActivity;
import com.treem.treem.activities.media.MediaActivity;
import com.treem.treem.activities.signup.phone.SignupPhoneActivity;
import com.treem.treem.application.AppConstants;
import com.treem.treem.application.ApplicationMain;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.TimestampUtils;
import com.treem.treem.helpers.Utils;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.content.UploadImageResponse;
import com.treem.treem.models.content.UploadItem;
import com.treem.treem.models.profile.UserProfile;
import com.treem.treem.services.Treem.TreemAuthenticationService;
import com.treem.treem.services.Treem.TreemContentService;
import com.treem.treem.services.Treem.TreemProfileService;
import com.treem.treem.services.Treem.TreemService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Settings->Profile fragment
 */
public class SettingsProfileFragment extends SettingsBaseFragment {

    private static final String TAG = SettingsProfileFragment.class.getSimpleName();
    private static final int REQUEST_PROFILE_IMAGE = 1;
    private static final int REQUEST_EDIT_PHONE = 2;
    private static final int MESSAGE_TEXT_CHANGED = 1;

    /**
     * Progress bars
     */
    private LoadingProgressBar loadProgressBar;
    private ProgressBar saveProgressBar;

    //Edit fields
    private EditText editUsername;
    private EditText editFirstName;
    private EditText editLastName;
    private EditText editEmail;
    private EditText editCity;
    private EditText editCountry;
    private EditText editState;

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

    /**
     * Phone number field
     */
    private TextView textPhoneNumber;

    //Buttons
    private Button btnSelectBirthday;
    private Button btnEditPhoneNumber;
    private Button btnSave;

    //Change profile image
    private ViewGroup btnChange;

    //Open media button
    private ViewGroup btnMedia;

    private ImageView avatarView;
    //Switch
    private SwitchCompat switchNonFriends;


    //is username valid flag
    private boolean isUsernameValid = false;
    //is user name valid flag
    private boolean isFirstNameValid = false;
    //is last name valid flag
    private boolean isLastNameValid = false;
    //is age valid flag
    private boolean isAgeValid = true;
    //is email valid flag
    private boolean isEmailValid = false;

    //store selected birthday date
    private Calendar selectedBirthday = GregorianCalendar.getInstance();

    //user profile data
    private UserProfile userProfile = null;

    //Store the new avatar from image selection activity
    private Uri updatedAvatar;

    //Flag to store requirement to upload avatar
    @SuppressWarnings("FieldCanBeLocal")
    private boolean needUpdateAvatar = false;
    //Flag to store requirement to upload profile
    private boolean needUpdateFields = false;

    private TreemService.NetworkRequestTask checkTask;

    private ProgressBar progressUsernameCheck;


    public SettingsProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        editUsername = (EditText)view.findViewById(R.id.editUserName);
        //limit field max length
        Utils.setMaxSymbols(editUsername, ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);
        editFirstName = (EditText)view.findViewById(R.id.editFirstName);
        //limit field max length
        Utils.setMaxSymbols(editFirstName,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);
        editLastName = (EditText)view.findViewById(R.id.editLastName);
        //limit field max length
        Utils.setMaxSymbols(editLastName,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);

        editEmail = (EditText)view.findViewById(R.id.editEmail);
        Utils.setMaxSymbols(editEmail,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);

        textErrorUsername = (TextView)view.findViewById(R.id.errUserName);
        textErrorFirstName = (TextView)view.findViewById(R.id.errFirstName);
        textErrorLastName = (TextView)view.findViewById(R.id.errLastName);
        textErrorEmail = (TextView)view.findViewById(R.id.errEmail);

        textBirthday = (TextView)view.findViewById(R.id.textBirthday);
        textErrorBirthday = (TextView)view.findViewById(R.id.errBirthday);


        btnSelectBirthday = (Button)view.findViewById(R.id.btnSelectBirthday);
        btnEditPhoneNumber = (Button)view.findViewById(R.id.btnEditPhoneNumber);

        btnSave = (Button)view.findViewById(R.id.profileSaveButton);

        loadProgressBar = new LoadingProgressBar((ViewGroup) view.findViewById(R.id.profileRootLayout));

        saveProgressBar = (ProgressBar)view.findViewById(R.id.progressSave);

        editCity = (EditText)view.findViewById(R.id.editCity);
        Utils.setMaxSymbols(editCity,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);
        editCountry = (EditText)view.findViewById(R.id.editCountry);
        Utils.setMaxSymbols(editCountry,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);
        editState = (EditText)view.findViewById(R.id.editState);
        Utils.setMaxSymbols(editState,ApplicationMain.MAX_REGISTRATION_FIELD_LENGTH);

        textPhoneNumber = (TextView) view.findViewById(R.id.textPhoneNumber);

        switchNonFriends = (SwitchCompat)view.findViewById(R.id.switchNonFriends);

        btnChange = (ViewGroup)view.findViewById(R.id.layoutChange);

        btnMedia = (ViewGroup)view.findViewById(R.id.profileButtonPicsVids);

        avatarView = (ImageView)view.findViewById(R.id.profileAvatar);

        progressUsernameCheck = (ProgressBar)view.findViewById(R.id.progressUsername);
        setWidgetActions();

        if (userProfile==null)
            loadUserProfile();
    }

    /**
     * Load user profile
     */
    private void loadUserProfile() {
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                if (data!=null){
                    try{
                        userProfile = new Gson().fromJson(data, UserProfile.class); //parse profile
                        if (isAdded())
                            fillViews(); //fill views with requested profile
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                        if (isAdded())
                            NotificationHelper.showError(getContext(),R.string.failed_parse_server_answer);
                    }
                }
                else{
                    if (isAdded())
                        NotificationHelper.showError(getContext(),R.string.failed_parse_server_answer);
                }
                if (isAdded())
                    loadProgressBar.toggleProgressBar(false);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                if (isAdded()) {
                    loadProgressBar.toggleProgressBar(false);
                    NotificationHelper.showError(getContext(), error.getDescription(getContext()));
                }
            }
        };
        loadProgressBar.toggleProgressBar(true);
        TreemProfileService.getCurrentUserProfile(request, CurrentTreeSettings.SHARED_INSTANCE.treeSession);
    }

    /**
     * Fill fragment views with user profile data
     */
    private void fillViews() {
        if (userProfile==null)
            return;
        updateProfileImage();
        setViewValue(editUsername,userProfile.username);
        setViewValue(editFirstName,userProfile.first);
        setViewValue(editLastName,userProfile.last);
        setViewValue(editEmail,userProfile.email);
        setViewValue(editCity,userProfile.r_locality);
        setViewValue(editState,userProfile.r_province);
        setViewValue(editCountry,userProfile.r_country);
        setViewValue(textPhoneNumber,userProfile.phone);
        Date dt = TimestampUtils.parseDate(userProfile.dob);
        //init selected birthday with data from user profile
        if (dt!=null)
            selectedBirthday.setTime(dt);

        checkBirthdayValid();

        setViewValue(textBirthday,TimestampUtils.formatDate(dt));

        switchNonFriends.setChecked(userProfile.isNonFriendsAccessEnabled());

        updateSaveButton();

    }

    /**
     * Fill textview or edittext fields with value
     * @param view view to fill
     * @param value value to fill
     */
    private void setViewValue(TextView view, String value) {
        if (view==null) //nothing do if field not found
            return;
        if (!TextUtils.isEmpty(value))
            view.setText(value);//fill with non empty value
        else
            view.setText("");
    }

    /**
     * Open phone edit screen
     */
    private void toPhoneEditScreen() {
        SignupPhoneActivity.editPhoneNumber(this,REQUEST_EDIT_PHONE,userProfile.phone);
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
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_profile);
        setBackVisible(true);
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
                toPhoneEditScreen();
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
                isUsernameValid = !TextUtils.isEmpty(s)&&s.length()>=AppConstants.MIN_USERNAME_LENGTH; //not empty username - valid
                clearUsernameError(); //clear user name error
                updateSaveButton(); //update save button enable
                String username = (s!=null?s.toString():null);
                if (username!=null&&username.trim().length()>=AppConstants.MIN_USERNAME_LENGTH){
                    if (checkStringChanged(editUsername,userProfile.username)) {
                        usernameUpdated(s.toString());
                    }
                    else{
                        isUsernameValid=true;
                        updateSaveButton();
                    }
                }
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
        editCountry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSaveButton();
            }
        });
        editCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSaveButton();
            }
        });
        editState.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSaveButton();
            }
        });
        //handle save button click
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationMain.SHARED_INSTANCE.hideKeyboard(btnSave);
                save(); //save avatar and fields
            }
        });
        switchNonFriends.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSaveButton();
            }
        });
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ImageSelectActivity.class);
                startActivityForResult(intent,REQUEST_PROFILE_IMAGE);
            }
        });
        btnMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaActivity.showMedia(getActivity(),null);
            }
        });

    }

    /**
     * upload image to server
     */
    private void saveImage(){
        startUploadProfileImage(updatedAvatar);
    }


    /**
     * Save data to server
     */
    private void save() {
        needUpdateAvatar = updatedAvatar!=null;
        needUpdateFields = getChanges().size()>0;
        if (needUpdateAvatar)
            saveImage();
        else if (needUpdateFields)
            saveProfile();

    }

    /**
     * Upload profile to server
     */
    private void saveProfile() {
        if (checkTask!=null)
            checkTask.cancel(true);
        Map<String,Object> changes = getChanges(); //get changes
        if (changes.size()>0){ //check if we have changes
            TreemServiceRequest request = new TreemServiceRequest() {
                @Override
                public void onSuccess(String data) {
                    updateProfileFields(); //update stored profile with updated data
                    if (isAdded()) {
                        showSaveProgress(false);
                        NotificationHelper.showSuccess(getContext(), R.string.profile_updated);
                        updateSaveButton();
                    }
                }

                @Override
                public void onFailure(TreemServiceResponseCode error, boolean wasHandled){
                    if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_1){
                        if (isAdded())
                            NotificationHelper.showError(getContext(),R.string.profile_set_failed_invalid_object);
                    }
                    else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_2){
                        isEmailValid = false;
                        if (isAdded()){
                            NotificationHelper.showError(getContext(),R.string.profile_set_failed_invalid_email);
                            showEmailError();
                        }
                    }
                    else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_3){
                        isUsernameValid = false;
                        if (isAdded()) {
                            NotificationHelper.showError(getContext(), R.string.profile_set_failed_user_exists);
                            showUsernameError();
                        }
                    }
                    else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_4){
                        isEmailValid = false;
                        if (isAdded()) {
                            NotificationHelper.showError(getContext(), R.string.profile_set_failed_email_exists);
                            showEmailError();
                        }
                    }
                    else{
                        if (isAdded())
                            NotificationHelper.showError(getContext(),getString(R.string.profile_error,error.getDescription(getContext())));
                    }
                    if (isAdded()) {
                        showSaveProgress(false);
                        updateSaveButton();
                    }
                }
            };
            showSaveProgress(true);
            TreemProfileService.setCurrentUserProfile(request,changes,CurrentTreeSettings.SHARED_INSTANCE.treeSession);
        }
        else{
            showSaveProgress(false);
            updateSaveButton();
        }
    }

    /**
     * Updated stored profile with changed data
     */
    private void updateProfileFields() {
        userProfile.updateProfile(getChanges());
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
     * Show/hide save progress bar and save button
     * @param isShow is need to show progress bar and hide the button?
     */
    private void showSaveProgress(boolean isShow) {
        saveProgressBar.setVisibility(isShow?View.VISIBLE:View.INVISIBLE);
        btnSave.setVisibility(isShow?View.INVISIBLE:View.VISIBLE);
        btnChange.setVisibility(isShow?View.INVISIBLE:View.VISIBLE);
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
     * Clear last name field error
     */
    private void clearLastNameError() {
        textErrorLastName.setText("");
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
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedBirthday.set(Calendar.YEAR,year);
                        selectedBirthday.set(Calendar.MONTH,monthOfYear);
                        selectedBirthday.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        checkBirthdayValid();
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
     * Check is user has 13 years old?
     */
    private void checkBirthdayValid() {
        Calendar now = GregorianCalendar.getInstance();
        now.add(Calendar.YEAR,-ApplicationMain.MIN_AGE_YEARS);
        if (selectedBirthday.getTimeInMillis()>now.getTimeInMillis()){ //check is valid age
            showAgeError(); //show error if age not valid
        }
        else{
            clearAgeError(); //clear age error on valid age
        }
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
        boolean isEnabled = false;
        if (validateFields()){
            if (getChanges().size()>0||updatedAvatar!=null)
                isEnabled = true;
        }
        btnSave.setEnabled(isEnabled);
    }

    /**
     * Get changed fields
     * @return the map with changed fields. Keys are model fields names
     */
    @NonNull
    private Map<String,Object> getChanges() {
        Map<String,Object> ret = new HashMap<>();
        if (userProfile!=null) {
            if (checkStringChanged(editUsername, userProfile.username))
                ret.put(UserProfile.keyUsername,editUsername.getText().toString());
            if (checkStringChanged(editFirstName, userProfile.first))
                ret.put(UserProfile.keyFirstName,editFirstName.getText().toString());
            if (checkStringChanged(editLastName, userProfile.last))
                ret.put(UserProfile.keyLastName,editLastName.getText().toString());
            if (checkStringChanged(editEmail, userProfile.email))
                ret.put(UserProfile.keyEmail,editEmail.getText().toString());
            if (checkStringChanged(editCity, userProfile.r_locality))
                ret.put(UserProfile.keyCity,editCity.getText().toString());
            if (checkStringChanged(editState, userProfile.r_province))
                ret.put(UserProfile.keyState,editState.getText().toString());
            if (checkStringChanged(editCountry, userProfile.r_country))
                ret.put(UserProfile.keyCountry,editCountry.getText().toString());
            if (checkBirthdayChanged(selectedBirthday,userProfile.dob))
                ret.put(UserProfile.keyBirthday,TimestampUtils.getISO8601StringForDate(selectedBirthday.getTime()));
            if (switchNonFriends.isChecked()!=userProfile.isNonFriendsAccessEnabled())
                ret.put(UserProfile.keyNonFriendsAccess,(short)(switchNonFriends.isChecked()?1:0));

        }
        return ret;
    }

    /**
     * Check is birthday changed
     * @param birthday the selected birthday
     * @param value birthday in the user profile
     * @return true if birthday changed
     */
    private boolean checkBirthdayChanged(Calendar birthday, String value) {
        if (birthday==null)
            return false;
        String newValue = TimestampUtils.getISO8601StringForDate(birthday.getTime());
        String oldDate = TimestampUtils.getISO8601StringForDate(TimestampUtils.parseDate(value));
        return !newValue.equals(oldDate);
    }

    /**
     * Check is value in edit text changed changed
     * @param view the edit text field
     * @param value value to check
     * @return true if value was changed
     */
    private boolean checkStringChanged(TextView view, String value) {
        if (view==null)
            return false;
        String newValue = view.getText().toString();
        //noinspection SimplifiableIfStatement
        if (TextUtils.isEmpty(newValue)&&TextUtils.isEmpty(value))
            return false;
        return !newValue.equals(value);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_PROFILE_IMAGE){ //Return from image selection activity
            if (resultCode== Activity.RESULT_OK&&data!=null){
                updatedAvatar = data.getData(); //get image data
                updateProfileImage(); //update profile image
                updateSaveButton(); //update save button
            }
        }
        else if (requestCode==REQUEST_EDIT_PHONE){ //was requested phone change
            if (resultCode==Activity.RESULT_OK&&data!=null){ //phone changed success
                String newPhone = data.getStringExtra(SignupPhoneActivity.EXTRA_PHONE_NUMBER); //get new phone
                if (newPhone!=null) {
                    if (userProfile != null) //update it if it is not null
                        userProfile.phone = newPhone;
                    if (isAdded())
                        textPhoneNumber.setText(newPhone);
                }
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Start upload profile image
     * @param image image uri
     */
    private void startUploadProfileImage(Uri image) {
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                if (data!=null) { //uploaded success and we have a server response
                    try {
                        UploadImageResponse response = new Gson().fromJson(data, UploadImageResponse.class); //decode response
                        userProfile.pr_pic = response.url; //get url
                        userProfile.pr_pic_stream = response.stream_url; //get stream url
                        updatedAvatar = null; //reset updated profile
                        if (isAdded()) {
                            updateProfileImage(); //update profile image
                            updateSaveButton(); //update save button
                        }

                    }
                    catch (JsonSyntaxException e){
                        if (isAdded())
                            NotificationHelper.showError(getContext(),R.string.failed_parse_server_answer);
                    }
                }
                else {
                    if (isAdded())
                        NotificationHelper.showError(getContext(), R.string.upload_file_failed);
                }
                if (needUpdateFields) //need to upload profile?
                    saveProfile();
                else {
                    if (isAdded()) {
                        showSaveProgress(false); //hide progress bar if not
                        if (updatedAvatar == null)
                            NotificationHelper.showSuccess(getContext(), R.string.profile_avatar_updated);
                    }
                }
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                if (isAdded()) {
                    switch (error) {
                        case GENERIC_RESPONSE_CODE_1:
                            NotificationHelper.showError(getContext(), R.string.upload_file_failed_invalid_request_data_object);
                            break;
                        case GENERIC_RESPONSE_CODE_2:
                            NotificationHelper.showError(getContext(), R.string.upload_file_failed_unsupported_file_type);
                            break;
                        case GENERIC_RESPONSE_CODE_3:
                            NotificationHelper.showError(getContext(), R.string.upload_file_failed_file_is_too_large);
                            break;
                        default:
                            NotificationHelper.showError(getContext(), getString(R.string.upload_file_failed_msg, error.getDescription(getContext())));
                    }
                }
                if (needUpdateFields){
                    saveProfile();
                }
                else{
                    if (isAdded())
                        showSaveProgress(false);
                }

            }
        };
        //get upload item
        UploadItem item=null;
        try{
            item = UploadItem.uploadImage(getContext(),image,true); //this is profile image from uri
        }
        catch (IOException e){ //failed to read image
            Log.e(TAG,"Failed to read image",e);
            NotificationHelper.showError(getContext(),R.string.profile_filed_read_image);
        }
        catch (OutOfMemoryError e){ //we don't have enough memory to create a base 64 string from image file
            Log.e(TAG,"Failed to convert image",e);
            NotificationHelper.showError(getContext(),R.string.profile_image_too_big_to_upload);
        }
        if (item!=null) {
            showSaveProgress(true);
            TreemContentService.uploadSingleImage(request, item, CurrentTreeSettings.SHARED_INSTANCE.treeSession); //upload image if we have a file
        }
        else{
            saveProfile(); //save profile if failed to get upload object
            NotificationHelper.showError(getContext(),R.string.upload_file_failed_parse_image_file);
        }
    }

    /**
     * Update profile image on the screen
     */
    private void updateProfileImage() {
        if (updatedAvatar!=null){ //Do we have an new image?
            avatarView.setImageURI(updatedAvatar); //load from uri
        }
        else if (userProfile!=null&&userProfile.pr_pic_stream!=null){ //Do we have an image in the profile?
            Picasso.with(getContext()) //load from profile
                    .load(userProfile.pr_pic_stream)
                    .placeholder(R.drawable.img_avatar)
                    .error(R.drawable.img_avatar)
                    .into(avatarView);
        }
        else{ //set default resource image
            avatarView.setImageResource(R.drawable.img_avatar);
        }
    }
    private Handler waitHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            checkUserNameOnEnter((String)msg.obj);
        }
    };

    private void usernameUpdated(String username){
        stopChecking();
        waitHandler.sendMessageDelayed(waitHandler.obtainMessage(MESSAGE_TEXT_CHANGED, username), AppConstants.CHECK_TEXT_DELAY);
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
                clearUsernameError();
                progressUsernameCheck.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                progressUsernameCheck.setVisibility(View.GONE);
                isUsernameValid = false;
                if (error!=TreemServiceResponseCode.CANCELED) {
                    showUsernameError();
                    checkTask = null;
                }
                updateSaveButton();
            }
        };
        //start network request
        checkTask = TreemAuthenticationService.checkUserName(request,username);

    }


}
