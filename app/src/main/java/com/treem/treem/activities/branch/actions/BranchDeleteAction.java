package com.treem.treem.activities.branch.actions;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.treem.treem.R;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.services.Treem.TreemBranchService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

/**
 * Date: 6/20/16.
 */
public class BranchDeleteAction extends BranchAction {

	BranchDeleteAction(Branch branch, BranchActionsFragment branchActionsFragment, BranchActionsFragment.OnBranchActionListener onBranchActionListener) {
		super(branch, branchActionsFragment, null, onBranchActionListener);
	}

	@Override
	void executeAction() {
		showConformation();
	}

	private void showConformation() {
		new AlertDialog.Builder(context)
				.setTitle(context.getString(R.string.delete_branch_confirmation_title))
				.setMessage(context.getString(R.string.delete_branch_confirmation_message))
				.setNegativeButton(context.getString(R.string.no), null)
				.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteBranch();
					}
				})
				.show();
	}

	private void deleteBranch() {
		final ProgressDialog progressDialog = ProgressDialog.show(context, null, context.getString(R.string.loading), true, false);

		TreemServiceRequest request = new TreemServiceRequest() {

			@Override
			public void onSuccess(String data) {
				progressDialog.dismiss();

				if (onBranchActionListener != null) {
					onBranchActionListener.onBranchDeleted(branch);
				}

				branchActionsFragment.getActivity().onBackPressed();
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
				progressDialog.dismiss();
			}
		};

		TreemBranchService.deleteUserBranch(CurrentTreeSettings.SHARED_INSTANCE.treeSession, request, branch);
	}
}
