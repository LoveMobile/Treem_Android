package com.treem.treem.activities.media;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.treem.treem.R;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.Utils;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.content.Media;
import com.treem.treem.services.Treem.TreemContentService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

/**
 * Image view activity
 */
public class ImageViewActivity extends AppCompatActivity{

    private static final String ARG_IMAGE_ID = "arg.image.id";
    private static final String ARG_IMAGE_URL = "arg.image.url";

    /**
     * Image id
     */
    public static final String EXTRA_ID="extra.image.id";
    /**
     * Is image was deleted
     */
    public static final String EXTRA_IS_DELETED="extra.isdeleted";

    /**
     * Loading placeholder
     */
    private LoadingProgressBar waitingProgressBar;

    /**
     * Image id
     */
    private long imageId;
    /**
     * Image url
     */
    private String imageUrl;

    /**
     * Image view
     */
    private ImageView image;
    /**
     * Root content layout
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ViewGroup rootLayout;

    /**
     * Show image
     * @param activity base activity
     * @param media media object instance
     * @param v image view with thumbnail
     * @param requestCode request code for activity result
     */
    public static void showImage(Activity activity, Media media,View v,int requestCode) {
        if (activity==null||media==null)
            return;
        Intent intent = new Intent(activity, ImageViewActivity.class);
        intent.putExtra(ARG_IMAGE_ID,media.getId());
        intent.putExtra(ARG_IMAGE_URL,media.getStreamUrl());
        Bundle bundle;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //animate shared transition for lolipop+
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, v, "thumbnail").toBundle();
        }
        else{//or show slid up/down animation for older devices
            bundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_up_over, R.anim.hold).toBundle();
        }
        ActivityCompat.startActivityForResult(activity,intent, requestCode, bundle);
    }
    /**
     * Show image
     * @param f base fragment
     * @param media media object instance
     * @param v image view with thumbnail
     * @param requestCode request code for activity result
     */

    public static void showImage(Fragment f, Media media, View v, int requestCode) {
        if (f==null||media==null)
            return;
        Intent intent = new Intent(f.getContext(), ImageViewActivity.class);
        intent.putExtra(ARG_IMAGE_ID,media.getId());
        intent.putExtra(ARG_IMAGE_URL,media.getStreamUrl());
        Bundle bundle;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP&&f.getActivity()!=null) { //animate shared transition for lolipop+
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(f.getActivity(), v, "thumbnail").toBundle();
        }
        else{//or show slid up/down animation for older devices
            bundle = ActivityOptionsCompat.makeCustomAnimation(f.getContext(), R.anim.slide_up_over, R.anim.hold).toBundle();
        }
        f.startActivityForResult(intent, requestCode, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        setContentView(R.layout.activity_image_view);
        parseIntent();
        setupToolbar();
        handleViews();
        loadImage();
    }

    /**
     * Load image
     */
    private void loadImage() {
        Picasso.with(this)
                .load(imageUrl)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        //nothing
                    }

                    @Override
                    public void onError() {
                        NotificationHelper.showError(ImageViewActivity.this,R.string.failed_load_media); //notify error
                        onBackPressed(); //go back
                    }
                });
    }

    /**
     * Handle activity views
     */
    private void handleViews() {
        rootLayout = (ViewGroup)findViewById(R.id.layoutImageViewRoot);
        waitingProgressBar = new LoadingProgressBar(rootLayout, Color.BLACK);
        image = (ImageView)findViewById(R.id.image);
        ImageButton buttonClose = (ImageButton) findViewById(R.id.buttonClose);
        if (buttonClose != null) {
            buttonClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        ImageButton buttonDelete = (ImageButton) findViewById(R.id.buttonDelete);
        if (buttonDelete != null) {
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    askDelete();
                }
            });
        }
    }

    /**
     * Ask delete image file
     */
    private void askDelete() {
        new AlertDialog.Builder(this,R.style.AlertDialogStyle)
                .setTitle(R.string.title_delete_image)
                .setMessage(R.string.msg_delete_image)
                .setNegativeButton(android.R.string.cancel,null)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDelete();
                    }
                })
                .show();
    }

    /**
     * Delete image file
     */
    private void doDelete() {
        waitingProgressBar.toggleProgressBar(true); //show waiting
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                waitingProgressBar.toggleProgressBar(false);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_ID,imageId);
                intent.putExtra(EXTRA_IS_DELETED,true);
                setResult(Activity.RESULT_OK,intent); //return with result to parent activity
                finish();
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                waitingProgressBar.toggleProgressBar(false);
                if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_1){
                    NotificationHelper.showError(ImageViewActivity.this,R.string.media_error_invalid_argument_passed);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_2){
                    NotificationHelper.showError(ImageViewActivity.this,R.string.media_error_wrong_id);
                }
                else{
                    NotificationHelper.showError(ImageViewActivity.this,getString(R.string.media_failed_load,error.getDescription(ImageViewActivity.this)));
                }
            }
        };
        TreemContentService.deleteImage(request,imageId, CurrentTreeSettings.SHARED_INSTANCE.treeSession);
    }


    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        //noinspection deprecation
        int darkColor = Utils.getDarkerColor(getResources().getColor(android.R.color.black));
        updateStatusbarColor(darkColor, Utils.ColorTint.lightTintColor);
    }

    /**
     * Update status bar colors
     * @param color background color
     * @param tint is dark or lite tinting for text
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
        imageId = getIntent().getLongExtra(ARG_IMAGE_ID,0);
        imageUrl = getIntent().getStringExtra(ARG_IMAGE_URL);
    }

    /**
     * Show animation for old versions of android only
     */
    @Override
    public void finish() {
        super.finish();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            overridePendingTransition(0, R.anim.slide_down_over); //show animation
        }
    }
}
