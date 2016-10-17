package com.treem.treem.activities.branch.members;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.helpers.Utils;
import com.treem.treem.models.branch.Branch;

/**
 * User Profile activity - open user profile activity
 */
public class SearchConfigActivity extends AppCompatActivity {
    private static final String ARG_CONFIG = "arg.config";
    private static final String ARG_BRANCH = "arg.branch";
    public static final String EXTRA_CONFIG = "extra.config";
    @SuppressWarnings("unused")
    private static final String TAG = SearchConfigActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private CheckBox checkMFirst;
    private CheckBox checkMLast;
    private CheckBox checkMPhone;
    private CheckBox checkMUsername;
    private CheckBox checkMEmail;

    private CheckBox checkRFriends;
    private CheckBox checkRInvited;
    private CheckBox checkRPending;
    private CheckBox checkRNotFriends;

    private CheckBox checkMContacts;

    @SuppressWarnings("FieldCanBeLocal")
    private Button buttonDone;
    @SuppressWarnings("FieldCanBeLocal")
    private Button buttonDefaults;

    public static void showConfig(Fragment f, Branch branch,MembersSearchConfig config, int requestCode) {
        if (f==null||config==null||f.getActivity()==null)
            return;
        Intent intent = new Intent(f.getContext(), SearchConfigActivity.class);
        intent.putExtra(ARG_CONFIG, new Gson().toJson(config,MembersSearchConfig.class));
        intent.putExtra(ARG_BRANCH, new Gson().toJson(branch,Branch.class));

        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(f.getActivity(), R.anim.slide_up_over, R.anim.hold).toBundle();
        f.startActivityForResult(intent,requestCode,bundle);
    }

