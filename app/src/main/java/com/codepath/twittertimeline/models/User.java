package com.codepath.twittertimeline.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sharath on 8/2/16.
 */
public class User implements Parcelable {
    String name;
    long uid;
    String screenName;
    String profileImageUrl;
    String tagline;
    int followersCount;
    int friendsCount;
    String profileBackdropImageUrl;
    long totalTweets;
    boolean isVerified;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public String getProfileBackdropImageUrl() {
        return profileBackdropImageUrl;
    }

    public void setProfileBackdropImageUrl(String profileBackdropImageUrl) {
        this.profileBackdropImageUrl = profileBackdropImageUrl;
    }

    public long getTotalTweets() {
        return totalTweets;
    }

    public void setTotalTweets(long totalTweets) {
        this.totalTweets = totalTweets;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public static User fromJSONObject(JSONObject jsonObject){
        User user = new User();
        try{
            user.name = jsonObject.getString("name");
            user.uid = jsonObject.getLong("id");
            user.screenName = jsonObject.getString("screen_name");
            user.profileImageUrl = jsonObject.getString("profile_image_url");
            user.tagline = jsonObject.getString("description");
            user.followersCount = jsonObject.getInt("followers_count");
            user.friendsCount = jsonObject.getInt("friends_count");
            user.profileBackdropImageUrl = jsonObject.getString("profile_background_image_url_https");
            user.totalTweets = jsonObject.getLong("statuses_count");
            user.isVerified = jsonObject.getBoolean("verified");
        }catch(JSONException e){
            e.printStackTrace();
        }
        return user;
    }

    public static List<User> fromJSONArray(JSONArray jsonArray){
        List<User> users = new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){
            try {
                users.add(fromJSONObject(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeLong(this.uid);
        dest.writeString(this.screenName);
        dest.writeString(this.profileImageUrl);
        dest.writeString(this.tagline);
        dest.writeInt(this.followersCount);
        dest.writeInt(this.friendsCount);
        dest.writeString(this.profileBackdropImageUrl);
        dest.writeLong(this.totalTweets);
        dest.writeByte(this.isVerified ? (byte) 1 : (byte) 0);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.name = in.readString();
        this.uid = in.readLong();
        this.screenName = in.readString();
        this.profileImageUrl = in.readString();
        this.tagline = in.readString();
        this.followersCount = in.readInt();
        this.friendsCount = in.readInt();
        this.profileBackdropImageUrl = in.readString();
        this.totalTweets = in.readLong();
        this.isVerified = in.readByte() != 0;
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
