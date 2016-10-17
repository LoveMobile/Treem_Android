package com.treem.treem.services.Treem;

import com.github.scribejava.core.model.Verb;
import com.google.gson.Gson;
import com.treem.treem.models.alert.Alert;
import com.treem.treem.models.session.TreeSession;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Set;

/**
 * Date: 5/17/16.
 */
public class TreemAlertService {

	private static final String PATH_ALERTS_COUNT = "count/";
    private static final String PATH_ALERTS = "alert/";
    private static final String PATH_VIEWED = "viewed/";
    private static final String PATH_CLEAR = "clear/";

    public static void getAlertsCount(TreemServiceRequest request, TreeSession treeSession) {
		request.url = TreemService.ALERTS_SERVICE_URL + PATH_ALERTS_COUNT;
		request.method = Verb.GET;
		request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

		TreemService.SHARED_INSTANCE.performRequest(request);
	}

    /**
     * Get alerts
     * @param request request
     * @param reason reason
     * @param page page number >=1
     * @param pageSize page size
     * @param treeSession session
     * @return
     */
    public static TreemService.NetworkRequestTask getAlerts(
            TreemServiceRequest request,
            String reason,
            Integer page,
            Integer pageSize,
            TreeSession treeSession){
        request.url = TreemService.ALERTS_SERVICE_URL+PATH_ALERTS;
        request.method = Verb.GET;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();
        request.searchOptions = new HashMap<>();
        request.searchOptions.put(TreemService.searchReason,reason);

        if (page!=null&&page>0) {
            request.searchOptions.put(TreemService.paginationPage, Integer.toString(page));
        }
        if (pageSize!=null&&pageSize>0){
            request.searchOptions.put(TreemService.paginationPageSize,Integer.toString(pageSize));
        }

        return TreemService.SHARED_INSTANCE.performRequest(request);
    }

    public static TreemService.NetworkRequestTask setAlertsRead(
            TreemServiceRequest request,
            Set<Alert.AlertJson> alerts,
            TreeSession treeSession){
        request.url = TreemService.ALERTS_SERVICE_URL+PATH_VIEWED;
        request.method = Verb.POST;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

        try {
            request.bodyData = new JSONArray(new Gson().toJson(alerts, Alert.AlertJson.SET_TYPE));
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return TreemService.SHARED_INSTANCE.performRequest(request);
    }
    public static TreemService.NetworkRequestTask clearAlerts(
            TreemServiceRequest request,
            Set<Alert.AlertJson> alerts,
            TreeSession treeSession){
        request.url = TreemService.ALERTS_SERVICE_URL+PATH_CLEAR;
        request.method = Verb.DELETE;
        request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

        try {
            request.bodyData = new JSONArray(new Gson().toJson(alerts, Alert.AlertJson.SET_TYPE));
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return TreemService.SHARED_INSTANCE.performRequest(request);
    }

}
