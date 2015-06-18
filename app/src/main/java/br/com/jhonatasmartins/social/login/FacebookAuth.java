package br.com.jhonatasmartins.social.login;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by jhonatas on 6/18/15.
 */
public class FacebookAuth extends Auth implements FacebookCallback<LoginResult> {

    public final String LOG_TAG = getClass().getName();

    LoginManager facebookLoginManager;
    CallbackManager facebookCallbackManager;
    Activity hostActivity;

    public FacebookAuth(Activity activity, OnAuthListener onAuthListener){
        FacebookSdk.sdkInitialize(activity.getApplicationContext());

        hostActivity = activity;
        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginManager = LoginManager.getInstance();
        facebookLoginManager.registerCallback(facebookCallbackManager, this);

        setOnAuthListener(onAuthListener);
    }

    @Override
    public void login() {
        facebookLoginManager.logInWithReadPermissions(hostActivity,
                Arrays.asList("public_profile", "email"));
    }

    @Override
    public void logout() {
        facebookLoginManager.logOut();
    }

    @Override
    public void revoke() {
        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token != null) {
            String user = token.getUserId();

            //url to revoke login DELETE {user_id}/permissions
            String graphPath = user + "/permissions";

            final GraphRequest requestLogout = new GraphRequest(token, graphPath, null, HttpMethod.DELETE, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    //call logout to clear token info and profile
                    logout();

                    onAuthListener.onRevoke();
                }
            });

            requestLogout.executeAsync();
        }else{
            onAuthListener.onLoginError("Token is null");
        }
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        GraphRequest meRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                String cover= "", name = "", email = "";

                try {
                    name = jsonObject.getString("name");
                    email = jsonObject.getString("email");
                    cover = jsonObject.getJSONObject("cover").getString("source");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

                requestProfilePhoto(name, email, cover);
            }
        });

        Bundle params = new Bundle();
        params.putString("fields", "name, email, cover");

        meRequest.setParameters(params);
        meRequest.executeAsync();
    }

    @Override
    public void onCancel() {
        onAuthListener.onLoginCancel();
    }

    @Override
    public void onError(FacebookException e) {
        Log.e(LOG_TAG, e.getMessage());
        onAuthListener.onLoginError(e.getMessage());
    }

    public CallbackManager getFacebookCallbackManager() {
        return facebookCallbackManager;
    }

    private void requestProfilePhoto(final String name, final String email, final String cover){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        final SocialProfile profile = new SocialProfile();
        profile.name = name;
        profile.email = email;
        profile.cover = cover;
        profile.network = Social.FACEBOOK;

        //url to get profile photo /{user-id}/picture?redirect=false
        String path = accessToken.getUserId() + "/picture";

        GraphRequest photoRequest = new GraphRequest(accessToken, path, null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                try {
                    profile.image = graphResponse.getJSONObject().getJSONObject("data").getString("url");
                }catch (JSONException e){
                    Log.e(LOG_TAG, e.getMessage());
                }

                onAuthListener.onLoginSuccess(profile);
            }
        });


        Bundle parameters = new Bundle();
        parameters.putBoolean("redirect", false);

        photoRequest.setParameters(parameters);
        photoRequest.executeAsync();
    }
}
