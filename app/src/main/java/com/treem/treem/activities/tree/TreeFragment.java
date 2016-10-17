package com.treem.treem.activities.tree;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.treem.treem.R;
import com.treem.treem.activities.branch.BranchActivity;
import com.treem.treem.activities.branch.actions.BranchActionsFragment;
import com.treem.treem.activities.branch.add.BranchAddFragment;
import com.treem.treem.activities.main.MainActivity;
import com.treem.treem.activities.main.PageBaseFragment;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.services.Treem.TreemBranchService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;
import com.treem.treem.util.DataConvertUtils;
import com.treem.treem.util.UIUtils;
import com.treem.treem.widget.HexagonButton;
import com.treem.treem.widget.HexagonGridLayout;
import com.treem.treem.widget.TreeGridTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dan on 4/6/16.
 */
public class TreeFragment extends PageBaseFragment implements HexagonGridLayout.OnBranchActionListener, BranchAddFragment.OnBranchAddActionListener, BranchActionsFragment.OnBranchActionListener {

	public static final int RC_SECRET_TREE_TOKEN = 1;

	private static final String TAG = TreeFragment.class.getSimpleName();
	private static final String ARG_SECRET_TREE_TOKEN = "secret_tree_token";
	private ProgressBar progressBar;
	private LinearLayout contentView;
	private HexagonGridLayout gridLayout;
	private TextView movingHint;
	private ImageView background;
	private int backgroundDrawable = R.drawable.home_background;

	private View movingNavigationTitle;
	private View movingActions;
	private View navigationOverlay;

	private Point initialGridPosition;

	private List<Branch> branchesData;

	private Branch parent;
	private List<Branch> branches;

	private int treeDepth;

