package com.treem.treem.activities.branch.actions;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;

import com.treem.treem.R;
import com.treem.treem.activities.branch.BranchBaseFragment;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.widget.HexagonButton;
import com.treem.treem.widget.TreeGridTheme;

public class BranchActionsFragment extends BranchBaseFragment implements View.OnClickListener {

	private static final String TAG = "BranchActionsFragment";

	private static final String ARG_BUTTON_INITIAL_POSITION = "button_initial_position";
	private static final String ARG_BUTTON_FINAL_POSITION = "button_final_position";
	private static final String ARG_BRANCH = "branch";

	public static final float TRANSLATION_OFFSET = -150;
	private static final String ARG_PARENT = "parent";
	private static final String ARG_THEME = "theme";

	private Point buttonInitialPosition;
	private Point buttonFinalPosition;

	private Branch branch;
	private Branch parent;

	private HexagonButton branchButton;
	private FrameLayout branchActionsLayout;
	private View actionButtonsHolder;
	private TreeGridTheme theme = TreeGridTheme.MEMBERS_THEME;

	private OnBranchActionListener onBranchActionListener;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		if (args != null) {
			parent = (Branch) args.getSerializable(ARG_PARENT);
			buttonInitialPosition = args.getParcelable(ARG_BUTTON_INITIAL_POSITION);
			buttonFinalPosition = args.getParcelable(ARG_BUTTON_FINAL_POSITION);
			branch = (Branch) args.getSerializable(ARG_BRANCH);

			int themeOrdinal = args.getInt(ARG_THEME, -1);

			if (themeOrdinal != -1) {
				theme = TreeGridTheme.values()[themeOrdinal];
			}
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (onBranchActionListener != null) {
			onBranchActionListener.onBranchActionsClosed();
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_branch_actions, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		branchButton = (HexagonButton) view.findViewById(R.id.branch_button);
		branchActionsLayout = (FrameLayout) view.findViewById(R.id.branch_actions_layout);
		actionButtonsHolder = view.findViewById(R.id.action_buttons_container);

		initHexagonButton();

		if (buttonInitialPosition != null && buttonFinalPosition != null) {
			animateHexagonButton();
		}

		initActionButtons(view);
	}

	public void setOnBranchActionListener(OnBranchActionListener onBranchActionListener) {
		this.onBranchActionListener = onBranchActionListener;
	}

	private void initHexagonButton() {
		int color = parent == null ? branch.getColor() : parent.getColor();

		branchButton.setText(branch.name);

		if (theme == TreeGridTheme.SECRET_THEME) {
			branchButton.setStrokeWidth(3);
			branchButton.setStrokeColor(color);
		} else {
			branchButton.setColors(new int[]{color, color});
			branchButton.setTintColors(new int[]{color, color});
		}
	}

	private void initActionButtons(View contentView) {
		Button editButton = (Button) contentView.findViewById(R.id.edit);
		Button moveBranch = (Button) contentView.findViewById(R.id.move);
		Button deleteBranch = (Button) contentView.findViewById(R.id.delete);
		Button close = (Button) contentView.findViewById(R.id.close);
		Button feed = (Button) contentView.findViewById(R.id.feed);
		Button members = (Button) contentView.findViewById(R.id.members);
		Button post = (Button) contentView.findViewById(R.id.post);
		Button chat = (Button) contentView.findViewById(R.id.chat);

		editButton.setOnClickListener(this);
		moveBranch.setOnClickListener(this);
		deleteBranch.setOnClickListener(this);
		feed.setOnClickListener(this);
		members.setOnClickListener(this);
		post.setOnClickListener(this);
		chat.setOnClickListener(this);
		close.setOnClickListener(this);
	}

	private void animateHexagonButton() {
		ObjectAnimator.ofFloat(branchButton, "x", buttonInitialPosition.x, buttonFinalPosition.x).start();
		ObjectAnimator.ofFloat(branchButton, "y", buttonInitialPosition.y, buttonFinalPosition.y).start();
	}

	private void executeAction(BranchAction branchAction, boolean hideActionButtons, boolean animateContent) {
		if (hideActionButtons) {
			actionButtonsHolder.setVisibility(View.GONE);
		}

		branchAction.executeAction();

		if (hideActionButtons && animateContent) {
			animateActionContainer();
		}
	}

	private void animateActionContainer() {
		PropertyValuesHolder translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, TRANSLATION_OFFSET, 0);
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0F, 1.0F);

		ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(branchActionsLayout, translateY, alpha);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.start();
	}

	/**
	 * Called when a view has been clicked.
	 *
	 * @param view The view that was clicked.
	 */
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.edit:
				executeAction(new BranchEditAction(branch, parent, theme, this, onBranchActionListener), true, true);
				break;
			case R.id.move:
				executeAction(new BranchMoveAction(branch, this, onBranchActionListener), false, false);
				break;
			case R.id.delete:
				executeAction(new BranchDeleteAction(branch, this, onBranchActionListener), false, false);
				break;
			case R.id.feed:
				getActivity().onBackPressed();
				onBranchActionListener.showBranch(getBranch());
				break;
			case R.id.members:
				getActivity().onBackPressed();
				onBranchActionListener.showBranchMembers(getBranch());
				break;
			case R.id.post:
				getActivity().onBackPressed();
				onBranchActionListener.showBranchPosts(getBranch());
				break;
			case R.id.chat:
				getActivity().onBackPressed();
				onBranchActionListener.showBranchChats(getBranch());
				break;
			case R.id.close:
				getActivity().onBackPressed();
				break;
		}
	}

	private Branch getBranch() {
		if (parent != null) {
			branch.color = parent.color;
		}
		return branch;
	}

	/**
	 * Get fragment tag for back stack
	 *
	 * @return fragment tag
	 */
	@Override
	public String getFragmentTag() {
		return TAG;
	}

	public static BranchActionsFragment newInstance(Branch parent,
													Point initialButtonPosition,
													Point finalButtonPosition,
													Branch branch,
													TreeGridTheme theme) {
		Bundle args = new Bundle();
		args.putSerializable(ARG_PARENT, parent);
		args.putParcelable(ARG_BUTTON_INITIAL_POSITION, initialButtonPosition);
		args.putParcelable(ARG_BUTTON_FINAL_POSITION, finalButtonPosition);
		args.putSerializable(ARG_BRANCH, branch);

		if (theme != null) {
			args.putInt(ARG_THEME, theme.ordinal());
		}

		BranchActionsFragment fragment = new BranchActionsFragment();
		fragment.setArguments(args);

		return fragment;
	}

	public HexagonButton getBranchButton() {
		return branchButton;
	}

	public interface OnBranchActionListener {

		/**
		 * Called when some action perform on BranchActionsFragment and branch has been changed
		 * when performing action.
		 *
		 * @param branch Edited branch
		 */
		void onEditActionPerformed(Branch branch);

		void onBranchDeleted(Branch branch);

		void onMoveActonPerformed(Branch branch);

		/**
		 * Called when BranchActionsFragment is closed
		 */
		void onBranchActionsClosed();

		void showBranch(Branch branch);

		void showBranchMembers(Branch branch);

		void showBranchPosts(Branch branch);

		void showBranchChats(Branch branch);
	}
}
