package com.treem.treem.services.Treem;

import com.github.scribejava.core.model.Verb;
import com.treem.treem.helpers.TimestampUtils;
import com.treem.treem.models.session.TreeSession;

import java.util.Date;
import java.util.HashMap;

/**
 * Feed network service
 */
public class TreemFeedService {
    //Path for media content
    private static final String pathContent = "content";
    private static final String pathPosts = "posts";

    public static void getMediaItems(
            TreemServiceRequest request,
            long userId, Integer page,
            Integer pageSize,
            TreeSession treeSession) {
        request.url = TreemService.FEED_SERVICE_URL + pathContent + (userId > 0 ? "/" + userId : "");
        request.method = Verb.GET;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        request.searchOptions = new HashMap<>();
        if (page != null && page > 0) {
            request.searchOptions.put(TreemService.paginationPage, Integer.toString(page));
        }
        if (pageSize != null && pageSize > 0) {
            request.searchOptions.put(TreemService.paginationPageSize, Integer.toString(pageSize));
        }
        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    public static TreemService.NetworkRequestTask getPosts(
            TreemServiceRequest request,
            long branchId,
            long timestamp,
            Integer page,
            Integer pageSize,
            TreeSession treeSession) {
        request.url = TreemService.FEED_SERVICE_URL + pathPosts + (branchId> 0 ? "/" + branchId: "");
        request.method = Verb.GET;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        request.searchOptions = new HashMap<>();
        if (page != null && page > 0) {
            request.searchOptions.put(TreemService.paginationPage, Integer.toString(page));
        }
        if (pageSize != null && pageSize > 0) {
            request.searchOptions.put(TreemService.paginationPageSize, Integer.toString(pageSize));
        }

        request.searchOptions.put(TreemService.paramFDate, TimestampUtils.getISO8601StringForDate(new Date(timestamp)));

        return TreemService.SHARED_INSTANCE.performRequest(request);
    }

}
