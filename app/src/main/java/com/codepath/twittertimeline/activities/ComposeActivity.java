package com.codepath.twittertimeline.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.databinding.ActivityComposeBinding;
import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.UiUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {
    private static final String TAG = ComposeActivity.class.getSimpleName();
    public static final int COMPOSE_TWEET_REQUEST_CODE = 1;
    public static final int REPLY_TWEET_REQUEST_CODE = 2;
    public static final String TWEET_OBJECT = "tweet_object";
    public static final String BASE_TWEET_OBJECT = "initial_tweet_object";
    public static final String IS_REPLY = "is_reply";

    ActivityComposeBinding binding;
    CoordinatorLayout coordinatorLayout;
    TextView tvReplyTo;
    EditText etTweet;
    TextView tvCharacters;
    Button btSubmit;
    TwitterClient client;

    boolean isReply;
    Tweet mInitialTweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_compose);
        client = TwitterApplication.getRestClient();
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        coordinatorLayout = binding.coordinatorLayout;
        tvReplyTo = binding.contentView.tvReplyTo;
        etTweet = binding.contentView.etTweet;
        tvCharacters = binding.contentView.tvCharacters;
        btSubmit = binding.contentView.submit;

        if(getIntent() != null && getIntent().getBooleanExtra(IS_REPLY,false)){
            isReply = true;
            mInitialTweet = getIntent().getParcelableExtra(BASE_TWEET_OBJECT);
            tvReplyTo.setVisibility(View.VISIBLE);
            tvReplyTo.setText("In reply to "+mInitialTweet.getUser().getName());
        }

        etTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentText = s.toString();
                int diff = 140 - currentText.length();
                String displayLength = String.valueOf(diff);
                tvCharacters.setText(displayLength);
                if(diff < 0){
                    tvCharacters.setTextColor(getResources().getColor(R.color.colorAccent));
                }else{
                    tvCharacters.setTextColor(getResources().getColor(R.color.twitter_actions_color));
                }
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweet = etTweet.getText().toString();
                if(!tweet.isEmpty() && tweet.length() <= 140){
                    if(isReply){
                        postAReply(tweet);
                    }else{
                        postNewTweet(tweet);
                    }
                }else{
                    UiUtils.showSnackBar(coordinatorLayout,getString(R.string.check_tweet_length));
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

    private void postNewTweet(String tweet){
        client.postNewTweet(tweet,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Tweet tweet = Tweet.fromJSON(response);
                Intent data = new Intent();
                data.putExtra(TWEET_OBJECT,tweet);
                setResult(Activity.RESULT_OK,data);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
    private void postAReply(String tweet){
        client.postNewReply(mInitialTweet.getUid(),tweet,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Tweet tweet = Tweet.fromJSON(response);
                Intent data = new Intent();
                data.putExtra(TWEET_OBJECT,tweet);
                setResult(Activity.RESULT_OK,data);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

}
