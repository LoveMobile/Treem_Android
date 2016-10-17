package com.treem.treem.activities.branch.actions;

import com.treem.treem.models.branch.Branch;

/**
 * Date: 6/15/16.
 */
public class BranchMoveAction extends BranchAction {

	BranchMoveAction(Branch branch, BranchActionsFragment branchActionsFragment, BranchActionsFragment.OnBranchActionListener onBranchActionListener) {
		super(branch, branchActionsFragment, null, onBranchActionListener);
	}

	@Override
	void executeAction() {
		if (onBranchActionListener != null) {
			branchActionsFragment.getActivity().onBackPressed();
			onBranchActionListener.onMoveActonPerformed(branch);
		}
	}
}
