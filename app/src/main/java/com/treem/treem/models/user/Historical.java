package com.treem.treem.models.user;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Historical points data for user
 */
public class Historical {
    public static final Type LIST_TYPE = new TypeToken<ArrayList<Historical>>() {}.getType();
    public double point_change;
    public double point_sum;
    public String period_start;
}