    private Branch branch;
    private MembersSearchConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_config);
        parseIntent();
        if (branch==null||config==null){
            finish();
            return;
        }
        setupToolbar();
        handleViews();
    }

    private void handleViews() {
        checkMFirst = (CheckBox)findViewById(R.id.searchMatchingFirst);
        checkMLast = (CheckBox)findViewById(R.id.searchMatchingLast);
        checkMEmail = (CheckBox)findViewById(R.id.searchMatchingEmail);
        checkMPhone = (CheckBox)findViewById(R.id.searchMatchingPhone);
        checkMUsername = (CheckBox)findViewById(R.id.searchMatchingUsername);

        checkRFriends = (CheckBox)findViewById(R.id.searchRelFriends);
        checkRInvited = (CheckBox)findViewById(R.id.searchRelInvited);
        checkRPending = (CheckBox)findViewById(R.id.searchRelPending);
        checkRNotFriends = (CheckBox)findViewById(R.id.searchRelNotFriends);

        checkMContacts = (CheckBox)findViewById(R.id.searchMelContacts);

        buttonDone = (Button)findViewById(R.id.done);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });

        buttonDefaults = (Button)findViewById(R.id.setDefaults);
        buttonDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                config.resetToDefaults();
                updateViews();
            }
        });
        checkMContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkMContacts.isChecked()){
                    if (!checkContactsPermission()){
                        checkMContacts.setChecked(false);
                        requestContactsPermissions();
                    }
                }
            }
        });
        updateViews();
    }

    private void updateViews() {
        checkMFirst.setChecked(config.getMatching().isFirstNameSet());
        checkMLast.setChecked(config.getMatching().isLastNameSet());
        checkMEmail.setChecked(config.getMatching().isEmailSet());
        checkMUsername.setChecked(config.getMatching().isUserNameSet());
        checkMPhone.setChecked(config.getMatching().isPhoneSet());

        checkRFriends.setChecked(config.getRelationship().isFriendsSet());
        checkRPending.setChecked(config.getRelationship().isPendingSet());
        checkRInvited.setChecked(config.getRelationship().isInvitedSet());
        checkRNotFriends.setChecked(config.getRelationship().isNotFriendsSet());

        checkMContacts.setChecked(config.getMiscellaneous().isShowContactsSet());

    }

    private void done() {
        config.getMatching().setFirstName(checkMFirst.isChecked());
        config.getMatching().setLastName(checkMLast.isChecked());
        config.getMatching().setEmail(checkMEmail.isChecked());
        config.getMatching().setPhone(checkMPhone.isChecked());
        config.getMatching().setUserName(checkMUsername.isChecked());

        config.getRelationship().setFriends(checkRFriends.isChecked());
        config.getRelationship().setInvited(checkRInvited.isChecked());
        config.getRelationship().setPending(checkRPending.isChecked());
        config.getRelationship().setNotFriends(checkRNotFriends.isChecked());

        config.getMiscellaneous().setAllowUseContacts(checkContactsPermission());
        if (checkContactsPermission())
            config.getMiscellaneous().setShowContacts(checkMContacts.isChecked());
        else
            config.getMiscellaneous().resetToDefaults();

        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONFIG,new Gson().toJson(config,MembersSearchConfig.class));
        setResult(Activity.RESULT_OK,intent);
        finish();
    }
    private boolean checkContactsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("unused")
    private void askAllowUseContacts() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CONTACTS)) {
            showContactsPermissionDescription();
        } else {
            requestContactsPermissions();
        }
    }

    /**
     * Show read contacts permission description dialog
     */
    private void showContactsPermissionDescription() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle(R.string.title_read_contacts_describe_permissions);
        builder.setMessage(R.string.msg_read_contacts_describe_permissions);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestContactsPermissions();
            }
        });
        builder.show();
    }

    /**
     * Request read contacts permission
     */
    private void requestContactsPermissions() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    /**
     * On request permission result handler
     * @param requestCode permission request code
     * @param permissions the array of permissions
     * @param grantResults result of action
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkMContacts.setChecked(true);
                    config.getMiscellaneous().setAllowUseContacts(true);
                }
                else {
                    config.getMiscellaneous().setAllowUseContacts(false);
                    config.getMiscellaneous().resetToDefaults();
                }

            }

        }
    }

    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar ==null){
            return;
        }
        //set toolbar as action bar
        setSupportActionBar(toolbar);
        //get toolbar title
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        String titleText;
        if (branch.name!=null){ //is activity not null
            titleText = branch.name; //set it
        }
        else {
            titleText = getString(R.string.branch_title); //set common branch title
        }
        toolbarTitle.setText(titleText);

        //close image button
        ImageView imageClose = (ImageView) toolbar.findViewById(R.id.toolbar_close);
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish(); //finish activity on close click
            }
        });
        imageClose.setVisibility(View.VISIBLE);
        ActionBar ab = getSupportActionBar();
        if (ab!=null)
            toolbar.setBackgroundColor(branch.getColor()); //set toolbar background color according tree color

        //get contrast color for selected color and set it to toolbar title and toolbar close button
        Utils.ColorTint tint = Utils.getContrastColor(branch.getColor());
        int textColor = Color.WHITE;
        if (tint== Utils.ColorTint.darkTintColor)
            //noinspection deprecation
            textColor = getResources().getColor(R.color.dark_gray);
        toolbarTitle.setTextColor(textColor);
        imageClose.setColorFilter(textColor);
        int color = Utils.getDarkerColor(branch.getColor());
        updateStatusbarColor(color,tint);
    }

    /**
     * Update status bar color
     * @param color current toolbar color
     * @param tint light or dark tint
     */
    private void updateStatusbarColor(int color,Utils.ColorTint tint) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {//get darker of the selected color and set it to status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(color);
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

                int newUiVisibility = getWindow().getDecorView().getSystemUiVisibility();

                if(tint== Utils.ColorTint.lightTintColor)
                {
                    //Light Text to show up on your dark status bar
                    newUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                else
                {
                    //Dark Text to show up on your light status bar
                    newUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }

                getWindow().getDecorView().setSystemUiVisibility(newUiVisibility);

            }
        }
    }


    /**
     * Parse incoming intent
     */
    private void parseIntent() {
        if (getIntent()==null) {
            return;
        }
        String json = getIntent().getStringExtra(ARG_BRANCH); ///get branch and deserialize it.
        if (json==null){
            return;
        }
        try{
            branch = new Gson().fromJson(json,Branch.class);
        }
        catch (JsonSyntaxException e){
            e.printStackTrace();
        }
        json = getIntent().getStringExtra(ARG_CONFIG);
        if (json==null)
            return;
        try{
            config = new Gson().fromJson(json,MembersSearchConfig.class);
        }
        catch (JsonSyntaxException e){
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,R.anim.slide_down_over); //show animation
    }
}
