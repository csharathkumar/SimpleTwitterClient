package com.codepath.twittertimeline.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.twittertimeline.R;
import com.codepath.twittertimeline.TwitterApplication;
import com.codepath.twittertimeline.TwitterClient;
import com.codepath.twittertimeline.activities.TimelineActivity;
import com.codepath.twittertimeline.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Sharath on 8/4/16.
 */
public class ComposeTweetDialogFragment extends DialogFragment {
    TwitterClient client;
    private EditText etTweet;
    public ComposeTweetDialogFragment(){
        client = TwitterApplication.getRestClient();
    }

    public static ComposeTweetDialogFragment newInstance(){
        ComposeTweetDialogFragment composeTweetDialogFragment = new ComposeTweetDialogFragment();

        return composeTweetDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.compose_tweet, null);
        etTweet = (EditText) view.findViewById(R.id.etTweet);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(getString(R.string.compose));
        alertDialogBuilder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "Positive Action", Toast.LENGTH_SHORT).show();
                ((TimelineActivity)getActivity()).postNewTweet(etTweet.getText().toString());
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "Negative Action", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        /*etTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int diff = 140 - s.length();
                Log.d("DEBUG", "Difference in length - "+diff);
                String displayLength = String.valueOf(diff);
                etTweet.setText(displayLength);
            }
        });*/
        return alertDialogBuilder.create();
    }
    private void postNewTweet(){
        client.postNewTweet(etTweet.getText().toString(),new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Tweet tweet = Tweet.fromJSON(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
}
