package com.treem.treem.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.shapes.Shape;
import android.support.annotation.ColorInt;

/**
 * Date: 5/19/16.
 */
public class HexagonShape extends Shape {

	private RectF mRect = new RectF();
	private Path mPath;

	private Paint strokePaint;

	private int[] colors;

	public HexagonShape() {
		strokePaint = new Paint();
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setColor(Color.TRANSPARENT);
		strokePaint.setStrokeWidth(0);
	}

	public int getStrokeColor() {
		return strokePaint.getColor();
	}

	public void setStrokeColor(@ColorInt int strokeColor) {
		strokePaint.setColor(strokeColor);
	}

	public int getStrokeWidth() {
		return (int) strokePaint.getStrokeWidth();
	}

	public void setStrokeWidth(int strokeWidth) {
		strokePaint.setStrokeWidth(strokeWidth);
	}

	public void setColors(int[] colors) {
		this.colors = colors;
	}

	/**
	 * Draw this shape into the provided Canvas, with the provided Paint.
	 * Before calling this, you must call {@link #resize(float, float)}.
	 *
	 * @param canvas the Canvas within which this shape should be drawn
	 * @param paint  the Paint object that defines this shape's characteristics
	 */
	@Override
	public void draw(Canvas canvas, Paint paint) {
		if (mPath == null) {
			return;
		}

		if (colors != null) {
			paint.setShader(new LinearGradient(0, 0, 0, mRect.bottom, colors, null, Shader.TileMode.REPEAT));
		}

		canvas.drawPath(mPath, paint);
		canvas.drawPath(mPath, strokePaint);
	}

	@Override
	protected void onResize(float width, float height) {
		mRect.set(0, 0, width, height);
		createPath();
	}

	private void createPath() {
		mPath = new Path();

		float boundsWidth = mRect.right;
		float boundsHeight = mRect.bottom;
		PointF center = new PointF(boundsWidth / 2, boundsHeight / 2);

		float radius = 0.5f * boundsHeight;

		mPath.moveTo(center.x, center.y + radius);

		float c = (float) (Math.PI / 3);

		for (int i = 1; i <= 5; i++) {
			float angleRadians = (float) i * c;
			float x = (float) (center.x + (radius * Math.sin(angleRadians)));
			float y = (float) (center.y + (radius * Math.cos(angleRadians)));

			mPath.lineTo(x, y);
		}

		mPath.close();
	}
}
