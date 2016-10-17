package com.treem.treem.activities.tree;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.treem.treem.R;

public class TreeViewActivity extends Activity {
    private static final String CLASSNAME = TreeViewActivity.class.getName();
    private static final String EXTRA_MODE = "extra.mode";
    private static final int MODE_ADD_USER_SELECT_BRANCH = 1;
    private static final int MODE_BRANCH_PLACE_SELECT = 2;

    public static final String EXTRA_BRANCH_ID = CLASSNAME + ".BRANCH_ID";
    public static final String EXTRA_POSITION = CLASSNAME + ".POSITION";
    public static final String EXTRA_PARENT_BRANCH_ID = CLASSNAME + ".PARENT_BRANCH_ID";

    private EditText editBranchId;
    private EditText editParentBranchId;
    private EditText editPosition;
    private Button buttonSelect;
    private int mode = MODE_ADD_USER_SELECT_BRANCH;

    public static void showAddUserSelectBranch(Activity activity, int requestCode) {
        if (activity == null)
            return;
        Intent intent = new Intent(activity, TreeViewActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_ADD_USER_SELECT_BRANCH);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void showAddUserSelectBranch(Fragment fragment, int requestCode) {
        if (fragment == null)
            return;
        Context context = fragment.getContext();
        if (context == null)
            return;
        Intent intent = new Intent(context, TreeViewActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_ADD_USER_SELECT_BRANCH);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void showBranchPlaceSelect(Fragment fragment, int requestCode) {
        if (fragment == null)
            return;
        Context context = fragment.getContext();
        if (context == null)
            return;
        Intent intent = new Intent(context, TreeViewActivity.class);
        intent.putExtra(EXTRA_MODE, MODE_BRANCH_PLACE_SELECT);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_view);
        parseIntent(getIntent());
        handleViews();
        updateViewsVisible();
    }

    private void updateViewsVisible() {
        switch (mode) {
            case MODE_ADD_USER_SELECT_BRANCH:
                editBranchId.setVisibility(View.VISIBLE);
                editPosition.setVisibility(View.GONE);
                editParentBranchId.setVisibility(View.GONE);
                break;
            case MODE_BRANCH_PLACE_SELECT:
                editBranchId.setVisibility(View.GONE);
                editPosition.setVisibility(View.VISIBLE);
                editParentBranchId.setVisibility(View.VISIBLE);
                break;

        }
    }

    private void handleViews() {
        editBranchId = (EditText) findViewById(R.id.branchId);
        editParentBranchId = (EditText) findViewById(R.id.parent_branch_id);
        editPosition = (EditText) findViewById(R.id.position);
        buttonSelect = (Button) findViewById(R.id.buttonSelect);
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSelection();
            }
        });
    }

    private void submitSelection() {
        switch (mode) {
            case MODE_ADD_USER_SELECT_BRANCH: {
                String branchId = editBranchId.getText().toString();
                if (!TextUtils.isEmpty(branchId)) {
                    try {
                        long id = Long.parseLong(branchId);
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_BRANCH_ID, id);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        return;
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
            break;
            case MODE_BRANCH_PLACE_SELECT: {
                String position = editPosition.getText().toString();
                Intent intent = new Intent();
                if (!TextUtils.isEmpty(position)) {
                    try {
                        int id = Integer.parseInt(position);
                        intent.putExtra(EXTRA_POSITION, id);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                String parentId = editParentBranchId.getText().toString();
                if (!TextUtils.isEmpty(parentId)) {
                    try {
                        long parentBranchId = Long.parseLong(parentId);
                        intent.putExtra(EXTRA_PARENT_BRANCH_ID, parentBranchId);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                setResult(Activity.RESULT_OK, intent);
                finish();

            }

        }
    }

    private void parseIntent(Intent intent) {
        if (intent == null)
            return;
        mode = intent.getIntExtra(EXTRA_MODE, MODE_ADD_USER_SELECT_BRANCH);
    }

}
