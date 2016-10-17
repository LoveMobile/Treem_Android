package com.treem.treem.activities.branch.members;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.activities.branch.BranchBaseFragment;
import com.treem.treem.activities.users.UserProfileActivity;
import com.treem.treem.application.AppConstants;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.AppSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.recyclerview.DividerItemDecoration;
import com.treem.treem.helpers.recyclerview.EndlessRecyclerViewLinearScrollListener;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.models.user.User;
import com.treem.treem.models.user.UserAdd;
import com.treem.treem.models.user.UserContact;
import com.treem.treem.models.user.UserRemove;
import com.treem.treem.services.Treem.TreemSeedingService;
import com.treem.treem.services.Treem.TreemService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;
import com.treem.treem.widget.Panel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Members fragment for branch activity
 */

public class BranchMembersFragment extends BranchBaseFragment implements BranchMembersAdapter.OnItemClickListener, BranchMembersAdapter.OnCheckedChangeListener, MembersPanel.OnActionListener {
    private static final String TAG = BranchMembersFragment.class.getSimpleName();

    //Branch argument
    private static final String ARG_BRANCH = "arg.branch";

    // Request code for request contacts permission
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    //Default page size
    private final static int pageSizeDefault=25;

    //Message text changed for search
    private static final int MESSAGE_TEXT_CHANGED = 1;

    //Request code for config screen
    private static final int REQUEST_CONFIG = 1;

    @SuppressWarnings("unused")
    private OnBranchMembersInteractionListener mListener;

    //Recycler view with members
    @SuppressWarnings("FieldCanBeLocal")
    private RecyclerView viewMembers;

    //No items view
    private NoMemberFoundView noItems;

    //Search field
    private EditText editSearch;

    //button settings
    private ImageButton buttonSettings;

    //Check all checkbox
    private CheckBox checkAll;

    //Selected branch
    private Branch branch;

    //Bottom panel
    private MembersPanel panel;

    //Loaded contacts
    private List<UserContact> contacts;

    //search config
    private MembersSearchConfig config = new MembersSearchConfig();

    //Last loaded page
    private int currentPage = 1;

    //Last load request task
    private TreemService.NetworkRequestTask task = null;

    //Flag loading data when true
    private boolean isLoading = false;

    //Endless scroller for recycler view
    private EndlessRecyclerViewLinearScrollListener mEndlessScroller;

    //Recycler view members adapter
    private BranchMembersAdapter adapter;

    //Loading progress
    private LoadingProgressBar loadingProgress;

    //Is contact grabbing now?
    private boolean isContactGrabbing = false;

    //Is remove warning shown?
    private boolean isRemoveWarningShown = false;

    public BranchMembersFragment() {
        // Required empty public constructor
    }

    public static BranchMembersFragment newInstance(Branch branch) {
        BranchMembersFragment fragment = new BranchMembersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BRANCH,new Gson().toJson(branch,Branch.class));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String json = getArguments().getString(ARG_BRANCH);
            if (json!=null)
                branch = new Gson().fromJson(json,Branch.class);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_branch_members, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //
        loadingProgress = new LoadingProgressBar((ViewGroup)view.findViewById(R.id.rootMembersView));
        panel = (MembersPanel)view.findViewById(R.id.membersPanel);
        panel.setActionListener(this);
        panel.setSelectionCount(0,0);

