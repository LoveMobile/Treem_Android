package com.treem.treem.activities.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.content.Media;
import com.treem.treem.models.content.Video;
import com.treem.treem.services.Treem.TreemContentService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

/**
 * Vide view activity
 */
public class VideoViewActivity extends AppCompatActivity {
    private static final String ARG_URL = "arg.url";
    private static final String ARG_MEDIA = "arg.media";
    private LoadingProgressBar loadProgressBar;

    /**
     * Create an activity with video preview
     * @param activity parent activity
     * @param url link to show video
     * @param view transition view (for lollipop+)
     */
    public static void showVideo(Activity activity,String url,View view){
        Intent intent = new Intent(activity,VideoViewActivity.class);
        intent.putExtra(ARG_URL,url);
        Bundle bundle;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //animate shared transition for lolipop+
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, "thumbnail").toBundle();
        }
        else{//or show slid up/down animation for older devices
            bundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_up_over, R.anim.hold).toBundle();
        }
        ActivityCompat.startActivity(activity,intent,bundle);
    }
    /**
     * Create an activity with video preview
     * @param f parent fragment
     * @param url link to show video
     * @param view transition view (for lollipop+)
     */

    @SuppressWarnings("unused")
    public static void showVideo(Fragment f, String url, View view){
        Intent intent = new Intent(f.getContext(),VideoViewActivity.class);
        intent.putExtra(ARG_URL,url);
        Bundle bundle;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP&&f.getActivity()!=null) { //animate shared transition for lolipop+
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(f.getActivity(), view, "thumbnail").toBundle();
        }
        else{//or show slid up/down animation for older devices
            bundle = ActivityOptionsCompat.makeCustomAnimation(f.getContext(), R.anim.slide_up_over, R.anim.hold).toBundle();
        }

        f.startActivity(intent,bundle);
    }

    public static void showVideo(Fragment f, Media media){
        Intent intent = new Intent(f.getContext(),VideoViewActivity.class);
        intent.putExtra(ARG_MEDIA,new Gson().toJson(media,Media.class));
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(f.getContext(), R.anim.slide_up_over, R.anim.hold).toBundle();
        f.startActivity(intent,bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        loadProgressBar = new LoadingProgressBar((ViewGroup)findViewById(R.id.main_content));
        Intent intent=getIntent();
        if (intent==null) {
            finish();
            return;
        }
        String loadUrl = getIntent().getStringExtra(ARG_URL);
        if (loadUrl!=null){
            showVideo(loadUrl);
        }
        else {
            String json = getIntent().getStringExtra(ARG_MEDIA);
            if (json != null) {
                try {
                    Media media = new Gson().fromJson(json, Media.class);
                    loadVideoDetails(media);
                }
                catch (JsonSyntaxException e){
                    e.printStackTrace();
                }
            }
        }
    }
    private void showVideo(String url){
        VideoView video = (VideoView) findViewById(R.id.video);
        /*
         * Show video
         */
        if (video!=null) {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(video);
            video.setMediaController(mediaController);
            video.setVideoURI(Uri.parse(url));
            video.start();
        }
        else{
            finish();
        }
        ImageButton buttonClose = (ImageButton) findViewById(R.id.buttonClose);
        if (buttonClose != null) {
            buttonClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    /**
     * Load video view details
     * @param media the media with video links
     */
    private void loadVideoDetails(Media media) {
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                if (data!=null){
                    try {
                        Video v = new Gson().fromJson(data, Video.class);
                        if (v!=null&&v.getVideoUrl()!=null)
                            openVideoPreview(v.getVideoUrl());
                        else {
                            NotificationHelper.showError(VideoViewActivity.this, R.string.error_empty_video_url);
                            finish();
                        }
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                        NotificationHelper.showError(VideoViewActivity.this,R.string.failed_parse_server_answer);
                        finish();
                    }
                }
                loadProgressBar.toggleProgressBar(false);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                loadProgressBar.toggleProgressBar(false);
                NotificationHelper.showError(VideoViewActivity.this,getString(R.string.error_load_video_details,error.getDescription(VideoViewActivity.this)));
            }
        };
        loadProgressBar.toggleProgressBar(true);
        TreemContentService.getVideoDetails(request,media.getId(), CurrentTreeSettings.SHARED_INSTANCE.treeSession);
    }

    /**
     * Try to open video preview with url
     * @param videoUrl video url
     */
    private void openVideoPreview(String videoUrl) {
        Uri uri = Uri.parse(videoUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, "video/*");
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.play_video)));
            finish();
        } catch (Exception e){
            e.printStackTrace();
            showVideo(videoUrl);
        }

    }

}
