package com.codepath.twittertimeline.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.SyncStateContract;

import com.codepath.twittertimeline.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sharath on 8/7/16.
 */
public class Media implements Parcelable {
    String type;
    String mediaUrlHttps;
    String mediaUrl;
    String videoUrl;
    String videoUrlHttps;

    public String getType() {
        return type;
    }

    public String getMediaUrlHttps() {
        return mediaUrlHttps;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getVideoUrlHttps() {
        return videoUrlHttps;
    }

    public static Media fromJSONObject(JSONObject jsonObject){
        Media media = new Media();
        try {
            media.type = jsonObject.getString("type");
            media.mediaUrl = jsonObject.getString("media_url");
            media.mediaUrlHttps = jsonObject.getString("media_url_https");
            if(media.type.equals(Constants.VIDEO_KEY)){
                JSONObject videoInfo = jsonObject.getJSONObject("video_info");
                JSONArray variants = videoInfo.getJSONArray("variants");
                media.videoUrlHttps = variants.getJSONObject(0).getString("url");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return media;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.mediaUrlHttps);
        dest.writeString(this.mediaUrl);
        dest.writeString(this.videoUrl);
        dest.writeString(this.videoUrlHttps);
    }

    public Media() {
    }

    protected Media(Parcel in) {
        this.type = in.readString();
        this.mediaUrlHttps = in.readString();
        this.mediaUrl = in.readString();
        this.videoUrl = in.readString();
        this.videoUrlHttps = in.readString();
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel source) {
            return new Media(source);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
}
