package com.codepath.twittertimeline.fragments;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.activities.ComposeActivity;
import com.codepath.twittertimeline.activities.MediaActivity;
import com.codepath.twittertimeline.adapters.TweetsRecyclerAdapter;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.EndlessRecyclerViewScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProfileActivityFragment extends TweetsListFragment implements TweetsRecyclerAdapter.OnItemClickListener{

    private static final String TAG = ProfileActivityFragment.class.getSimpleName();
    private String mScreenName;
    public ProfileActivityFragment() {
    }

    public static ProfileActivityFragment newInstance(String screenName){
        ProfileActivityFragment profileActivityFragment = new ProfileActivityFragment();
        Bundle args = new Bundle();
        args.putString("SCREEN_NAME",screenName);
        profileActivityFragment.setArguments(args);
        return profileActivityFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mScreenName = getArguments().getString("SCREEN_NAME");
        }
    }

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

    private void populateTimeline(final boolean initial, long id) {
        client.getUserTimeLine(mScreenName,new JsonHttpResponseHandler(){
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
                //favoriteTweet(position, tweet);
                break;
            case R.id.actionRetweet:
                //retweetTweet(position,tweet);
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
