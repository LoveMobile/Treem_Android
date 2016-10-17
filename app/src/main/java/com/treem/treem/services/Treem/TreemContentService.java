package com.treem.treem.services.Treem;

import com.github.scribejava.core.model.Verb;
import com.google.gson.Gson;
import com.treem.treem.models.content.UploadItem;
import com.treem.treem.models.session.TreeSession;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Content network service
 */
public class TreemContentService {
    //Path for users tree settings api
    private static final String pathUploadSingleItem = "upload";
    private static final String pathRemoveImage="removeimg/";
    private static final String pathVideo = "video/";

    /**
     * Upload single image
     * @param item data for upload image
     * @param request treem request
     */
    public static void uploadSingleImage(TreemServiceRequest request, UploadItem item,TreeSession treeSession) {
        request.url = TreemService.CONTENT_SERVICE_URL + pathUploadSingleItem;
        request.method = Verb.POST;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        try {
            request.bodyData = new JSONObject(new Gson().toJson(item, UploadItem.class));
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    public static void deleteImage(TreemServiceRequest request, long imageId, TreeSession treeSession) {
        request.url = TreemService.CONTENT_SERVICE_URL + pathRemoveImage+imageId;
        request.method = Verb.DELETE;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    /**
     * Load video details
     * @param request treem request
     * @param contentId video id
     * @param treeSession tree session
     */
    public static void getVideoDetails(TreemServiceRequest request,long contentId,TreeSession treeSession){
        request.url = TreemService.CONTENT_SERVICE_URL + pathVideo+contentId;
        request.method = Verb.GET;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        TreemService.SHARED_INSTANCE.performRequest(request);
    }
}
