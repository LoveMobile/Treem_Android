package com.treem.treem.activities.settings;

import android.support.v4.app.Fragment;

import com.treem.treem.helpers.BaseFragment;

/**
 * Base fragment for settings
 */
public abstract class SettingsBaseFragment extends BaseFragment{


    /**
     * Settings menu item selected
     * @param itemId id of the selected item
     */
    protected void menuSelected(int itemId) {
        SettingsMenuInteraction baseFragment = getBaseFragment(); //get base fragment
        if (baseFragment!=null){
            baseFragment.menuSelected(itemId); //throw selected menu item to base fragment
        }
    }

    /**
     * Get base fragment
     * @return reference to base fragment or null if target fragment was not set
     */
    private SettingsMenuInteraction getBaseFragment() {
        Fragment f = getTargetFragment(); //get target fragment
        if (f!=null&& f instanceof SettingsMenuInteraction) //is fragment may handle SettingsMenuInteraction events?
            return (SettingsMenuInteraction)f; //return reference to fragment
        return null; //or null if fragment was not found
    }

    /**
     * Set toolbar title
     * @param titleId title id to set
     */
    protected void setTitle(int titleId){
        SettingsMenuInteraction baseFragment = getBaseFragment();
        if (baseFragment!=null)
            baseFragment.setTitle(titleId);
    }

    /**
     * Set back button visible
     * @param visible true to show or false to hide
     */
    protected void setBackVisible(boolean visible){
        SettingsMenuInteraction baseFragment = getBaseFragment();
        if (baseFragment!=null)
            baseFragment.setBackVisible(visible);
    }

    /**
     * Check is animation disabled it on fragment revert
     * @return true if animation should be disabled
     */
    protected boolean isDisableAnimation() {
        SettingsMenuInteraction baseFragment = getBaseFragment();
        return baseFragment != null && baseFragment.isDisableAnimation();
    }
}
