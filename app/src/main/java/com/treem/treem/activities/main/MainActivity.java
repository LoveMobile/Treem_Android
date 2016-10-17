package com.treem.treem.activities.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.treem.treem.R;
import com.treem.treem.activities.alerts.AlertsFragment;
import com.treem.treem.activities.equityrewards.EquityRewardsFragment;
import com.treem.treem.activities.settings.ProfileActivity;
import com.treem.treem.activities.settings.SettingsFragment;
import com.treem.treem.activities.signup.SignupQuestionActivity;
import com.treem.treem.activities.signup.SignupRegisterUserActivity;
import com.treem.treem.activities.signup.phone.SignupPhoneActivity;
import com.treem.treem.activities.tree.SecretTreeLoginActivity;
import com.treem.treem.activities.tree.TreeFragment;
import com.treem.treem.application.ApplicationMain;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.BackButtonClickHandler;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.ProfileHelper;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.alert.AlertCount;
import com.treem.treem.models.profile.UserProfile;
import com.treem.treem.models.rollout.Rollout;
import com.treem.treem.services.Treem.TreemAlertService;
import com.treem.treem.services.Treem.TreemAuthenticationService;
import com.treem.treem.services.Treem.TreemEquityService;
import com.treem.treem.services.Treem.TreemOAuthUserToken;
import com.treem.treem.services.Treem.TreemProfileService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Matthew Walker on 2/11/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class MainActivity extends AppCompatActivity implements
		SettingsFragment.OnSettingsFragmentInteractionListener,
		EquityRewardsFragment.OnRewardsFragmentInteractionListener,
		DispatchTouchEventListener {

	public static final String EXTRA_SECRET_TREE_TOKEN = "secret_tree_token";

	// View pager page positions
	private static final int pageTree = 0;
	private static final int pageRewards = 1;
	private static final int pageAlerts = 2;
	private static final int pageSettings = 3;

	private int fragmentTabs[] = {R.string.tab_tree, R.string.tab_rewards, R.string.tab_alerts, R.string.tab_profile};

	private TabLayout mainTabLayout;
	private ViewPager mainViewPager;
	private Toolbar toolbar;
	private TextView tab0, tab1, /*tab2,*/
			tab3;
	private RelativeLayout notification_layout;
	private TextView tab22, notification_count;

	private ImageView avatar;
	private TextView userName;

	//Rewards layout at the activity title
	private View rewardsLayout;
	//Profile layout at the activity title
	private View profileLayout;
	private TextView rewardsValue;

	//Previous selected page at the pager
	private int pagePreviousSelected = -1;

	// Layout of the main toolbar
	private ViewGroup toolbarLayoutMain;
	//Layout of the settings toolbar
	private ViewGroup toolbarLayoutOther;

	//Settings toolbar handler
	private OtherToolbar toolbarOther;

	/**
	 * Progress bar
	 */
	private LoadingProgressBar logoutProgressBar;

	/**
	 * Set of touch listeners
	 */
	private Set<OnDispatchTouchListener> touchListeners = new HashSet<>();

	private String secretTreetoken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if user logged in
		if (ApplicationMain.SHARED_INSTANCE.isDeviceAuthenticated()) {
			if (ApplicationMain.SHARED_INSTANCE.isUserAuthenticated()) {
				if (TreemOAuthUserToken.SHARED_INSTANCE.isUserRegistered()) { //check is user registered or not
					// both device and user are authenticated, and user is registered. show main screen
					this.showMain();
				} else { //show user registration screen if user was not registered
					this.showUserRegistration();
				}
			} else {
				// device is authenticated but user is not
				this.showSignupDeviceIsAuthenticated();
			}
		} else {
			// device is not authenticated go to initial signup
			TreemOAuthUserToken.SHARED_INSTANCE.clearAccessTokens();

			this.showSignupNoDeviceAuthentication();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == TreeFragment.RC_SECRET_TREE_TOKEN && resultCode == Activity.RESULT_OK) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra(EXTRA_SECRET_TREE_TOKEN, data.getStringExtra(SecretTreeLoginActivity.EXTRA_TOKEN));

			startActivity(intent);
			finish();
		}
	}

	private void showSignupNoDeviceAuthentication() {
		// redirect to login
		Intent intent = new Intent(this, SignupQuestionActivity.class);

		startActivity(intent);
		finish();
	}

	/**
	 * Show user registration screen
	 */
	private void showUserRegistration() {
		// redirect to user registration
		Intent intent = new Intent(this, SignupRegisterUserActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	private void showSignupDeviceIsAuthenticated() {
		// redirect to login
		Intent intent = new Intent(this, SignupPhoneActivity.class);

		startActivity(intent);
		finish();
	}

	private void showMain() {
		setContentView(R.layout.activity_main);

		secretTreetoken = getIntent().getStringExtra(EXTRA_SECRET_TREE_TOKEN);

		// setup the tab control on the page
		initialize();

		// Load necessary data
		loadUserProfile();
		loadAlertsCount();

		if (secretTreetoken == null) {
			getUserRollout();
		}

		setSupportActionBar(toolbar);

		MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), getApplicationContext());
		mainViewPager.setAdapter(pagerAdapter);
		mainViewPager.addOnPageChangeListener(pagerOnPageChangeListener); //add page change listener
		mainTabLayout.setupWithViewPager(mainViewPager);


		for (int i = 0; i < pagerAdapter.getCount(); i++) {
			if (i == 0) {
				tab0.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tree_tab_pressed_ic, 0, 0);
				tab0.setTextColor(getResources().getColor(R.color.SecondPrimaryBar));
				mainTabLayout.getTabAt(i).setCustomView(tab0);
			} else if (i == 1) {
				mainTabLayout.getTabAt(i).setCustomView(secretTreetoken == null ? tab1 : notification_layout);
			} else if (i == 2) {
				//   mainTabLayout.getTabAt(i).setCustomView(tab2);
				mainTabLayout.getTabAt(i).setCustomView(secretTreetoken == null ? notification_layout : tab3);
			} else if (i == 3) {
				mainTabLayout.getTabAt(i).setCustomView(tab3);
			}

		}

		mainTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				mainViewPager.setCurrentItem(tab.getPosition());

				if (tab.getPosition() == 0) {
					tab0.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tree_tab_pressed_ic, 0, 0);
					tab0.setTextColor(getResources().getColor(R.color.SecondPrimaryBar));
					tab1.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.rewards_tab_ic, 0, 0);
					tab1.setTextColor(getResources().getColor(R.color.DefaultTabColor));
					tab22.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.alerts_tab_ic, 0, 0);
					tab22.setTextColor(getResources().getColor(R.color.DefaultTabColor));
					tab3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_profile_tab, 0, 0);
					tab3.setTextColor(getResources().getColor(R.color.DefaultTabColor));
				} else if (tab.getPosition() == 1 && secretTreetoken == null) {
					tab0.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tree_tab_ic, 0, 0);
					tab0.setTextColor(getResources().getColor(R.color.DefaultTabColor));
					tab1.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.rewards_tab_pressed_ic, 0, 0);
					tab1.setTextColor(getResources().getColor(R.color.SecondPrimaryBar));
					tab22.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.alerts_tab_ic, 0, 0);
					tab22.setTextColor(getResources().getColor(R.color.DefaultTabColor));
					tab3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_profile_tab, 0, 0);
					tab3.setTextColor(getResources().getColor(R.color.DefaultTabColor));
				} else if (tab.getPosition() == 2 && secretTreetoken == null || (tab.getPosition() == 1 && secretTreetoken != null)) {
					tab0.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tree_tab_ic, 0, 0);
					tab0.setTextColor(getResources().getColor(R.color.DefaultTabColor));
					tab1.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.rewards_tab_ic, 0, 0);
					tab1.setTextColor(getResources().getColor(R.color.DefaultTabColor));
					tab22.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.alerts_tab_pressed_ic, 0, 0);
					tab22.setTextColor(getResources().getColor(R.color.SecondPrimaryBar));
					tab3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_profile_tab, 0, 0);
					tab3.setTextColor(getResources().getColor(R.color.DefaultTabColor));
				} else if (tab.getPosition() == 3 && secretTreetoken == null || (tab.getPosition() == 2 && secretTreetoken != null)) {
					tab0.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tree_tab_ic, 0, 0);
					tab0.setTextColor(getResources().getColor(R.color.DefaultTabColor));
					tab1.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.rewards_tab_ic, 0, 0);
					tab1.setTextColor(getResources().getColor(R.color.DefaultTabColor));
					tab22.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.alerts_tab_ic, 0, 0);
					tab22.setTextColor(getResources().getColor(R.color.DefaultTabColor));
					tab3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_profile_tab_pressed, 0, 0);
					tab3.setTextColor(getResources().getColor(R.color.SecondPrimaryBar));
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				mainViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
				mainViewPager.setCurrentItem(tab.getPosition());
			}
		});
	}

	/**
	 * Get settings toolbar handler
	 *
	 * @return settings toolbar handler
	 */
	@Override
	public OtherToolbar getOtherToolbar() {
		return toolbarOther;
	}

	/**
	 * Ask user about logout
	 */
	@Override
	public void askLogout() {
		new AlertDialog.Builder(this, R.style.AlertDialogStyle)
				.setTitle(R.string.title_logout)
				.setMessage(R.string.msg_logout)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doLogout();
					}
				})
				.show();
	}

	/**
	 * Do user logout
	 */
	private void doLogout() {
		TreemServiceRequest request = new TreemServiceRequest() {
			@Override
			public void onSuccess(String data) {
				logoutProgressBar.toggleProgressBar(false);
				completeLogout();
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
				// show general alert view
				logoutProgressBar.toggleProgressBar(false);
				if (error == TreemServiceResponseCode.INVALID_ACCESS_TOKEN) //user could made login from another device
					completeLogout();
				else {
					NotificationHelper.showError(MainActivity.this, error.getDescription(MainActivity.this));
				}
			}
		};

		// show loading progress bar indicator
		logoutProgressBar.toggleProgressBar(true);
		TreemAuthenticationService.logout(request);

	}

	/**
	 * Complete logout process.
	 * Clear all tokens and user status
	 */
	private void completeLogout() {
		TreemOAuthUserToken.SHARED_INSTANCE.clearAccessTokens();
		TreemOAuthUserToken.SHARED_INSTANCE.clearUserStatus();
		toEnterPhone();
		ProfileHelper.getInstance(this).clearProfile();
	}

	/**
	 * Close this activity and open Signup enter phone
	 */
	private void toEnterPhone() {
		Intent intent = new Intent(this, SignupPhoneActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void initialize() {
		toolbar = (Toolbar) findViewById(R.id.tool_bar);
		//handle toolbars
		toolbarLayoutMain = (ViewGroup) findViewById(R.id.toolbar_ralativelayout);
		toolbarLayoutOther = (ViewGroup) findViewById(R.id.toolbar_other);
		//handle settings toolbar views
		toolbarOther = new OtherToolbar(toolbarLayoutOther);

		logoutProgressBar = new LoadingProgressBar((ViewGroup) findViewById(R.id.layoutMainRoot));

		mainViewPager = (ViewPager) findViewById(R.id.mainViewPager);
		mainTabLayout = (TabLayout) findViewById(R.id.mainTablayout);
		tab0 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
		tab1 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
	   /* tab2 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);*/
		tab3 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
		notification_layout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.custom_alert_tab, null);
		tab22 = (TextView) notification_layout.findViewById(R.id.tab);
		notification_count = (TextView) notification_layout.findViewById(R.id.notification_count);
		initializeTabsIconsColors();

		userName = (TextView) findViewById(R.id.userName);
		avatar = (ImageView) findViewById(R.id.profileAvatar);

		rewardsLayout = findViewById(R.id.rewards_layout);
		profileLayout = findViewById(R.id.account_layout);
		rewardsValue = (TextView) findViewById(R.id.rewards_value);

		rewardsLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openRewardScreen();
			}
		});
		profileLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openProfileScreen();
			}
		});
		//init toolbars for current item
		switchToolbar(mainViewPager.getCurrentItem());

		updateTitle(mainViewPager.getCurrentItem());
	}

	/**
	 * Open profile activity on user click user name at the title
	 */
	private void openProfileScreen() {
		ProfileActivity.showProfile(this);
	}

	/**
	 * Open rewards screen on user click rewards points at thhe title of the activity
	 */
	private void openRewardScreen() {
		mainViewPager.setCurrentItem(pageRewards, true);
	}

	/**
	 * Load user profile
	 */
	private void loadUserProfile() {
		TreemServiceRequest request = new TreemServiceRequest() {

			@Override
			public void onSuccess(String data) {
				Gson gson = new Gson();
				UserProfile userProfile = gson.fromJson(data, UserProfile.class);
				showUserProfile(userProfile);
				ProfileHelper.getInstance(MainActivity.this).setProfile(userProfile);
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
			}
		};

		TreemProfileService.getCurrentUserProfile(request, CurrentTreeSettings.SHARED_INSTANCE.treeSession);
	}

	private void loadAlertsCount() {
		TreemServiceRequest request = new TreemServiceRequest() {

			@Override
			public void onSuccess(String data) {
				Gson gson = new Gson();
				AlertCount alertCount = gson.fromJson(data, AlertCount.class);
				showAlertsCount(alertCount);
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
			}
		};

		TreemAlertService.getAlertsCount(request, CurrentTreeSettings.SHARED_INSTANCE.treeSession);
	}

	private void getUserRollout() {
		TreemServiceRequest request = new TreemServiceRequest() {

			@Override
			public void onSuccess(String data) {
				Gson gson = new Gson();
				Rollout rollout = gson.fromJson(data, Rollout.class);
				showRolloutInfo(rollout);
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
			}
		};

		TreemEquityService.getUserRollout(request, CurrentTreeSettings.SHARED_INSTANCE.treeSession);
	}

	private void showUserProfile(UserProfile userProfile) {
		userName.setText(userProfile.getFullName());

		Picasso.with(this)
				.load(userProfile.avatar_stream_url)
				.placeholder(R.drawable.img_avatar)
				.error(R.drawable.img_avatar)
				.into(avatar);
	}

	private void showAlertsCount(AlertCount alertCount) {
		if (alertCount.getUnreadAlerts() > 0) {
			notification_count.setVisibility(View.VISIBLE);
			notification_count.setText(String.valueOf(alertCount.getUnreadAlerts()));
		}
	}

	private void showRolloutInfo(Rollout rollout) {
		double percent = Math.round(rollout.getPercentile() * 100);

		rewardsLayout.setVisibility(View.VISIBLE);
		rewardsValue.setText(getString(R.string.rewards_info, (int) percent, (int) rollout.getPoints()));
	}

	private void updateTitle(int position) {
		switch (position) {
			case pageAlerts:
				toolbarOther.setTitle(R.string.toolbar_title_alerts);
				break;
			case pageRewards:
				toolbarOther.setTitle(R.string.toolbar_title_rewards);
				break;
			case pageSettings:
				toolbarOther.setTitle(R.string.toolbar_title_settings);
				break;
			default: {
				//do nothing
			}
		}
	}

	private void initializeTabsIconsColors() {
		//Default Tabs
		tab0.setText(fragmentTabs[0]);
		tab0.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.tree_tab_ic, 0, 0);
		tab0.setTextColor(getResources().getColor(R.color.DefaultTabColor));

		tab1.setText(fragmentTabs[1]);
		tab1.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.rewards_tab_ic, 0, 0);
		tab1.setTextColor(getResources().getColor(R.color.DefaultTabColor));

      /*  tab2.setText(fragmentTabs[2]);
		tab2.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.alerts_tab_ic, 0, 0);
        tab2.setTextColor(getResources().getColor(R.color.DefaultTabColor));*/

		tab3.setText(fragmentTabs[3]);
		tab3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_profile_tab, 0, 0);
		tab3.setTextColor(getResources().getColor(R.color.DefaultTabColor));

		tab22.setText(fragmentTabs[2]);
		tab22.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.alerts_tab_ic, 0, 0);
		tab22.setTextColor(getResources().getColor(R.color.DefaultTabColor));
	}

	/**
	 * Handle view pager page changes and switch toolbars
	 */
	private ViewPager.OnPageChangeListener pagerOnPageChangeListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}

		@Override
		public void onPageSelected(int position) {
			switchToolbar(position);
			updateTitle(position);
			pagePreviousSelected = position; //save last position
		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	/**
	 * Switch toolbars
	 *
	 * @param position the current view pager position
	 */
	private void switchToolbar(int position) {
		if (position != pageTree) {
			showMainToolbar(false); //hide main toolbar
			showOtherToolbar(true); //show other toolbar
		} else { //into tree pages
			showMainToolbar(true); // show main toolbar
			showOtherToolbar(false); //hide other toolbar
		}
	}

	/**
	 * Show or hide settings toolbar
	 *
	 * @param isShow is settings toolbar need to be show?
	 */
	private void showOtherToolbar(boolean isShow) {
		toolbarLayoutOther.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}

	/**
	 * Show or hide main toolbar
	 *
	 * @param isShow is main toolbar need to be show?
	 */
	private void showMainToolbar(boolean isShow) {
		toolbarLayoutMain.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onBackPressed() {
		if (!backButtonHandled()) //try to handle back button at fragment
			super.onBackPressed();
	}

	/**
	 * Handle back button at fragments
	 *
	 * @return true if back button was handled by fragment
	 */
	private boolean backButtonHandled() {
		FragmentManager fm = getSupportFragmentManager();
		List<Fragment> fragments = fm.getFragments(); //get all existing fragments
		for (Fragment f : fragments) {
			if (f instanceof BackButtonClickHandler) { //is fragment need handle back button
				boolean isHandled = ((BackButtonClickHandler) f).backButtonHandle(); //is fragment handled back button
				if (isHandled)
					return true; //do not need to handle the back button at the activity
			}
		}
		return false; //need to handle back button at the activity
	}

	/**
	 * Register touch event listener from fragment
	 *
	 * @param listener the listener
	 */
	@Override
	public void registerTouchEventListener(OnDispatchTouchListener listener) {
		if (listener != null)
			touchListeners.add(listener);
	}

	/**
	 * Unregister touch event listener from fragment
	 *
	 * @param listener the listener
	 */
	@Override
	public void unregisterTouchEventListener(OnDispatchTouchListener listener) {
		if (listener != null)
			touchListeners.remove(listener);
	}

	// class to handle the main pager
	private class MainViewPagerAdapter extends FragmentPagerAdapter {

		public MainViewPagerAdapter(FragmentManager supportFragmentManager, Context applicationContext) {
			super(supportFragmentManager);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
				case 0:
					return TreeFragment.newInstance(secretTreetoken);
				case 1:
					return secretTreetoken == null ? new EquityRewardsFragment() : new AlertsFragment();
				case 2:
					return secretTreetoken == null ? new AlertsFragment() : new SettingsFragment();
				case 3:
					return new SettingsFragment();
			}

			return null;
		}

		@Override
		public int getCount() {
			return secretTreetoken == null ? fragmentTabs.length : fragmentTabs.length - 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getString(fragmentTabs[position]);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		for (OnDispatchTouchListener listener : touchListeners) {
			if (listener.onTouchEvent(ev)) //dispatch touch event to fragment
				return true; //return tru if handled event by fragment
		}
		return super.dispatchTouchEvent(ev);

	}

	public void showFragment(Fragment branchAddFragment) {
		getSupportFragmentManager().beginTransaction()
				.add(R.id.add_branch_container, branchAddFragment)
				.addToBackStack(null)
				.commit();
	}
}
