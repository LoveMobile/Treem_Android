package com.treem.treem.activities.signup.phone;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;

import com.treem.treem.helpers.BaseFragment;

/**
 * Base fragment for phone confirmation activity
 */
public abstract class PhoneBaseFragment extends BaseFragment {

    /**
     * Interface for interacting fragments with activity
     */
    public interface OnPhoneFragmentInteractionListener {
        /**
         * Is activity in edit mode or in signup
         * @return true if it is in edit mode
         */
        boolean isEditMode();

        /**
         * Get old phone
         * @return old phone or null if activity in signup mode
         */
        String getOldPhone();

        /**
         * Get new entered phone
         * @return new entered phone or null if new phone was not set
         */
        String getNewPhone();

        /**
         * Send check phone number request
         * @param phone phone number
         */
        void checkPhoneNumber(String phone);

        /**
         * Go back one screen or close the activity
         */
        void onBackPressed();

        /**
         * verify sent code
         * @param code the sent sms code
         */
        void verifyUserDevice(String code);

        /**
         * Request to resend verification code
         * @param buttonResend reference to send button to disable it during send
         * @param textStatus reference to text view to set last requested time
         */
        void resendVerificationCode(Button buttonResend, TextView textStatus);

        /**
         * Finish activity
         */
        void finish();
    }

    /**
     * Reference to parent activity
     */
    protected OnPhoneFragmentInteractionListener mListener;

    /**
     * Store old phone
     */
    protected String oldPhone;
    /**
     * Store new phone
     */
    protected String newPhone;

    /**
     * True if activity in edit mode
     */
    protected boolean isEditMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * Get init values from activity
         */
        if (mListener!=null){
            isEditMode = mListener.isEditMode();
            if (isEditMode)
                oldPhone = mListener.getOldPhone();
            newPhone = mListener.getNewPhone();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPhoneFragmentInteractionListener) {
            mListener = (OnPhoneFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPhoneFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Check entered phone number
     * @param phone the phone number
     */
    protected void checkPhoneNumber(String phone) {
        if (mListener!=null)
            mListener.checkPhoneNumber(phone);
    }

    /**
     * Go back on one screen
     */
    protected void back(){
        if (mListener!=null)
            mListener.onBackPressed();
    }

    /**
     * Verify user device
     * @param code verification code
     */
    protected void verifyUserDevice(String code) {
        if (mListener!=null)
            mListener.verifyUserDevice(code);
    }

    /**
     * Request resend verification code
     * @param resendCode button for resend to diable it during request
     * @param textStatus status to show the result
     */
    protected void resendVerificationCode(Button resendCode, TextView textStatus) {
        if (mListener!=null)
            mListener.resendVerificationCode(resendCode,textStatus);
    }

    /**
     * Finish activity
     */
    protected void finish(){
        if (mListener!=null)
            mListener.finish();
    }
}
