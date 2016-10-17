package com.treem.treem.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.treem.treem.R;
import com.treem.treem.models.branch.Branch;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date: 5/19/16.
 */
public class HexagonButton extends Button implements AutofitHelper.OnTextSizeChangeListener {

	private static final Pattern EMOJI_PATTERN = Pattern.compile("[\u2190-\u21FF\u2600-\u26FF\u2700-\u27BF\u3000-\u303F\u1F300-\u1F64F\u1F680-\u1F6FF\uD83C-\uDBFF\uDC00-\uDFFF]");
	private static final Pattern EMOJI_GROUP_PATTERN = Pattern.compile("[\u2190-\u21FF\u2600-\u26FF\u2700-\u27BF\u3000-\u303F\u1F300-\u1F64F\u1F680-\u1F6FF\uD83C-\uDBFF\uDC00-\uDFFF]+");

	public static final String IMAGE_PLACEHOLDER = "[]";

	boolean notifyTextChanged = true;
	private String plainText = "";
	private int maxTextLines;

	private RectF viewActiveArea;

	private AutofitHelper mHelper;

	private Drawable icon;

	private boolean hasStroke;
	private int strokeColor;
	private int strokeWidth;

	private int[] colors;
	private int[] tintColors;

	private Branch branch;
	private Branch parentBranch;

	HexagonGridPosition gridPosition;

	public HexagonButton(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	public HexagonButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public HexagonButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public HexagonButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	public void setPositionInGrid(int x, int y) {
		this.gridPosition = new HexagonGridPosition(x, y);
	}

	public int getPositionX() {
		return this.gridPosition.x;
	}

	public int getPositionY() {
		return this.gridPosition.y;
	}

	private void init(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
		parseAttributes(context, attrs, defStyle, defStyleRes);

		setGravity(Gravity.CENTER);
		setClickable(true);
		mHelper = AutofitHelper.create(this, attrs, defStyle).addOnTextSizeChangeListener(this);

		initViewBackground();

		setText(getText());
	}

	private void parseAttributes(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HexagonButton, defStyle, defStyleRes);

		icon = typedArray.getDrawable(R.styleable.HexagonButton_image);
		hasStroke = typedArray.hasValue(R.styleable.HexagonButton_strokeColor);

		// Handle background colors
		parseColors(typedArray);

		if (hasStroke) {
			strokeColor = typedArray.getColor(R.styleable.HexagonButton_strokeColor, Color.TRANSPARENT);
			strokeWidth = typedArray.getDimensionPixelSize(R.styleable.HexagonButton_strokeWidth, 0);
		}

		typedArray.recycle();
	}

	private void parseColors(TypedArray typedArray) {
		int gradientStartColor = typedArray.getColor(R.styleable.HexagonButton_shapeGradientColorStart, -1);
		int gradientEndColor = typedArray.getColor(R.styleable.HexagonButton_shapeGradientColorEnd, gradientStartColor);

		int gradientTintColorStart = typedArray.getColor(R.styleable.HexagonButton_shapeGradientTintColorStart, gradientStartColor);
		int gradientTintColorEnd = typedArray.getColor(R.styleable.HexagonButton_shapeGradientTintColorEnd, gradientEndColor);

		if (gradientStartColor != -1) {
			colors = new int[]{gradientStartColor, gradientEndColor};
			tintColors = new int[]{gradientTintColorStart, gradientTintColorEnd};
			return;
		}

		int color = typedArray.getColor(R.styleable.HexagonButton_shapeColor, -1);
		int tintColor = typedArray.getColor(R.styleable.HexagonButton_shapeTintColor, color);

		if (color != -1) {
			colors = new int[]{color, color};
			tintColors = new int[]{tintColor, tintColor};
		}
	}

