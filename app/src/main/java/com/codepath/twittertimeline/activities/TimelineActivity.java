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
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.fab)
    FloatingActionButton fabCompose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*ComposeTweetDialogFragment composeTweetDialogFragment = ComposeTweetDialogFragment.newInstance();
                composeTweetDialogFragment.show(getSupportFragmentManager(),"Compose");*/
                Intent intent = new Intent(TimelineActivity.this, ComposeActivity.class);
                startActivityForResult(intent,ComposeActivity.COMPOSE_TWEET_REQUEST_CODE);
            }
        });

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public CoordinatorLayout getCoordinatorLayout(){
        return coordinatorLayout;
    }


}
