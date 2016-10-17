package com.treem.treem.application;

import android.content.Context;

import com.treem.treem.models.session.TreeSession;

/**
 * Created by Dan on 4/7/16.
 */
public class CurrentTreeSettings {

    public enum TreeType {
        Main(1),
        Secret(2),
        Public(3);

        private final int value;
        TreeType(int value) { this.value = value; }
        public int getIntValue() { return value; }
    }

    private static Context context;

    /* Static application settings */
    public final static CurrentTreeSettings SHARED_INSTANCE     = new CurrentTreeSettings();

    public final static Integer mainTreeID      = 1;
    public final static Integer secretTreeID    = 2;
    public final static Integer publicTreeID    = 3;

    // default is initial tree
    public TreeSession treeSession = new TreeSession(1, null);
    public TreeSession secretTreeSession = new TreeSession(secretTreeID, null);

    public Long getCurrentBranchID(){
        Long curId = treeSession.currentBranchID;
        return ((curId == null || curId < 0) ? 0 : curId);
    }

    public TreeType getCurrentTree(){
        Integer curTreeId = treeSession.treeID;
        if(curTreeId != null && curTreeId < TreeType.values().length)
            return TreeType.values()[curTreeId];
        else return null;
    }
}


