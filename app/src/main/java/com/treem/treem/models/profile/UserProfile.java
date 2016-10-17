package com.treem.treem.models.profile;

import java.util.Map;

/**
 * User profile class
 */
@SuppressWarnings("unused")
public class UserProfile {

	/**
	 * Some fields names
	 */
	public static final String keyUsername = "username";
	public static final String keyFirstName = "first";
	public static final String keyLastName = "last";
	public static final String keyEmail = "email";
	public static final String keyCity = "r_locality";
	public static final String keyState = "r_province";
	public static final String keyCountry = "r_country";
	public static final String keyBirthday = "dob";
	public static final String keyNonFriendsAccess = "n_fr_access";
    private static final int STATUS_FRIENDS = 2;
    private static final int STATUS_PENDING = 0;
    private static final int LAST_ACTION_MYSELF = 1;


    /**
     * Check is this user friend to current
     * @return true if users are friends
     */
    public boolean isFriend() {
        return status==STATUS_FRIENDS;
    }

    /**
     * Check is user sent or receive friend request from current user
     * @return true if users have pending friend request
     */
    public boolean isPending() {
        return status==STATUS_PENDING;
    }

    /**
     * Check is last status modifications was done by current user
     * @return true if last status modification was done by current user
     */
    public boolean isLastActionMine() {
        return last_action == LAST_ACTION_MYSELF;
    }


    @SuppressWarnings("unused")
	public static class UserBranch {
		public String[] path; // contains the branches names (in order) that the user belongs on (ex: [“Friends”,”CA”,”SD”])
		public String color; // color of the branch path
	}

	@SuppressWarnings("unused")
	public static class UserAttribute {
		@SuppressWarnings("unused")
		public static class Attr {
			public long a_id; // attribute id
			public String name; // attribute name
			public int s_year; // attribute start year
			public int e_year; // attribute end year
		}

		public int a_type; // 0=Education, 1=Work
		public Attr[] attrs;
	}

	public long id; // user_id
	public short self; // indicates if the user who made the request is this user, 1=True, 0 or NULL = False
	public String username;
	public String first; // first name
	public String last; // last name
	public short n_fr_access; // only for own profile, indicates if non friends can view profile details, 1=TRUE, 0 or NULL  = False
	public String phone; // only for own profile
	public String dob; // only for own profile,  format: yyyy-MM-ddTHH:mm:ss.sssssssZ
	public String create_date;  // only for own profile,  format: yyyy-MM-ddTHH:mm:ss.sssssssZ
	public String email; // only for own profile
	public int status; // only non self, indicates friendship, 0=PENDING, 2=FRIENDS, NULL=Not Friends
	public short last_action; // only non self, indicates if the current user made the last change in status, 1=TRUE, 0 or NULL  = False
	public UserBranch[] branches; // only non self, indicates branches the friend is on
	public String avatar; // only for self and friends, if “n_fr_access” is true, will return for non friends as well
	public String avatar_stream_url;
	public String pr_pic; // only for self and friends, if “n_fr_access” is true, will return for non friends as well
	public String pr_pic_stream; // only for self and friends, if “n_fr_access” is true, will return for non friends as well
	public String r_locality; // (resides city) only for self and friends, if “n_fr_access” is true, will return for non friends as well
	public String r_province; // (resides state) only for self and friends, if “n_fr_access” is true, will return for non friends as well
	public String r_country; // (resides country) only for self and friends, if “n_fr_access” is true, will return for non friends as well
	public UserAttribute[] attributes;

	public boolean isNonFriendsAccessEnabled() {
		return n_fr_access == 1;
	}

	/**
	 * Update stored profile when profile was saved
	 *
	 * @param changes the map with changes - keys is a fields names
	 */
	public void updateProfile(Map<String, Object> changes) {
		if (changes == null || changes.size() == 0)
			return;
		for (String field : changes.keySet()) {
			if (keyUsername.equals(field))
				username = (String) changes.get(field);
			else if (keyFirstName.equals(field))
				first = (String) changes.get(field);
			else if (keyLastName.equals(field))
				last = (String) changes.get(field);
			else if (keyEmail.equals(field))
				email = (String) changes.get(field);
			else if (keyCity.equals(field))
				r_locality = (String) changes.get(field);
			else if (keyState.equals(field))
				r_province = (String) changes.get(field);
			else if (keyCountry.equals(field))
				r_country = (String) changes.get(field);
			else if (keyBirthday.equals(field)) {
				dob = (String) changes.get(field);
				if (dob != null) { //workaround to handle difference between api requires date format and responses
					dob = dob.replace("Z", ".0000");
				}
			} else if (keyNonFriendsAccess.equals(field))
				n_fr_access = (short) changes.get(field);
		}
	}

	public String getFullName() {
		return (first == null ? "" : first) + " " + (last == null ? "" : last);
	}
}
