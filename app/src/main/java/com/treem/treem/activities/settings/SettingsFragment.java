package com.treem.treem.activities.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.treem.treem.R;
import com.treem.treem.activities.main.OtherToolbar;
import com.treem.treem.activities.main.PageBaseFragment;
import com.treem.treem.helpers.BackButtonClickHandler;


/**
 * Settings fragment
 * Created by Dan on 4/6/16.
 */
public class SettingsFragment extends PageBaseFragment implements
        SettingsMenuInteraction, //handle menu interaction events
        OtherToolbar.OnBackClickListener, // handle toolbar back button click events
        BackButtonClickHandler //handle back button click events
    {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    //fragment manager
    private FragmentManager fragmentManager;

    //Settings toolbar views handler
    private OtherToolbar toolbar;

    //Reference to base activity
    private OnSettingsFragmentInteractionListener mListener;
    private boolean isDisableAnimation = false;

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getChildFragmentManager(); //get child fragment manager
        if (mListener!=null)
            toolbar = mListener.getOtherToolbar(); //get settings toolbar from base activity
        if (toolbar !=null)
            toolbar.setBackListener(this); //set toolbar back button listener to this object
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState==null)
            toMenu();
    }

    /**
     * Open menu fragment
     */
    private void toMenu() {
        isDisableAnimation = true;
        Fragment oldFragment = revertFragment(SettingsMenuFragment.TAG);
        if (oldFragment==null) {
            SettingsMenuFragment f = new SettingsMenuFragment();
            switchFragment(f, true);
        }
    }

    /**
     * Switch settings fragment
     * @param f new fragment
     * @param isMainFragment is this a main fragment (for fragment animation)
     */
    private void switchFragment(SettingsBaseFragment f,boolean isMainFragment){
        isDisableAnimation = false;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        //set custom animation
        ft.setCustomAnimations(isMainFragment?0:R.anim.enter,R.anim.exit,R.anim.pop_enter,isMainFragment?0:R.anim.pop_exit);

        ft.addToBackStack(f.getFragmentTag());
        ft.replace(R.id.containerSettings,f,f.getFragmentTag());
        //set target fragment to this object to handle menu interaction events by this fragment
        f.setTargetFragment(this, 0);

        ft.commit();
    }

    /**
     * Handle menu selection item events
     * @param itemId id of selected menu
     */
    @Override
    public void menuSelected(int itemId) {
        switch (itemId){
            case R.string.settings_profile:
                toSettingsProfile();
                break;
            case R.string.settings_tree:
                toSettingsTree();
                break;
            case R.string.settings_help:
                toSettingsHelp();
                break;
            case R.string.settings_logout:
                askLogout();
                break;
        }
    }

    /**
     * Set toolbar title
     * @param titleId title string id
     */
    @Override
    public void setTitle(int titleId) {
        if (toolbar !=null)
            toolbar.setTitle(titleId);
    }


    /**
     * Set toolbar back button visible
     * @param visible is visible?
     */
    @Override
    public void setBackVisible(boolean visible) {
        if (toolbar !=null)
            toolbar.setBackVisible(visible);
    }

    /**
     * Check is animation should be disabled to prevent animation on revert previous fragment
     * @return true if animation must be disabled
     */
    @Override
    public boolean isDisableAnimation() {
        return isDisableAnimation;
    }

    /**
     * Ask about logout
     */
    private void askLogout() {
        if (mListener!=null)
            mListener.askLogout();
    }

    /**
     * Open settings help fragment
     */
    private void toSettingsHelp() {
        SettingsHelpFragment f = new SettingsHelpFragment();
        switchFragment(f,false);
    }

    /**
     * Open settings tree fragment
     */
    private void toSettingsTree() {
        SettingsTreeFragment f = new SettingsTreeFragment();
        switchFragment(f,false);
    }

    /**
     * Open settings profile fragment
     */
    private void toSettingsProfile() {
        SettingsProfileFragment f = new SettingsProfileFragment();
        switchFragment(f,false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsFragmentInteractionListener) {
            mListener = (OnSettingsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * handle toolbar back button click
     */
    @Override
    public void onBackClick() {
        fragmentManager.popBackStack(); //pop fragment from back stack
    }

    /**
     * Is back button handled by this fragment
     * @return true if handled
     */
    @Override
    public boolean backButtonHandle() {
        if (isFragmentPrimary() //true only if current fragment selected at the view pager
                &&fragmentManager.getBackStackEntryCount()>1){ //if fragment more then 1 at stack
            onBackClick(); //remove fragment from stack
            return true; //we handled the back button
        }
        return false; //we did not handle the back button
    }

    public interface OnSettingsFragmentInteractionListener{
        /**
         * Get settings toolbar view handler
         * @return settings toolbar handler
         */
        OtherToolbar getOtherToolbar();

        /**
         * Ask user about logout
         */
        void askLogout();
    }

    /**
     * Revert previously created fragment if exists
     * @param tag fragment tag
     * @return fragment if it exists
     */
    private Fragment revertFragment(String tag) {
        Fragment fr = fragmentManager.findFragmentByTag(tag);
        fragmentManager.popBackStackImmediate(tag, 0);
        return fr;
    }

    /**
     * Revert menu fragment when page is switching
     * @param isPrimary true if view pager shows the fragment or false if hide
     */
    @Override
    protected void setFragmentPrimary(@SuppressWarnings("UnusedParameters") boolean isPrimary) {
        super.setFragmentPrimary(isPrimary);
        if (!isPrimary&&isAdded()){
            revertFragment(SettingsMenuFragment.TAG); //revert menu fragment on parent fragment hide
        }
    }

}
