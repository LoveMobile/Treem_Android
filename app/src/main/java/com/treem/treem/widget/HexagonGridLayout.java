package com.treem.treem.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.treem.treem.R;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.util.UIUtils;

import org.codetome.hexameter.core.api.AxialCoordinate;
import org.codetome.hexameter.core.api.Hexagon;
import org.codetome.hexameter.core.api.HexagonOrientation;
import org.codetome.hexameter.core.api.HexagonalGrid;
import org.codetome.hexameter.core.api.HexagonalGridBuilder;
import org.codetome.hexameter.core.api.HexagonalGridLayout;
import org.codetome.hexameter.core.api.Point;
import org.codetome.hexameter.core.backport.Optional;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

/**
 * Date: 5/18/16.
 */
public class HexagonGridLayout extends ViewGroup {

	private static final int DEFAULT_GRID_WIDTH = 3;
	private static final int DEFAULT_GRID_HEIGHT = 3;

	private int radius;
	private int gridWidth = DEFAULT_GRID_WIDTH;
	private int gridHeight = DEFAULT_GRID_HEIGHT;
	private int supportedChildViews;

	private HexagonalGrid grid;
	private List<Hexagon> hexagons;

	private HexagonButton centerButton;
	private List<HexagonButton> buttons;

	private int offset;

	private int buttonWidth;
	private int buttonHeight;

	private TreeGridTheme theme;
	private OnBranchActionListener onBranchActionListener;

	private Mode mode = Mode.NORMAL;
	private Branch movingBrnach;

	public HexagonGridLayout(Context context) {
		super(context);
	}

	public HexagonGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public HexagonGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public HexagonGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	public void setOnBranchActionListener(OnBranchActionListener onBranchActionListener) {
		this.onBranchActionListener = onBranchActionListener;
	}

