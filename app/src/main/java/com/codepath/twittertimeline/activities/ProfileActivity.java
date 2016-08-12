package com.codepath.twittertimeline.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.fragments.ProfileActivityFragment;
import com.codepath.twittertimeline.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ProfileActivity extends AppCompatActivity {
    TwitterClient client;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        client = TwitterApplication.getRestClient();
        client.getUserInfo(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                user = User.fromJSONObject(response);
                //My current user account information
                getSupportActionBar().setTitle(user.getScreenName());
                populateProfileHeader(user);
            }
        });
        //Get screen name
        String screenName = getIntent().getStringExtra("SCREEN_NAME");
        if(savedInstanceState == null){
            //Create user timeline fragment
            ProfileActivityFragment profileActivityFragment = ProfileActivityFragment.newInstance(screenName);
            //Display the fragment
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container,profileActivityFragment);
            ft.commit();
        }
    }

    void populateProfileHeader(User user){
        TextView tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        TextView tvTagline = (TextView) findViewById(R.id.tvTagline);
        ImageView ivProfile = (ImageView) findViewById(R.id.ivProfile);
        TextView tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        TextView tvFollowing = (TextView) findViewById(R.id.tvFollowing);

        tvScreenName.setText(user.getName());
        tvTagline.setText(user.getTagline());
        tvFollowers.setText(user.getFollowersCount()+" Followers");
        tvFollowing.setText(user.getFriendsCount()+" Following");
        Picasso.with(this).load(user.getProfileImageUrl()).into(ivProfile);
    }
}
