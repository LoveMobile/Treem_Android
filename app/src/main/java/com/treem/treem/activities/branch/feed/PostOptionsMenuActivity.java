package com.treem.treem.activities.branch.feed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.helpers.Utils;
import com.treem.treem.models.post.Post;
import com.treem.treem.models.user.User;

/**
 * Show options activity for a post
 */
public class PostOptionsMenuActivity extends Activity {
    /**
     * Options "who can see this post" selected
     */
    public static final int OPTION_WHO_CAN_SEE = 1;

    /**
     * Options "Report" selected
     */
    public static final int OPTION_REPORT = 2;
    /**
     * Options "Reaction" selected
     */
    public static final int OPTION_REACTIONS = 3;

    /**
     * Options "edit this post" selected
     */
    public static final int OPTION_EDIT = 4;

    /**
     * Options "Delete this post" selected
     */
    public static final int OPTION_DELETE = 5;
    private static final String ARG_POST = "arg.post";
    private static final String ARG_IS_SHARE = "arg.is.share";

    /**
     * Extra contain a post data
     */
    public static final String EXTRA_POST = "extra.post";

    /**
     * Is share or post option clicked
     */
    public static final String EXTRA_IS_SHARE = "extra.is.share";

    /**
     * Selected option
     */
    public static final String EXTRA_OPTION = "extra.option";

    /**
     * Show options menu
     * @param fragment parent fragment
     * @param anchor clicked option view
     * @param post clicked post
     * @param isShare is share or post option clicked
     * @param requestCode request code for activity result
     */
    public static void showOptionsMenu(Fragment fragment, View anchor, Post post, boolean isShare, int requestCode){
        if (fragment==null||fragment.getActivity()==null||post==null)
            return;
        Intent intent = new Intent(fragment.getContext(), PostOptionsMenuActivity.class);

        intent.putExtra(ARG_POST,new Gson().toJson(post,Post.class));
        intent.putExtra(ARG_IS_SHARE,isShare);
        Bundle bundle;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP&&anchor!=null) { //animate shared transition for lolipop+
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(fragment.getActivity(), anchor, "options").toBundle();
        }
        else{//or show slid up/down animation for older devices
            bundle = ActivityOptionsCompat.makeCustomAnimation(fragment.getActivity(), R.anim.slide_up_over, R.anim.hold).toBundle();
        }
        fragment.startActivityForResult(intent, requestCode, bundle);

    }

    // Selected post
    private Post post;

    //Is share or post option clicked
    private boolean isShare;

    //Options views
    private ViewGroup optionWhoCanSee;
    private ViewGroup optionReactions;
    private ViewGroup optionEdit;
    private ViewGroup optionDelete;
    private ViewGroup optionReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateStatusBarColor();
        setContentView(R.layout.activity_post_options_menu);
        parseIntent();
        if (post==null) { //post was null - return without menu
            finish();
            return;
        }
        handleViews();
        setVisibility();
    }

    /**
     * Set view visibility
     */
    private void setVisibility() {
        optionWhoCanSee.setVisibility(View.VISIBLE); //always visible
        boolean isSelfPost = isSelfPost(); //is self post or not?

        //Options visible for seld post only
        optionReactions.setVisibility(isSelfPost?View.VISIBLE:View.GONE);
        optionDelete.setVisibility(isSelfPost?View.VISIBLE:View.GONE);
        optionEdit.setVisibility(isSelfPost?View.VISIBLE:View.GONE);

        //Option visible for non self
        optionReport.setVisibility(!isSelfPost?View.VISIBLE:View.GONE);
    }

    /**
     * Check is self post
     * @return true if this is a self post
     */
    private boolean isSelfPost() {
        Long id = isShare ? post.getShareUserId() : post.getUserId(); //get user id for clicked option
        User user = post.getUser(id); //get user for lcicked option
        return user != null && user.isSelf(); //return true if clicked on seld post
    }

    /**
     * Set status bar color to black
     */
    private void updateStatusBarColor() {
        //get contrast color for selected color and set it to toolbar title and toolbar close button
        Utils.ColorTint tint = Utils.ColorTint.darkTintColor;
        Utils.updateStatusbarColor(getWindow(),Color.BLACK,tint);
    }

    /**
     * Handle views
     */
    private void handleViews() {
        ViewGroup rootView = (ViewGroup) findViewById(R.id.rootView);
        //Set click handler to close activity on click outside buttons
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Handle and set click listener to options buttons
        optionWhoCanSee = (ViewGroup)findViewById(R.id.option_who_can_see);
        optionWhoCanSee.setOnClickListener(onOptionClickListener);
        optionReactions = (ViewGroup)findViewById(R.id.option_reactions);
        optionReactions.setOnClickListener(onOptionClickListener);
        optionEdit = (ViewGroup)findViewById(R.id.option_edit);
        optionEdit.setOnClickListener(onOptionClickListener);
        optionDelete= (ViewGroup)findViewById(R.id.option_delete);
        optionDelete.setOnClickListener(onOptionClickListener);
        optionReport= (ViewGroup)findViewById(R.id.option_report);
        optionReport.setOnClickListener(onOptionClickListener);
    }

    /**
     * Click handler for options
     */
    private View.OnClickListener onOptionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //select clicked option
            switch (v.getId()){
                case R.id.option_who_can_see:
                    select(OPTION_WHO_CAN_SEE);
                    break;
                case R.id.option_report:
                    select(OPTION_REPORT);
                    break;
                case R.id.option_reactions:
                    select(OPTION_REACTIONS);
                    break;
                case R.id.option_edit:
                    select(OPTION_EDIT);
                    break;
                case R.id.option_delete:
                    select(OPTION_DELETE);
                    break;
                default:
                    finish();
            }
        }
    };

    /**
     * Finish activity with selected parameter
     * @param option selected option
     */
    private void select(int option) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_POST,new Gson().toJson(post,Post.class)); //set post to intent extra
        intent.putExtra(EXTRA_IS_SHARE,isShare); //set is share
        intent.putExtra(EXTRA_OPTION,option); //set selected option
        setResult(Activity.RESULT_OK,intent);
        finish();
    }

    /**
     * Parse incoming intent
     */
    private void parseIntent() {
        if (getIntent()==null)
            return;
        String json = getIntent().getStringExtra(ARG_POST);
        if (json==null)
            return;
        try{
            post = new Gson().fromJson(json,Post.class);
        }
        catch (JsonSyntaxException e){
            e.printStackTrace();
        }
        isShare = getIntent().getBooleanExtra(ARG_IS_SHARE,false);
    }
}
