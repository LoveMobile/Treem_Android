package com.treem.treem.services.Treem;

import com.github.scribejava.core.model.Verb;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.treem.treem.models.alert.Alert;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.models.session.TreeSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Created by Dan on 4/7/16.
 */
public class TreemBranchService {
	private static final String PATH_SHARE = "share/";
	private static final String PATH_DECLINE = PATH_SHARE + "decline";

	public static void getUserBranches(TreeSession treeSession, TreemServiceRequest request, Long parentBranchID) {
		request.url = TreemService.BRANCH_SERVICE_URL;
		request.method = Verb.GET;
		request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

		if (parentBranchID != null && parentBranchID > 0)
			request.url += parentBranchID.toString();

		// add the fields parameters
		//request.url += "?fields=id,name,color,position,icon,url,ex_type,children";

		TreemService.SHARED_INSTANCE.performRequest(request);
	}

	public static TreemService.NetworkRequestTask setUserBranch(TreeSession treeSession, TreemServiceRequest request, Branch branch) {
		request.url = TreemService.BRANCH_SERVICE_URL;
		request.method = Verb.POST;
		request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

		// make sure the branch object was passed...
		if (branch != null) {

			if (branch.id != null && branch.id > 0)
				request.url += branch.id.toString();

			// serialize the branch object to a string then to a json object
			try {
				GsonBuilder gsonBuilder = new GsonBuilder();
				gsonBuilder.addSerializationExclusionStrategy(new ExclusionStrategy() {

					@Override
					public boolean shouldSkipField(FieldAttributes f) {
						return f.getName().equals("parent");
					}

					@Override
					public boolean shouldSkipClass(Class<?> clazz) {
						return false;
					}
				});

				request.bodyData = new JSONObject(gsonBuilder.create().toJson(branch));
			} catch (JSONException e) {
				request.bodyData = null;
			}

			return TreemService.SHARED_INSTANCE.performRequest(request);

		}
		return null;
	}

	/**
	 * Remove branch
	 *
	 * @param treeSession tree session token
	 * @param request     service request
	 * @param branch      branch which will be removed
	 */
	public static void deleteUserBranch(TreeSession treeSession, TreemServiceRequest request, Branch branch) {
		request.url = TreemService.BRANCH_SERVICE_URL + branch.id.toString();
		request.method = Verb.DELETE;
		request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

		TreemService.SHARED_INSTANCE.performRequest(request);
	}

	/**
	 * Decline share requests
	 *
	 * @param treeSession tree sesion token
	 * @param alerts      the set of alerts
	 * @param request     service request
	 * @return network task
	 */
	public static TreemService.NetworkRequestTask declineShare(TreeSession treeSession, Set<Alert.AlertJson> alerts, TreemServiceRequest request) {
		request.url = TreemService.BRANCH_SERVICE_URL + PATH_DECLINE;
		request.method = Verb.POST;
		request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

		try {
			request.bodyData = new JSONArray(new Gson().toJson(alerts, Alert.AlertJson.SET_TYPE));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return TreemService.SHARED_INSTANCE.performRequest(request);
	}

	/**
	 * Accept share requests
	 *
	 * @param treeSession tree sesion token
	 * @param alert       the alert
	 * @param placement   placement for the new branch
	 * @param request     service request
	 * @return network task
	 */
	public static TreemService.NetworkRequestTask acceptShare(TreeSession treeSession, Alert alert, Branch placement, TreemServiceRequest request) {
		request.url = TreemService.BRANCH_SERVICE_URL + PATH_SHARE + alert.getSourceId();
		request.method = Verb.POST;
		request.HTTPCustomHeaders = treeSession.getTreemServiceHeader();

		if (placement != null)
			request.bodyData = placement.getPlacement();

		return TreemService.SHARED_INSTANCE.performRequest(request);
	}

}
