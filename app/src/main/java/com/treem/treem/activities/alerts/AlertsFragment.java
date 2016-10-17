package com.treem.treem.activities.alerts;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.activities.main.PageBaseFragment;
import com.treem.treem.activities.tree.TreeViewActivity;
import com.treem.treem.activities.users.UserProfileActivity;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.Stub;
import com.treem.treem.helpers.recyclerview.EndlessRecyclerViewLinearScrollListener;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.alert.Alert;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.models.user.UserAdd;
import com.treem.treem.models.user.UserRemove;
import com.treem.treem.services.Treem.TreemAlertService;
import com.treem.treem.services.Treem.TreemBranchService;
import com.treem.treem.services.Treem.TreemSeedingService;
import com.treem.treem.services.Treem.TreemService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;
import com.treem.treem.widget.Panel;
import com.treem.treem.widget.TintableImageView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Alerts fragment page
 */
public class AlertsFragment extends PageBaseFragment {
    @SuppressWarnings("unused")
    private static final String TAG = AlertsFragment.class.getSimpleName();
    // Default load page size
    private static final int pageSizeDefault = 20;

    //Reasons for request
    private static final String reasonAlert = Integer.toString(Alert.REASON_NON_REQUEST_ALERTS);
    private static final String reasonInvited = Integer.toString(Alert.REASON_PENDING_FRIEND_REQUEST);
    private static final String reasonBranch = Integer.toString(Alert.REASON_BRANCH_SHARE);

    //Index of the alert tab
    private static final int tabIdxAlert = 0;
    //Index of the invited friends tab
    private static final int tabIdxInvited = 1;
    //Index of the shared branch tab
    private static final int tabIdxBrnach = 2;
    private static final int REQUEST_ADD_USER_PLACEMENT = 1;
    private static final int REQUEST_BRANCH_PLACE_SELECT = 2;

    //Tabs
    private TabLayout tabs;

    //Recycler view with items
    private RecyclerView viewItems;

    //No items text view
    private TextView textNoItems;

    //Swipe to refresh layout
    private SwipeRefreshLayout layoutRefresh;

    //Current selected tab
    private int selectedTab=tabIdxAlert;

    //Last loaded page
    private int currentPage = 1;

    //Last load request task
    private TreemService.NetworkRequestTask task = null;

    //Flag loading data when true
    private boolean isLoading = false;

    //Adapters for different pages
    private AlertsAdapter adapterAlert;
    private AlertsAdapter adapterInvite;
    private AlertsAdapter adapterBranch;

    //Bottom panel with actions
    private AlertsPanel panelActions;

    private LoadingProgressBar updateAlertsBar;

