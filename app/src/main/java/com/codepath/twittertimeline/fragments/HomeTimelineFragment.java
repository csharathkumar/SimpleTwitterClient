package com.codepath.twittertimeline.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.activities.ComposeActivity;
import com.codepath.twittertimeline.activities.MediaActivity;
import com.codepath.twittertimeline.activities.ProfileActivity;
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
public class HomeTimelineFragment extends TweetsListFragment implements TweetsRecyclerAdapter.OnItemClickListener {

    private static final String TAG = HomeTimelineFragment.class.getSimpleName();
    private FloatingActionButton fabCompose;
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
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        coordinatorLayout = ((TimelineActivity)getActivity()).getCoordinatorLayout();
        if(fabCompose != null){
            fabCompose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ComposeTweetDialogFragment composeTweetDialogFragment = ComposeTweetDialogFragment.newInstance(null);
                    composeTweetDialogFragment.setTargetFragment(HomeTimelineFragment.this, ComposeActivity.COMPOSE_TWEET_REQUEST_CODE);
                    composeTweetDialogFragment.show(getActivity().getSupportFragmentManager(),"Compose");
                }
            });
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof TimelineActivity){
            fabCompose = ((TimelineActivity)context).getFabCompose();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_timeline, menu);

        MenuItem compose = menu.findItem(R.id.compose);
        compose.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ComposeTweetDialogFragment composeTweetDialogFragment = ComposeTweetDialogFragment.newInstance(null);
                composeTweetDialogFragment.setTargetFragment(HomeTimelineFragment.this, ComposeActivity.COMPOSE_TWEET_REQUEST_CODE);
                composeTweetDialogFragment.show(getActivity().getSupportFragmentManager(),"Compose");
                return true;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
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
    @Override
    public void onItemClick(View itemView, int position) {
        Tweet tweet = tweets.get(position);
        switch(itemView.getId()){
            case R.id.actionReply:
                ComposeTweetDialogFragment composeTweetDialogFragment = ComposeTweetDialogFragment.newInstance(tweet);
                composeTweetDialogFragment.setTargetFragment(HomeTimelineFragment.this, ComposeActivity.REPLY_TWEET_REQUEST_CODE);
                composeTweetDialogFragment.show(getActivity().getSupportFragmentManager(),"Reply");
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
            case R.id.ivProfile:
                Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                String screenName = String.valueOf(itemView.getTag());
                profileIntent.putExtra("SCREEN_NAME",screenName);
                startActivity(profileIntent);
                break;
            default:
                //Toast.makeText(getApplicationContext(), tweet.getBody(),Toast.LENGTH_SHORT).show();
        }
    }
}
