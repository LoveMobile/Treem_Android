package com.treem.treem.services.Treem;

import com.github.scribejava.core.model.Verb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Matthew Walker on 2/16/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 *
 * Extend options when passing Treem service requests
 */

abstract public class TreemServiceRequest {
    public String url                   = null;
    public Verb method                  = Verb.GET;
    public HashSet failureCodesHandled  = null;
    public HashMap HTTPCustomHeaders    = null;
    public Map<String,String> searchOptions        = null;
    public Object bodyData          = null;

    abstract public void onSuccess(String data);
    abstract public void onFailure(TreemServiceResponseCode error, boolean wasHandled);
}
