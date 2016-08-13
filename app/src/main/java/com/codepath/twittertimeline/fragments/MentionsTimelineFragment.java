package com.codepath.twittertimeline.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.activities.ComposeActivity;
import com.codepath.twittertimeline.activities.MediaActivity;
import com.codepath.twittertimeline.activities.TimelineActivity;
import com.codepath.twittertimeline.adapters.TweetsRecyclerAdapter;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.EndlessRecyclerViewScrollListener;
import com.codepath.twittertimeline.utils.UiUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Sharath on 8/10/16.
 */
public class MentionsTimelineFragment extends TweetsListFragment implements TweetsRecyclerAdapter.OnItemClickListener{
    private static final String TAG = HomeTimelineFragment.class.getSimpleName();
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tweets = new ArrayList<>();
        tweetsRecyclerAdapter = new TweetsRecyclerAdapter(getActivity(),tweets,this);
        rvTweets.setAdapter(tweetsRecyclerAdapter);

        rvTweets.setOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

                populateTimeline(false,tweetsRecyclerAdapter.getMaxTweetId());
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                populateTimeline(true,1);
            }
        });
        client = TwitterApplication.getRestClient();
        populateTimeline(true,1);
    }

    //creation lifecycle event

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        coordinatorLayout = ((TimelineActivity)getActivity()).getCoordinatorLayout();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private void favoriteTweet(final int position, Tweet tweet) {
        boolean create = !tweet.isFavorited();
        client.favoriteTweet(create,tweet.getUid(),new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //JSONObject jsonObject = response.getJSONObject(0);
                    Tweet tweetReturned = Tweet.fromJSON(response);
                    tweetsRecyclerAdapter.replaceItemAtPosition(tweetReturned,position);
                    UiUtils.showSnackBar(coordinatorLayout,"Favorited");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG,"Error while favoriting a tweet - "+errorResponse.toString());
                UiUtils.showSnackBar(coordinatorLayout,getString(R.string.favorite_unsuccessful));
                //Toast.makeText(TimelineActivity.this,"Favorite unsuccessful",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retweetTweet(final int position, Tweet tweet){
        boolean create = !tweet.isRetweeted();
        client.retweet(create,tweet.getUid(),new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Tweet tweetReturned = Tweet.fromJSON(response);
                    tweetsRecyclerAdapter.replaceItemAtPosition(tweetReturned,position);
                    UiUtils.showSnackBar(coordinatorLayout,getString(R.string.retweeted));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG,"Error while favoriting a tweet - "+errorResponse.toString());
                UiUtils.showSnackBar(coordinatorLayout,getString(R.string.retweet_unsuccessful));
            }
        });
    }

    private void populateTimeline(final boolean initial, long id) {
        client.getMentionsTimeline(initial,id,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                String res = response.toString();
                Log.d(TAG,"Response returned is - "+res);
                if(initial){
                    tweets.clear();
                }
                tweets.addAll(Tweet.fromJSONArray(response));
                tweetsRecyclerAdapter.notifyDataSetChanged();
                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG,"Error returned is - "+errorResponse.toString());
                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }
    public void postNewTweet(String status){
        client.postNewTweet(status,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Tweet tweet = Tweet.fromJSON(response);
                tweetsRecyclerAdapter.addItemAtPosition(tweet,0);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    @Override
    public void onItemClick(View itemView, int position) {
        Tweet tweet = tweets.get(position);
        switch(itemView.getId()){
            case R.id.actionReply:
                Intent intent = new Intent(getActivity(),ComposeActivity.class);
                intent.putExtra(ComposeActivity.IS_REPLY,true);
                intent.putExtra(ComposeActivity.BASE_TWEET_OBJECT,tweet);
                startActivityForResult(intent,ComposeActivity.REPLY_TWEET_REQUEST_CODE);
                break;
            case R.id.actionFavorite:
                favoriteTweet(position, tweet);
                break;
            case R.id.actionRetweet:
                retweetTweet(position,tweet);
                break;
            case R.id.videoView:
            case R.id.ivImage:
                Intent mediaIntent = new Intent(getActivity(), MediaActivity.class);
                mediaIntent.putExtra(MediaActivity.TWEET_TO_DISPLAY,tweet);
                mediaIntent.putExtra(MediaActivity.TWEET_POSITION,position);
                startActivityForResult(mediaIntent,MediaActivity.OPEN_MEDIA_ACTIVITY_REQUEST_CODE);
                break;
            default:
                //Toast.makeText(getApplicationContext(), tweet.getBody(),Toast.LENGTH_SHORT).show();
        }
    }
}
