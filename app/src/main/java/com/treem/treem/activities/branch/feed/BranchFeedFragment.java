package com.treem.treem.activities.branch.feed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.activities.branch.BranchBaseFragment;
import com.treem.treem.activities.media.ImageViewActivity;
import com.treem.treem.activities.media.VideoViewActivity;
import com.treem.treem.activities.users.UserProfileActivity;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.Utils;
import com.treem.treem.helpers.recyclerview.DividerItemDecoration;
import com.treem.treem.helpers.recyclerview.EndlessRecyclerViewLinearScrollListener;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.models.content.Media;
import com.treem.treem.models.post.Post;
import com.treem.treem.models.user.User;
import com.treem.treem.services.Treem.TreemFeedService;
import com.treem.treem.services.Treem.TreemService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

import java.util.List;

/**
 * Feed fragment for branch activity
 */

public class BranchFeedFragment extends BranchBaseFragment {
    private static final String TAG = BranchFeedFragment.class.getSimpleName();
    private static final String ARG_BRANCH = "arg.branch";

    //Default feed page size
    private static final int defaultPageSize = 25;
    private static final int REQUEST_OPTIONS_ACTION = 1;

    //View for no feeds
    private ViewGroup noFeeds;

    //View for no error
    private ViewGroup errorView;

    //Button for refresh
    private Button buttonRetry;


    //View with feeds
    private RecyclerView feedsView;

    //Swipe to refresh layout
    private SwipeRefreshLayout refreshLayout;

    //Reference to parent activity
    @SuppressWarnings("unused")
    private OnBranchFeedInteractionListener mListener;

    //Selected branch
    private Branch branch;

    //Loading bar
    private LoadingProgressBar loadingBar;

    //Current loaded page
    private int currentPage = 1;

    //Current session timestamp. Updated on every page refresh
    private long currentViewTimestamp;

    //Current network task
    private TreemService.NetworkRequestTask task;

    //Feeds adapter to show
    private FeedsAdapter adapter;

    //endless support for recycle view
    private EndlessRecyclerViewLinearScrollListener endlessListener;

    public BranchFeedFragment() {
        // Required empty public constructor
    }

    /**
     * Create new instance of this fragment
     * @param branch selected branch
     * @return new instance
     */
    public static BranchFeedFragment newInstance(Branch branch) {
        BranchFeedFragment fragment = new BranchFeedFragment();
        Bundle args = new Bundle();
        if (branch!=null) {
            try {
                args.putString(ARG_BRANCH, new Gson().toJson(branch, Branch.class));
            }
            catch (JsonSyntaxException e){
                e.printStackTrace();
            }
        }
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
            if (json!=null){
                try {
                    branch = new Gson().fromJson(json,Branch.class);
                }
                catch (JsonSyntaxException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_branch_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        noFeeds = (ViewGroup)view.findViewById(R.id.noFeeds);
        feedsView = (RecyclerView)view.findViewById(R.id.feeds);
        refreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refreshLayout);
        FrameLayout rootView = (FrameLayout) view.findViewById(R.id.rootFeedsView);
        loadingBar = new LoadingProgressBar(rootView);
        errorView = (ViewGroup)view.findViewById(R.id.errorLoading);
        buttonRetry = (Button)view.findViewById(R.id.buttonRetry);

        setWidgetActions();
        createAdapter(); //create receycler adapter
        if (isFragmentPrimary()) //is fragment selected
            beginLoadFeeds(); //start load
    }

    /**
     * Start load feeds
     */
    private void beginLoadFeeds() {
        if (task!=null) //cancel unfinished load task
            task.cancel(true);
        currentPage = 1; //reset page number
        currentViewTimestamp = System.currentTimeMillis(); //set new timestamp
        if (refreshLayout.isRefreshing())
            refreshLayout.setRefreshing(false);

        loadCurrentPage(); //load current page
    }

    /**
     * Listener to handle adapter clicks on the feeds
     */
    private FeedsAdapter.OnFeedClickListener onClickListener = new FeedsAdapter.OnFeedClickListener() {
        /**
         * On profile image click
         * @param v profile image
         * @param post clicked post
         * @param user clicked user
         */
        @Override
        public void onProfileClick(View v, Post post, User user) {
            UserProfileActivity.showUserProfile(getContext(),user.getId());
        }

        /**
         * On image click
         * @param v clicked image
         * @param post clicked post
         */
        @Override
        public void onImageClick(View v, Post post) {
            Media media = new Media(post);
            ImageViewActivity.showImage(BranchFeedFragment.this,media,v,0);
        }

        /**
         * On video click
         * @param v clicked thumbnail
         * @param post clicked post
         */
        @Override
        public void onVideoClick(View v, Post post) {
            Media media = new Media(post);
            VideoViewActivity.showVideo(BranchFeedFragment.this,media);
        }

        /**
         * On link preview click
         * @param v clicked link preview
         * @param post clicked post
         */
        @Override
        public void onLinkPreviewClick(View v, Post post) {
            if (post.getLinkUrl()!=null) {
                //open browser with link
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getLinkUrl()));
                try {
                    startActivity(browserIntent);
                }
                catch (ActivityNotFoundException e){
                    e.printStackTrace();
                    NotificationHelper.showError(getContext(),R.string.failed_show_link);
                }
            }
        }

