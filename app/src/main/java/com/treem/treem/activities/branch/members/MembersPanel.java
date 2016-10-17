package com.treem.treem.activities.branch.members;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.treem.treem.R;
import com.treem.treem.widget.Panel;

/**
 * Alerts fragment action panel
 */
public class MembersPanel extends Panel {

    //Button action listener
    private OnActionListener actionListener;


    /**
     * Handle panel action events
     */
    public interface OnActionListener{
        /**
         * Handle save button click
         * @param panel link to this object
         */
        void onSaveButtonClick(Panel panel);

        /**
         * Handle cancel button click
         * @param panel link to this object
         */
        void onCancelButtonClick(Panel panel);
    }
    //Action panel buttons
    private Button buttonActionSave;
    private Button buttonActionCancel;

    //Action pannel views
    private ImageView imageAdd;
    private ImageView imageRemove;
    private TextView textAdd;
    private TextView textRemove;
    private int highliteColor;
    private int standardColor;

    public MembersPanel(Context context) {
        super(context);
    }

    public MembersPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MembersPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        highliteColor = getResources().getColor(R.color.tint_color);
        standardColor = getResources().getColor(R.color.member_panel_gray);
        imageAdd = (ImageView)findViewById(R.id.panel_members_add_image);
        imageRemove = (ImageView)findViewById(R.id.panel_members_remove_image);
        textAdd = (TextView)findViewById(R.id.panel_members_add_text);
        textRemove = (TextView)findViewById(R.id.panel_members_remove_text);
        buttonActionSave = (Button)findViewById(R.id.actionButtonSave);
        buttonActionSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (actionListener!=null)
                    actionListener.onSaveButtonClick(MembersPanel.this);
            }
        });
        buttonActionCancel = (Button)findViewById(R.id.actionButtonCancel);
        buttonActionCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (actionListener!=null)
                    actionListener.onCancelButtonClick(MembersPanel.this);
            }
        });
    }

    /**
     * Set selection counts
     * @param addCount count of add users
     * @param removeCount count of remove users
     */
    public void setSelectionCount(int addCount, int removeCount) {
        String text = "+"+addCount;
        textAdd.setText(text);
        text = "-"+removeCount;
        textRemove.setText(text);
        if (addCount>0){
            textAdd.setTextColor(highliteColor);
            imageAdd.setColorFilter(highliteColor);
        }
        else{
            textAdd.setTextColor(standardColor);
            imageAdd.setColorFilter(standardColor);
        }
        if (removeCount>0){
            textRemove.setTextColor(highliteColor);
            imageRemove.setColorFilter(highliteColor);
        }
        else{
            textRemove.setTextColor(standardColor);
            imageRemove.setColorFilter(standardColor);
        }
        if (addCount!=0&&removeCount!=0)
            buttonActionSave.setText(R.string.save);
        else if (addCount!=0)
            buttonActionSave.setText(R.string.add);
        else
            buttonActionSave.setText(R.string.remove);
    }

    /**
     * Set panel actions listener
     * @param listener listener
     */
    public void setActionListener(OnActionListener listener) {
        actionListener = listener;
    }

}
