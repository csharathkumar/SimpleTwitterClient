package com.codepath.twittertimeline.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.models.Media;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.Constants;
import com.codepath.twittertimeline.utils.UtilityMethods;
import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.utils.Constants;
import com.codepath.twittertimeline.utils.UtilityMethods;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by Sharath on 8/3/16.
 */
public class TweetsRecyclerAdapter extends RecyclerView.Adapter<TweetsRecyclerAdapter.TweetViewHolder> {
    private Context mContext;
    private List<Tweet> mTweets;
    // Define listener member variable
    private OnItemClickListener listener;
    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    /*public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }*/
    public TweetsRecyclerAdapter(Context context, List<Tweet>tweets, OnItemClickListener clickListener){
        mContext = context;
        mTweets = tweets;
        listener = clickListener;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View tweetView = inflater.inflate(R.layout.item_tweet,parent,false);
        TweetViewHolder tweetViewHolder = new TweetViewHolder(tweetView);
        return tweetViewHolder;
    }

    @Override
    public void onBindViewHolder(TweetViewHolder holder, int position) {
        Tweet tweet = mTweets.get(position);
        holder.tvUsername.setText(tweet.getUser().getName());
        holder.tvScreenName.setText("@"+tweet.getUser().getScreenName());
        holder.tvTimestamp.setText(UtilityMethods.getTimeDifference(tweet.getCreatedAt()));
        holder.tvBody.setText(tweet.getBody());
        holder.ivProfileImage.setImageResource(0);
        holder.ivProfileImage.setTag(tweet.getUser().getScreenName());
        Picasso.with(mContext)
                .load(tweet.getUser().getProfileImageUrl())
                .transform(new CropCircleTransformation())
                .fit().centerCrop()
                .into(holder.ivProfileImage);

        if(tweet.isFavorited()){
            holder.ivFavorite.setImageResource(R.drawable.ic_favorited);
        }else{
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_twitter);
        }
        if(tweet.getFavoritesCount() > 0){
            holder.tvFavoritesCount.setVisibility(View.VISIBLE);
            holder.tvFavoritesCount.setText(String.valueOf(tweet.getFavoritesCount()));
        }else{
            holder.tvFavoritesCount.setVisibility(View.GONE);
        }
        if(tweet.getRetweetCount() > 0){
            holder.tvRetweetCount.setVisibility(View.VISIBLE);
            holder.tvRetweetCount.setText(String.valueOf(tweet.getRetweetCount()));
        }else{
            holder.tvRetweetCount.setVisibility(View.GONE);
        }
        if(tweet.isRetweet()){
            if(tweet.isRetweeted()){
                holder.tvRetweetReplyInfo.setText("You Retweeted");
                holder.ivRetweet.setImageResource(R.drawable.ic_retweeted);
            }else{
                holder.tvRetweetReplyInfo.setText(tweet.getRetweetUserName()+" Retweeted");
                holder.ivRetweet.setImageResource(R.drawable.ic_retweet_downloaded);
            }
            holder.tvRetweetReplyInfo.setVisibility(View.VISIBLE);
        }else{
            holder.tvRetweetReplyInfo.setVisibility(View.GONE);
            holder.ivRetweet.setImageResource(R.drawable.ic_retweet_downloaded);
        }
        /*if(tweet.getInReplyToUserName() != null){
            holder.tvRetweetReplyInfo.setText("In reply to "+tweet.getInReplyToUserName());
            holder.tvRetweetReplyInfo.setVisibility(View.VISIBLE);
        }else{
            holder.tvRetweetReplyInfo.setVisibility(View.GONE);
        }*/
        Media media = tweet.getMedia();
        Media extendedMedia = tweet.getExtendedMedia();
        if(media != null){
            holder.mediaLayout.setVisibility(View.VISIBLE);
            String imageToLoad = "";
            if(extendedMedia != null && extendedMedia.getType().equalsIgnoreCase(Constants.VIDEO_KEY)){
                holder.playImageButton.setVisibility(View.VISIBLE);
                imageToLoad = extendedMedia.getMediaUrlHttps();
            }else{
                holder.playImageButton.setVisibility(View.GONE);
                imageToLoad = media.getMediaUrlHttps();
            }
            holder.ivImage.setImageResource(0);
            Picasso.with(mContext)
                    .load(imageToLoad+":small")
                    .fit().centerCrop()
                    .into(holder.ivImage);
        }else{
            holder.mediaLayout.setVisibility(View.GONE);
        }
    }
    public long getMaxTweetId(){
        if(mTweets != null && !mTweets.isEmpty()){
            Tweet tweet = mTweets.get(mTweets.size()-1);
            return tweet.getUid();
        }
        return 0;
    }

    public void addItemAtPosition(Tweet tweet, int position){
        mTweets.add(position,tweet);
        notifyItemInserted(position);
    }

    public void replaceItemAtPosition(Tweet tweet, int position){
        mTweets.set(position,tweet);
        notifyItemChanged(position);
    }
    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    public class TweetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.tvRetweetReplyInfo)
        TextView tvRetweetReplyInfo;
        @BindView(R.id.ivProfile)
        ImageView ivProfileImage;
        @BindView(R.id.tvBody)
        TextView tvBody;
        @BindView(R.id.tvUserName)
        TextView tvUsername;
        @BindView(R.id.tvScreenName)
        TextView tvScreenName;
        @BindView(R.id.tvTimestamp)
        TextView tvTimestamp;
        @BindView(R.id.mediaLayout)
        FrameLayout mediaLayout;
        @BindView(R.id.ivImage)
        ImageView ivImage;
        @BindView(R.id.playButton)
        ImageView playImageButton;
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

        public TweetViewHolder(final View itemView){
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
            ivReply.setOnClickListener(this);
            ivFavorite.setOnClickListener(this);
            ivRetweet.setOnClickListener(this);
            ivImage.setOnClickListener(this);
            playImageButton.setOnClickListener(this);
            ivProfileImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v,getLayoutPosition());
        }
    }
}
