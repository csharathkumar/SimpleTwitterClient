package com.codepath.twittertimeline.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.fragments.FollowersActivityFragment;
import com.codepath.twittertimeline.fragments.ProfileActivityFragment;

public class FollowersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        ;
        boolean isFollowers = getIntent().getBooleanExtra(FollowersActivityFragment.IS_FOLLOWERS,false);
        String screenName = getIntent().getStringExtra(FollowersActivityFragment.SCREEN_NAME);
        if(isFollowers){
            getSupportActionBar().setTitle("Followers");
        }else{
            getSupportActionBar().setTitle("Friends");
        }

        if(savedInstanceState == null){
            //Create user timeline fragment
            FollowersActivityFragment followersActivityFragment = FollowersActivityFragment.newInstance(isFollowers,screenName);
            //Display the fragment
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container,followersActivityFragment);
            ft.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }
}
