package com.treem.treem.activities.branch.actions;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.treem.treem.R;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.services.Treem.TreemBranchService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;
import com.treem.treem.widget.HexagonButton;
import com.treem.treem.widget.TreeGridTheme;

public class BranchEditAction extends BranchAction implements RadioGroup.OnCheckedChangeListener, TextWatcher {

	private HexagonButton branchButton;
	private EditText branchNameField;
	private RadioGroup branchColorOptions;
	private Button saveAction;
	private Button cancelActon;

	private Branch parent;

	private int selectedColor = -1;

	BranchEditAction(Branch branch, Branch parent, TreeGridTheme theme, BranchActionsFragment branchActionsFragment, BranchActionsFragment.OnBranchActionListener onBranchActionListener) {
		super(branch, branchActionsFragment, theme, onBranchActionListener);
		this.parent = parent;
	}

	@Override
	void executeAction() {
		View view = branchActionsFragment.getView();

		if (view == null) {
			return;
		}

		FrameLayout actionsContainer = (FrameLayout) view.findViewById(R.id.branch_actions_layout);
		setupView(actionsContainer);
	}

	private void setupView(FrameLayout container) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		layoutInflater.inflate(R.layout.edit_branch_action_form, container);

		branchButton = branchActionsFragment.getBranchButton();
		branchNameField = (EditText) container.findViewById(R.id.branch_name);
		branchColorOptions = (RadioGroup) container.findViewById(R.id.branch_color_options);
		saveAction = (Button) container.findViewById(R.id.save_action);
		cancelActon = (Button) container.findViewById(R.id.cancel_action);

		branchColorOptions.setOnCheckedChangeListener(this);
		branchNameField.addTextChangedListener(this);

		saveAction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSaveButtonClick();
			}
		});

		cancelActon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCancelButtonClick();
			}
		});

		if (parent == null) {
			showBranchColorOptions();
		} else {
			hideBranchColorOptions();
			selectColor(parent.getColor());
		}

		branchNameField.setText(branch.name);
	}

	private void hideBranchColorOptions() {

	}

	private void showBranchColorOptions() {
		TypedArray colorsArray = context.getResources().obtainTypedArray(R.array.branch_colors);

		for (int i = 0; i < colorsArray.length(); i++) {
			int color = colorsArray.getColor(i, Color.TRANSPARENT);
			showBranchColorOption(color, branch.getColor() == color);
		}

		colorsArray.recycle();
	}

	private void showBranchColorOption(int color, boolean checked) {
		RadioButton branchColorButton = createBranchColorButton();
		branchColorButton.setBackgroundDrawable(createBranchButtonBackground(color));
		branchColorButton.setTag(color);

		branchColorOptions.addView(branchColorButton);

		if (checked) {
			branchColorButton.setChecked(true);
		}
	}

	private RadioButton createBranchColorButton() {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		return (RadioButton) layoutInflater.inflate(R.layout.branch_color_option, branchColorOptions, false);
	}

	private Drawable createBranchButtonBackground(int color) {
		GradientDrawable defaultBackground = new GradientDrawable();
		defaultBackground.setColor(color);

		GradientDrawable checkedBackground = new GradientDrawable();
		checkedBackground.setColor(color);
		checkedBackground.setStroke(context.getResources().getDimensionPixelSize(R.dimen.branch_color_option_border), Color.WHITE);

		StateListDrawable background = new StateListDrawable();

		background.addState(new int[]{android.R.attr.state_checked}, checkedBackground);
		background.addState(new int[]{}, defaultBackground);

		return background;
	}

	private void onSaveButtonClick() {
		closeKeyboard();

		branch.name = branchNameField.getText().toString();
		branch.color = String.format("#%06X", (0xFFFFFF & selectedColor));

		if (parent != null) {
			branch.parent_id = parent.id;
		}

		final ProgressDialog progressDialog = ProgressDialog.show(context, null, context.getString(R.string.loading), true, false);

		TreemServiceRequest request = new TreemServiceRequest() {

			@Override
			public void onSuccess(String data) {
				progressDialog.dismiss();

				if (onBranchActionListener != null) {
					onBranchActionListener.onEditActionPerformed(branch);
				}

				branchActionsFragment.getActivity().onBackPressed();
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
				progressDialog.dismiss();
			}
		};

		TreemBranchService.setUserBranch(CurrentTreeSettings.SHARED_INSTANCE.treeSession, request, branch);
	}

	private void onCancelButtonClick() {
		closeKeyboard();
		branchActionsFragment.getActivity().onBackPressed();
	}

	private void closeKeyboard() {
		View view = branchActionsFragment.getActivity().getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	/**
	 * <p>Called when the checked radio button has changed. When the
	 * selection is cleared, checkedId is -1.</p>
	 *
	 * @param group     the group in which the checked radio button has changed
	 * @param checkedId the unique identifier of the newly checked radio button
	 */
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		RadioButton checkedButton = (RadioButton) group.findViewById(checkedId);
		selectColor((int) checkedButton.getTag());
	}

	private void selectColor(int color) {
		selectedColor = color;
		updateHexagonButton();
		setSaveButtonState();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		branchButton.setText(s.toString());
		setSaveButtonState();
	}

	private void setSaveButtonState() {
		saveAction.setEnabled(selectedColor != 1 && branchNameField.getText().length() > 0);
	}

	private void updateHexagonButton() {
		int[] colors = {selectedColor, selectedColor};

		if (theme == TreeGridTheme.SECRET_THEME) {
			branchButton.setStrokeWidth(3);
			branchButton.setStrokeColor(selectedColor);
		} else {
			branchButton.setColors(colors);
			branchButton.setTintColors(colors);
		}
	}
}
