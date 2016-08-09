package com.codepath.twittertimeline.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.adapters.TweetsRecyclerAdapter;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.DividerItemDecoration;
import com.codepath.twittertimeline.utils.EndlessRecyclerViewScrollListener;
import com.codepath.twittertimeline.utils.UiUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {
    private static final String TAG = TimelineActivity.class.getSimpleName();
    TwitterClient client;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.rvTweets)
    RecyclerView rvTweets;
    TweetsRecyclerAdapter tweetsRecyclerAdapter;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fab)
    FloatingActionButton fabCompose;
    List<Tweet> tweets;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvTweets.setLayoutManager(linearLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        rvTweets.addItemDecoration(itemDecoration);
        tweets = new ArrayList<>();
        tweetsRecyclerAdapter = new TweetsRecyclerAdapter(this,tweets);
        rvTweets.setAdapter(tweetsRecyclerAdapter);
        tweetsRecyclerAdapter.setOnItemClickListener(new TweetsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Tweet tweet = tweets.get(position);
                switch(itemView.getId()){
                    case R.id.actionReply:
                        Intent intent = new Intent(TimelineActivity.this,ComposeActivity.class);
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
                        Intent mediaIntent = new Intent(TimelineActivity.this, MediaActivity.class);
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
        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*ComposeTweetDialogFragment composeTweetDialogFragment = ComposeTweetDialogFragment.newInstance();
                composeTweetDialogFragment.show(getSupportFragmentManager(),"Compose");*/
                Intent intent = new Intent(TimelineActivity.this, ComposeActivity.class);
                startActivityForResult(intent,ComposeActivity.COMPOSE_TWEET_REQUEST_CODE);
            }
        });
        client = TwitterApplication.getRestClient();
        populateTimeline(true,1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_timeline,menu);
        MenuItem compose = menu.findItem(R.id.compose);
        compose.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(TimelineActivity.this, ComposeActivity.class);
                startActivityForResult(intent,ComposeActivity.COMPOSE_TWEET_REQUEST_CODE);
                return true;
            }
        });
        return true;
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void populateTimeline(final boolean initial, long id) {
        client.getHomeTimeline(initial,id,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG,"Response returned is - "+response.toString());
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

}