	private Branch branchThis = new Branch();
	private TreeGridTheme theme = TreeGridTheme.MEMBERS_THEME;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		if (args != null) {
			String secretTreeToken = args.getString(ARG_SECRET_TREE_TOKEN);

			if (secretTreeToken != null) {
				theme = TreeGridTheme.SECRET_THEME;
				backgroundDrawable = android.R.color.black;

				CurrentTreeSettings.SHARED_INSTANCE.treeSession.treeID = CurrentTreeSettings.secretTreeID;
				CurrentTreeSettings.SHARED_INSTANCE.treeSession.token = secretTreeToken;
			}
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// This is basically the "viewDidLoad()" method in swift from TreeViewController.swift

        /*
		// tie outlet to view to the hexagon grid viewcontroller
        self.hexagonGridView = gridView

        self.initialEquityBarHeightConstant = self.TopEquityViewHeight.constant

        // hide by default
        self.showHideEquity(true)

        // preload content credentials (call always passes default tree settings due to service design)
        TreemContentService.sharedInstance.checkRepoCreds(TreeSession(treeID: CurrentTreeSettings.mainTreeID, token: nil), complete: nil)

        // load current tree
        self.loadCurrentTree()
        */
		return inflater.inflate(R.layout.fragment_tree, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		contentView = (LinearLayout) view.findViewById(R.id.tree_container);
		gridLayout = (HexagonGridLayout) view.findViewById(R.id.hexagon_grid);
		movingHint = (TextView) view.findViewById(R.id.moving_hint);
		navigationOverlay = view.findViewById(R.id.navigation_overlay);
		background = (ImageView) view.findViewById(R.id.background_image);
		background.setImageResource(backgroundDrawable);

		gridLayout.setTheme(theme);
		gridLayout.setOnBranchActionListener(this);

		loadBranches();
	}

	private void loadBranches() {
		TreemBranchService.getUserBranches(CurrentTreeSettings.SHARED_INSTANCE.treeSession, new TreemServiceRequest() {

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(String data) {
				Branch[] result = DataConvertUtils.parseJson(data, Branch[].class);

				List<Branch> branches = result == null ? new ArrayList<Branch>() : Arrays.asList(result);
				showContent(branches);
				initActionButtons();
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
				progressBar.setVisibility(View.GONE);
			}
		}, null);
	}

	private void buildDataSet(List<Branch> branches, Branch parent) {
		for (Branch branch : branches) {
			branch.parent = parent;

			if (branch.children != null && branch.children.length > 0) {
				buildDataSet(branch.children, branch);
			}
		}
	}

	private void buildDataSet(Branch[] branches, Branch parent) {
		for (Branch branch : branches) {
			branch.parent = parent;

			if (branch.children != null && branch.children.length > 0) {
				buildDataSet(branch.children, branch);
			}
		}
	}

	private void showContent(List<Branch> branches) {
		buildDataSet(branches, null);

		this.branches = new ArrayList<>(branches);
		this.branchesData = new ArrayList<>(branches);

		progressBar.setVisibility(View.GONE);
		contentView.setVisibility(View.VISIBLE);

		initButtons(branches, true);
	}

	private void initButtons(final List<Branch> branches, boolean animate) {
		List<HexagonButton> buttons;

		if (treeDepth > 2) {
			buttons = new ArrayList<>(7);
			Branch branch = getBranchForPosition(branches, Branch.BranchPosition.Center);
			HexagonButton hexagonButton = createCenterButton(branch, parent);
			gridLayout.setButtonInCentre(hexagonButton);
		} else {
			buttons = new ArrayList<>(7);

			for (Branch.BranchPosition position : Branch.BranchPosition.values()) {
				if (position == Branch.BranchPosition.None) {
					continue;
				}

				HexagonButton hexagonButton;
				Branch branch = getBranchForPosition(branches, position);

				if (position == Branch.BranchPosition.Center) {
					hexagonButton = createCenterButton(branch, parent);
					gridLayout.setButtonInCentre(hexagonButton);
				} else if (branch != null) {
					hexagonButton = createBranchButton(branch, parent);
					buttons.add(hexagonButton);
				} else {
					hexagonButton = createEmptyBranch(position);
					buttons.add(hexagonButton);
				}
			}
		}

		gridLayout.setButtons(buttons, animate);
	}

	private HexagonButton createBranchButton(Branch branch, Branch parent) {
		Branch.BranchPosition position = branch.getPosition();
		int[] branchCoordinates = UIUtils.getBranchCoordinates(position);

		return createBranchButton(branch, parent, branchCoordinates[0], branchCoordinates[1]);
	}

	private HexagonButton createCenterButton(Branch centerBranch, Branch parent) {
		HexagonButton hexagonButton = new HexagonButton(getContext());
		hexagonButton.setPositionInGrid(0, 0);

		if (centerBranch == null) {
			Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_feed).mutate();

			hexagonButton.setIcon(icon);
			hexagonButton.setText(parent == null ? getString(R.string.all) : parent.name);
			hexagonButton.setParentBranch(parent);
		} else {
			Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_feed).mutate();

			hexagonButton.setIcon(icon);
			hexagonButton.setParentBranch(parent);
			hexagonButton.setText(parent == null ? getString(R.string.all) : parent.name);
		}
		return hexagonButton;
	}

	private HexagonButton createBranchButton(final Branch branch, Branch parent, int positionX, int positionY) {
		HexagonButton hexagonButton = new HexagonButton(getContext());
		hexagonButton.setPositionInGrid(positionX, positionY);
		hexagonButton.setText(branch.name);
		hexagonButton.setBranch(branch);
		hexagonButton.setParentBranch(parent);
		hexagonButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				BranchActivity.showMembers(getActivity(), branch);
				return true;
			}
		});
		return hexagonButton;

	}

	private HexagonButton createEmptyBranch(Branch.BranchPosition position) {
		Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.add_tree).mutate();
		int[] branchCoordinates = UIUtils.getBranchCoordinates(position);

		HexagonButton hexagonButton = new HexagonButton(getContext());
		hexagonButton.setPositionInGrid(branchCoordinates[0], branchCoordinates[1]);
		hexagonButton.setText(R.string.add_new);
		hexagonButton.setIcon(icon);

		return hexagonButton;
	}

	private void initActionButtons() {
		View view = getView();

		if (view == null) {
			return;
		}

		View secretTreeHandler = view.findViewById(R.id.secret_tree_handler);
		Button explore = (Button) view.findViewById(R.id.explore);
		Button chat = (Button) view.findViewById(R.id.chat);
		Button members = (Button) view.findViewById(R.id.members);
		Button post = (Button) view.findViewById(R.id.post);
		Button back = (Button) view.findViewById(R.id.back);

		explore.getCompoundDrawables()[1].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
		chat.getCompoundDrawables()[1].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
		members.getCompoundDrawables()[1].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
		post.getCompoundDrawables()[1].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
		back.getCompoundDrawables()[1].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);


		members.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showBranchMembers(parent == null ? branchThis : parent);
			}
		});

		post.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showBranchPosts(parent == null ? branchThis : parent);
			}
		});

		chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showBranchChats(parent == null ? branchThis : parent);
			}
		});

		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				navigateBack();
			}
		});

		secretTreeHandler.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showSecretTree();
			}
		});

		if (theme == TreeGridTheme.SECRET_THEME) {
			Button exit = (Button) view.findViewById(R.id.exit);
			exit.getCompoundDrawables()[1].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

			exit.setVisibility(View.VISIBLE);
			exit.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					exitSecretTree();
				}
			});
		}
	}

	public void showBranch(Branch branch) {
		BranchActivity.showBranch(getActivity(), branch);
	}

	public void showBranchMembers(Branch branch) {
		BranchActivity.showMembers(getActivity(), branch);
	}

	public void showBranchPosts(Branch branch) {
		BranchActivity.showPosts(getActivity(), branch);
	}

	public void showBranchChats(Branch branch) {
		BranchActivity.showChats(getActivity(), branch);
	}

	@Override
	public void onBranchButtonClick(Branch branch, HexagonButton view) {
		navigateToBranch(branch);
	}

	@Override
	public void onCenterBranchButtonClick(HexagonButton view) {
		showBranch(parent == null ? branchThis : parent);
	}

	@Override
	public void onNewBranchRequested(int positionX, int positionY, HexagonButton view) {
		View gridParent = (View) gridLayout.getParent();
		Point screenSize = UIUtils.getScreenSize(getContext());

		float buttonX = gridParent.getX() + view.getX();

		int[] testViewPosition = new int[2];
		int[] buttonPosition = new int[2];

		getView().findViewById(R.id.tree_screen_container).getLocationOnScreen(testViewPosition);
		view.getLocationInWindow(buttonPosition);

		int targetX = (screenSize.x - view.getWidth()) / 2;
		int targetY = getResources().getDimensionPixelSize(R.dimen.branch_button_action_margin_top);

		Point buttonInitialPosition = new Point(buttonPosition[0] - testViewPosition[0], buttonPosition[1] - testViewPosition[1]);
		Point buttonFinalPosition = new Point(
				(int) (buttonInitialPosition.x + (targetX - buttonX)),
				buttonInitialPosition.y + ((testViewPosition[1] - buttonPosition[1]) + targetY));

		showAddScreen(buttonInitialPosition, buttonFinalPosition, positionX, positionY);

		if (initialGridPosition == null) {
			initialGridPosition = new Point((int) gridParent.getX(), (int) gridParent.getY());
		}

		gridParent.animate()
				.xBy((targetX - buttonX))
				.yBy((testViewPosition[1] - buttonPosition[1]) + targetY)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.start();
	}

	@Override
	public void onEditBranchClick(Branch branch, HexagonButton view) {
		View gridParent = (View) gridLayout.getParent();
		Point screenSize = UIUtils.getScreenSize(getContext());

		float buttonX = gridParent.getX() + view.getX();

		int[] testViewPosition = new int[2];
		int[] buttonPosition = new int[2];

		getView().findViewById(R.id.tree_screen_container).getLocationOnScreen(testViewPosition);
		view.getLocationInWindow(buttonPosition);

		int targetX = (screenSize.x - view.getWidth()) / 2;
		int targetY = getResources().getDimensionPixelSize(R.dimen.branch_button_action_margin_top);

		Point buttonInitialPosition = new Point(buttonPosition[0] - testViewPosition[0], buttonPosition[1] - testViewPosition[1]);
		Point buttonFinalPosition = new Point(
				(int) (buttonInitialPosition.x + (targetX - buttonX)),
				buttonInitialPosition.y + ((testViewPosition[1] - buttonPosition[1]) + targetY));

		showBranchActions(buttonInitialPosition, buttonFinalPosition, branch);

		if (initialGridPosition == null) {
			initialGridPosition = new Point((int) gridParent.getX(), (int) gridParent.getY());
		}

		gridParent.animate()
				.xBy((targetX - buttonX))
				.yBy((testViewPosition[1] - buttonPosition[1]) + targetY)
				.setInterpolator(new AccelerateDecelerateInterpolator())
				.start();
	}

	@Override
	public void onBranchMoveRequested(Branch branch, HexagonButton view) {
		Branch.BranchPosition newPosition = UIUtils.getPosition(view.getPositionX(), view.getPositionY());
		branch.setPosition(newPosition);

		final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, getString(R.string.loading), true, false);

		TreemServiceRequest request = new TreemServiceRequest() {

			@Override
			public void onSuccess(String data) {
				progressDialog.dismiss();

				finishBranchMoving();
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
				progressDialog.dismiss();
			}
		};

		TreemBranchService.setUserBranch(CurrentTreeSettings.SHARED_INSTANCE.treeSession, request, branch);
	}

	private void showAddScreen(Point initialButtonPosition, Point finalButtonPosition, int positionX, int positionY) {
		BranchAddFragment branchAddFragment = BranchAddFragment.newInstance(parent, initialButtonPosition, finalButtonPosition, positionX, positionY, theme);
		branchAddFragment.setOnBranchAddActionListener(this);

		((MainActivity) getActivity()).showFragment(branchAddFragment);
	}

	private void showBranchActions(Point initialButtonPosition, Point finalButtonPosition, Branch branch) {
		BranchActionsFragment fragment = BranchActionsFragment.newInstance(parent, initialButtonPosition, finalButtonPosition, branch, theme);
		fragment.setOnBranchActionListener(this);

		((MainActivity) getActivity()).showFragment(fragment);
	}

	@Override
	public void onBranchAdded(Branch branch, int positionX, int positionY) {
		branches.add(branch);

		if (parent != null) {
			branch.parent = parent;
			parent.children = new Branch[branches.size()];
			branches.toArray(parent.children);
		}

		gridLayout.clearGrid();
		initButtons(branches, false);
	}

	/**
	 * Called when some action perform on BranchActionsFragment and branch has been changed
	 * when performing action.
	 *
	 * @param branch Edited branch
	 */
	@Override
	public void onEditActionPerformed(Branch branch) {
		gridLayout.updateButtonForBranch(branch);
	}

	@Override
	public void onMoveActonPerformed(Branch branch) {
		gridLayout.moveBranch(branch);

		showMovingHint();
		showMovingNavigationTitle(branch);
		showMovingActions();
		hideTreeButtons();
		background.setImageResource(R.color.hexagon_grid_overlay);
	}

	@Override
	public void onBranchDeleted(Branch branch) {
		branches.remove(branch);

		if (parent != null) {
			parent.children = new Branch[branches.size()];
			branches.toArray(parent.children);
		}

		gridLayout.clearGrid();
		initButtons(branches, false);
	}

	@Override
	public void onBranchAddClosed() {
		animateToInitialPosition();
	}

	/**
	 * Called when BranchActionsFragment is closed
	 */
	@Override
	public void onBranchActionsClosed() {
		animateToInitialPosition();
	}

	private void finishBranchMoving() {
		hideMovingHint();
		hideMovingNavigationTitle();
		hideMovingActions();
		showTreeButtons();

		gridLayout.finishMoving();
		gridLayout.clearGrid();
		initButtons(branches, false);
		background.setImageResource(backgroundDrawable);
	}

	private void animateToInitialPosition() {
		if (initialGridPosition != null) {
			View gridParent = (View) gridLayout.getParent();
			gridParent.animate()
					.x(initialGridPosition.x)
					.y(initialGridPosition.y)
					.setInterpolator(new AccelerateDecelerateInterpolator())
					.start();
		}
	}

	private void showMovingHint() {
		movingHint.setVisibility(View.VISIBLE);
	}

	private void showMovingNavigationTitle(Branch branch) {
		if (movingNavigationTitle == null) {
			loadMovingNavigationTitle();
		}

		movingNavigationTitle.setBackgroundColor(branch.getColor());
		((TextView) movingNavigationTitle.findViewById(R.id.branch_name)).setText(branch.name);

		ViewGroup titleContainer = (ViewGroup) getActivity().findViewById(R.id.tool_bar_container);
		titleContainer.addView(movingNavigationTitle);
	}

	private void showMovingActions() {
		if (movingActions == null) {
			loadMovingActions();
		}

		ViewGroup actionsContainer = (ViewGroup) getActivity().findViewById(R.id.main_tab_layout_container);
		actionsContainer.addView(movingActions);
	}

	private void showTreeButtons() {
		View view = getView();

		if (view == null) {
			return;
		}

		Button explore = (Button) view.findViewById(R.id.explore);
		Button chat = (Button) view.findViewById(R.id.chat);
		Button members = (Button) view.findViewById(R.id.members);
		Button post = (Button) view.findViewById(R.id.post);

		explore.setVisibility(View.VISIBLE);
		chat.setVisibility(View.VISIBLE);
		members.setVisibility(View.VISIBLE);
		post.setVisibility(View.VISIBLE);

		if (theme == TreeGridTheme.SECRET_THEME) {
			Button exit = (Button) view.findViewById(R.id.exit);
			exit.setVisibility(View.VISIBLE);
		}
	}

	private void hideMovingHint() {
		movingHint.setVisibility(View.GONE);
	}

	private void hideMovingNavigationTitle() {
		ViewGroup parent = ((ViewGroup) movingNavigationTitle.getParent());
		parent.removeView(movingNavigationTitle);
	}

	private void hideMovingActions() {
		ViewGroup parent = ((ViewGroup) movingActions.getParent());
		parent.removeView(movingActions);
	}

	private void hideTreeButtons() {
		View view = getView();

		if (view == null) {
			return;
		}

		Button explore = (Button) view.findViewById(R.id.explore);
		Button chat = (Button) view.findViewById(R.id.chat);
		Button members = (Button) view.findViewById(R.id.members);
		Button post = (Button) view.findViewById(R.id.post);

		explore.setVisibility(View.INVISIBLE);
		chat.setVisibility(View.INVISIBLE);
		members.setVisibility(View.INVISIBLE);
		post.setVisibility(View.INVISIBLE);

		if (theme == TreeGridTheme.SECRET_THEME) {
			Button exit = (Button) view.findViewById(R.id.exit);
			exit.setVisibility(View.INVISIBLE);
		}
	}

	private void loadMovingNavigationTitle() {
		ViewGroup titleContainer = (ViewGroup) getActivity().findViewById(R.id.tool_bar_container);
		movingNavigationTitle = getLayoutInflater(null).inflate(R.layout.move_branch_title, titleContainer, false);
	}

	private void loadMovingActions() {
		ViewGroup actionsContainer = (ViewGroup) getActivity().findViewById(R.id.main_tab_layout_container);
		movingActions = getLayoutInflater(null).inflate(R.layout.move_branch_actions, actionsContainer, false);

		movingActions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finishBranchMoving();
			}
		});
	}

	// Branch navigation

	private void navigateToBranch(Branch branch) {
		if (parent == null) {
			showNavigationOverlay(branch);
			showBackButton();
		}

		String color = parent == null ? branch.color : parent.color;

		parent = branch;
		parent.color = color;

		branches = branch.children == null ? new ArrayList<Branch>() : new ArrayList<>(Arrays.asList(branch.children));

		gridLayout.clearGrid();
		treeDepth++;
		initButtons(branches, false);
		animateNavigation(branch.getPosition(), false);
	}

	private void navigateBack() {
		if (parent == null) {
			hideNavigationOverlay();
			hideBackButton();
			return;
		}

		if (parent.parent == null) {
			Branch.BranchPosition animationPosition = parent.getPosition();

			parent = null;
			branches = branchesData;

			hideNavigationOverlay();
			hideBackButton();
			animateNavigation(animationPosition, true);
		} else {
			Branch.BranchPosition animationPosition = parent.getPosition();

			parent = parent.parent;
			branches = parent.children == null ? new ArrayList<Branch>() : new ArrayList<>(Arrays.asList(parent.children));
			animateNavigation(animationPosition, true);
		}

		gridLayout.clearGrid();
		treeDepth--;
		initButtons(branches, false);
	}

	private void showNavigationOverlay(Branch branch) {
		int red = Color.red(branch.getColor());
		int green = Color.green(branch.getColor());
		int blue = Color.blue(branch.getColor());

		navigationOverlay.setBackgroundColor(Color.argb(175, red, green, blue));
	}

	private void hideNavigationOverlay() {
		navigationOverlay.setBackgroundColor(Color.TRANSPARENT);
	}

	private void showBackButton() {
		View view = getView();

		if (view == null) {
			return;
		}

		view.findViewById(R.id.back).setVisibility(View.VISIBLE);
	}

	private void hideBackButton() {
		View view = getView();

		if (view == null) {
			return;
		}

		view.findViewById(R.id.back).setVisibility(View.GONE);
	}

	private void animateNavigation(Branch.BranchPosition animationPosition, boolean reverse) {
		int directionX;
		int directionY;

		switch (animationPosition) {
			case TopLeft:
				directionX = -1;
				directionY = -1;
				break;
			case TopRight:
				directionX = -1;
				directionY = 1;
				break;
			case Left:
				directionX = 0;
				directionY = -1;
				break;
			case Right:
				directionX = 0;
				directionY = 1;
				break;
			case Center:
				directionX = 0;
				directionY = 0;
				break;
			case BottomLeft:
				directionX = 1;
				directionY = -1;
				break;
			case BottomRight:
				directionX = 1;
				directionY = 1;
				break;
			default:
				return;
		}

		int translationX = 200 * directionX;
		int translationY = 200 * directionY;

		if (reverse) {
			translationX *= -1;
			translationY *= -1;
		}

		PropertyValuesHolder translateXAnimator = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, translationX, 0);
		PropertyValuesHolder translateYAnimator = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, translationY, 0);

		View gridParent = (View) gridLayout.getParent();

		ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(gridParent, translateXAnimator, translateYAnimator);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.start();
	}

	// end branch navigation

	// secret tree

	private void showSecretTree() {
		SecretTreeLoginActivity.showLoginScreen(getActivity(), RC_SECRET_TREE_TOKEN);
	}

	private void exitSecretTree() {
		CurrentTreeSettings.SHARED_INSTANCE.treeSession.treeID = CurrentTreeSettings.mainTreeID;
		CurrentTreeSettings.SHARED_INSTANCE.treeSession.token = null;

		startActivity(new Intent(getContext(), MainActivity.class));
		getActivity().finish();
	}

	// end secret tree

	private static Branch getBranchForPosition(List<Branch> branches, Branch.BranchPosition branchPosition) {
		for (Branch branch : branches) {
			if (branch.getPosition() == branchPosition) {
				return branch;
			}
		}
		return null;
	}

	@Override
	public String getFragmentTag() {
		return TAG;
	}

	public static TreeFragment newInstance(String secretTreeToken) {
		Bundle args = new Bundle();
		args.putString(ARG_SECRET_TREE_TOKEN, secretTreeToken);

		TreeFragment treeFragment = new TreeFragment();
		treeFragment.setArguments(args);

		return treeFragment;
	}
}
