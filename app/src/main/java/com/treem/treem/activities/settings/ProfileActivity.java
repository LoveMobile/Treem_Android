package com.treem.treem.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.treem.treem.R;
import com.treem.treem.helpers.BaseFragment;
import com.treem.treem.helpers.Utils;

/**
 * Profile activity - open user profile fragment in single activity
 */
public class ProfileActivity extends AppCompatActivity {
    /**
     * Show user profile
     * @param activity parent activity
     */
    public static void showProfile(Activity activity){
        if (activity==null)
            return;
        Intent intent = new Intent(activity, ProfileActivity.class);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_up_over, R.anim.hold).toBundle();
        ActivityCompat.startActivity(activity,intent,bundle);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupToolbar();
        if (savedInstanceState==null)
            toProfileScreen();
    }

    /**
     * Open profile fragment
     */
    private void toProfileScreen() {
        switchFragment(new SettingsProfileFragment());
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
     * Switch settings fragment
     * @param f new fragment
     */
    private void switchFragment(BaseFragment f){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container,f,f.getFragmentTag());
        ft.commit();
    }

}
