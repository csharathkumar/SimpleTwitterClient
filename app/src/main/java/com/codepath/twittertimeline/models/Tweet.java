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

public class Tweet implements Parcelable {
    private String body;
    private long uid;
    private User user;
    private String createdAt;
    private boolean favorited;
    private Media media;
    private Media extendedMedia;
    private long retweetCount;
    private long favoritesCount;
    private boolean retweeted;
    private String inReplyToUserName;
    private boolean isRetweet;
    private String retweetUserName;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public long getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(long retweetCount) {
        this.retweetCount = retweetCount;
    }

    public long getFavoritesCount() {
        return favoritesCount;
    }

    public void setFavoritesCount(long favoritesCount) {
        this.favoritesCount = favoritesCount;
    }

    public Media getExtendedMedia() {
        return extendedMedia;
    }

    public void setExtendedMedia(Media extendedMedia) {
        this.extendedMedia = extendedMedia;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public String getInReplyToUserName() {
        return inReplyToUserName;
    }

    public void setInReplyToUserName(String inReplyToUserName) {
        this.inReplyToUserName = inReplyToUserName;
    }

    public boolean isRetweet() {
        return isRetweet;
    }

    public void setRetweet(boolean retweet) {
        isRetweet = retweet;
    }

    public String getRetweetUserName() {
        return retweetUserName;
    }

    public void setRetweetUserName(String retweetUserName) {
        this.retweetUserName = retweetUserName;
    }

    public static Tweet fromJSON(JSONObject jsonObject){
        Tweet tweet = new Tweet();
        try {
            tweet.createdAt = jsonObject.getString("created_at");
            tweet.retweetCount = jsonObject.getLong("retweet_count");
            tweet.favorited = jsonObject.getBoolean("favorited");
            tweet.retweeted = jsonObject.getBoolean("retweeted");



            if(jsonObject.has("retweeted_status")){
                tweet.uid = jsonObject.getJSONObject("retweeted_status").getLong("id");
                tweet.body = jsonObject.getJSONObject("retweeted_status").getString("text");
                tweet.favoritesCount = jsonObject.getJSONObject("retweeted_status").getLong("favorite_count");
                tweet.user = User.fromJSONObject(jsonObject.getJSONObject("retweeted_status").getJSONObject("user"));
                tweet.retweetUserName = jsonObject.getJSONObject("user").getString("name");
                tweet.isRetweet = true;
            }else{
                tweet.body = jsonObject.getString("text");
                tweet.uid = jsonObject.getLong("id");
                tweet.user = User.fromJSONObject(jsonObject.getJSONObject("user"));
                tweet.favoritesCount = jsonObject.getLong("favorite_count");
                tweet.isRetweet = false;
                tweet.retweetUserName = null;
            }
            JSONObject entities = jsonObject.getJSONObject("entities");
            if(entities.has("media")){
                JSONArray mediaArray = entities.getJSONArray("media");
                if(mediaArray != null && mediaArray.length() > 0){
                    tweet.media = Media.fromJSONObject(mediaArray.getJSONObject(0));
                }
            }
            if(jsonObject.has("extended_entities")){
                JSONObject extendedObj = jsonObject.getJSONObject("extended_entities");
                if(extendedObj.has("media")){
                    JSONArray extendedMediaArray = extendedObj.getJSONArray("media");
                    if(extendedMediaArray != null && extendedMediaArray.length() > 0){
                        tweet.extendedMedia = Media.fromJSONObject(extendedMediaArray.getJSONObject(0));
                    }
                }
            }

            if(jsonObject.has("in_reply_to_screen_name") && !jsonObject.isNull("in_reply_to_screen_name")){
                tweet.inReplyToUserName = jsonObject.getString("in_reply_to_screen_name");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tweet;
    }

    public static List<Tweet> fromJSONArray(JSONArray jsonArray){
        List<Tweet> tweets = new ArrayList<>();
        JSONObject jsonObject = null;
        for(int i=0;i<jsonArray.length();i++){
            try {
                jsonObject = jsonArray.getJSONObject(i);
                tweets.add(fromJSON(jsonObject));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }

        }
        return tweets;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.body);
        dest.writeLong(this.uid);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.createdAt);
        dest.writeString(String.valueOf(this.favorited));
        dest.writeParcelable(this.media,flags);
        dest.writeLong(this.retweetCount);
        dest.writeLong(this.favoritesCount);
        dest.writeParcelable(this.extendedMedia,flags);
        dest.writeString(String.valueOf(this.retweeted));
        dest.writeString(this.inReplyToUserName);
        dest.writeString(String.valueOf(isRetweet));
        dest.writeString(retweetUserName);
    }

    public Tweet() {
    }

    protected Tweet(Parcel in) {
        this.body = in.readString();
        this.uid = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.createdAt = in.readString();
        this.favorited = Boolean.parseBoolean(in.readString());
        this.media = in.readParcelable(Media.class.getClassLoader());
        this.retweetCount = in.readLong();
        this.favoritesCount = in.readLong();
        this.extendedMedia = in.readParcelable(Media.class.getClassLoader());
        this.retweeted = Boolean.parseBoolean(in.readString());
        this.inReplyToUserName = in.readString();
        this.isRetweet = Boolean.parseBoolean(in.readString());
        this.retweetUserName = in.readString();
    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        @Override
        public Tweet createFromParcel(Parcel source) {
            return new Tweet(source);
        }

        @Override
        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
}
