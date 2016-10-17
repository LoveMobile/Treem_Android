package com.treem.treem.helpers;

import com.treem.treem.models.branch.Branch;

public class Stub {
    public static Branch getBranch(){
        Branch branch = new Branch();
        branch.name = "Test";
        branch.color = String.format("#%02x%02x%02x",(int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
        branch.position = 4;
        branch.parent = null;
        return branch;
    }

    public static Branch getPlacementBranch(long parentId, int position) {
        Branch branch = new Branch();
        branch.position = position;
        branch.name = "Test";
        branch.color = String.format("#%02x%02x%02x",(int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
        branch.parent = new Branch();
        branch.id = parentId;
        return branch;
    }
}
