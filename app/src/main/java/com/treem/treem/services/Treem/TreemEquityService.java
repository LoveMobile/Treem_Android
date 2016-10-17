package com.treem.treem.services.Treem;

import com.github.scribejava.core.model.Verb;
import com.treem.treem.models.session.TreeSession;

import java.util.HashMap;

/**
 * Equity network service
 */
public class TreemEquityService {
    //Path for users tree settings api
    private static final String pathTopFriends = "friends";
    private static final String pathRollout="rollout";
    private static final String pathUserOverTime="history";

    /**
     * get top friends
     * @param request treem request
     * @param limit limit of users
     * @param treeSession session
     */
    public static void getTopFriends(TreemServiceRequest request, Integer limit,TreeSession treeSession) {
        request.url = TreemService.EQUITY_SERVICE_URL + pathTopFriends;
        request.method = Verb.GET;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        if (limit!=null) {
            request.searchOptions = new HashMap<>();
            request.searchOptions.put(TreemService.searchOptionLimit, limit.toString());
        }
        TreemService.SHARED_INSTANCE.performRequest(request);
    }
    /**
     * get user rollout
     * @param request treem request
     * @param treeSession session
     */
    public static void getUserRollout(TreemServiceRequest request, TreeSession treeSession) {
        request.url = TreemService.EQUITY_SERVICE_URL + pathRollout;
        request.method = Verb.GET;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        TreemService.SHARED_INSTANCE.performRequest(request);
    }

    /**
     * get historical data
     * @param request treem request
     * @param scale result scale (day,week,month)
     * @param startDate start date of data request
     * @param endDate end date of data request
     * @param treeSession session
     */
    public static TreemService.NetworkRequestTask getHistoricalData(
            TreemServiceRequest request,
            String scale,
            String startDate,
            String endDate,
            TreeSession treeSession) {

        request.url = TreemService.EQUITY_SERVICE_URL + pathUserOverTime;
        request.method = Verb.GET;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        request.searchOptions = new HashMap<>();
        if (scale!=null)
            request.searchOptions.put(TreemService.searchScale, scale);
        if (startDate!=null)
            request.searchOptions.put(TreemService.searchStartDate, startDate);
        if (endDate!=null)
            request.searchOptions.put(TreemService.searchEndDate, endDate);
        return TreemService.SHARED_INSTANCE.performRequest(request);
    }
}
