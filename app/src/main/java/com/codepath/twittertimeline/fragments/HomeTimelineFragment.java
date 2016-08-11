package com.codepath.twittertimeline.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.EndlessRecyclerViewScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Sharath on 8/9/16.
 */
public class HomeTimelineFragment extends TweetsListFragment {

    private static final String TAG = HomeTimelineFragment.class.getSimpleName();
    TwitterClient client;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = TwitterApplication.getRestClient();
        populateTimeline(true,1);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvTweets.setOnScrollListener(new EndlessRecyclerViewScrollListener(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)) {
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
                addAll(Tweet.fromJSONArray(response));
                //tweetsRecyclerAdapter.notifyDataSetChanged();
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
}