	private void initViewBackground() {
		HexagonShape backgroundShape = new HexagonShape();
		HexagonShape backgroundTintShape = new HexagonShape();

		backgroundShape.setColors(colors);
		backgroundTintShape.setColors(tintColors);

		if (hasStroke) {
			backgroundShape.setStrokeColor(strokeColor);
			backgroundShape.setStrokeWidth(strokeWidth);

			backgroundTintShape.setStrokeColor(strokeColor);
			backgroundTintShape.setStrokeWidth(strokeWidth);
		}

		StateListDrawable states = new StateListDrawable();

		states.addState(new int[]{android.R.attr.state_pressed}, new ShapeDrawable(backgroundTintShape));
		states.addState(new int[]{}, new ShapeDrawable(backgroundShape));

		setBackgroundDrawable(states);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		calculateViewActiveArea();
		calculateMaxTextLines();
		setViewPaddings();
	}

	public int getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(@ColorInt int strokeColor) {
		this.strokeColor = strokeColor;
		this.hasStroke = true;
		initViewBackground();
	}

	public int getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(int strokeWidth) {
		this.strokeWidth = strokeWidth;
		this.hasStroke = true;
		initViewBackground();
	}

	public void setColors(int[] colors) {
		this.colors = colors;
		initViewBackground();
	}

	public void setTintColors(int[] tintColors) {
		this.tintColors = tintColors;
		initViewBackground();
	}

	private void calculateViewActiveArea() {
		viewActiveArea = new RectF();

		float boundsWidth = getWidth();
		float boundsHeight = getHeight();

		PointF center = new PointF(boundsWidth / 2, boundsHeight / 2);

		float radius = 0.5f * boundsHeight;
		float c = (float) (Math.PI / 3);

		for (int i = 1; i <= 4; i++) {
			float angleRadians = (float) i * c;
			float x = (float) (center.x + (radius * Math.sin(angleRadians)));
			float y = (float) (center.y + (radius * Math.cos(angleRadians)));

			switch (i) {
				case 1:
					viewActiveArea.right = x;
					viewActiveArea.bottom = y;
					break;
				case 4:
					viewActiveArea.left = x;
					viewActiveArea.top = y;
					break;
			}
		}
	}

	private void setViewPaddings() {
		int paddingLeft = (int) viewActiveArea.left;
		int paddingTop = (int) viewActiveArea.top;
		int paddingRight = (int) (getWidth() - viewActiveArea.right);
		int paddingBottom = (int) (getHeight() - viewActiveArea.bottom);

		setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
	}