        /**
         * On post options click
         * @param v clicked view
         * @param post clicked post
         * @param isShare is share options click
         */
        @Override
        public void onOptionsClick(View v, Post post, boolean isShare) {
            PostOptionsMenuActivity.showOptionsMenu(BranchFeedFragment.this,v,post,isShare,REQUEST_OPTIONS_ACTION);
        }
    };

    /**
     * Create recycler adapter
     */
    private void createAdapter() {
        adapter = new FeedsAdapter(getContext(),onClickListener);
        feedsView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //init endless scrollser
        endlessListener = new EndlessRecyclerViewLinearScrollListener(layoutManager) {
            /**
             * Called when need more data load
             * @return true if starts to load
             */
            @Override
            public boolean onLoadMore() {
                if (!adapter.isLoading()) {
                    currentPage++;
                    loadCurrentPage();
                }
                return true;
            }
        };
        feedsView.addOnScrollListener(endlessListener);
        //set feed splitter
        DividerItemDecoration decorator = new DividerItemDecoration(getContext(),R.drawable.divider_feeds);
        feedsView.addItemDecoration(decorator);
        feedsView.setLayoutManager(layoutManager);
    }

    /**
     * Set widgets actions
     */
    private void setWidgetActions() {
        //set refresh layout listener
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshStart();
            }
        });

        //handle error retry button
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshStart();
            }
        });
    }

    /**
     * Start refresh
     */
    private void refreshStart() {
        beginLoadFeeds();
    }

    /**
     * Load current selected page
     */
    private void loadCurrentPage() {
        if (currentPage==1){ //show loading indicator for first page
            loadingBar.toggleProgressBar(true);
        }
        adapter.startLoading(currentPage!=1); //inform adapter about loading
        updateNoItems(false);
        TreemServiceRequest request = new TreemServiceRequest( ) {
            @Override
            public void onSuccess(String data) {
                task = null;
                loadingBar.toggleProgressBar(false);
                adapter.stopLoading();
                List<Post> posts = null;
                boolean isError = false;
                if (!Utils.isEmptyAnswer(data)) {
                    try {
                        posts = new Gson().fromJson(data,Post.LIST_TYPE);
                        if (currentPage==1){
                            adapter.setData(posts);
                        }
                        else{
                            adapter.addNewData(posts);
                        }
                    }
                    catch (JsonSyntaxException e){
                        e.printStackTrace();
                        NotificationHelper.showError(getContext(),R.string.failed_parse_server_answer);
                        isError = true;
                    }
                }
                endlessListener.dataLoaded(posts!=null&&posts.size()>=defaultPageSize);
                updateNoItems(isError);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                task = null;
                loadingBar.toggleProgressBar(false);
                adapter.stopLoading();
                if (TreemServiceResponseCode.CANCELED != error){
                    NotificationHelper.showError(getContext(), getString(R.string.failed_load_feeds, error.getDescription(getContext())));
                }
                endlessListener.dataLoaded(false);
                updateNoItems(true);
            }
        };
        task  = TreemFeedService.getPosts(
                request,
                branch!=null&&branch.id!=null?branch.id:0,
                currentViewTimestamp,
                currentPage,
                defaultPageSize,
                CurrentTreeSettings.SHARED_INSTANCE.treeSession
        );
    }

    /**
     * Show or hide no items view
     */
    private void updateNoItems(boolean isError) {
        if (adapter.isLoading()){
            noFeeds.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        }
        else{
            if (adapter.getItemCount()>0) {
                noFeeds.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
            }
            else {
                if (isError) {
                    errorView.setVisibility(View.VISIBLE);
                    noFeeds.setVisibility(View.GONE);
                }
                else {
                    errorView.setVisibility(View.GONE);
                    noFeeds.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBranchFeedInteractionListener) {
            mListener = (OnBranchFeedInteractionListener) context;
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

    /**
     * Current page reselected
     */
    public void reselected() {
        refreshStart(); //refresh feeds
    }

    public interface OnBranchFeedInteractionListener {
    }

    @Override
    protected void setFragmentPrimary(@SuppressWarnings("UnusedParameters") boolean isPrimary) {
        super.setFragmentPrimary(isPrimary);
        if (adapter!=null&&isPrimary&&adapter.getItemCount()==0&&!adapter.isLoading())
            beginLoadFeeds();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_OPTIONS_ACTION&&resultCode== Activity.RESULT_OK&&data!=null) {
            String json = data.getStringExtra(PostOptionsMenuActivity.EXTRA_POST);
            Post post = null;
            if (json != null) {
                try {
                    post = new Gson().fromJson(json, Post.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
            if (post != null) {
                handleOptionClick(post,data.getBooleanExtra(PostOptionsMenuActivity.EXTRA_IS_SHARE,false),data.getIntExtra(PostOptionsMenuActivity.EXTRA_OPTION,-1));
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("DefaultLocale")
    private void handleOptionClick(Post post, boolean isShare, int selectedOption) {
        Toast.makeText(getContext(),String.format("Selected option: %d",selectedOption),Toast.LENGTH_SHORT).show();
    }
}
