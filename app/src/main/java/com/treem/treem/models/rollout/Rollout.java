package com.treem.treem.models.rollout;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Date: 5/18/16.
 */
public class Rollout {
    public static final Type LIST_TYPE = new TypeToken<ArrayList<Rollout>>() {}.getType();

    @SerializedName("user_id")
	private long userId;

	@SerializedName("points")
	private double points;

	@SerializedName("change_today")
	private double changeToday;

	@SerializedName("percentile")
	private double percentile;

	@SerializedName("earns_equity")
	private boolean earnsEquity;

    @SerializedName("user_first_last")
    private String userName;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public double getPoints() {
		return points;
	}

	public void setPoints(double points) {
		this.points = points;
	}

	public double getChangeToday() {
		return changeToday;
	}

	public void setChangeToday(double changeToday) {
		this.changeToday = changeToday;
	}

	public double getPercentile() {
		return percentile;
	}

	public void setPercentile(double percentile) {
		this.percentile = percentile;
	}

	public boolean isEarnsEquity() {
		return earnsEquity;
	}

	public void setEarnsEquity(boolean earnsEquity) {
		this.earnsEquity = earnsEquity;
	}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public int gerPercent(){
        return Math.round((float)percentile*100);
    }
}
