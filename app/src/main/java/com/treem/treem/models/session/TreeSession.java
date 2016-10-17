package com.treem.treem.models.session;

import com.treem.treem.models.branch.Branch;

import java.util.HashMap;

/**
 * Created by Dan on 4/7/16.
 */
public class TreeSession {
    public Integer treeID                   = 0;
    public String token                     = null;
    public Branch currentBranch             = null;
    public Long currentBranchID             = 0L;

    private static String keyTreeSessionID  = "treem-sid";

    public TreeSession() { }
    public TreeSession(Integer treeID, String token){
        this.treeID = treeID;
        this.token = token;
    }

    public HashMap<String,String> getTreemServiceHeader() {
        HashMap<String,String> header = null;

        if(this.treeID != null && this.treeID > 0){
            header = new HashMap<String,String>();
            String headerVal = "tree_id=" + this.treeID.toString();

            if(this.token != null && !this.token.isEmpty())
                headerVal += ",token=" + this.token;

            header.put(this.keyTreeSessionID, headerVal);

        }

        return header;
    }

    /**
     * Get tree session for main tree
     * @return tree session for a main tree with id==1
     */
    /*public static TreeSession getMainTreeSession(){
        return new TreeSession(1, null);
    }
    */
    /*

    // get custom Tree http header for the services that need it
    func getTreemServiceHeader() -> [String : String]? {
        var header: [String : String]? = nil

        if self.treeID > 0 {
            header = [self.keyTreeSessionID : "tree_id=\(self.treeID)"]

            if let token = self.token where !token.isEmpty {
                header![self.keyTreeSessionID]! += ",token=" + token
            }
        }

        return header
    }
     */
}
