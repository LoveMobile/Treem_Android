package com.treem.treem.activities.branch.chat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.treem.treem.R;
import com.treem.treem.activities.branch.BranchBaseFragment;

/**
 * Chat fragment for branch activity
 * //TODO in further TROIDs
 */
public class BranchChatFragment extends BranchBaseFragment {
    private static final String TAG = BranchChatFragment.class.getSimpleName();
    private OnBranchChatInteractionListener mListener;

    public BranchChatFragment() {
        // Required empty public constructor
    }

    public static BranchChatFragment newInstance() {
        BranchChatFragment fragment = new BranchChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_branch_chat, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBranchChatInteractionListener) {
            mListener = (OnBranchChatInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBranchFeedInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnBranchChatInteractionListener {
    }
}
