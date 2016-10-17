package com.treem.treem.activities.media;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.Utils;
import com.treem.treem.helpers.recyclerview.EndlessRecyclerViewGridScrollListener;
import com.treem.treem.helpers.recyclerview.PaddingItemDecoration;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.content.Media;
import com.treem.treem.models.content.Video;
import com.treem.treem.services.Treem.TreemContentService;
import com.treem.treem.services.Treem.TreemFeedService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

import java.util.List;

/**
 * Media list activity
 */
public class MediaActivity extends AppCompatActivity implements MediaViewAdapter.OnMediaClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = MediaActivity.class.getSimpleName();

    private static final String ARG_USER = "arg.user";

    /**
     * Default page size
     */
    private static final int pageSizeDefault = 100;
    /**
     * Default columns count
     */
    private static final int columnCount = 3;

    //Request code for delete image
    private static final int REQUEST_DELETE = 1;

    //Selected user
    private long selectedUser;

    /**
     * Loading placeholder
     */
    private LoadingProgressBar loadProgressBar;

    /**
     * Failed to load data layout
     */
    private View layoutFailedLoad;

    /**
     * Root content layout
     */
    private ViewGroup rootLayout;

    /**
     * Current loaded page
     */
    private int currentPage = 1;

    /**
     * Recycler view adapter for media content
     */
    private MediaViewAdapter adapterMedia;

    private TextView noItemsView;

    /**
     * Recycler view scroll listener to handle endless scrolling events
     */
    private EndlessRecyclerViewGridScrollListener scrollListener;

    /**
     * Show media activity
     * @param activity parent activity
     * @param userId id of selected user null for current user
     */
    public static void showMedia(Activity activity, Long userId) {
        if (activity==null)
            return;
        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra(ARG_USER, userId);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_up_over, R.anim.hold).toBundle();
        ActivityCompat.startActivity(activity,intent,bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        setContentView(R.layout.activity_media);
        parseIntent();
        setupToolbar();
        handleViews();
        loadContent();
    }

    /**
     * Load media content list
     */
    private void loadContent() {
        rootLayout.removeView(layoutFailedLoad);
        noItemsView.setVisibility(View.GONE);
        if (adapterMedia.getItemCount()==0) { //is this a first load?
            //reset data before load
            currentPage = 1;
            loadProgressBar.toggleProgressBar(true);
            scrollListener.reset();
        }
        else
            adapterMedia.startLoading();

        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                //hide visual loading elements
                loadProgressBar.toggleProgressBar(false);
                adapterMedia.stopLoading();
                //check is data loaded
                if (!TextUtils.isEmpty(data)&&!"\"\"".equals(data)){
                    try {
                        //parse response
                        List<Media> items = new Gson().fromJson(data, Media.LIST_TYPE);
                        if (adapterMedia.getItemCount() == 0)
                            adapterMedia.setItems(items);
                        else
                            adapterMedia.addData(items);
                        //is next data loaded
                        scrollListener.dataLoaded(items.size()>=pageSizeDefault);
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                        scrollListener.dataLoaded(false);
                    }
                }
                else{
                    scrollListener.dataLoaded(false);
                }
                checkShowNoItems();
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                //hide loading elements
                loadProgressBar.toggleProgressBar(false);
                adapterMedia.stopLoading();
                scrollListener.dataLoaded(false);
                if (adapterMedia.getItemCount()==0) //is this a first page?
                    showFailed();
                NotificationHelper.showError(MediaActivity.this,getString(R.string.media_failed_load,error.getDescription(MediaActivity.this)));
            }
        };

        //get media data
        TreemFeedService.getMediaItems(
                request,
                selectedUser,
                currentPage,
                pageSizeDefault,
                CurrentTreeSettings.SHARED_INSTANCE.treeSession
        );
    }

    /**
     * Check and show the no items view if needed
     */
    private void checkShowNoItems() {
        if (adapterMedia.getItemCount()==0) //is adapter empty - show no items view
            noItemsView.setVisibility(View.VISIBLE);
    }

    /**
     * Show failed load layout
     */
    private void showFailed() {
        rootLayout.addView(layoutFailedLoad);
    }

    /**
     * Handle activity views
     */
    private void handleViews() {
        rootLayout = (ViewGroup)findViewById(R.id.mediaRootLayout);
        layoutFailedLoad = LayoutInflater.from(this).inflate(R.layout.layout_rewards_failed_load,rootLayout,false);
        Button reload = (Button)layoutFailedLoad.findViewById(R.id.buttonReload);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadContent();
            }
        });
        loadProgressBar = new LoadingProgressBar((ViewGroup) findViewById(R.id.mediaRootLayout));
        setupRecyclerView();
        noItemsView = (TextView)findViewById(R.id.mediaNoItems);
        if (selectedUser>0&&noItemsView!=null)
            noItemsView.setText(R.string.user_no_media_files);
    }

    /**
     * Setup recycler view with media items
     */
    private void setupRecyclerView() {
        RecyclerView itemsView = (RecyclerView) findViewById(R.id.itemsMedia);
        if (itemsView==null)
            return;

        //setup layout manager
        GridLayoutManager layoutManager = new GridLayoutManager(this, columnCount);
        itemsView.setLayoutManager(layoutManager);

        //init adapter
        adapterMedia = new MediaViewAdapter(this,this);
        itemsView.setAdapter(adapterMedia);

        //add padding item decoration
        itemsView.addItemDecoration(new PaddingItemDecoration(getResources().getDimensionPixelOffset(R.dimen.media_items_padding),columnCount));

        //init scroll listener for endless scrolling
        scrollListener = new EndlessRecyclerViewGridScrollListener(layoutManager) {
            @Override
            public boolean onLoadMore() {
                if (adapterMedia==null||adapterMedia.getItemCount()<=1) //do not load more data while adapter doesn't load main data
                    return true;
                adapterMedia.startLoading(); //start loading
                currentPage++; //increase page
                loadContent(); //load data
                return true;
            }
        };
        itemsView.addOnScrollListener(scrollListener);

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
        //noinspection deprecation
        int color = getResources().getColor(R.color.media_toolbar_color);
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
        int darkColor = Utils.getDarkerColor(color);
        updateStatusbarColor(darkColor,tint);
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
        selectedUser = getIntent().getLongExtra(ARG_USER,0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,R.anim.slide_down_over); //show animation
    }

    @Override
    public void onMediaClick(Media media, View v) {
        if (media!=null){
            if (media.getType()==Media.mediaTypeImage){
                ImageViewActivity.showImage(this,media,v,REQUEST_DELETE);
            }
            else if (media.getType()==Media.mediaTypeVideo){
                loadVideoDetails(media,v);
            }
        }
    }

    private void loadVideoDetails(Media media, final View view) {
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                if (data!=null){
                    try {
                        Video v = new Gson().fromJson(data, Video.class);
                        if (v!=null&&v.getVideoUrl()!=null)
                            openVideoPreview(v.getVideoUrl(),view);
                        else
                            NotificationHelper.showError(MediaActivity.this,R.string.error_empty_video_url);
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                        NotificationHelper.showError(MediaActivity.this,R.string.failed_parse_server_answer);
                    }
                }
                loadProgressBar.toggleProgressBar(false);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                loadProgressBar.toggleProgressBar(false);
                NotificationHelper.showError(MediaActivity.this,getString(R.string.error_load_video_details,error.getDescription(MediaActivity.this)));
            }
        };
        loadProgressBar.toggleProgressBar(true);
        TreemContentService.getVideoDetails(request,media.getId(),CurrentTreeSettings.SHARED_INSTANCE.treeSession);
    }

    private void openVideoPreview(String videoUrl,View view) {
        Uri uri = Uri.parse(videoUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, "video/*");
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.play_video)));
        } catch (Exception e){
            e.printStackTrace();
            VideoViewActivity.showVideo(this,videoUrl,view);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_DELETE&&resultCode==Activity.RESULT_OK&&data!=null){
            long id = data.getLongExtra(ImageViewActivity.EXTRA_ID,0);
            boolean isDeleted = data.getBooleanExtra(ImageViewActivity.EXTRA_IS_DELETED,false);
            if (id!=0&&isDeleted){
                adapterMedia.deleteItemId(id);
            }
            checkShowNoItems();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
