package com.codepath.twittertimeline.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.models.Tweet;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Sharath on 8/2/16.
 */
public class TweetArrayAdapter extends ArrayAdapter<Tweet> {
    public TweetArrayAdapter(Context context, List<Tweet> tweets) {
        super(context, android.R.layout.simple_list_item_1, tweets);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet,parent,false);
        }
        ImageView ivProfileImage = (ImageView) convertView.findViewById(R.id.ivProfile);
        TextView tvBody = (TextView) convertView.findViewById(R.id.tvBody);
        TextView tvUsername = (TextView) convertView.findViewById(R.id.tvUserName);

        Tweet tweet = getItem(position);
        tvUsername.setText(tweet.getUser().getName());
        tvBody.setText(tweet.getBody());
        ivProfileImage.setImageResource(0);
        Picasso.with(getContext())
                .load(tweet.getUser().getProfileImageUrl())
                .into(ivProfileImage);
        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }
}
