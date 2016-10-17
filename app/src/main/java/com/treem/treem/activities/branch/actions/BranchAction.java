package com.treem.treem.activities.branch.actions;

import android.content.Context;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.widget.TreeGridTheme;

/**
 * Date: 6/10/16.
 */
abstract class BranchAction {

	Branch branch;
	TreeGridTheme theme;
	Context context;

	BranchActionsFragment branchActionsFragment;
	BranchActionsFragment.OnBranchActionListener onBranchActionListener;

	BranchAction(Branch branch, BranchActionsFragment branchActionsFragment, TreeGridTheme theme, BranchActionsFragment.OnBranchActionListener onBranchActionListener) {
		this.branch = branch;
		this.context = branchActionsFragment.getContext();
		this.branchActionsFragment = branchActionsFragment;
		this.onBranchActionListener = onBranchActionListener;
		this.theme = theme;
	}

	abstract void executeAction();
}
