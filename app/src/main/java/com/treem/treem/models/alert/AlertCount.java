package com.treem.treem.models.alert;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Date: 5/18/16.
 */
public class AlertCount {

	@SerializedName("total")
	private int totalAlerts;

	@SerializedName("unread")
	private int unreadAlerts;

	@SerializedName("total_nonrequest")
	private int totalNonRequestAlerts;

	@SerializedName("unread_nonrequest")
	private int unreadNonRequestAlerts;

	@SerializedName("friend")
	private int totalFriendAlerts;

	@SerializedName("friend_unread")
	private int unreadFriendAlerts;

	@SerializedName("branch")
	private int totalBranchAlerts;

	@SerializedName("branch_unread")
	private int unreadBranchAlerts;

	public int getTotalAlerts() {
		return totalAlerts;
	}

	public void setTotalAlerts(int totalAlerts) {
		this.totalAlerts = totalAlerts;
	}

	public int getUnreadAlerts() {
		return unreadAlerts;
	}

	public void setUnreadAlerts(int unreadAlerts) {
		this.unreadAlerts = unreadAlerts;
	}

	public int getTotalNonRequestAlerts() {
		return totalNonRequestAlerts;
	}

	public void setTotalNonRequestAlerts(int totalNonRequestAlerts) {
		this.totalNonRequestAlerts = totalNonRequestAlerts;
	}

	public int getUnreadNonRequestAlerts() {
		return unreadNonRequestAlerts;
	}

	public void setUnreadNonRequestAlerts(int unreadNonRequestAlerts) {
		this.unreadNonRequestAlerts = unreadNonRequestAlerts;
	}

	public int getTotalFriendAlerts() {
		return totalFriendAlerts;
	}

	public void setTotalFriendAlerts(int totalFriendAlerts) {
		this.totalFriendAlerts = totalFriendAlerts;
	}

	public int getUnreadFriendAlerts() {
		return unreadFriendAlerts;
	}

	public void setUnreadFriendAlerts(int unreadFriendAlerts) {
		this.unreadFriendAlerts = unreadFriendAlerts;
	}

	public int getTotalBranchAlerts() {
		return totalBranchAlerts;
	}

	public void setTotalBranchAlerts(int totalBranchAlerts) {
		this.totalBranchAlerts = totalBranchAlerts;
	}

	public int getUnreadBranchAlerts() {
		return unreadBranchAlerts;
	}

	public void setUnreadBranchAlerts(int unreadBranchAlerts) {
		this.unreadBranchAlerts = unreadBranchAlerts;
	}
}