        editSearch = (EditText)view.findViewById(R.id.search);
        editSearch.addTextChangedListener(updateSearchWatcher);
        noItems = (NoMemberFoundView) view.findViewById(R.id.noMembersFound);
        noItems.setBranch(branch);
        viewMembers = (RecyclerView)view.findViewById(R.id.members);
        viewMembers.addItemDecoration(new DividerItemDecoration(getContext(),R.drawable.recycler_divider));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        viewMembers.setLayoutManager(layoutManager);
        adapter = new BranchMembersAdapter(getContext(),branch==null||branch.id==0);
        adapter.setOnItemClickListener(this);
        adapter.setOnCheckedChangeListener(this);
        viewMembers.setAdapter(adapter);
        mEndlessScroller = new EndlessRecyclerViewLinearScrollListener(layoutManager) {
            @Override
            public boolean onLoadMore() {
                loadMoreData();
                return true;
            }
        };
        checkAll = (CheckBox)view.findViewById(R.id.selectAll);
        checkAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    adapter.selectAll();
                else
                    adapter.deselectAll();
            }
        });
        buttonSettings = (ImageButton)view.findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettings();
            }
        });
        doFirstLoadIfNeeded();
    }

    private void doFirstLoadIfNeeded() {
        if (isFragmentPrimary()){
            if (checkContactsPermission()) {
                config.getMiscellaneous().setAllowUseContacts(true);
                loadUsers();
            }
            else{
                AppSettings settings = new AppSettings(getContext());
                if (settings.isMembersContactPermissionsAsked()){
                    config.getMiscellaneous().setAllowUseContacts(false);
                    config.getMiscellaneous().setShowContacts(false);
                    loadUsers();
                }
                else{
                    askAllowUseContacts();
                    settings.setMembersContactPermissionAsked(true);
                }
            }
        }
    }


    private void showSettings() {
        SearchConfigActivity.showConfig(this,branch,config,REQUEST_CONFIG);
    }

    private boolean checkContactsPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void askAllowUseContacts() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            showContactsPermissionDescription();
        } else {
            requestContactsPermissions();
        }
    }

    /**
     * Show read contacts permission description dialog
     */
    private void showContactsPermissionDescription() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
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
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
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
                    config.getMiscellaneous().setAllowUseContacts(true);
                }
                loadUsers();
            }

        }
    }

    private void loadMoreData() {
        if (currentPage<=1) //do not load more data while adapter doesn't load main data
            return;
        adapter.startLoading(); //start loading
        currentPage++; //increase page
        loadData(); //load data
    }

    private void loadUsers() {
        isLoading = false;
        if (task!=null&&!task.isCancelled()) //cancel current loading operation
            task.cancel(true);
        task = null;
        currentPage = 1; //reset page number
        mEndlessScroller.reset(); //reset endless scroller
        mEndlessScroller.setAllowLoadMore(false);
        adapter.clearSelectionAdd();
        adapter.clearSelectionRemove();
        updatePanel(0,0);
        if (isContactGrabbing)
            return;
        if (!loadingProgress.isShown())
            loadingProgress.toggleProgressBar(true);
        if (config.getMiscellaneous().isShowContactsSet()&&contacts==null) {
            requestContacts();
        }
        else {
            loadData();
        }

    }

    private void requestContacts() {
        new GetContactsTask().execute();
    }

    @Override
    public void onAvatarClick(User user, int position) {
        if (user!=null&&user.isTreemUser())
            UserProfileActivity.showUserProfile(getActivity(),user.getId());
    }

    @Override
    public void onCheckedClick(User user, int position) {
        int countRemove = adapter.getSelectedRemove().size();
        int countAdd = adapter.getSelectedAdd().size();
        if (countRemove>0&&!isRemoveWarningShown){
            showRemoveWarning();
        }
        updatePanel(countAdd,countRemove);
    }

    private void showRemoveWarning() {
        isRemoveWarningShown = true;
        new AlertDialog.Builder(getContext(),R.style.AlertDialogStyle)
                .setTitle(R.string.title_removing_members)
                .setMessage(R.string.msg_removing_members)
                .setPositiveButton(android.R.string.ok,null)
                .show();
    }

    private void updatePanel(int countAdd, int countRemove) {
        if (panel==null)
            return;
        if (countAdd==0&&countRemove==0)
            panel.hide();
        else {
            panel.setSelectionCount(countAdd, countRemove);
            panel.show();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        panel.setActionListener(null);
    }

    @Override
    public void onSaveButtonClick(Panel panel) {
        addUsers(adapter.getSelectedAdd());
    }

    private void addUsers(Set<User> selectedAdd) {
        if (selectedAdd!=null&&selectedAdd.size()>0) {
            Set<UserAdd> users = new HashSet<>();
            for (User u : selectedAdd) {
                users.add(new UserAdd(u));
            }
            TreemServiceRequest request = new TreemServiceRequest() {
                @Override
                public void onSuccess(String data) {
                    adapter.updateStatusAndOnBranch();
                    removeUsers(adapter.getSelectedRemove());
                }

                @Override
                public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
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

                    removeUsers(adapter.getSelectedRemove());
                }
            };
            loadingProgress.toggleProgressBar(true);
            TreemSeedingService.setUsers(CurrentTreeSettings.SHARED_INSTANCE.treeSession,
                    users, branch==null?0:branch.id, request);
        }
        else
            removeUsers(adapter.getSelectedRemove());
    }

    private void removeUsers(Set<User> selectedRemove) {
        if (selectedRemove!=null&&selectedRemove.size()>0) {
            Set<UserRemove> users = new HashSet<>();
            for (User u : selectedRemove) {
                users.add(new UserRemove(u));
            }
            TreemServiceRequest request = new TreemServiceRequest() {
                @Override
                public void onSuccess(String data) {
                    adapter.clearOnBranch();
                    loadingProgress.toggleProgressBar(false);

                }

                @Override
                public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                    NotificationHelper.showError(getContext(),getString(R.string.seeding_failed_set_user,error.getDescription(getContext())));
                    loadingProgress.toggleProgressBar(false);
                }
            };
            loadingProgress.toggleProgressBar(true);
            TreemSeedingService.trimUsers(CurrentTreeSettings.SHARED_INSTANCE.treeSession,
                    users, branch==null?0:branch.id, request);
        }
        else
            loadingProgress.toggleProgressBar(false);
    }

    @Override
    public void onCancelButtonClick(Panel panel) {
        checkAll.setChecked(false);
        adapter.reset();
    }

    private class GetContactsTask extends AsyncTask<Void,Void,List<UserContact>>{
        @Override
        protected void onPreExecute() {
            isContactGrabbing = true;
        }

        @Override
        protected List<UserContact> doInBackground(Void... params) {
            if (!checkContactsPermission())
                return null;
            return getContacts();
        }

        @Override
        protected void onPostExecute(List<UserContact> result) {
            contacts = result;
            isContactGrabbing = false;
            if (isAdded())
                loadData();
        }
    }

    private void loadData() {
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                loadingProgress.toggleProgressBar(false);
                if (data!=null){
                    List<User> users = null;
                    if (!TextUtils.isEmpty(data)&&!"\"\"".equals(data)) { //check is data empty
                        try {
                            users = new Gson().fromJson(data, User.LIST_TYPE);
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                            NotificationHelper.showError(getContext(),R.string.failed_parse_server_answer);
                        }
                    }
                    task = null;
                    if (users!=null){
                        if (contacts!=null) {
                            for (User user : users) {
                                if (user.getContactId() != null && user.getContactId() < contacts.size()) {
                                    user.setUserContact(contacts.get(user.getContactId()));
                                }
                            }
                        }
                    }
                    else{
                        allLoaded(true); //all loaded if data empty
                    }
                    if (currentPage==1){
                        createAdapter(users);
                    }
                    else
                        addNewData(users);


                }
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {

                if (error==TreemServiceResponseCode.CANCELED&&isLoading) //if cancel and loading new data - return
                    return;
                task = null;
                if (loadingProgress.isShown()){//stop refreshing widget
                    loadingProgress.toggleProgressBar(false);
                }
                adapter.stopLoading(); //stop loading at adapter
                NotificationHelper.showError(getContext(),getString(R.string.members_failed_load_data,error.getDescription(getContext())));
                allLoaded(true);
            }
        };
        isLoading = true;
        //start loading
        task = TreemSeedingService.searchUsers(
                request,
                branch==null?0:branch.id,
                editSearch.getText()!=null?editSearch.getText().toString():null,
                config,
                contacts,
                new ArrayList<Integer>(),
                currentPage,
                pageSizeDefault,
                CurrentTreeSettings.SHARED_INSTANCE.treeSession);
        if (currentPage==1) {
            allLoaded(false); //set all loaded to false and start refreshing for loading first page
        }
    }
    /**
     * Next page loaded
     * @param users new users
     */
    private void addNewData(List<User> users) {
        adapter.addData(users); //add data to current adapter
        if (users!=null) {
            allLoaded(users.size() < pageSizeDefault);
        }
    }

    /**
     * Data loaded for the first page
     * @param users new users
     */
    private void createAdapter(List<User> users) {
        if (users!=null&&users.size()>0) { //set data if it is
            adapter.setItems(users);
            allLoaded(users.size()<pageSizeDefault);
        }
        else{ //clear data for empty answer
            adapter.clear();
            allLoaded(true);
        }
    }

    /**
     * Handler for load users delay
     */
    @SuppressLint("HandlerLeak")
    private Handler waitHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            loadUsers();
        }
    };

    //Search string updated
    private void searchStringUpdated(String username){
        stopChecking(); //stop checking
        //and start delay
        waitHandler.sendMessageDelayed(waitHandler.obtainMessage(MESSAGE_TEXT_CHANGED, username), AppConstants.SEARCH_DELAY);

    }

    /**
     * Stop delay and checking
     */
    private void stopChecking() {
        waitHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        if (task !=null)
            task.cancel(true);
        task = null;
    }

    /**
     * Get contacts
     * @return the list of device contacts
     */
    private List<UserContact> getContacts() {
        if (!checkContactsPermission())
            return null;
        List<UserContact> contacts=new ArrayList<>();
        ContentResolver cr = getContext().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur!=null) {
            try {
                while (cur.moveToNext()&&getContext()!=null) { //enum contacts
                    long user_contact_id = cur.getLong(cur.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                    UserContact c = UserContact.importFromContacts(getContext(),user_contact_id);
                    if (c!=null) {
                        if (c.phones!=null) { //create new contact for every phone
                            for (String phone : c.phones) {
                                UserContact uc = new UserContact(c.firstName, c.lastName, phone, c.emails);
                                contacts.add(uc);
                                uc.contactId = (long) (contacts.size() - 1);
                            }
                        }
                    }
                }
            }
            finally {
                cur.close();
            }
        }
        return contacts;
    }

    /**
     * Set loaded status
     * @param isAllLoaded is all items loaded
     */
    private void allLoaded(boolean isAllLoaded) {
        adapter.stopLoading(); //remove adapter waiting widget
        updateNoItems();
        //reset endless scroller
        mEndlessScroller.dataLoaded(!isAllLoaded);
    }

    /**
     * Update no item view visibility
     */
    private void updateNoItems() {
        if (task==null) { //data loaded - show no items if needed
            if (adapter.getItemCount() > 0)
                noItems.hide();
            else {
                noItems.show();
            }
        }
        else{ //new data loading
            noItems.hide();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBranchMembersInteractionListener) {
            mListener = (OnBranchMembersInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBranchFeedInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnBranchMembersInteractionListener {
    }
    private TextWatcher updateSearchWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            searchStringUpdated(s!=null?s.toString():null);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_CONFIG&&resultCode== Activity.RESULT_OK&&data!=null){
            String json = data.getStringExtra(SearchConfigActivity.EXTRA_CONFIG);
            if (json!=null){
                try {
                    config = new Gson().fromJson(json, MembersSearchConfig.class);
                    updateSettingsButton();
                    loadUsers();
                }
                catch (JsonSyntaxException e){
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Update settings button icon
     */
    private void updateSettingsButton() {
        if (config.isUpdated())
            buttonSettings.setImageResource(R.drawable.ic_gear_full);
        else
            buttonSettings.setImageResource(R.drawable.ic_gear_empty);
    }

    @Override
    protected void setFragmentPrimary(@SuppressWarnings("UnusedParameters") boolean isPrimary) {
        super.setFragmentPrimary(isPrimary);
        if (adapter!=null&&isPrimary&&adapter.getItemCount()==0&&!isLoading)
            doFirstLoadIfNeeded();
    }
}
