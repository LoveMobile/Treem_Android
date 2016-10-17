package com.treem.treem.activities.branch.add;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.JsonObject;
import com.treem.treem.R;
import com.treem.treem.activities.branch.BranchBaseFragment;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.services.Treem.TreemBranchService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;
import com.treem.treem.util.DataConvertUtils;
import com.treem.treem.util.UIUtils;
import com.treem.treem.widget.HexagonButton;
import com.treem.treem.widget.TreeGridTheme;

public class BranchAddFragment extends BranchBaseFragment implements RadioGroup.OnCheckedChangeListener, TextWatcher {

	private static final String TAG = "BranchAddFragment";

	private static final String ARG_BUTTON_INITIAL_POSITION = "button_initial_position";
	private static final String ARG_BUTTON_FINAL_POSITION = "button_final_position";
	private static final String ARG_BUTTON_POSITION_X = "position_x";
	private static final String ARG_BUTTON_POSITION_Y = "position_y";
	private static final String ARG_PARENT = "parent";
	private static final String ARG_THEME = "theme";

	private HexagonButton branchButton;
	private EditText branchNameField;
	private RadioGroup branchColorOptions;
	private Button saveAction;
	private Button cancelActon;
	private TreeGridTheme theme = TreeGridTheme.MEMBERS_THEME;

	private Point buttonInitialPosition;
	private Point buttonFinalPosition;

	private int positionX;
	private int positionY;

	private Branch parent;

	private OnBranchAddActionListener onBranchAddActionListener;

	private int selectedColor = -1;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		if (args != null) {
			parent = (Branch) args.getSerializable(ARG_PARENT);
			buttonInitialPosition = args.getParcelable(ARG_BUTTON_INITIAL_POSITION);
			buttonFinalPosition = args.getParcelable(ARG_BUTTON_FINAL_POSITION);
			positionX = args.getInt(ARG_BUTTON_POSITION_X);
			positionY = args.getInt(ARG_BUTTON_POSITION_Y);

			int themeOrdinal = args.getInt(ARG_THEME, -1);

			if (themeOrdinal != -1) {
				theme = TreeGridTheme.values()[themeOrdinal];
			}
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_add_branch, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		branchButton = (HexagonButton) view.findViewById(R.id.branch_button);
		branchNameField = (EditText) view.findViewById(R.id.branch_name);
		branchColorOptions = (RadioGroup) view.findViewById(R.id.branch_color_options);
		saveAction = (Button) view.findViewById(R.id.save_action);
		cancelActon = (Button) view.findViewById(R.id.cancel_action);

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

		if (buttonInitialPosition != null && buttonFinalPosition != null) {
			animateHexagonButton();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		if (onBranchAddActionListener != null) {
			onBranchAddActionListener.onBranchAddClosed();
		}
	}

	private void animateHexagonButton() {
		ObjectAnimator.ofFloat(branchButton, "x", buttonInitialPosition.x, buttonFinalPosition.x).start();
		ObjectAnimator.ofFloat(branchButton, "y", buttonInitialPosition.y, buttonFinalPosition.y).start();
	}

	public void setOnBranchAddActionListener(OnBranchAddActionListener onBranchAddActionListener) {
		this.onBranchAddActionListener = onBranchAddActionListener;
	}

	private void showBranchColorOptions() {
		TypedArray colorsArray = getResources().obtainTypedArray(R.array.branch_colors);

		for (int i = 0; i < colorsArray.length(); i++) {
			showBranchColorOption(colorsArray.getColor(i, Color.TRANSPARENT), i == 0);
		}

		colorsArray.recycle();
	}

	private void hideBranchColorOptions() {
		branchColorOptions.setVisibility(View.GONE);
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
		LayoutInflater layoutInflater = getLayoutInflater(null);
		return (RadioButton) layoutInflater.inflate(R.layout.branch_color_option, branchColorOptions, false);
	}

	private Drawable createBranchButtonBackground(int color) {
		GradientDrawable defaultBackground = new GradientDrawable();
		defaultBackground.setColor(color);

		GradientDrawable checkedBackground = new GradientDrawable();
		checkedBackground.setColor(color);
		checkedBackground.setStroke(getResources().getDimensionPixelSize(R.dimen.branch_color_option_border), Color.WHITE);

		StateListDrawable background = new StateListDrawable();

		background.addState(new int[]{android.R.attr.state_checked}, checkedBackground);
		background.addState(new int[]{}, defaultBackground);

		return background;
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

	/**
	 * Get fragment tag for back stack
	 *
	 * @return fragment tag
	 */
	@Override
	public String getFragmentTag() {
		return TAG;
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

	private void onSaveButtonClick() {
		closeKeyboard();

		final Branch branch = new Branch();
		branch.name = branchNameField.getText().toString();
		branch.setPosition(UIUtils.getPosition(positionX, positionY));
		branch.color = String.format("#%06X", (0xFFFFFF & selectedColor));

		if (parent != null) {
			branch.parent_id = parent.id;
		}

		final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null, getString(R.string.loading), true, false);

		TreemServiceRequest request = new TreemServiceRequest() {

			@Override
			public void onSuccess(String data) {
				progressDialog.dismiss();

				if (onBranchAddActionListener != null) {
					JsonObject parsedData = DataConvertUtils.parseJson(data, JsonObject.class);
					branch.id = parsedData.get("id").getAsLong();

					onBranchAddActionListener.onBranchAdded(branch, positionX, positionY);
				}

				getActivity().onBackPressed();
			}

			@Override
			public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
				progressDialog.dismiss();
			}
		};

		TreemBranchService.setUserBranch(CurrentTreeSettings.SHARED_INSTANCE.treeSession, request, branch);
	}

	private void closeKeyboard() {
		View view = getActivity().getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	private void onCancelButtonClick() {
		closeKeyboard();
		getActivity().onBackPressed();
	}

	public static BranchAddFragment newInstance(Branch parent,
												Point initialButtonPosition,
												Point finalButtonPosition,
												int positionX,
												int positionY,
												TreeGridTheme theme) {
		Bundle args = new Bundle();
		args.putSerializable(ARG_PARENT, parent);
		args.putParcelable(ARG_BUTTON_INITIAL_POSITION, initialButtonPosition);
		args.putParcelable(ARG_BUTTON_FINAL_POSITION, finalButtonPosition);
		args.putInt(ARG_BUTTON_POSITION_X, positionX);
		args.putInt(ARG_BUTTON_POSITION_Y, positionY);

		if (theme != null) {
			args.putInt(ARG_THEME, theme.ordinal());
		}

		BranchAddFragment fragment = new BranchAddFragment();
		fragment.setArguments(args);

		return fragment;
	}

	public interface OnBranchAddActionListener {

		void onBranchAdded(Branch branch, int positionX, int positionY);

		void onBranchAddClosed();
	}
}
