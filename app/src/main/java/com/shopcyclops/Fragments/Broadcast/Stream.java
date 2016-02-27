package com.shopcyclops.Fragments.Broadcast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.Map;

/**
 * Kickflip base Stream response
 */
public class Stream extends Response implements Comparable<Stream>, Serializable {

    private int mStreamId;

    private String mStreamType;

    private String mChatUrl;

    private String mStreamUrl;

    private double mLatitude;

    private double mLongitude;

    private String mCity;

    private String mState;

    private String mCountry;

    private boolean mPrivate;

    private String mTitle;

    private String mDescription;

    private String mStore;

    private String mExtraInfo;

    private String mThumbnailUrl;

    private String mTimeStarted;

    private int mLength;

    private int mOwnerId;

    private String mOwnerAvatar;

    private boolean mDeleted;

    public String getStore() {
        return mStore;
    }

    public void setStore(String mStore) {
        this.mStore = mStore;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public int getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(int mOwnerId) {
        this.mOwnerId = mOwnerId;
    }

    public String getOwnerAvatar() {
        return mOwnerAvatar;
    }

    public void setOwnerAvatar(String mOwnerAvatar) {
        this.mOwnerAvatar = mOwnerAvatar;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String url) {
        mThumbnailUrl = url;
    }

    public int getStreamId() {
        return mStreamId;
    }

    public void setStreamId(int mStreamId) {
        this.mStreamId = mStreamId;
    }

    public String getStreamType() {
        return mStreamType;
    }

    public void setStreamType(String mStreamType) {
        this.mStreamType = mStreamType;
    }

    public String getChatUrl() {
        return mChatUrl;
    }

    public void setChatUrl(String mChatUrl) {
        this.mChatUrl = mChatUrl;
    }

    public String getStreamUrl() {
        return mStreamUrl;
    }

    public void setStreamURL(String mStreamUrl) {
        this.mStreamUrl = mStreamUrl;
    }

    public String getTimeStarted() {
        return mTimeStarted;
    }

    public void setTimeStated(String mTimeStarted) {
        this.mTimeStarted = mTimeStarted;
    }

    public int getLengthInSeconds() {
        return mLength;
    }

    public void setLengthInSeconds(int mLength) {
        this.mLength = mLength;
    }

    public Map getExtraInfo() {
        if (mExtraInfo != null && !mExtraInfo.equals("")) {
            return new Gson().fromJson(mExtraInfo, Map.class);
        }
        return null;
    }

    public void setExtraInfo(Map mExtraInfo) {
        this.mExtraInfo = new Gson().toJson(mExtraInfo);
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String mCity) {
        this.mCity = mCity;
    }

    public String getState() {
        return mState;
    }

    public void setState(String mState) {
        this.mState = mState;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String mCountry) {
        this.mCountry = mCountry;
    }

    public boolean isPrivate() {
        return mPrivate;
    }

    public void setIsPrivate(boolean mPrivate) {
        this.mPrivate = mPrivate;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    @Override
    public int compareTo(Stream another) {
        return another.getTimeStarted().compareTo(getTimeStarted());
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

}
