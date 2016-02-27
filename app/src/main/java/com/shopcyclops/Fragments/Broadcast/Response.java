package com.shopcyclops.Fragments.Broadcast;


/**
 * JSON API object for Kickflip Responses
 */
public class Response {

    private boolean mSuccess;

    private String mReason;

    public Response() {
        // Required default Constructor
    }

    public boolean isSuccessful() {
        return mSuccess;
    }

    public String getReason() {
        return mReason;
    }
}