	private void calculateMaxTextLines() {
		int lineHeight = getLineHeight();
		int viewHeight = (int) viewActiveArea.height();

		maxTextLines = viewHeight / lineHeight;

		if (icon == null) {
			setMaxLines(maxTextLines);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTextSize(int unit, float size) {
		super.setTextSize(unit, size);
		if (mHelper != null) {
			mHelper.setTextSize(unit, size);
		}
	}

	/**
	 * Returns whether or not the text will be automatically re-sized to fit its constraints.
	 */
	public boolean isSizeToFit() {
		return mHelper.isEnabled();
	}

	/**
	 * Sets the property of this field (sizeToFit), to automatically resize the text to fit its
	 * constraints.
	 */
	public void setSizeToFit() {
		setSizeToFit(true);
	}

	/**
	 * If true, the text will automatically be re-sized to fit its constraints; if false, it will
	 * act like a normal TextView.
	 *
	 * @param sizeToFit
	 */
	public void setSizeToFit(boolean sizeToFit) {
		mHelper.setEnabled(sizeToFit);
	}

	/**
	 * Returns the maximum size (in pixels) of the text in this View.
	 */
	public float getMaxTextSize() {
		return mHelper.getMaxTextSize();
	}

	/**
	 * Set the maximum text size to the given value, interpreted as "scaled pixel" units. This size
	 * is adjusted based on the current density and user font size preference.
	 *
	 * @param size The scaled pixel size.
	 * @attr ref android.R.styleable#TextView_textSize
	 */
	public void setMaxTextSize(float size) {
		mHelper.setMaxTextSize(size);
	}

	/**
	 * Set the maximum text size to a given unit and value. See TypedValue for the possible
	 * dimension units.
	 *
	 * @param unit The desired dimension unit.
	 * @param size The desired size in the given units.
	 * @attr ref android.R.styleable#TextView_textSize
	 */
	public void setMaxTextSize(int unit, float size) {
		mHelper.setMaxTextSize(unit, size);
	}

	/**
	 * Returns the minimum size (in pixels) of the text in this View.
	 */
	public float getMinTextSize() {
		return mHelper.getMinTextSize();
	}

	/**
	 * Set the minimum text size to the given value, interpreted as "scaled pixel" units. This size
	 * is adjusted based on the current density and user font size preference.
	 *
	 * @param minSize The scaled pixel size.
	 * @attr ref me.grantland.R.styleable#AutofitTextView_minTextSize
	 */
	public void setMinTextSize(int minSize) {
		mHelper.setMinTextSize(TypedValue.COMPLEX_UNIT_SP, minSize);
	}

	/**
	 * Set the minimum text size to a given unit and value. See TypedValue for the possible
	 * dimension units.
	 *
	 * @param unit    The desired dimension unit.
	 * @param minSize The desired size in the given units.
	 * @attr ref me.grantland.R.styleable#AutofitTextView_minTextSize
	 */
	public void setMinTextSize(int unit, float minSize) {
		mHelper.setMinTextSize(unit, minSize);
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
		setText(plainText);
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);

		if (!notifyTextChanged) {
			notifyTextChanged = true;
			return;
		}

		this.plainText = text.toString();

		if (mHelper == null) {
			return;
		}

		if (icon == null) {
			setSizeToFit(true);
			setMaxLines(maxTextLines);

			if (isSingleEmoji(text)) {
				enlargeEmoji(text);
			}
		} else {
			setMaxLines(2);
			setSizeToFit(false);
			setIcon(text);
		}
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Branch getParentBranch() {
		return parentBranch;
	}

	public void setParentBranch(Branch parentBranch) {
		this.parentBranch = parentBranch;
	}

	private void setIcon(CharSequence text) {
		text = IMAGE_PLACEHOLDER + (text.length() > 0 ? "\n" + text : "");
		icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());

		SpannableString spannableString = new SpannableString(text);
		spannableString.setSpan(new ImageSpan(icon, ImageSpan.ALIGN_BASELINE), 0, IMAGE_PLACEHOLDER.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

		notifyTextChanged = false;
		setText(spannableString);
	}

	private boolean isSingleEmoji(CharSequence charSequence) {
		try {
			String text = charSequence.toString();

			byte[] utf8 = text.getBytes("UTF-8");
			String value = new String(utf8, "UTF-8");

			Matcher groupMatcher = EMOJI_GROUP_PATTERN.matcher(value);

			if (!groupMatcher.matches()) {
				return false;
			}

			Matcher matcher = EMOJI_PATTERN.matcher(value);
			int emojiCount = 0;

			while (matcher.find()) {
				emojiCount++;
			}

			return emojiCount == 1;
		} catch (UnsupportedEncodingException ex) {
			return false;
		}
	}

	private void enlargeEmoji(final CharSequence text) {
		if (viewActiveArea == null) {
			getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

				@Override
				public void onGlobalLayout() {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
					enlargeEmofyTextSize(text);
				}
			});
		} else {
			enlargeEmofyTextSize(text);
		}
	}

	private void enlargeEmofyTextSize(CharSequence text) {
		int maxLineCount = Math.max(1, Math.round(viewActiveArea.height() / getLineHeight()) - 1);

		SpannableString spannableString = new SpannableString(text);
		spannableString.setSpan(new AbsoluteSizeSpan((int) (maxLineCount * getTextSize())), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

		notifyTextChanged = false;
		setText(spannableString);
	}

	@Override
	public void onTextSizeChange(float textSize, float oldTextSize) {
	}
}
