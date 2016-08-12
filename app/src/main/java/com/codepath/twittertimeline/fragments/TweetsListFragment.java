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
    LinearLayoutManager linearLayoutManager;
    // inflation logic
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_timeline,container, false);
        ButterKnife.bind(this,view);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvTweets.setLayoutManager(linearLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        rvTweets.addItemDecoration(itemDecoration);
        return view;
    }

    public LinearLayoutManager getLinearLayoutManager(){
        return linearLayoutManager;
    }
    //creation lifecycle event

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        coordinatorLayout = ((TimelineActivity)getActivity()).getCoordinatorLayout();
    }

    public CoordinatorLayout getCoordinatorLayout(){
        return coordinatorLayout;
    }
}
