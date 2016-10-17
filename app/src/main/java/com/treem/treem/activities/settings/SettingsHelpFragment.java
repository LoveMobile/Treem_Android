package com.treem.treem.activities.settings;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.treem.treem.R;

/**
 * Settings->Help fragment
 */
public class SettingsHelpFragment extends SettingsBaseFragment {

    /**
     * Link to a help
     */
    private static final String webViewLink = "https://help.treemtest.com/";

    //WebView to show the help
    private WebView webHelp;

    //progress bar to show waiting
    private ProgressBar progressLoading;

    private static final String TAG = SettingsHelpFragment.class.getSimpleName();

    public SettingsHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_help, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        webHelp = (WebView)view.findViewById(R.id.webHelp);
        progressLoading = (ProgressBar)view.findViewById(R.id.progressBar);
        loadHelp();
    }

    /**
     * Init webview and load help
     */
    private void loadHelp() {
        //Show the progress bar
        progressLoading.setVisibility(View.VISIBLE);
        //set web client to handle loading complete
        webHelp.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //hide the progress
                progressLoading.setVisibility(View.GONE);
            }
        });
        //start loading
        webHelp.loadUrl(webViewLink);
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_help);
        setBackVisible(true);
    }

}
