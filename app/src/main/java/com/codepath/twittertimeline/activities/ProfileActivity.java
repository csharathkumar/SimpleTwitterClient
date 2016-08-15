package com.codepath.twittertimeline.activities;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.fragments.ProfileActivityFragment;
import com.codepath.twittertimeline.models.User;
import com.codepath.twittertimeline.prefs.SharedPreferenceUtils;
import com.codepath.twittertimeline.utils.LinkifiedTextView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    TwitterClient client;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        client = TwitterApplication.getRestClient();
        //Get screen name
        final String screenName = getIntent().getStringExtra("SCREEN_NAME");
        boolean current = false;
        boolean fetchInfo = true;
        if(screenName == null){
            current = true;
            if(SharedPreferenceUtils.hasTotalInfoAboutCurrentUser(ProfileActivity.this)){
               fetchInfo = false;
            }
        }

        if(fetchInfo){
            client.getUserInfo(current, screenName, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    user = User.fromJSONObject(response);
                    setUserInfo(user);
                }
            });
        }else{
            user = SharedPreferenceUtils.getUserInfoFromPreferences(this);
            setUserInfo(user);
        }

        if(savedInstanceState == null){
            //Create user timeline fragment
            ProfileActivityFragment profileActivityFragment = ProfileActivityFragment.newInstance(screenName);
            //Display the fragment
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container,profileActivityFragment);
            ft.commit();
        }
        collapsingToolbarLayout.setTitle(" ");
    }
    private void setUserInfo(User user){
        populateProfileHeader(user);
        setTitle(user);
    }
    private void setTitle(final User user){
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    if(user != null){
                        collapsingToolbarLayout.setTitle(user.getScreenName());
                        isShow = true;
                    }
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    void populateProfileHeader(User user){
        TextView tvFullname = (TextView) findViewById(R.id.tvFullName);
        TextView tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        LinkifiedTextView tvTagline = (LinkifiedTextView) findViewById(R.id.tvTagline);
        ImageView ivProfile = (ImageView) findViewById(R.id.ivProfile);
        ImageView ivBackdrop = (ImageView) findViewById(R.id.backdrop);
        TextView tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        TextView tvFollowing = (TextView) findViewById(R.id.tvFollowing);

        if(user.isVerified()){
            tvFullname.setCompoundDrawablePadding(10);
            tvFullname.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_verified,0);
            tvFullname.setText(user.getName());
        }else{
            tvFullname.setText(user.getName());
        }

        tvScreenName.setText(String.format("@%s", user.getScreenName()));
        tvTagline.setText(user.getTagline());
        tvFollowers.setText(String.format(getString(R.string.followers), user.getFollowersCount()));
        tvFollowing.setText(String.format(getString(R.string.following), user.getFriendsCount()));
        Picasso.with(this).load(user.getProfileImageUrl())
                .into(ivProfile);
        Picasso.with(this).load(user.getProfileBackdropImageUrl()).into(ivBackdrop);
        Log.d(TAG,"Profile backdrop url is - "+user.getProfileBackdropImageUrl());
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }
}
