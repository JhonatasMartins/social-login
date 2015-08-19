package br.com.jhonatasmartins.social.login;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import br.com.jhonatasmartins.social.R;
import io.fabric.sdk.android.Fabric;

/**
 * Created by jhonatas on 8/18/15.
 */
public class TwitterAuth extends Auth{

    final String LOG_TAG = getClass().getName();

    Activity activity;
    TwitterAuthClient authClient;

    public TwitterAuth(Activity activity, OnAuthListener authListener){
        TwitterAuthConfig authConfig = new TwitterAuthConfig(activity.getString(R.string.twitter_key),
                activity.getString(R.string.twitter_secret));
        Fabric.with(activity, new Twitter(authConfig), new TweetComposer());

        authClient = new TwitterAuthClient();
        this.activity = activity;
        setOnAuthListener(authListener);
    }

    @Override
    public void login() {
        authClient.authorize(activity, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                SocialProfile socialProfile = new SocialProfile();
                socialProfile.name = session.getUserName();
                socialProfile.network = SocialProfile.TWITTER;

                onAuthListener.onLoginSuccess(socialProfile);
            }

            @Override
            public void failure(TwitterException e) {
                Log.e(LOG_TAG, e.getMessage());
                onAuthListener.onLoginError(e.getMessage());
            }
        });
    }

    @Override
    public void logout() {
    }

    @Override
    public void revoke() {
        TwitterSession session = Twitter.getSessionManager().getActiveSession();

        if (session != null){
            Twitter.getSessionManager().clearActiveSession();
        }

        onAuthListener.onRevoke();
    }

    @Override
    public void share(String content, Uri imageOrVideo) {

        TweetComposer.Builder builder = new TweetComposer.Builder(activity)
                .text(content);

        if(imageOrVideo != null) {
            builder.image(imageOrVideo);
        }

        builder.show();
    }

    public TwitterAuthClient getAuthClient() {
        return authClient;
    }
}
