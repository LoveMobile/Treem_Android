package com.treem.treem.activities.main;

import com.treem.treem.helpers.BaseFragment;

/**
 * Base fragment for ViewPager fragments
 */
public abstract class PageBaseFragment extends BaseFragment {
    private boolean isPagePrimary = false;
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        setFragmentPrimary(menuVisible);
    }

    /**
     * Call this function when page with fragment selected or go away
     * @param isPrimary true if page selected or false otherwise
     */
    protected void setFragmentPrimary(@SuppressWarnings("UnusedParameters") boolean isPrimary){
        isPagePrimary = isPrimary;
    }

    /**
     * Return true if fragment is primary
     * @return true if fragment primary
     */
    public boolean isFragmentPrimary(){
        return isPagePrimary;
    }
}
