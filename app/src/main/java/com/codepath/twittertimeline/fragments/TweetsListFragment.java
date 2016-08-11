package com.codepath.twittertimeline.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.activities.ComposeActivity;
import com.codepath.twittertimeline.activities.MediaActivity;
import com.codepath.twittertimeline.activities.TimelineActivity;
import com.codepath.twittertimeline.adapters.TweetsRecyclerAdapter;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.DividerItemDecoration;
import com.codepath.twittertimeline.utils.EndlessRecyclerViewScrollListener;
import com.codepath.twittertimeline.utils.UiUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Sharath on 8/9/16.
 */
public class TweetsListFragment extends Fragment {
    private static final String TAG = TweetsListFragment.class.getSimpleName();
    TwitterClient client;
    List<Tweet> tweets;

    @BindView(R.id.rvTweets)
    RecyclerView rvTweets;
    TweetsRecyclerAdapter tweetsRecyclerAdapter;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    CoordinatorLayout coordinatorLayout;
    // inflation logic
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_timeline,container, false);
        ButterKnife.bind(this,view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvTweets.setLayoutManager(linearLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        rvTweets.addItemDecoration(itemDecoration);
        tweets = new ArrayList<>();
        tweetsRecyclerAdapter = new TweetsRecyclerAdapter(getActivity(),tweets);
        rvTweets.setAdapter(tweetsRecyclerAdapter);
        tweetsRecyclerAdapter.setOnItemClickListener(new TweetsRecyclerAdapter.OnItemClickListener() {
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
        });
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
        return view;
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
        if(requestCode == ComposeActivity.COMPOSE_TWEET_REQUEST_CODE || requestCode == ComposeActivity.REPLY_TWEET_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                if(data != null){
                    Tweet tweet = data.getParcelableExtra(ComposeActivity.TWEET_OBJECT);
                    tweetsRecyclerAdapter.addItemAtPosition(tweet,0);
                    rvTweets.scrollToPosition(0);
                }
            }
        }else if(requestCode == MediaActivity.OPEN_MEDIA_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                boolean tweetModified = data.getBooleanExtra(MediaActivity.IS_TWEET_MODIFIED,false);
                if(tweetModified){
                    int position = data.getIntExtra(MediaActivity.TWEET_POSITION,0);
                    Tweet modifiedTweet = data.getParcelableExtra(MediaActivity.MODIFIED_TWEET);
                    tweetsRecyclerAdapter.replaceItemAtPosition(modifiedTweet,position);
                }
            }
        }
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
        client.getHomeTimeline(initial,id,new JsonHttpResponseHandler(){
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

    public void addAll(List<Tweet> tweets){
        tweets.addAll(tweets);
        tweetsRecyclerAdapter.notifyDataSetChanged();
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
}
