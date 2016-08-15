package com.codepath.twittertimeline.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Created by Sharath on 8/14/16.
 */
public class FollowersRecyclerAdapter extends RecyclerView.Adapter<FollowersRecyclerAdapter.ViewHolder>{
    private Context mContext;
    private List<User> mUsers;
    public FollowersRecyclerAdapter(Context context, List<User> users){
        mContext = context;
        mUsers = users;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View tweetView = inflater.inflate(R.layout.item_follower,parent,false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.tvUserName.setText(user.getName());
        holder.tvScreenName.setText("@"+user.getScreenName());
        Picasso.with(mContext).load(user.getProfileImageUrl())
                .transform(new CropCircleTransformation())
                .into(holder.ivProfile);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivProfile;
        TextView tvUserName;
        TextView tvScreenName;
        public ViewHolder(final View itemView) {
            super(itemView);
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            tvScreenName = (TextView) itemView.findViewById(R.id.tvScreenName);
        }
    }
}
