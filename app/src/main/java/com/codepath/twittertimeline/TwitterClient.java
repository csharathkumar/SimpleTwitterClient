package com.codepath.twittertimeline;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;

import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.Constants;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "IAL9OadxFOLTFPC5Td2CpeuqZ";       // Change this
	public static final String REST_CONSUMER_SECRET = "b2s5OCGg9lC7J1Pmy5o5qFwoMdPhncNnhfPDTyV3aX7ySNQ2C4"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cpsimpletweets"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */

	//Home Timeline
	public void getHomeTimeline(boolean isInitial, long id, AsyncHttpResponseHandler handler){
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", Constants.PAGE_SIZE);
		if(isInitial){
			params.put("since_id",id);
		}else{
			params.put("max_id",id);
		}

		getClient().get(apiUrl,params,handler);
	}

	//Home Timeline
	public void getMentionsTimeline(boolean isInitial, long id, AsyncHttpResponseHandler handler){
		String apiUrl = getApiUrl("statuses/mentions_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", Constants.PAGE_SIZE);
		if(isInitial){
			params.put("since_id",id);
		}else{
			params.put("max_id",id);
		}

		getClient().get(apiUrl,params,handler);
	}

	//Post a tweet
	//https://api.twitter.com/1.1/statuses/update.json
	public void postNewTweet(String status, AsyncHttpResponseHandler handler){
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		try {
			params.put("status", status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		getClient().post(apiUrl,params,handler);
	}
	//Post a Reply
	//https://api.twitter.com/1.1/statuses/update.json
	public void postNewReply(long originalTweetId, String status, AsyncHttpResponseHandler handler){
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		try {
			params.put("status", status);
			params.put("in_reply_to_status_id",originalTweetId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		getClient().post(apiUrl,params,handler);
	}
	//Favorite or unfavoritea tweet
	//https://api.twitter.com/1.1/favorites/create.json?id=243138128959913986
	//https://api.twitter.com/1.1/favorites/destroy.json?id=243138128959913986
	public void favoriteTweet(boolean create, long id, AsyncHttpResponseHandler handler){
		String apiUrl = "";
		if(create){
			apiUrl = getApiUrl("favorites/create.json");
		}else{
			apiUrl = getApiUrl("favorites/destroy.json");
		}
		RequestParams params = new RequestParams();
		try {
			params.put("id", id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		getClient().post(apiUrl,params,handler);
	}

	//Retweet a tweet
	//https://api.twitter.com/1.1/statuses/retweet/241259202004267009.json
	//https://api.twitter.com/1.1/statuses/unretweet/680526305473867776.json
	public void retweet(boolean create, long id, AsyncHttpResponseHandler handler){
		String apiUrl = "";
		if(create){
			apiUrl = getApiUrl("statuses/retweet/"+id+".json");
		}else{
			apiUrl = getApiUrl("statuses/unretweet/"+id+".json");
		}
		getClient().post(apiUrl,handler);
	}

	public void getUserTimeLine(String screenName, AsyncHttpResponseHandler handler){
		String apiUrl = getApiUrl("statuses/user_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", Constants.PAGE_SIZE);
		params.put("screen_name",screenName);
		getClient().get(apiUrl,params,handler);
	}

	public void getUserInfo(AsyncHttpResponseHandler handler){
		String apiUrl = getApiUrl("account/verify_credentials.json");
		getClient().get(apiUrl,null,handler);
	}
}