package com.treem.treem.services.Treem;

import com.github.scribejava.core.model.Verb;
import com.google.gson.Gson;
import com.treem.treem.models.profile.UserTreeSettings;
import com.treem.treem.models.session.TreeSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Profile network service
 */
public class TreemProfileService {
    //Path for users tree settings api
    private static final String pathUserTreeSettings = "user-tree-settings";

    /**
     * Get user tree settings (push notifications)
     * @param treeSession current tree session
     * @param request treem request
     */
    public static void getUserTreeSetting(TreemServiceRequest request,TreeSession treeSession) {
        request.url = TreemService.PROFILE_SERVICE_URL + pathUserTreeSettings;
        request.method = Verb.GET;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    /**
     * Set user's tree settings
     * @param treeSession session for current tree
     * @param request treem request
     * @param settings new settings
     */
    public static void setUserTreeSettings(TreemServiceRequest request,TreeSession treeSession, UserTreeSettings settings) {
        request.url = TreemService.PROFILE_SERVICE_URL + pathUserTreeSettings;
        request.method = Verb.POST;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

        // serialize the branch object to a string then to a json object
        try {
            request.bodyData = new JSONObject(new Gson().toJson(settings, UserTreeSettings.class));
        } catch (JSONException e) {
            request.bodyData = null;
        }


        TreemService.SHARED_INSTANCE.performRequest(request);

    }

    /**
     * Get profile of current user
     * @param request treem request
     * @param treeSession tree session to get profile
     */
    public static void getCurrentUserProfile(TreemServiceRequest request,TreeSession treeSession){
        getUserProfile(request,null,treeSession);
    }

    /**
     * Get profile for selected use
     * @param request treem request
     * @param userId user id (null for current user)
     * @param treeSession tree session
     */
    public static void getUserProfile(TreemServiceRequest request,Long userId,TreeSession treeSession){
        request.url = TreemService.PROFILE_SERVICE_URL + (userId!=null?Long.toString(userId):"");
        request.method = Verb.GET;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    /**
     * Update profile at the server
     * @param request treem request
     * @param changes the map with changes. Keys are model fields names
     * @param treeSession tree session
     */
    public static void setCurrentUserProfile(TreemServiceRequest request, Map<String,Object> changes,TreeSession treeSession) {
        if (changes==null)
            return;
        request.url = TreemService.PROFILE_SERVICE_URL;
        request.method = Verb.POST;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();


        /*
         * create a json object with updated fields
         */
        JSONObject body = new JSONObject();
        try {
            for (String field:changes.keySet()){
                body.put(field,changes.get(field));
            }
        } catch (JSONException e) {
            request.bodyData = null;
        }
        request.bodyData = body;
        TreemService.SHARED_INSTANCE.performRequest(request);

    }
}
