package com.treem.treem.activities.branch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.treem.treem.R;
import com.treem.treem.activities.branch.chat.BranchChatFragment;
import com.treem.treem.activities.branch.feed.BranchFeedFragment;
import com.treem.treem.activities.branch.members.BranchMembersFragment;
import com.treem.treem.activities.branch.post.BranchPostFragment;
import com.treem.treem.helpers.Utils;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.util.Helper;
import com.treem.treem.widget.TintableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Branch activity
 */
public class BranchActivity extends AppCompatActivity
    implements BranchChatFragment.OnBranchChatInteractionListener,
        BranchFeedFragment.OnBranchFeedInteractionListener,
        BranchMembersFragment.OnBranchMembersInteractionListener,
        BranchPostFragment.OnBrenchPostInteractionListener{

    private static final String ARG_BRANCH = "arg.branch";
    private static final String ARG_ACTIVE_TAB = "arg.active.tab";
    /**
     * Indexes of the tab pages
     */
    private static final int tabFeed = 0;
    private static final int tabMembers = 1;
    private static final int tabPost = 2;
    private static final int tabChat = 3;


    //Current branch
    private Branch branch;

    //Pager adapter
    private PagerAdapter adapter;
    //Tab layout
    private TabLayout tabLayout;
    //Last seleced tab
    private int selectedTab = tabFeed;

    //The tilte of the activity
    private TextView toolbarTitle;

    /**
     * Show branch info
     * @param activity parent activity
     * @param branch selected branch
     */
    public static void showBranch(Activity activity, Branch branch) {
        if (activity==null)
            return;
        Intent intent = new Intent(activity, BranchActivity.class);
        intent.putExtra(ARG_BRANCH, Helper.createGsonSerializer().toJson(branch, Branch.class));
        intent.putExtra(ARG_ACTIVE_TAB,tabFeed);

        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_up_over, R.anim.hold).toBundle();
        ActivityCompat.startActivity(activity,intent,bundle);
    }

    /**
     * Show branch members info
     * @param activity parent activity
     * @param branch selected branch
     */
    public static void showMembers(Activity activity, Branch branch) {
        if (activity==null || branch==null) {
            return;
        }

        Intent intent = new Intent(activity, BranchActivity.class);
        intent.putExtra(ARG_BRANCH, Helper.createGsonSerializer().toJson(branch, Branch.class));
        intent.putExtra(ARG_ACTIVE_TAB, tabMembers);

        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_up_over, R.anim.hold).toBundle();
        ActivityCompat.startActivity(activity,intent,bundle);
    }

    public static void showPosts(@NonNull Activity context, @NonNull Branch branch) {
		Intent intent = new Intent(context, BranchActivity.class);
		intent.putExtra(ARG_BRANCH, Helper.createGsonSerializer().toJson(branch, Branch.class));
		intent.putExtra(ARG_ACTIVE_TAB, tabPost);

		Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_over, R.anim.hold).toBundle();
		ActivityCompat.startActivity(context, intent, bundle);
    }

	public static void showChats(@NonNull Activity context, @NonNull Branch branch) {
		Intent intent = new Intent(context, BranchActivity.class);
		intent.putExtra(ARG_BRANCH, Helper.createGsonSerializer().toJson(branch, Branch.class));
		intent.putExtra(ARG_ACTIVE_TAB, tabChat);

		Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_over, R.anim.hold).toBundle();
		ActivityCompat.startActivity(context, intent, bundle);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch);
        parseIntent();
        setupToolbar();
        setTitle();
        initTabs();
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
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
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
            toolbar.setBackgroundColor(getBranchColor()); //set toolbar background color according tree color

        //get contrast color for selected color and set it to toolbar title and toolbar close button
        Utils.ColorTint tint = Utils.getContrastColor(getBranchColor());
        int textColor = Color.WHITE;
        if (tint== Utils.ColorTint.darkTintColor)
            //noinspection deprecation
            textColor = getResources().getColor(R.color.dark_gray);
        toolbarTitle.setTextColor(textColor);
        imageClose.setColorFilter(textColor);
        int color = Utils.getDarkerColor(getBranchColor());
        Utils.updateStatusbarColor(getWindow(),color,tint);
    }

    private int getBranchColor() {
        return Branch.getColor(branch);
    }


    /**
     * Init tabs
     */
    private void initTabs() {
        adapter = new PagerAdapter(getSupportFragmentManager()); //create pager adapter
        ViewPager viewPager = (ViewPager) findViewById(R.id.branchTabsPager); //find pager
        if (viewPager==null)
            return;
        viewPager.setAdapter(adapter); //set adapter
        viewPager.setCurrentItem(selectedTab); //select current item
        tabLayout = (TabLayout)findViewById(R.id.branchTabs);
        if (tabLayout==null)
            return;
        tabLayout.setupWithViewPager(viewPager); //setup tabs from view pager
        for (int i=0;i<tabLayout.getTabCount();i++){
            setupCustomView(i); //set custom view to tabs
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager){
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                super.onTabReselected(tab);
                adapter.tabReselected(tab.getPosition());
            }
        });
    }

    /**
     * Set custom view for tab
     * @param idx index of selected tab
     */
    private void setupCustomView(int idx) {
        TabLayout.Tab tab = tabLayout.getTabAt(idx); //get tab
        PagerAdapter.TabItem item = adapter.getTabItem(idx); //get tab item
        View v = LayoutInflater.from(this).inflate(R.layout.layout_branch_tab,tabLayout,false); //inflate layout for tab
        TintableImageView ti = (TintableImageView)v.findViewById(R.id.image); //get icon and set resource image
        ti.setImageResource(item.icon);
        TextView tv = (TextView)v.findViewById(R.id.title); //get title
        tv.setText(item.title); //set titke
        if (tab==null)
            return;
        tab.setCustomView(v); //set custom view to selected tab
        if (idx==selectedTab) { //workaround for reselection tab
            tab.select();  //select tab
            v.setSelected(true); //select view for tab
        }
        else{
            v.setSelected(false);
        }

    }

    /**
     * Set activity title
     */
    private void setTitle() {
        String titleText;
        if (branch!=null&&branch.name!=null){ //is activity not null
            titleText = branch.name; //set it
        }
        else {
            titleText = getString(R.string.branch_title); //set common branch title
        }
        toolbarTitle.setText(titleText);

    }

    /**
     * Parse incoming intent
     */
    private void parseIntent() {
        if (getIntent()==null) {
            return;
        }
        String json = getIntent().getStringExtra(ARG_BRANCH); ///get branch and deserialize it.
        if (json==null){
            return;
        }
        try{
            branch = new Gson().fromJson(json,Branch.class);
        }
        catch (JsonSyntaxException e){
            e.printStackTrace();
        }
        selectedTab = getIntent().getIntExtra(ARG_ACTIVE_TAB,tabFeed);
    }

    /**
     * View pager adapter
     */
    private class PagerAdapter extends FragmentPagerAdapter {
        private BranchFeedFragment feeds;
        private BranchMembersFragment members;
        private BranchPostFragment post;
        private BranchChatFragment chat;

        public void tabReselected(int position) {
            if (position==tabFeed)
                feeds.reselected();
        }

        /**
         * Tab item to store title and icon
         */
        public class TabItem{
            public int title;
            public int icon;

            /**
             * Creete tab item object
             * @param title title id
             * @param icon icon id
             */
            public TabItem(int title,int icon){
                this.title = title;
                this.icon = icon;
            }
            @Override
            public String toString(){
                return getString(title);
            }
        }
        //the list of tabs
        private final List<TabItem> fragmentTabs;
        //create adapter object
        public PagerAdapter(FragmentManager fm) {
            super(fm);
            //init tabs
            fragmentTabs = new ArrayList<>();
            fragmentTabs.add(new TabItem(R.string.branch_tab_feed,R.drawable.ic_feed));
            fragmentTabs.add(new TabItem(R.string.branch_tab_members,R.drawable.ic_members));
            fragmentTabs.add(new TabItem(R.string.branch_tab_post,R.drawable.ic_post));
            fragmentTabs.add(new TabItem(R.string.branch_tab_chat,R.drawable.ic_chat));
            feeds = BranchFeedFragment.newInstance(branch);
            members = BranchMembersFragment.newInstance(branch);
            post = BranchPostFragment.newInstance();
            chat = BranchChatFragment.newInstance();
        }


        public TabItem getTabItem(int position){
            return fragmentTabs.get(position);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case tabFeed:
                    return feeds;
                case tabMembers:
                    return members;
                case tabPost:
                    return post;
                case tabChat:
                    return chat;
            }
            throw new RuntimeException("Page doesn't exists");
        }

        @Override
        public int getCount() {
            return fragmentTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(fragmentTabs.get(position).title);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,R.anim.slide_down_over); //show animation
    }
}
