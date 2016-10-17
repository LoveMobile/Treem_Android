package com.treem.treem.services.Treem;

/**
 * Created by Matthew Walker on 2/17/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */

public class TreemServiceResponse {
    public Integer error;
    public Object data;

    public TreemServiceResponseCode getResponseCode() {
        // start with error, require success to be asserted
        TreemServiceResponseCode responseCode = TreemServiceResponseCode.OTHER_ERROR;

        // if error code returned in call
        if (this.error != null) {
            responseCode = TreemServiceResponseCode.get(this.error);

            // if valid response code could not be found
            if (responseCode == null) {
                responseCode = TreemServiceResponseCode.OTHER_ERROR;
            }
        }
        // if no error, assume success if any data is passed back
        else if (data != null) {
            responseCode = TreemServiceResponseCode.SUCCESS;
        }

        return responseCode;
    }

    @Override
    public String toString() {
        if (this.error != null) {
            return "Error: " + Integer.toString(this.error);
        }
        else {
            return "Data: " + data.toString();
        }
    }
}
