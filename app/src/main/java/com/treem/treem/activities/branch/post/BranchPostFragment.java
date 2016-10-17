package com.treem.treem.activities.branch.post;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.treem.treem.R;
import com.treem.treem.activities.branch.BranchBaseFragment;

/**
 * Post fragment for branch activity
 * //TODO in further TROIDs
 */
public class BranchPostFragment extends BranchBaseFragment {
    private static final String TAG = BranchPostFragment.class.getSimpleName();
    private OnBrenchPostInteractionListener mListener;

    public BranchPostFragment() {
        // Required empty public constructor
    }

    public static BranchPostFragment newInstance() {
        BranchPostFragment fragment = new BranchPostFragment();
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
        return inflater.inflate(R.layout.fragment_branch_post, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBrenchPostInteractionListener) {
            mListener = (OnBrenchPostInteractionListener) context;
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

    public interface OnBrenchPostInteractionListener {
    }
}
