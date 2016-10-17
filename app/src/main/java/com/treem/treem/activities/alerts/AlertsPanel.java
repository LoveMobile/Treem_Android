package com.treem.treem.activities.alerts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.treem.treem.R;
import com.treem.treem.widget.Panel;

/**
 * Alerts fragment action panel
 */
public class AlertsPanel extends Panel {

    //Button action listener
    private OnActionListener actionListener;


    /**
     * Handle panel action events
     */
    public interface OnActionListener{
        /**
         * Handle left button click
         * @param panel link to this object
         */
        void onLeftButtonClick(Panel panel);

        /**
         * Handle right button click
         * @param panel link to this object
         */
        void onRightButtonClick(Panel panel);
    }
    //Action panel buttons
    private Button buttonActionLeft;
    private Button buttonActionRight;


    public AlertsPanel(Context context) {
        super(context);
    }

    public AlertsPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlertsPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        buttonActionLeft = (Button)findViewById(R.id.actionButtonLeft);
        buttonActionLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (actionListener!=null)
                    actionListener.onLeftButtonClick(AlertsPanel.this);
            }
        });
        buttonActionRight = (Button)findViewById(R.id.actionButtonRight);
        buttonActionRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (actionListener!=null)
                    actionListener.onRightButtonClick(AlertsPanel.this);
            }
        });
    }

    /**
     * Set handled alert and buttons text
     * @param leftButton left button title
     * @param rightButton right button title
     */
    public void setAlert(int leftButton, int rightButton) {
        if (buttonActionLeft!=null)
            buttonActionLeft.setText(leftButton);
        if (buttonActionRight!=null)
            buttonActionRight.setText(rightButton);
    }

    /**
     * Set panel actions listener
     * @param listener listener
     */
    public void setActionListener(OnActionListener listener) {
        actionListener = listener;
    }

}
