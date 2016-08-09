package com.codepath.twittertimeline.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.models.Media;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.Constants;
import com.codepath.twittertimeline.utils.UiUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class MediaActivity extends AppCompatActivity {
    public static final String TWEET_TO_DISPLAY = "tweet_to_display";
    public static final String TWEET_POSITION = "tweet_position";
    public static final String MODIFIED_TWEET = "modified_tweet";
    public static final String IS_TWEET_MODIFIED = "is_tweet_modified";

    public static final int OPEN_MEDIA_ACTIVITY_REQUEST_CODE = 101;
    private static final String TAG = MediaActivity.class.getSimpleName();

    TwitterClient client;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.ivImage)
    ImageView ivImage;
    @BindView(R.id.videoView)
    VideoView videoView;
    @BindView(R.id.actionFavorite)
    ImageView ivFavorite;
    @BindView(R.id.tvFavoritesCount)
    TextView tvFavoritesCount;
    @BindView(R.id.tvRetweetCount)
    TextView tvRetweetCount;
    @BindView(R.id.actionRetweet)
    ImageView ivRetweet;
    @BindView(R.id.actionReply)
    ImageView ivReply;
    int mPosition;
    Tweet mTweet;
    boolean tweetModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        ButterKnife.bind(this);
        client = TwitterApplication.getRestClient();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.image);
        mTweet = getIntent().getParcelableExtra(TWEET_TO_DISPLAY);
        mPosition = getIntent().getIntExtra(TWEET_POSITION,0);
        Glide.with(this)
                .load(mTweet.getMedia().getMediaUrlHttps()+":large")
                .into(ivImage);
        ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retweetTweet();
            }
        });
        ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteTweet();
            }
        });
        ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaActivity.this,ComposeActivity.class);
                intent.putExtra(ComposeActivity.IS_REPLY,true);
                intent.putExtra(ComposeActivity.BASE_TWEET_OBJECT,mTweet);
                startActivityForResult(intent,ComposeActivity.REPLY_TWEET_REQUEST_CODE);
            }
        });
        setIcons();
        Media extendedMedia = mTweet.getExtendedMedia();
        if(extendedMedia != null){
            if(extendedMedia.getType().equalsIgnoreCase(Constants.VIDEO_KEY)){
                getSupportActionBar().setTitle(R.string.video);
                MediaController videoControl = new MediaController(this);
                videoControl.setAnchorView(videoView);
                videoView.setMediaController(videoControl);
                ivImage.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                Uri videoUri = Uri.parse(extendedMedia.getVideoUrlHttps());
                videoView.setVideoURI(videoUri);
                videoView.start();
            }else{
                ivImage.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
            }
        }
    }
    private void setIcons(){
        if(mTweet.isFavorited()){
            ivFavorite.setImageResource(R.drawable.ic_favorited);
        }else{
            ivFavorite.setImageResource(R.drawable.ic_favorite_white);
        }
        if(mTweet.isRetweeted()){
            ivRetweet.setImageResource(R.drawable.ic_retweeted);
        }else{
            ivRetweet.setImageResource(R.drawable.ic_retweet_white);
        }
        ivReply.setImageResource(R.drawable.ic_reply_white);

        if(mTweet.getFavoritesCount() > 0){
            tvFavoritesCount.setVisibility(View.VISIBLE);
            tvFavoritesCount.setText(String.valueOf(mTweet.getFavoritesCount()));
            tvFavoritesCount.setTextColor(ContextCompat.getColor(MediaActivity.this,R.color.white));
        }else{
            tvFavoritesCount.setVisibility(View.GONE);
        }
        if(mTweet.getRetweetCount() > 0){
            tvRetweetCount.setVisibility(View.VISIBLE);
            tvRetweetCount.setText(String.valueOf(mTweet.getRetweetCount()));
            tvRetweetCount.setTextColor(ContextCompat.getColor(MediaActivity.this,R.color.white));
        }else{
            tvRetweetCount.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_media,menu);
        MenuItem item = menu.findItem(R.id.share);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                // pass in the URL currently being used by the WebView
                shareIntent.putExtra(Intent.EXTRA_TEXT, mTweet.getBody());
                startActivity(shareIntent);
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ComposeActivity.REPLY_TWEET_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                if(data != null){
                    Tweet repliedTweet = data.getParcelableExtra(ComposeActivity.TWEET_OBJECT);
                    UiUtils.showSnackBar(coordinatorLayout,getString(R.string.replied_successfully));
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult();
    }

    public void setResult(){
        Intent intent = new Intent();
        if(tweetModified){
            intent.putExtra(MODIFIED_TWEET,mTweet);
            intent.putExtra(IS_TWEET_MODIFIED,true);
        }
        intent.putExtra(TWEET_POSITION,mPosition);
        setResult(Activity.RESULT_OK,intent);

        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            setResult();
        }
        return super.onOptionsItemSelected(item);
    }

    private void favoriteTweet() {
        boolean create = !mTweet.isRetweeted();;
        client.favoriteTweet(create,mTweet.getUid(),new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //JSONObject jsonObject = response.getJSONObject(0);
                    mTweet = Tweet.fromJSON(response);
                    tweetModified = true;
                    UiUtils.showSnackBar(coordinatorLayout,getString(R.string.favorited));
                    setIcons();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG,"Error while favoriting a tweet - "+errorResponse.toString());
                UiUtils.showSnackBar(coordinatorLayout,getString(R.string.favorite_successful));
            }
        });
    }

    private void retweetTweet(){
        boolean create = !mTweet.isRetweeted();
        client.retweet(create,mTweet.getUid(),new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    mTweet = Tweet.fromJSON(response);
                    tweetModified = true;
                    setIcons();
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
}
