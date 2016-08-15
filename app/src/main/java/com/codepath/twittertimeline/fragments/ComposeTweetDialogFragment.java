package com.codepath.twittertimeline.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.activities.ComposeActivity;
import com.codepath.twittertimeline.activities.TimelineActivity;
import com.codepath.twittertimeline.databinding.FragmentComposeBinding;
import com.codepath.twittertimeline.models.Tweet;
import com.codepath.twittertimeline.models.User;
import com.codepath.twittertimeline.prefs.SharedPreferenceUtils;
import com.codepath.twittertimeline.utils.UtilityMethods;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by Sharath on 8/4/16.
 */
public class ComposeTweetDialogFragment extends DialogFragment {
    TwitterClient client;
    private EditText etTweet;
    private ImageView ivCloseDialog;
    FragmentComposeBinding binding;
    Tweet baseTweet;

    public ComposeTweetDialogFragment(){
        client = TwitterApplication.getRestClient();
    }

    public static ComposeTweetDialogFragment newInstance(Tweet baseTweet){
        ComposeTweetDialogFragment composeTweetDialogFragment = new ComposeTweetDialogFragment();
        Bundle args = new Bundle();
        if(baseTweet != null){
            args.putParcelable(ComposeActivity.BASE_TWEET_OBJECT,baseTweet);
        }
        composeTweetDialogFragment.setArguments(args);
        return composeTweetDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null && getArguments().getParcelable(ComposeActivity.BASE_TWEET_OBJECT) != null){
            baseTweet = getArguments().getParcelable(ComposeActivity.BASE_TWEET_OBJECT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_compose,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etTweet = binding.etTweetText;
        etTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentText = s.toString();
                int diff = 140 - currentText.length();
                String displayLength = String.valueOf(diff);
                binding.tvCharacters.setText(displayLength);
                if(diff < 0){
                    binding.tvCharacters.setTextColor(getResources().getColor(R.color.colorAccent));
                }else{
                    binding.tvCharacters.setTextColor(getResources().getColor(R.color.twitter_actions_color));
                }
                if(s.length() == 0 || diff < 0){
                    binding.btnSubmitTweet.setEnabled(false);
                }else{
                    binding.btnSubmitTweet.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.btnSubmitTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postNewTweet();
            }
        });
        ivCloseDialog = binding.ivClose;
        ivCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        //Load current user profile
        User user = SharedPreferenceUtils.getUserInfoFromPreferences(getActivity());
        if(user != null){
            Picasso.with(getActivity()).load(user.getProfileImageUrl())
                    .transform(new CropCircleTransformation())
                    .into(binding.ivProfileImage);
        }

        if (baseTweet != null) {
            binding.tvReplyTo.setVisibility(View.VISIBLE);
            binding.ivReplyIcon.setVisibility(View.VISIBLE);
            binding.tvReplyTo.setText(String.format(getResources().getString(R.string.in_reply_to), baseTweet.getUser().getName()));
            binding.etTweetText.setText("@" + baseTweet.getUser().getScreenName() + " ");
            binding.etTweetText.setSelection(binding.etTweetText.getText().length());
        }
    }
    @Override
    public void onResume() {
        // Get existing layout params for the window
        final ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    private void sendResult(Tweet tweet){
        if(getTargetFragment() == null)
            return;

        Intent data = new Intent();
        data.putExtra(ComposeActivity.TWEET_OBJECT,tweet);
        getTargetFragment().onActivityResult(getTargetRequestCode(),Activity.RESULT_OK,data);
        dismiss();
    }
    private void postNewTweet(){
        if(!UtilityMethods.isOnline(getActivity())){
            Toast.makeText(getActivity(), R.string.no_internet,Toast.LENGTH_SHORT).show();
            return;
        }
        binding.flLoading.setVisibility(View.VISIBLE);
        binding.etTweetText.setEnabled(false);
        Long replyToTweetId = null;
        if(baseTweet != null){
            replyToTweetId = baseTweet.getUid();
        }
        client.postNewTweet(etTweet.getText().toString(),replyToTweetId,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Tweet tweet = Tweet.fromJSON(response);
                sendResult(tweet);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                binding.flLoading.setVisibility(View.GONE);
                binding.etTweetText.setEnabled(true);
                Toast.makeText(getActivity(),"Error submitting tweet.",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
