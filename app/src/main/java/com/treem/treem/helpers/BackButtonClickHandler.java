package com.treem.treem.helpers;

/**
 * Interface for fragments that wants to handle back button
 */
public interface BackButtonClickHandler {
    /**
     * Inform listener about back button clicked
     * @return true if back button click was handled by listener ot false otherwise
     */
    boolean backButtonHandle();
}
