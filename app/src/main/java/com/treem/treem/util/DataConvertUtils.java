package com.treem.treem.util;

import com.google.gson.Gson;

/**
 * Date: 6/1/16.
 */
public final class DataConvertUtils {

	private DataConvertUtils() {
	}

	public static <T> T parseJson(String data, Class<T> type) {
		Gson gson = new Gson();

		try {
			return gson.fromJson(data, type);
		} catch (Exception ed) {
			return null;
		}
	}
}
