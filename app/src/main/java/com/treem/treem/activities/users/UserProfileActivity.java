package com.treem.treem.activities.users;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;
import com.treem.treem.R;
import com.treem.treem.activities.media.MediaActivity;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.Utils;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.profile.UserProfile;
import com.treem.treem.services.Treem.TreemProfileService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

/**
 * User Profile activity - open user profile activity
 */
public class UserProfileActivity extends AppCompatActivity {
    private static final String ARG_USER_ID = "arg.user.id";
    //User id
    private long userId = -1L;
    //Loaded user profile
    private UserProfile userProfile;

    //Waiting progress
    private LoadingProgressBar loadingProgressBar;

    //Screen widgets
    private ImageView imageAvatar;
    private TextView textName;
    private TextView textUsername;
    private TextView textStatus;
    private ImageView imgStatus;
    private TextView textStatusAction;
    @SuppressWarnings("FieldCanBeLocal")
    private ViewGroup layoutRegisteredActions;
    private ViewGroup layoutAlbum;
    private ViewGroup layoutFeed;
    private ViewGroup layoutChat;
    private TextView textCity;
    private TextView textState;
    private TextView textCountry;
    /**
     * Show user profile
     * @param activity parent activity
     */
    public static void showUserProfile(Activity activity,long userId){
        if (activity==null)
            return;
        Intent intent = new Intent(activity, UserProfileActivity.class);
        intent.putExtra(ARG_USER_ID,userId);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_up_over, R.anim.hold).toBundle();
        ActivityCompat.startActivity(activity,intent,bundle);

    }
    public static void showUserProfile(Context context, long userId){
        if (context==null)
            return;
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(ARG_USER_ID,userId);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_over, R.anim.hold).toBundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            context.startActivity(intent,bundle);
        }
        else{
            context.startActivity(intent);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userId = getIntent().getLongExtra(ARG_USER_ID, -1L);
        loadingProgressBar = new LoadingProgressBar((ViewGroup)findViewById(R.id.layoutProfileRoot));
        setupToolbar();
        handleViews();
        setWidgetActions();
        if (userProfile==null)
            loadUserProfile();
        else
            fillViews();
    }

    /**
     * Set widget actions
     */
    private void setWidgetActions() {
        layoutFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
        layoutAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaActivity.showMedia(UserProfileActivity.this,userId);
            }
        });
        layoutChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
        textStatusAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
    }

    /**
     * Handle screen views
     */
    private void handleViews() {
        imageAvatar = (ImageView)findViewById(R.id.profileAvatar);
        textName = (TextView)findViewById(R.id.textName);
        textUsername = (TextView)findViewById(R.id.textUsername);
        layoutRegisteredActions = (ViewGroup)findViewById(R.id.buttonLayout);
        layoutAlbum = (ViewGroup)findViewById(R.id.profileButtonPicsVids);
        layoutChat = (ViewGroup)findViewById(R.id.profileButtonChat);
        layoutFeed = (ViewGroup)findViewById(R.id.profileButtonFeeds);
        textStatus = (TextView)findViewById(R.id.textStatusValue);
        textStatusAction = (TextView)findViewById(R.id.textStatusAction);
        imgStatus = (ImageView)findViewById(R.id.imgStatus);
        textCity = (TextView)findViewById(R.id.textCityValue);
        textState = (TextView)findViewById(R.id.textStateValue);
        textCountry = (TextView)findViewById(R.id.textCountryValue);
    }

    /**
     * Fill screen views with values
     */
    private void fillViews() {
        Picasso.with(this) //load from profile
                .load(userProfile.pr_pic_stream)
                .placeholder(R.drawable.img_avatar)
                .error(R.drawable.img_avatar)
                .into(imageAvatar);
        String name = (TextUtils.isEmpty(userProfile.first)?"":(userProfile.first+" "))+(TextUtils.isEmpty(userProfile.last)?"":(userProfile.last));
        textName.setText(name);
        textUsername.setText(userProfile.username);
        if (TextUtils.isEmpty(userProfile.r_country))
            textCountry.setText("-");
        else
            textCountry.setText(userProfile.r_country);
        if (TextUtils.isEmpty(userProfile.r_locality))
            textCity.setText("-");
        else
            textCity.setText(userProfile.r_locality);
        if (TextUtils.isEmpty(userProfile.r_province))
            textState.setText("-");
        else
            textState.setText(userProfile.r_province);
        updateViewsByStatus();

    }

    /**
     * Update status widgets
     */
    private void updateViewsByStatus() {
        textStatusAction.setVisibility(View.VISIBLE);
        if (userProfile.isFriend()){
            textStatus.setText(R.string.status_friends);
            imgStatus.setImageResource(R.drawable.img_friends);
            textStatusAction.setText(R.string.status_action_unfriend);
            layoutRegisteredActions.setVisibility(View.VISIBLE);
        }else if (userProfile.isPending()){
            layoutRegisteredActions.setVisibility(View.GONE);
            textStatus.setText(R.string.status_pending);
            imgStatus.setImageResource(R.drawable.img_pending);
            if (userProfile.isLastActionMine())
                textStatusAction.setText(R.string.status_action_cancel);
            else
                textStatusAction.setVisibility(View.GONE);

        }
        else{
            layoutRegisteredActions.setVisibility(View.GONE);
            textStatus.setText(R.string.status_not_friends);
            imgStatus.setImageResource(R.drawable.img_add_friend);
            textStatusAction.setText(R.string.status_action_add_friend);

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
        //noinspection deprecation
        int color = getResources().getColor(R.color.colorPrimary);
        //set toolbar as action bar
        setSupportActionBar(toolbar);
        //get toolbar title
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        //close image button
        ImageView imageClose = (ImageView) toolbar.findViewById(R.id.toolbar_close);
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //finish activity on close click
            }
        });
        imageClose.setVisibility(View.VISIBLE);
        ActionBar ab = getSupportActionBar();
        if (ab!=null)
            toolbar.setBackgroundColor(color); //set toolbar background color according tree color

        //get contrast color for selected color and set it to toolbar title and toolbar close button
        Utils.ColorTint tint = Utils.getContrastColor(color);
        int textColor = Color.WHITE;
        if (tint== Utils.ColorTint.darkTintColor)
            //noinspection deprecation
            textColor = getResources().getColor(R.color.dark_gray);
        toolbarTitle.setTextColor(textColor);
        imageClose.setColorFilter(textColor);
        int darkcolor = Utils.getDarkerColor(color);
        updateStatusbarColor(darkcolor,tint);
    }

    /**
     * Update status bar color
     * @param color the color of the status bar
     * @param tint drak or light tint of the text tatus bar
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,R.anim.slide_down_over); //show animation
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
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                        NotificationHelper.showError(UserProfileActivity.this,R.string.failed_parse_server_answer);
                    }
                }
                else{
                    NotificationHelper.showError(UserProfileActivity.this,R.string.failed_parse_server_answer);
                }
                loadingProgressBar.toggleProgressBar(false);
                if (userProfile==null)
                    finish();
                else
                    fillViews();
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                loadingProgressBar.toggleProgressBar(false);
                NotificationHelper.showError(UserProfileActivity.this, error.getDescription(UserProfileActivity.this));
            }
        };
        loadingProgressBar.toggleProgressBar(true);
        TreemProfileService.getUserProfile(request, userId,CurrentTreeSettings.SHARED_INSTANCE.treeSession);
    }

}
