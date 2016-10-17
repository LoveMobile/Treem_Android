package com.treem.treem.services.Treem;

import android.text.TextUtils;

import com.github.scribejava.core.model.Verb;
import com.google.gson.Gson;
import com.treem.treem.activities.branch.members.MembersSearchConfig;
import com.treem.treem.models.session.TreeSession;
import com.treem.treem.models.user.User;
import com.treem.treem.models.user.UserAdd;
import com.treem.treem.models.user.UserContact;
import com.treem.treem.models.user.UserRemove;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreemSeedingService {

    private static final String PATH_DECLINE = "trim/";
    private static final String PATH_DECLINE_SET = "cut";
    private static final String PATH_TRUNK = "trunk";
    private static final String PATH_SEARCH = "search";
    private static final String SEARCH_PARAM_SEARCH = "search";
    private static final String SEARCH_PARAM_STATUS = "status";
    private static final String MATCHING_FIRST = "first";
    private static final String MATCHING_LAST = "last";
    private static final String MATCHING_PHONE = "phone";
    private static final String MATCHING_EMAIL = "email";
    private static final String MATCHING_USERNAME = "username";
    private static final String SEARCH_PARAM_MATCHING = "options";
    private static final String SEARCH_PARAM_BRANCHONLY = "branchOnly";


    /**
     * Decline friends requests
     * @param treeSession tree session token
     * @param users the set of users
     * @param request service request
     * @return network task
     */
    public static TreemService.NetworkRequestTask trimUsers(TreeSession treeSession, Set<UserRemove> users, long branchId, TreemServiceRequest request){
        request.url = TreemService.SEEDING_SERVICE_URL+(branchId>0?PATH_DECLINE+Long.toString(branchId):PATH_DECLINE_SET);
        request.method = Verb.DELETE;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

        try {
            request.bodyData = new JSONArray(new Gson().toJson(users, UserRemove.SET_TYPE));
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return TreemService.SHARED_INSTANCE.performRequest(request);
    }
    public static TreemService.NetworkRequestTask setUsers(TreeSession treeSession, Set<UserAdd> users, long branchId, TreemServiceRequest request){
        request.url = TreemService.SEEDING_SERVICE_URL+(branchId>0?Long.toString(branchId):PATH_TRUNK);
        request.method = Verb.POST;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

        try {
            request.bodyData = new JSONArray(new Gson().toJson(users, UserAdd.SET_TYPE));
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return TreemService.SHARED_INSTANCE.performRequest(request);
    }
    private static class SearchUserBody{
        public List<UserContact> contact_list=new ArrayList<>();
        public List<Integer> priority_list=new ArrayList<>();
        public SearchUserBody(List<UserContact> contacts,List<Integer> priority){
            contact_list = contacts;
            priority_list = priority;
        }

        public SearchUserBody() {

        }
    }
    public static TreemService.NetworkRequestTask searchUsers(
            TreemServiceRequest request,
            Long branchId,
            String searchString,
            MembersSearchConfig searchConfig,
            List<UserContact> contacts,
            List<Integer> priority,
            int currentPage,
            int pageSize,
            TreeSession treeSession) {

        request.url = TreemService.SEEDING_SERVICE_URL+PATH_SEARCH+(branchId>0?"/"+Long.toString(branchId):"");
        request.method = Verb.POST;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        SearchUserBody body;
        if (searchConfig!=null&&searchConfig.getMiscellaneous().isShowContactsSet()&&contacts!=null) {
            body = new SearchUserBody(contacts, priority);
        }
        else{
            body = new SearchUserBody();
        }
        try {
            request.bodyData = new JSONObject(new Gson().toJson(body, SearchUserBody.class));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        request.searchOptions = getSearchOptions(searchString,searchConfig,currentPage,pageSize);

        return TreemService.SHARED_INSTANCE.performRequest(request);
    }

    private static Map<String,String> getSearchOptions(String searchString, MembersSearchConfig searchConfig, int currentPage, int pageSize) {
        Map<String,String> params = new HashMap<>();
        if (!TextUtils.isEmpty(searchString)){
            params.put(SEARCH_PARAM_SEARCH,searchString.trim());
        }
        Set<String> statuses = new HashSet<>();
        if (searchConfig.getRelationship().isFriendsSet()){
            statuses.add(Integer.toString(User.STATUS_FRIENDS));
        }
        if (searchConfig.getRelationship().isInvitedSet()){
            statuses.add(Integer.toString(User.STATUS_INVITED));
        }
        if (searchConfig.getRelationship().isNotFriendsSet()){
            statuses.add(Integer.toString(User.STATUS_NOT_FRIENDS));
        }
        if (searchConfig.getRelationship().isPendingSet()){
            statuses.add(Integer.toString(User.STATUS_PENDING));
        }
        if (searchConfig.getMiscellaneous().isShowContactsSet())
            statuses.add(Integer.toString(User.STATUS_NOT_IN_TRIM));

        if (statuses.size()>0){
            String status = "";
            for (String st:statuses){
                if (status.length()>0)
                    status+=",";
                status+=st;
            }
            params.put(SEARCH_PARAM_STATUS,status);
        }
        Set<String> matching = new HashSet<>();
        if (searchConfig.getMatching().isFirstNameSet())
            matching.add(MATCHING_FIRST);
        if (searchConfig.getMatching().isLastNameSet())
            matching.add(MATCHING_LAST);
        if (searchConfig.getMatching().isEmailSet())
            matching.add(MATCHING_EMAIL);
        if (searchConfig.getMatching().isPhoneSet())
            matching.add(MATCHING_PHONE);
        if (searchConfig.getMatching().isUserNameSet())
            matching.add(MATCHING_USERNAME);
        if (matching.size()>0){
            String matchings = "";
            for (String m:matching){
                if (matchings.length()>0)
                    matchings+=",";
                matchings+=m;
            }
            params.put(SEARCH_PARAM_MATCHING,matchings);
        }
        if (searchConfig.getMiscellaneous().isShowContactsSet())
            params.put(SEARCH_PARAM_BRANCHONLY,"false");
        else
            params.put(SEARCH_PARAM_BRANCHONLY,"true");
        if (currentPage>0) {
            params.put(TreemService.paginationPage, Integer.toString(currentPage));
        }
        if (pageSize>0){
            params.put(TreemService.paginationPageSize,Integer.toString(pageSize));
        }

        return params;
    }
}
