package com.treem.treem.activities.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.models.profile.UserTreeSettings;
import com.treem.treem.services.Treem.TreemProfileService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

/**
 * Settings->Tree fragment
 */
public class SettingsTreeFragment extends SettingsBaseFragment {

    private static final String TAG = SettingsTreeFragment.class.getSimpleName();

    //Push notification switch
    private SwitchCompat switchPush;

    //progress bar to show waiting
    private ProgressBar progressPush;

    //used to prevent send push request on switch init
    private boolean lastPushSettings = false;


    public SettingsTreeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_tree, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        switchPush = (SwitchCompat)view.findViewById(R.id.switchPush);
        progressPush = (ProgressBar)view.findViewById(R.id.progressPush);
        setViewsHandlers();
        loadPushValue();
    }

    /**
     * Load settings for current tree
     */
    private void loadPushValue() {
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                if (data!=null) {
                    try {
                        UserTreeSettings settings = new Gson().fromJson(data,UserTreeSettings.class);
                        lastPushSettings = settings.isPushSet();
                        if (isAdded()) {
                            switchPush.setChecked(settings.isPushSet());
                            stopWaiting(true);
                        }
                        return;
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                        if (isAdded()) {
                            if (getContext() != null)
                                NotificationHelper.showError(getContext(), R.string.failed_parse_server_answer);
                            stopWaiting(false);
                        }
                        return;
                    }
                }
                if (isAdded()) {
                    stopWaiting(false);
                    if (getContext() != null)
                        NotificationHelper.showError(getContext(), R.string.error_general_message);
                }
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                if (isAdded()) {
                    if (getContext() != null)
                        NotificationHelper.showError(getContext(), error.getDescription(getContext()));
                    stopWaiting(false);

                }
            }
        };
        startWaiting();
        TreemProfileService.getUserTreeSetting(request, CurrentTreeSettings.SHARED_INSTANCE.treeSession);
    }

    /**
     * Stop and hide the progress bar and show the switch
     * @param isSuccess is request completed success
     */
    private void stopWaiting(@SuppressWarnings("UnusedParameters") boolean isSuccess) {
        progressPush.setVisibility(View.GONE);
        switchPush.setVisibility(isSuccess?View.VISIBLE:View.GONE);
    }

    /**
     * Show progress bar and hide the push switch
     */
    private void startWaiting() {
        progressPush.setVisibility(View.VISIBLE);
        switchPush.setVisibility(View.GONE);
    }

    /**
     * Set handlers for a views
     */
    private void setViewsHandlers() {
        switchPush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked!=lastPushSettings){ //update only in manual switch
                    lastPushSettings = isChecked;
                    savePushValue();
                }
            }
        });
    }

    /**
     * Save current tree push value
     */
    private void savePushValue() {
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                if (isAdded())
                    stopWaiting(true);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                if (isAdded()) {
                    if (getContext() != null)
                        NotificationHelper.showError(getContext(), error.getDescription(getContext()));
                    stopWaiting(false);
                }
            }
        };
        startWaiting();
        UserTreeSettings settings = new UserTreeSettings();
        settings.setPush(switchPush.isChecked());
        TreemProfileService.setUserTreeSettings(request, CurrentTreeSettings.SHARED_INSTANCE.treeSession,settings);
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.settings_tree);
        setBackVisible(true);
    }

}
