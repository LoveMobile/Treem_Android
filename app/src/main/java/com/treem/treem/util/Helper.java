package com.treem.treem.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Date: 6/27/16.
 */
public final class Helper {

	private Helper() {}

	public static Gson createGsonSerializer() {
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

		return gsonBuilder.create();
	}
}
