package com.treem.treem.widget;

import android.graphics.Color;

/**
 * Date: 5/27/16.
 */
public enum TreeGridTheme {

	MEMBERS_THEME(
			Color.WHITE,
			Color.TRANSPARENT,
			Color.TRANSPARENT,
			Color.rgb(102, 141, 60),
			Color.TRANSPARENT,
			Color.WHITE,
			Color.argb(125, 201, 201, 201),
			Color.argb(242, 201, 201, 201),
			Color.TRANSPARENT,
			Color.WHITE,
			0.3F,
			Color.WHITE,
			1.4F,
			Color.WHITE
	),

	SECRET_THEME(
			Color.rgb(225, 225, 225),
			Color.TRANSPARENT,
			Color.TRANSPARENT,
			Color.rgb(170, 176, 170),
			Color.TRANSPARENT,
			Color.rgb(225, 225, 225),
			Color.argb(216, 0, 0, 0),
			Color.rgb(104, 110, 104),
			Color.TRANSPARENT,
			Color.rgb(225, 225, 225),
			0.6F,
			Color.WHITE,
			1.8F,
			Color.BLACK
	),

	EXPLORE_THEME(
			Color.WHITE,
			Color.TRANSPARENT,
			Color.TRANSPARENT,
			Color.rgb(86, 127, 171),
			Color.TRANSPARENT,
			Color.WHITE,
			Color.argb(125, 201, 201, 201),
			Color.argb(242, 201, 201, 211),
			Color.TRANSPARENT,
			Color.WHITE,
			0.3F,
			Color.WHITE,
			1.4F,
			Color.WHITE
	);

	private final int barTextBoldColor;
	private final int backgroundColor;
	private final int buttonStrokeColor;
	private final int initialBranchColor;
	private final int defaultBranchFillColor;
	private final int backFillColor;
	private final int addFillColor;
	private final int addTitleColor;
	private final int actionFillColor;
	private final int actionTitleColor;
	private final float editBranchAlpha;
	private final int branchBarTitleColor;
	private final float hexagonLineWidth;
	private final int centerBranchFillColor;

	TreeGridTheme(int barTextBoldColor,
				  int backgroundColor,
				  int buttonStrokeColor,
				  int initialBranchColor,
				  int defaultBranchFillColor,
				  int backFillColor,
				  int addFillColor,
				  int addTitleColor,
				  int actionFillColor,
				  int actionTitleColor,
				  float editBranchAlpha,
				  int branchBarTitleColor,
				  float hexagonLineWidth,
				  int centerBranchFillColor) {
		this.barTextBoldColor = barTextBoldColor;
		this.backgroundColor = backgroundColor;
		this.buttonStrokeColor = buttonStrokeColor;
		this.initialBranchColor = initialBranchColor;
		this.defaultBranchFillColor = defaultBranchFillColor;
		this.backFillColor = backFillColor;
		this.addFillColor = addFillColor;
		this.addTitleColor = addTitleColor;
		this.actionFillColor = actionFillColor;
		this.actionTitleColor = actionTitleColor;
		this.editBranchAlpha = editBranchAlpha;
		this.branchBarTitleColor = branchBarTitleColor;
		this.hexagonLineWidth = hexagonLineWidth;
		this.centerBranchFillColor = centerBranchFillColor;
	}

	public int getBarTextBoldColor() {
		return barTextBoldColor;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public int getButtonStrokeColor() {
		return buttonStrokeColor;
	}

	public int getInitialBranchColor() {
		return initialBranchColor;
	}

	public int getDefaultBranchFillColor() {
		return defaultBranchFillColor;
	}

	public int getBackFillColor() {
		return backFillColor;
	}

	public int getAddFillColor() {
		return addFillColor;
	}

	public int getAddTitleColor() {
		return addTitleColor;
	}

	public int getActionFillColor() {
		return actionFillColor;
	}

	public int getActionTitleColor() {
		return actionTitleColor;
	}

	public float getEditBranchAlpha() {
		return editBranchAlpha;
	}

	public int getBranchBarTitleColor() {
		return branchBarTitleColor;
	}

	public float getHexagonLineWidth() {
		return hexagonLineWidth;
	}

	public int getCenterBranchFillColor() {
		return centerBranchFillColor;
	}
}