    //Endless scroller for recycler view
    private EndlessRecyclerViewLinearScrollListener mEndlessScroller;
    private Alert lastSelectedAlert;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tabs = (TabLayout)view.findViewById(R.id.alertTabs);
        viewItems = (RecyclerView)view.findViewById(R.id.alertItemsView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        viewItems.setLayoutManager(layoutManager);
        adapterAlert = new AlertsAdapter(getContext(),false);
        adapterBranch = new AlertsAdapter(getContext(),true);
        adapterInvite = new AlertsAdapter(getContext(),true);
        panelActions = (AlertsPanel) view.findViewById(R.id.testPanel);
        panelActions.setActionListener(onActionPanelClickListener);
        textNoItems = (TextView)view.findViewById(R.id.alertNoItems);
        updateAlertsBar = new LoadingProgressBar((ViewGroup)view.findViewById(R.id.alertsRootLayout));
        updateAdapters();
        layoutRefresh = (SwipeRefreshLayout)view.findViewById(R.id.alertLayoutRefresh);
        //workaround to show refresh layout
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        layoutRefresh.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));


        //handle endless listener
        mEndlessScroller = new EndlessRecyclerViewLinearScrollListener(layoutManager) {
            @Override
            public boolean onLoadMore() {
                loadMoreData();
                return true;
            }
        };

        //handle swipe to refresh event
        layoutRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadSelectedTab();
                layoutRefresh.setRefreshing(false);
            }
        });

        viewItems.addOnScrollListener(mEndlessScroller);

        initTabs(); //init tabs
        loadSelectedTab(); //load selected tab
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        panelActions.setActionListener(null);
    }

    private AlertsPanel.OnActionListener onActionPanelClickListener = new AlertsPanel.OnActionListener() {
        @Override
        public void onLeftButtonClick(Panel panel) {
            markSelectedAlertsRead();
        }

        @Override
        public void onRightButtonClick(Panel panel) {
            if (selectedTab==tabIdxAlert)
                removeSelectedAlerts();
            else
                declineSelectedAlerts();
        }
    };

    /**
     * Update recycler view adapters
     */
    private void updateAdapters() {

        //Set listeners for checked click
        adapterAlert.setOnCheckedClickListener(new AlertsAdapter.OnCheckedChangeListener() {
            @Override
            public void onCheckedClick(Alert alert, int position,int total) {
                if (total>0)
                    showPanel();
                else
                    hidePanel();
            }
        });
        adapterAlert.setOnItemClickListener(new AlertsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Alert alert, int position) {
                //noting
            }

            @Override
            public void onAvatarClick(Alert alert, int position) {
                if (alert!=null&&alert.getUserFrom()!=null)
                    showUserProfile(alert.getUserFrom().getId());
            }
        });
        adapterInvite.setOnCheckedClickListener(new AlertsAdapter.OnCheckedChangeListener() {
            @Override
            public void onCheckedClick(Alert alert, int position,int total) {
                if (total>0)
                    showPanel();
                else
                    hidePanel();
            }
        });
        adapterInvite.setOnItemClickListener(new AlertsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Alert alert, int position) {
                //nothing
            }
            @Override
            public void onAvatarClick(Alert alert, int position) {
                if (alert!=null&&alert.getUserFrom()!=null)
                    showUserProfile(alert.getUserFrom().getId());
            }

        });
        adapterInvite.setOnButtonClickListener(new AlertsAdapter.OnButtonsClickListener() {
            @Override
            public void onAcceptButtonClick(Alert alert, int position) {
                accept(alert);
            }

            @Override
            public void onDeclineButtonClick(Alert alert, int position) {
                Set<Alert> alerts = new HashSet<>();
                alerts.add(alert);
                decline(alerts);
            }
        });

        adapterBranch.setOnCheckedClickListener(new AlertsAdapter.OnCheckedChangeListener() {
            @Override
            public void onCheckedClick(Alert alert, int position,int total) {
                if (total>0)
                    showPanel();
                else
                    hidePanel();
            }
        });
        adapterBranch.setOnItemClickListener(new AlertsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Alert alert, int position) {
                //nothing
            }
            @Override
            public void onAvatarClick(Alert alert, int position) {
                if (alert!=null&&alert.getUserFrom()!=null)
                    showUserProfile(alert.getUserFrom().getId());
            }

        });
        adapterBranch.setOnButtonClickListener(new AlertsAdapter.OnButtonsClickListener() {
            @Override
            public void onAcceptButtonClick(Alert alert, int position) {
                accept(alert);
            }

            @Override
            public void onDeclineButtonClick(Alert alert, int position) {
                Set<Alert> alerts = new HashSet<>();
                alerts.add(alert);
                decline(alerts);
            }
        });


    }

    private void showUserProfile(long id) {
        UserProfileActivity.showUserProfile(getActivity(),id);
    }

    /**
     * Show bottom action panel
     */
    private void showPanel() {
        panelActions.setAlert(R.string.mark_as_read,selectedTab==tabIdxAlert?
                R.string.remove:
                R.string.decline);
        panelActions.show();
    }

    /**
     * Hide bottom action panel
     */
    private void hidePanel(){
        panelActions.hide();
    }

    /**
     * Decline button click
     */
    private void declineSelectedAlerts() {
        if (getSelectedAdapter()!=null)
            decline(getSelectedAdapter().getSelected());
    }

    /**
     * Remove button click
     */
    private void removeSelectedAlerts() {
        if (getSelectedAdapter()!=null){
            final AlertsAdapter adapter = getSelectedAdapter();
            Set<Alert> alerts = adapter.getSelected();
            if (alerts.size()>0){
                Set<Alert.AlertJson> toRemove = new HashSet<>();
                for (Alert alert:alerts)
                    toRemove.add(new Alert.AlertJson(alert));
                TreemServiceRequest request = new TreemServiceRequest() {
                    @Override
                    public void onSuccess(String data) {
                        updateAlertsBar.toggleProgressBar(false);
                        adapter.removeSelected();
                    }

                    @Override
                    public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                        updateAlertsBar.toggleProgressBar(false);
                        adapter.clearSelection();
                        NotificationHelper.showError(getContext(),getString(R.string.failed_alerts_remove,error.getDescription(getContext())));
                        loadSelectedTab();
                    }
                };
                updateAlertsBar.toggleProgressBar(true);
                TreemAlertService.clearAlerts(request,toRemove,CurrentTreeSettings.SHARED_INSTANCE.treeSession);
            }
        }
    }

    /**
     * Mark alert as read
     */
    private void markSelectedAlertsRead() {
        if (getSelectedAdapter()!=null){
            final AlertsAdapter adapter = getSelectedAdapter();
            Set<Alert> alerts = adapter.getSelected();

            if (alerts.size()>0){
                Set<Alert.AlertJson> toRemove = new HashSet<>();
                for (Alert alert:alerts) {
                    alert.setViewed(true);
                    toRemove.add(new Alert.AlertJson(alert));
                }

                TreemServiceRequest request = new TreemServiceRequest() {
                    @Override
                    public void onSuccess(String data) {
                        updateAlertsBar.toggleProgressBar(false);
                        adapter.markSelectedAsRead();
                    }

                    @Override
                    public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                        updateAlertsBar.toggleProgressBar(false);
                        adapter.clearSelection();
                        NotificationHelper.showError(getContext(),getString(R.string.failed_set_alerts_read,error.getDescription(getContext())));
                        loadSelectedTab();
                    }
                };
                updateAlertsBar.toggleProgressBar(true);
                TreemAlertService.setAlertsRead(request,toRemove,CurrentTreeSettings.SHARED_INSTANCE.treeSession);
            }
        }
    }

    /**
     * Start load next page with data
     */
    private void loadMoreData() {
        AlertsAdapter adapter = getSelectedAdapter(); //get selected adapter
        if (currentPage<=1) //do not load more data while adapter doesn't load main data
            return;
        adapter.startLoading(); //start loading
        currentPage++; //increase page
        loadData(); //load data
    }


    /**
     * Load selected tab
     */
    private void loadSelectedTab() {
        isLoading = false;
        if (task!=null&&!task.isCancelled()) //cancel current loading operation
            task.cancel(true);
        task = null;
        currentPage = 1; //reset page number
        mEndlessScroller.reset(); //reset endless scroller
        mEndlessScroller.setAllowLoadMore(false);
        if (getSelectedAdapter()!=null)
            getSelectedAdapter().clearSelection();
        hidePanel();
        loadData(); //load data
    }

    /**
     * Load data for current page
     */
    private void loadData() {
        /**
         * Load data request
         */
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                task = null;
                List<Alert> alerts = null;
                if (!TextUtils.isEmpty(data)&&!"\"\"".equals(data)) { //check is data empty
                    try {
                        alerts = new Gson().fromJson(data, Alert.LIST_TYPE);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                if (alerts!=null){
                    if (currentPage==1){
                        createAdapter(alerts); //if we have data and we are loading the first page
                    }
                    else{
                        addNewData(alerts); //we loaded next page of data
                    }
                }
                else{
                    allLoaded(true); //all loaded if data empty
                }
                if (updateAlertsBar.isShown()){//stop refreshing widget
                    updateAlertsBar.toggleProgressBar(false);
                    layoutRefresh.setEnabled(true);
                }
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                if (error==TreemServiceResponseCode.CANCELED&&isLoading) //if cancel and loading new data - return
                    return;
                task = null;
                if (updateAlertsBar.isShown()){//stop refreshing widget
                    updateAlertsBar.toggleProgressBar(false);
                    layoutRefresh.setEnabled(true);
                }
                getSelectedAdapter().stopLoading(); //stop loading at adapter
                if (isFragmentPrimary()) //show error if primary
                    NotificationHelper.showError(getContext(),getString(R.string.alert_failed_load_data,error.getDescription(getContext())));
            }
        };

        //set request reason
        String reason = reasonAlert;
        if (selectedTab==tabIdxInvited)
            reason = reasonInvited;
        else if (selectedTab==tabIdxBrnach)
            reason = reasonBranch;
        isLoading = true;
        //start loading
        task = TreemAlertService.getAlerts(
                request,
                reason,
                currentPage,
                pageSizeDefault,
                CurrentTreeSettings.SHARED_INSTANCE.treeSession);
        if (currentPage==1) {
            allLoaded(false); //set all loaded to false and start refreshing for loading first page
            startRefreshing();
        }
    }

    /**
     * Start refreshing
     */
    private void startRefreshing() {
        updateAlertsBar.toggleProgressBar(true);
        layoutRefresh.setEnabled(false);
    }

    /**
     * set loaded flag for scroller and show no items vioew if needed
     * @param isAllLoaded true is all data loaded
     */
    private void allLoaded(boolean isAllLoaded) {
        getSelectedAdapter().stopLoading(); //remove adapter waiting widget
        updateNoItems();
        //reset endless scroller
        mEndlessScroller.dataLoaded(!isAllLoaded);
    }

    private void updateNoItems() {
        if (task==null) { //data loaded - show no items if needed
            if (viewItems.getAdapter().getItemCount() > 0)
                textNoItems.setVisibility(View.GONE);
            else {
                switch (selectedTab){
                    case tabIdxAlert:
                        textNoItems.setText(R.string.no_items_alert);
                        break;
                    case tabIdxInvited:
                        textNoItems.setText(R.string.no_items_invited);
                        break;
                    case tabIdxBrnach:
                        textNoItems.setText(R.string.no_items_branch);
                        break;
                }
                textNoItems.setVisibility(View.VISIBLE);
            }
        }
        else{ //new data loading
            textNoItems.setVisibility(View.GONE);
        }
    }

    /**
     * Next page loaded
     * @param alerts new alerts
     */
    private void addNewData(List<Alert> alerts) {
        getSelectedAdapter().addData(alerts); //add data to current adapter
        allLoaded(alerts.size()<pageSizeDefault);
    }

    /**
     * Data loaded for the first page
     * @param alerts new alerts
     */
    private void createAdapter(List<Alert> alerts) {
        AlertsAdapter adapter = getSelectedAdapter(); //get selected adapter
        if (alerts!=null&&alerts.size()>0) { //set data if it is
            adapter.setItems(alerts);
            allLoaded(alerts.size()<pageSizeDefault);
        }
        else{ //clear data for empty answer
            adapter.clear();
            allLoaded(true);
        }
    }

    /**
     * Init tab layout
     */
    private void initTabs() {
        //init tabs
        tabs.addTab(getTabWithIcon(R.drawable.ic_alert),selectedTab==tabIdxAlert);
        tabs.addTab(getTabWithIcon(R.drawable.ic_invited),selectedTab==tabIdxInvited);
        tabs.addTab(getTabWithIcon(R.drawable.ic_share),selectedTab==tabIdxBrnach);
        //get selected tab
        selectedTab = tabs.getSelectedTabPosition();
        //set selected adapter to recycler view
        viewItems.setAdapter(getSelectedAdapter());
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //update adapter ans start load on tab change
                selectedTab = tab.getPosition();
                viewItems.setAdapter(getSelectedAdapter());
                loadSelectedTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * Get adapter for selected page
     * @return adapter for current page
     */
    private AlertsAdapter getSelectedAdapter() {
        switch (selectedTab){
            case tabIdxBrnach:
                return adapterBranch;
            case tabIdxInvited:
                return adapterInvite;
            default:
                return adapterAlert;
        }
    }

    /**
     * Create new tab and set icon to it
     * @param iconId icon resource id
     * @return new tab
     */
    private TabLayout.Tab getTabWithIcon(int iconId){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        TabLayout.Tab tab = tabs.newTab();
        View v = inflater.inflate(R.layout.layout_tab,tabs,false);
        TintableImageView tiv = (TintableImageView)v.findViewById(R.id.tabIcon);
        tiv.setImageResource(iconId);
        tab.setCustomView(v);
        return tab;
    }
    private void decline(Set<Alert> alerts){
        if (selectedTab==tabIdxInvited){
            askDeclineFriends(alerts);
        }
        else{
            askDeclineBranches(alerts);
        }
    }

    private void askDeclineBranches(final Set<Alert> alerts) {
        new AlertDialog.Builder(getContext(),R.style.AlertDialogStyle)
                .setTitle(R.string.title_decline_branch)
                .setMessage(R.string.msg_decline_branch)
                .setNegativeButton(android.R.string.no,null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        declineBranches(alerts);
                    }
                })
                .show();
    }

    private void declineBranches(final Set<Alert> alerts) {
        final AlertsAdapter adapter = getSelectedAdapter();
        updateAlertsBar.toggleProgressBar(true);
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                updateAlertsBar.toggleProgressBar(false);
                adapter.removeAlerts(alerts);
                updateNoItems();
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                updateAlertsBar.toggleProgressBar(false);
                adapter.clearSelection();
                NotificationHelper.showError(getContext(),getString(R.string.failed_decline_branch,error.getDescription(getContext())));
                loadSelectedTab();
            }
        };
        Set<Alert.AlertJson> toDecline = new HashSet<>();
        for (Alert alert:alerts) {
            alert.setViewed(true);
            toDecline.add(new Alert.AlertJson(alert));
        }
        TreemBranchService.declineShare(CurrentTreeSettings.SHARED_INSTANCE.treeSession,
                toDecline,request);
    }

    private void declineFriends(final Set<Alert> alerts) {
        final AlertsAdapter adapter = getSelectedAdapter();
        updateAlertsBar.toggleProgressBar(true);
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                updateAlertsBar.toggleProgressBar(false);
                adapter.removeAlerts(alerts);
                updateNoItems();
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                updateAlertsBar.toggleProgressBar(false);
                adapter.clearSelection();
                NotificationHelper.showError(getContext(),getString(R.string.failed_decline_branch,error.getDescription(getContext())));
                loadSelectedTab();
            }
        };
        Set<UserRemove> toDecline = new HashSet<>();
        for (Alert alert:alerts) {
            if (alert.getUserFrom()!=null) {
                toDecline.add(new UserRemove(alert.getUserFrom()));
            }
        }
        TreemSeedingService.trimUsers(CurrentTreeSettings.SHARED_INSTANCE.treeSession,
                toDecline,0,request);
    }

    private void askDeclineFriends(final Set<Alert> alerts) {
        new AlertDialog.Builder(getContext(),R.style.AlertDialogStyle)
                .setTitle(R.string.title_decline_friends)
                .setMessage(R.string.msg_decline_friends)
                .setNegativeButton(android.R.string.no,null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        declineFriends(alerts);
                    }
                })
                .show();

    }
    private void accept(Alert alert){
        if (selectedTab==tabIdxBrnach){
            askInsertBranchPlacement(alert);
        }
        else{
            askAddUserBranch(alert);
        }
    }

    private void askAddUserBranch(Alert alert) {
        lastSelectedAlert = alert;
        TreeViewActivity.showAddUserSelectBranch(this,REQUEST_ADD_USER_PLACEMENT);
    }

    private void askInsertBranchPlacement(Alert alert) {
        lastSelectedAlert = alert;
        TreeViewActivity.showBranchPlaceSelect(this,REQUEST_BRANCH_PLACE_SELECT);
    }

    private void insertBranchPlacement(final Alert alert, Branch placement) {
        final AlertsAdapter adapter = getSelectedAdapter();
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                updateAlertsBar.toggleProgressBar(false);
                adapter.removeAlert(alert);
                NotificationHelper.showSuccess(getContext(),R.string.branch_added);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                updateAlertsBar.toggleProgressBar(false);
                if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_1){
                    NotificationHelper.showError(getContext(),R.string.error_branch_invalid_request_data);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_2){
                    NotificationHelper.showError(getContext(),R.string.error_branch_invalid_tree_id);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_3){
                    NotificationHelper.showError(getContext(),R.string.error_branch_invalid_placement);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_4){
                    NotificationHelper.showError(getContext(),R.string.error_branch_invalid_source);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_5){
                    NotificationHelper.showError(getContext(),R.string.error_branch_exceeded_depth);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_6){
                    NotificationHelper.showError(getContext(),R.string.error_branch_exceeded_branches);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_7){
                    NotificationHelper.showError(getContext(),R.string.error_branch_position_already_exists);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_8){
                    NotificationHelper.showError(getContext(),R.string.error_branch_invalid_share);
                }
                else
                    NotificationHelper.showError(getContext(),getString(R.string.failed_accept_branch,error.getDescription(getContext())));
                loadSelectedTab();
            }
        };
        updateAlertsBar.toggleProgressBar(true);
        TreemBranchService.acceptShare(CurrentTreeSettings.SHARED_INSTANCE.treeSession,
                alert,placement,request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_ADD_USER_PLACEMENT&&resultCode== Activity.RESULT_OK&&data!=null){
            long branchId = data.getLongExtra(TreeViewActivity.EXTRA_BRANCH_ID,0);
            acceptUserInvite(lastSelectedAlert,branchId);
        }
        else if (requestCode==REQUEST_BRANCH_PLACE_SELECT&&resultCode== Activity.RESULT_OK&&data!=null){
            long parentId = data.getLongExtra(TreeViewActivity.EXTRA_PARENT_BRANCH_ID,0);
            int position = data.getIntExtra(TreeViewActivity.EXTRA_POSITION,0);
            Branch branch = Stub.getPlacementBranch(parentId,position);
            insertBranchPlacement(lastSelectedAlert,branch);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void acceptUserInvite(final Alert alert, long branchId) {
        final AlertsAdapter adapter = getSelectedAdapter();
        if (alert!=null&&alert.getUserFrom()!=null) {
            TreemServiceRequest request = new TreemServiceRequest() {
                @Override
                public void onSuccess(String data) {
                    updateAlertsBar.toggleProgressBar(false);
                    NotificationHelper.showSuccess(getContext(),R.string.user_added);
                    if (adapter!=null)
                        adapter.removeAlert(alert);
                }

                @Override
                public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                    updateAlertsBar.toggleProgressBar(false);
                    if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_1){
                        NotificationHelper.showError(getContext(),R.string.error_seeding_invalid_parameters_passed);
                    }
                    else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_2){
                        NotificationHelper.showError(getContext(),R.string.error_seeding_invalid_branch_id);
                    }
                    else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_3){
                        NotificationHelper.showError(getContext(),R.string.error_seeding_user_dont_exists);
                    }
                    else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_4){
                        NotificationHelper.showError(getContext(),R.string.error_seeding_invitation_code_generation_failed);
                    }
                    else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_5){
                        NotificationHelper.showError(getContext(),R.string.error_try_seed_self);
                    }
                    else
                        NotificationHelper.showError(getContext(),getString(R.string.seeding_failed_set_user,error.getDescription(getContext())));
                }
            };
            Set<UserAdd> user = new HashSet<>();
            user.add(new UserAdd(alert.getUserFrom()));
            updateAlertsBar.toggleProgressBar(true);
            TreemSeedingService.setUsers(CurrentTreeSettings.SHARED_INSTANCE.treeSession, user, branchId, request);
        }
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
}

