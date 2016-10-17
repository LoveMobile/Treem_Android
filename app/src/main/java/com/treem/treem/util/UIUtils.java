package com.treem.treem.util;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

import com.treem.treem.models.branch.Branch;

import java.util.Arrays;

/**
 * Date: 6/9/16.
 */
public final class UIUtils {

	private static final int[][] BUTTON_POSITIONS = {
			{0, -1},
			{1, -1},
			{-1, 0},
			{0, 0},
			{1, 0},
			{-1, 1},
			{0, 1}
	};

	private UIUtils() {
	}

	public static Point getScreenSize(Context context) {
		Point result = new Point();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getSize(result);
		return result;
	}

	public static int[] getBranchCoordinates(Branch.BranchPosition position) {
		int positionX = 0, positionY = 0;

		switch (position) {
			case TopLeft:
				positionX = BUTTON_POSITIONS[0][0];
				positionY = BUTTON_POSITIONS[0][1];
				break;
			case TopRight:
				positionX = BUTTON_POSITIONS[1][0];
				positionY = BUTTON_POSITIONS[1][1];
				break;
			case Left:
				positionX = BUTTON_POSITIONS[2][0];
				positionY = BUTTON_POSITIONS[2][1];
				break;
			case Center:
				positionX = BUTTON_POSITIONS[3][0];
				positionY = BUTTON_POSITIONS[3][1];
				break;
			case Right:
				positionX = BUTTON_POSITIONS[4][0];
				positionY = BUTTON_POSITIONS[4][1];
				break;
			case BottomLeft:
				positionX = BUTTON_POSITIONS[5][0];
				positionY = BUTTON_POSITIONS[5][1];
				break;
			case BottomRight:
				positionX = BUTTON_POSITIONS[6][0];
				positionY = BUTTON_POSITIONS[6][1];
				break;
		}

		return new int[]{positionX, positionY};
	}

	public static Branch.BranchPosition getPosition(int coordinateX, int coordinateY) {
		int[] coordinates = {coordinateX, coordinateY};

		if (Arrays.equals(BUTTON_POSITIONS[0], coordinates)) {
			return Branch.BranchPosition.TopLeft;
		} else if (Arrays.equals(BUTTON_POSITIONS[1], coordinates)) {
			return Branch.BranchPosition.TopRight;
		} else if (Arrays.equals(BUTTON_POSITIONS[2], coordinates)) {
			return Branch.BranchPosition.Left;
		} else if (Arrays.equals(BUTTON_POSITIONS[3], coordinates)) {
			return Branch.BranchPosition.Center;
		} else if (Arrays.equals(BUTTON_POSITIONS[4], coordinates)) {
			return Branch.BranchPosition.Right;
		} else if (Arrays.equals(BUTTON_POSITIONS[5], coordinates)) {
			return Branch.BranchPosition.BottomLeft;
		} else if (Arrays.equals(BUTTON_POSITIONS[6], coordinates)) {
			return Branch.BranchPosition.BottomRight;
		}

		return null;
	}
}
