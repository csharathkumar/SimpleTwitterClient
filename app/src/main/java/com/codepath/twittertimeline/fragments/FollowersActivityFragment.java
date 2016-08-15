package com.codepath.twittertimeline.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.adapters.FollowersRecyclerAdapter;
import com.codepath.twittertimeline.models.User;
import com.codepath.twittertimeline.utils.DividerItemDecoration;
import com.codepath.twittertimeline.utils.EndlessRecyclerViewScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * A placeholder fragment containing a simple view.
 */
public class FollowersActivityFragment extends Fragment {
    public static final String IS_FOLLOWERS = "is_followers";
    public static final String SCREEN_NAME = "screen_name";
    private boolean isFollowers;
    private String screenName;
    String globalCursor = "-1";
    private List<User> users;
    private FollowersRecyclerAdapter adapter;
    private RecyclerView rvFollowers;
    private TwitterClient client;

    public FollowersActivityFragment() {
    }

    public static FollowersActivityFragment newInstance(boolean isFollowers, String screenName){
        FollowersActivityFragment fragment = new FollowersActivityFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_FOLLOWERS,isFollowers);
        args.putString(SCREEN_NAME,screenName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFollowers = getArguments().getBoolean(IS_FOLLOWERS);
        screenName = getArguments().getString(SCREEN_NAME);
        users = new ArrayList<>();

        client = TwitterApplication.getRestClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_followers, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        adapter = new FollowersRecyclerAdapter(getActivity(),users);
        rvFollowers = (RecyclerView) view.findViewById(R.id.rvFollowers);
        rvFollowers.setLayoutManager(linearLayoutManager);
        rvFollowers.setAdapter(adapter);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        rvFollowers.addItemDecoration(itemDecoration);
        rvFollowers.setOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

                populateFollowersList(globalCursor);
            }
        });
        populateFollowersList(globalCursor);
    }

    private void populateFollowersList(final String cursor){
        client.getFollowersList(isFollowers,screenName,cursor,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    globalCursor = response.getString("next_cursor");
                    users.addAll(User.fromJSONArray(response.getJSONArray("users")));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
}
