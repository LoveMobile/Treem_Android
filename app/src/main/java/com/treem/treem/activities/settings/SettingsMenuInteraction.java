package com.treem.treem.activities.settings;

/**
 * Settings menu interaction events
 */
public interface SettingsMenuInteraction {
    /**
     * Settings menu item selected
     * @param itemId the id of selected menu
     */
    void menuSelected(int itemId);

    /**
     * Set toolbar title
     * @param titleId string id of title
     */
    void setTitle(int titleId);

    /**
     * Show or hide the toolbar back button
     * @param visible is back button visible
     */
    void setBackVisible(boolean visible);

    /**
     * Is this revert fragment and we need to disable animation
     * @return true if animation should be disabled
     */
    boolean isDisableAnimation();
}