	public void setGridSize(int gridWidth, int gridHeight) {
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;

		initHexData();
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void clearGrid() {
		removeAllViews();
		centerButton = null;

		if (buttons != null) {
			buttons.clear();
		}
	}

	public void setButtonInCentre(HexagonButton button) {
		removeView(centerButton);
		this.centerButton = button;

		button.setPositionInGrid(0, 0);
		setButton(button, false);
	}

	public void setButtons(List<HexagonButton> buttons, boolean animate) {
		this.buttons = buttons;
		for (HexagonButton button : buttons) {
			setButton(button, 0, animate);
		}
	}

	public List<HexagonButton> getButtons() {
		return buttons;
	}

	public HexagonButton getButtonAtCoordinate(int x, int y) {
		if (centerButton == null && buttons == null) {
			return null;
		}

		HexagonGridPosition position = new HexagonGridPosition(x, y);

		if (centerButton != null && position.equals(centerButton.gridPosition)) {
			return centerButton;
		}

		if (buttons != null) {
			for (int i = 0; i < buttons.size(); i++) {
				HexagonButton hexagonButton = buttons.get(i);
				if (position.equals(hexagonButton.gridPosition)) {
					return hexagonButton;
				}
			}
		}

		return null;
	}

	public void updateButtonForBranch(Branch branch) {
		if (buttons == null) {
			return;
		}

		HexagonButton button = null;

		for (int i = 0; i < buttons.size(); i++) {
			HexagonButton hexagonButton = buttons.get(i);
			if (hexagonButton.getBranch() != null && hexagonButton.getBranch().id == branch.id) {
				button = hexagonButton;
				break;
			}
		}

		if (button != null) {
			button.setBranch(branch);
			button.setText(branch.name);

			setButton(button);
		}
	}

	public void setButton(HexagonButton button) {
		HexagonButton oldButton = getButtonAtCoordinate(button.gridPosition.x, button.gridPosition.y);

		if (oldButton != null) {
			removeView(oldButton);
		}

		setButton(button, false);
	}

	public void setTheme(TreeGridTheme theme) {
		this.theme = theme;
	}

	public void moveBranch(Branch branch) {
		this.movingBrnach = branch;
		int[] coordinates = UIUtils.getBranchCoordinates(branch.getPosition());
		moveBranch(coordinates[0], coordinates[1]);
	}

	private void moveBranch(int positionX, int positionY) {
		HexagonButton hexagonButton = getButtonAtCoordinate(positionX, positionY);
		moveBranch(hexagonButton);
	}

	private void moveBranch(HexagonButton hexagonButton) {
		this.mode = Mode.MOVING;

		if (centerButton.getBranch() == null) {
			centerButton.setAlpha(0.5F);
		}

		for (HexagonButton button : buttons) {
			if (button == hexagonButton) {
				button.setAlpha(0.5F);
			}

			Branch branch = button.getBranch();

			if (branch == null) {
				button.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_branch_move));
				button.setText(R.string.move_here);
				setEmptyBranchHexagonButtonProperties(button);
			}
		}
	}

	public void finishMoving() {
		this.mode = Mode.NORMAL;
	}

	private void init(Context context, AttributeSet attrs) {
		mode = Mode.NORMAL;
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HexagonGridLayout);

		radius = a.getDimensionPixelOffset(R.styleable.HexagonGridLayout_radius,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
						48,
						getResources().getDisplayMetrics()));

		gridWidth = a.getInteger(R.styleable.HexagonGridLayout_gridWidth, Integer.MAX_VALUE);
		gridHeight = a.getInteger(R.styleable.HexagonGridLayout_gridHeight, Integer.MAX_VALUE);
		offset = a.getDimensionPixelSize(R.styleable.HexagonGridLayout_offset, 1);

		a.recycle();
		initHexData();
	}

	private void initHexData() {
		HexagonalGridBuilder builder = new HexagonalGridBuilder()
				.setGridWidth(gridWidth)
				.setGridHeight(gridHeight)
				.setGridLayout(HexagonalGridLayout.HEXAGONAL)
				.setOrientation(HexagonOrientation.POINTY_TOP)
				.setRadius(radius);

		grid = builder.build();
		grid.getHexagons().toList().subscribe(new Action1<List<Hexagon>>() {

			@Override
			public void call(List<Hexagon> hexagons) {
				HexagonGridLayout.this.hexagons = hexagons;
			}
		});

		initButtonSize();
	}

	private void initButtonSize() {
		if (hexagons != null && hexagons.size() > 0) {
			List<Point> points = getHexagonPoints(hexagons.get(0));

			int left = (int) points.get(3).getCoordinateX();
			int top = (int) points.get(4).getCoordinateY();
			int right = (int) points.get(0).getCoordinateX();
			int bottom = (int) points.get(1).getCoordinateY();

			buttonWidth = right - left;
			buttonHeight = bottom - top;
		}
	}

	private void setButton(HexagonButton button, int position, boolean animate) {
		LayoutParams layoutParams = generateDefaultLayoutParams();
		layoutParams.width = buttonWidth;
		layoutParams.height = buttonHeight;
		button.setLayoutParams(layoutParams);

		if (position >= 0) {
			addView(button, position);
		} else {
			addView(button);
		}

		if (animate) {
			animateFromCenter(button);
		}

		if (theme == null) {
			return;
		}

		if (button == centerButton) {
			setCenterHexagonButtonProperties(button);
		} else {
			setEmptyBranchHexagonButtonProperties(button);
		}

		initButtonActionListener(button);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int wCount = gridWidth;

		int hCount = gridHeight;

		supportedChildViews = wCount * hCount - (wCount / 2) - 1;

		// Find out how big everyone wants to be
		int spec = MeasureSpec.makeMeasureSpec(2 * radius, MeasureSpec.EXACTLY);
		measureChildren(spec, spec);

		int minLeft = -1;
		int minTop = -1;
		int maxRight = -1;
		int maxBottom = -1;

		for (Hexagon hexagon : hexagons) {
			List<Point> points = getHexagonPoints(hexagon);

			int left = (int) points.get(3).getCoordinateX();
			int top = (int) points.get(4).getCoordinateY();
			int right = (int) points.get(0).getCoordinateX();
			int bottom = (int) points.get(1).getCoordinateY();

			if (left < minLeft || minLeft == -1) {
				minLeft = left;
			}

			if (top < minTop || minLeft == -1) {
				minTop = top;
			}

			if (right > maxRight || maxRight == -1) {
				maxRight = right;
			}

			if (bottom > maxBottom || maxBottom == -1) {
				maxBottom = bottom;
			}
		}

		// Check against minimum height and width
		int width = Math.max(maxRight - minLeft + ((gridWidth - 1) * offset), getSuggestedMinimumWidth());
		int height = Math.max(maxBottom - minTop + ((gridHeight - 1) * offset), getSuggestedMinimumHeight());

		setMeasuredDimension(resolveSize(width, widthMeasureSpec),
				resolveSize(height, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (int c = 0; c < supportedChildViews; c++) {
			View child = getChildAt(c);

			if (!(child instanceof HexagonButton)) {
				continue;
			}

			HexagonButton hexagonButton = ((HexagonButton) child);
			AxialCoordinate axialCoordinate = getAxialCoordinateForButton(hexagonButton);

			Optional<Hexagon> hexagon = grid.getByAxialCoordinate(axialCoordinate);

			if (!hexagon.isPresent()) {
				return;
			}

			List<Point> points = getHexagonPoints(hexagon.get());

			int left = (int) points.get(3).getCoordinateX() - buttonWidth / 2;
			int top = (int) points.get(4).getCoordinateY();
			int right = (int) points.get(0).getCoordinateX() - buttonWidth / 2;
			int bottom = (int) points.get(1).getCoordinateY();

			// Calculate offset between buttons
			int verticalOffset = axialCoordinate.getGridZ() * offset;
			int horizontalOffset = axialCoordinate.getGridX() * offset;

			top += verticalOffset;
			bottom += verticalOffset;

			left += horizontalOffset;
			right += horizontalOffset;

			child.layout(left, top, right, bottom);
		}
	}

	private void initButtonActionListener(final HexagonButton hexagonButton) {
		hexagonButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleButtonClickListener(hexagonButton);
			}
		});

		if (hexagonButton.getBranch() != null) {
			hexagonButton.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (mode == Mode.NORMAL && onBranchActionListener != null) {
						onBranchActionListener.onEditBranchClick(hexagonButton.getBranch(), hexagonButton);
						return true;
					}

					return false;
				}
			});
		}
	}

	private void handleButtonClickListener(HexagonButton hexagonButton) {
		if (onBranchActionListener != null) {
			Branch branch = hexagonButton.getBranch();

			if (branch == null) {
				switch (mode) {
					case NORMAL:
						if (hexagonButton == centerButton) {
							onBranchActionListener.onCenterBranchButtonClick(hexagonButton);
						} else {
							onBranchActionListener.onNewBranchRequested(hexagonButton.gridPosition.x, hexagonButton.gridPosition.y, hexagonButton);
						}
						break;
					case MOVING:
						onBranchActionListener.onBranchMoveRequested(movingBrnach, hexagonButton);
						break;
				}
			} else {
				onBranchActionListener.onBranchButtonClick(branch, hexagonButton);
			}
		}
	}

	@SuppressWarnings("ResourceAsColor")
	private void setCenterHexagonButtonProperties(HexagonButton hexagonButton) {
		int color;

		if (hexagonButton.getParentBranch() != null) {
			color = hexagonButton.getParentBranch().getColor();
		} else {
			color = hexagonButton.getBranch() == null ? theme.getInitialBranchColor() : theme.getActionTitleColor();
		}

		hexagonButton.setStrokeColor(theme.getButtonStrokeColor());
		hexagonButton.setTextColor(color);
		hexagonButton.setStrokeWidth(5);

		Drawable icon = hexagonButton.getIcon();

		if (icon != null) {
			icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		}

		int backgroundColor = hexagonButton.getBranch() == null ? theme.getCenterBranchFillColor() : hexagonButton.getBranch().getColor();

		hexagonButton.setColors(new int[]{backgroundColor, backgroundColor});
		hexagonButton.setTintColors(new int[]{backgroundColor, backgroundColor});
	}

	private void setEmptyBranchHexagonButtonProperties(HexagonButton hexagonButton) {
		int color;
		int textColor;

		if (hexagonButton.getBranch() == null) {
			color = theme.getAddFillColor();
			textColor = theme.getAddTitleColor();

			Drawable icon = hexagonButton.getIcon();

			if (icon != null) {
				icon.setColorFilter(theme.getAddTitleColor(), PorterDuff.Mode.SRC_ATOP);
			}
		} else {
			color = hexagonButton.getParentBranch() == null ? hexagonButton.getBranch().getColor() : hexagonButton.getParentBranch().getColor();
			textColor = theme.getActionTitleColor();
		}

		hexagonButton.setTextColor(textColor);
		hexagonButton.setStrokeColor(theme.getButtonStrokeColor());
		hexagonButton.setStrokeWidth(5);

		if (theme == TreeGridTheme.SECRET_THEME) {
			hexagonButton.setStrokeWidth(3);
			hexagonButton.setStrokeColor(color);
		} else {
			hexagonButton.setColors(new int[]{color, color});
			hexagonButton.setTintColors(new int[]{color, color});
		}
	}

	private void setButton(HexagonButton button, boolean animate) {
		setButton(button, -1, animate);
	}

	private AxialCoordinate getAxialCoordinateForButton(HexagonButton button) {
		int x = button.gridPosition.x;
		int y = button.gridPosition.y;

		return AxialCoordinate.fromCoordinates(x + 1, y + 1);
	}

	private void animateFromCenter(final HexagonButton hexagonButton) {
		hexagonButton.setVisibility(View.INVISIBLE);
		hexagonButton.post(new Runnable() {
			@Override
			public void run() {
				Optional<Hexagon> hexagon = grid.getByAxialCoordinate(AxialCoordinate.fromCoordinates(1, 1));

				if (!hexagon.isPresent()) {
					return;
				}

				List<Point> points = getHexagonPoints(hexagon.get());

				int left = (int) points.get(3).getCoordinateX() - buttonWidth / 2;
				int top = (int) points.get(4).getCoordinateY();

				ObjectAnimator transformX = ObjectAnimator.ofFloat(hexagonButton, View.X, left, hexagonButton.getX()).setDuration(300);
				transformX.addListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationStart(Animator animation) {
						hexagonButton.setVisibility(View.VISIBLE);
					}
				});
				transformX.start();

				ObjectAnimator.ofFloat(hexagonButton, View.Y, top, hexagonButton.getY()).setDuration(300).start();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private List<Point> getHexagonPoints(Hexagon hexagon) {
		if (hexagon.getPoints() instanceof List) {
			return ((List) hexagon.getPoints());
		}

		return new ArrayList<>(hexagon.getPoints());
	}

	public interface OnBranchActionListener {

		void onBranchButtonClick(Branch branch, HexagonButton view);

		void onCenterBranchButtonClick(HexagonButton view);

		void onNewBranchRequested(int positionX, int positionY, HexagonButton view);

		void onEditBranchClick(Branch branch, HexagonButton view);

		void onBranchMoveRequested(Branch branch, HexagonButton view);
	}

	private enum Mode {
		NORMAL, EDITING, MOVING
	}
}
