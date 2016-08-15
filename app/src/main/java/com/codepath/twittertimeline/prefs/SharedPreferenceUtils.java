package com.codepath.twittertimeline.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.models.User;

/**
 * Created by Sharath on 8/14/16.
 */
public class SharedPreferenceUtils {
    public static final String HAS_USER_INFO = "has_user_info";
    public static final String KEY_CURRENT_USER_ID = "id";
    public static final String KEY_CURRENT_USER_NAME = "userName";
    public static final String KEY_CURRENT_USER_SCREEN_NAME = "screenName";
    public static final String KEY_USER_PROFILE_IMAGE_URL = "profileImageUrl";
    public static final String KEY_USER_BACKDROP_IMAGE_URL = "backdropUrl";
    public static final String KEY_USER_VERIFIED = "is_user_verified";
    public static final String KEY_FOLLOWERS_COUNT = "followers_count";
    public static final String KEY_FOLLOWING_COUNT = "following_count";
    public static final String KEY_USER_TAGLINE = "tagline";
    public static final String KEY_TOTAL_TWEETS = "total_tweets";

    public static void storeUserInformation(Context context, User user){
        SharedPreferences sharedPref = context.getSharedPreferences("CURRENT_USER_INFO", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(KEY_CURRENT_USER_ID,user.getUid());
        editor.putString(KEY_CURRENT_USER_NAME,user.getName());
        editor.putString(KEY_CURRENT_USER_SCREEN_NAME,user.getScreenName());
        editor.putString(KEY_USER_PROFILE_IMAGE_URL,user.getProfileImageUrl());
        editor.putString(KEY_USER_BACKDROP_IMAGE_URL,user.getProfileBackdropImageUrl());
        editor.putBoolean(KEY_USER_VERIFIED,user.isVerified());
        editor.putInt(KEY_FOLLOWERS_COUNT,user.getFollowersCount());
        editor.putInt(KEY_FOLLOWING_COUNT,user.getFriendsCount());
        editor.putString(KEY_USER_TAGLINE,user.getTagline());
        editor.putLong(KEY_TOTAL_TWEETS,user.getTotalTweets());
        editor.putBoolean(HAS_USER_INFO,true);
        editor.commit();

    }

    public static User getUserInfoFromPreferences(Context context){
        User user = new User();
        SharedPreferences sharedPref = context.getSharedPreferences("CURRENT_USER_INFO", Context.MODE_PRIVATE);
        user.setUid(sharedPref.getLong(KEY_CURRENT_USER_ID,0));
        user.setName(sharedPref.getString(KEY_CURRENT_USER_NAME,""));
        user.setScreenName(sharedPref.getString(KEY_CURRENT_USER_SCREEN_NAME,""));
        user.setProfileImageUrl(sharedPref.getString(KEY_USER_PROFILE_IMAGE_URL,""));
        user.setProfileBackdropImageUrl(sharedPref.getString(KEY_USER_BACKDROP_IMAGE_URL,""));
        user.setVerified(sharedPref.getBoolean(KEY_USER_VERIFIED,false));
        user.setFollowersCount(sharedPref.getInt(KEY_FOLLOWERS_COUNT,0));
        user.setFriendsCount(sharedPref.getInt(KEY_FOLLOWING_COUNT,0));
        user.setTagline(sharedPref.getString(KEY_USER_TAGLINE,""));
        user.setTotalTweets(sharedPref.getLong(KEY_TOTAL_TWEETS,0));
        return user;
    }

    public static boolean hasTotalInfoAboutCurrentUser(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("CURRENT_USER_INFO", Context.MODE_PRIVATE);
        String temp = sharedPref.getString(KEY_USER_PROFILE_IMAGE_URL,"");
        if(temp.length() > 0){
            return true;
        }
        return false;
    }
}
