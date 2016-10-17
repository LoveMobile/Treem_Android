package com.treem.treem.helpers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Base fragment for all other fragments
 */
@SuppressWarnings("unused")
public abstract class BaseFragment extends Fragment {
    private InputMethodManager inputMethodManager;

    /**
     * Get fragment tag for back stack
     * @return fragment tag
     */
    public abstract String getFragmentTag();


    /**
     * Hide keyboard on enter click
     * @param v edit text view
     */
    protected void hideKeyboardOnEnterClick(EditText v){
        v.setOnEditorActionListener(hideKeyboardOnEnterClickAction);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext()!=null)
            inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    /**
     * Hide keyboard
     * @param v current view
     */
    protected void hideKeyboard(View v) {
        if (inputMethodManager!=null)
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    /**
     * Listener for keyboard action. Helps to hide keyboard on enter click and call child listeners
     */
    protected TextView.OnEditorActionListener hideKeyboardOnEnterClickAction = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENDCALL) { //is DONE clicked?
                hideKeyboard(v); //hide keyboard
                return onEnterClick(v); //call child listener
            }
            return false;
        }
    };
    /**
     * Called when used clicked DONE button
     * @param v the view in which done clicked
     * @return true event handled
     */
    protected boolean onEnterClick(@SuppressWarnings("UnusedParameters") View v){
        return true;
    }

}
