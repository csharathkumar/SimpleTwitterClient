package com.codepath.twittertimeline.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.fragments.ComposeTweetDialogFragment;
import com.codepath.twittertimeline.fragments.HomeTimelineFragment;
import com.codepath.twittertimeline.fragments.MentionsTimelineFragment;
import com.codepath.twittertimeline.models.User;
import com.codepath.twittertimeline.prefs.SharedPreferenceUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {
    private static final String TAG = TimelineActivity.class.getSimpleName();

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.fab)
    FloatingActionButton fabCompose;
    TwitterClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);
        client = TwitterApplication.getRestClient();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager.setAdapter(new TweetsFragmentPagerAdapter(getSupportFragmentManager(),TimelineActivity.this));
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0){
                    fabCompose.setVisibility(View.VISIBLE);
                }else{
                    fabCompose.setVisibility(View.GONE);
                }
                //DrawableCompat.setTint(tabLayout.getTabAt(position).getIcon(), ContextCompat.getColor(TimelineActivity.this, R.color.colorAccent));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //fetch current user info and store it in shared preferences
        if(!SharedPreferenceUtils.hasTotalInfoAboutCurrentUser(this)){
            client.getUserInfo(true, "", new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    User user = User.fromJSONObject(response);
                    SharedPreferenceUtils.storeUserInformation(TimelineActivity.this,user);
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_timeline,menu);
        MenuItem profile = menu.findItem(R.id.profile);
        profile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Launch the profile view
                Intent intent = new Intent(TimelineActivity.this,ProfileActivity.class);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
    }

    public CoordinatorLayout getCoordinatorLayout(){
        return coordinatorLayout;
    }

    public FloatingActionButton getFabCompose() {
        return fabCompose;
    }

    public void onProfileView(MenuItem item) {

    }
    public class TweetsFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[] { "Timeline", "Mentions"};
        private int[] tabIcons = {
                R.drawable.ic_timeline,
                R.drawable.ic_mentions
        };
        private Context context;

        public TweetsFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    HomeTimelineFragment fragment = new HomeTimelineFragment();
                    return fragment;
                case 1:
                    MentionsTimelineFragment mentionsTimelineFragment = new MentionsTimelineFragment();
                    return mentionsTimelineFragment;
            }
            return new HomeTimelineFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            Drawable image = ContextCompat.getDrawable(TimelineActivity.this,tabIcons[position]);//context.getResources().getDrawable(tabIcons[position]);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            // Replace blank spaces with image icon
            SpannableString sb = new SpannableString("   " + tabTitles[position]);
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }
    }
}
