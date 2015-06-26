package br.com.jhonatasmartins.social.login;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

/**
 * Created by jhonatas on 6/18/15.
 */
public class GoogleAuth extends Auth
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final int GOOGLE_SIGN_IN = 100000001;
    final String LOG_TAG = getClass().getName();

    GoogleApiClient googleApiClient;
    Activity hostActivity;

    public GoogleAuth(Activity activity, OnAuthListener authListener){
        hostActivity = activity;
        googleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        setOnAuthListener(authListener);
    }

    @Override
    public void login() {
        if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()){
            googleApiClient.connect();
        }
    }

    @Override
    public void logout() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void revoke() {
        if (googleApiClient.isConnected()){
            Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            onAuthListener.onRevoke();
                        }
                    }
            );
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //get info from profile
        SocialProfile profile = getProfileInfo();
        onAuthListener.onLoginSuccess(profile);
    }

    @Override
    public void onConnectionSuspended(int cause) {

        googleApiClient.connect();
    }

    @Override
   public void onConnectionFailed(ConnectionResult connectionResult) {
        if(connectionResult.hasResolution()){

            try {
                /** you must call {@link #login()} again on
                 * onActivityResult method if requestCode equals #GOOGLE_SIGN_IN
                 * */
                hostActivity.startIntentSenderForResult(connectionResult.getResolution().getIntentSender(),
                        GOOGLE_SIGN_IN, null, 0, 0, 0);
            }catch (IntentSender.SendIntentException e){
                Log.e(LOG_TAG, e.getMessage());
                googleApiClient.connect();
            }
        }else{
            String message = "Google Plus Error: "+ connectionResult.getErrorCode();
            Log.e(LOG_TAG, message);

            onAuthListener.onLoginError(message);
        }
    }

    private SocialProfile getProfileInfo(){
        SocialProfile profile = new SocialProfile();

        Person person = Plus.PeopleApi.getCurrentPerson(googleApiClient);

        profile.email = Plus.AccountApi.getAccountName(googleApiClient);
        profile.name = person.getDisplayName();
        profile.network = Social.GOOGLE;

        if (person.hasCover()){
            Person.Cover cover = person.getCover();

            if (cover.hasCoverPhoto()){
                profile.cover = cover.getCoverPhoto().getUrl();
            }
        }

        if(person.hasImage()){
            profile.image = person.getImage().getUrl();
        }

        return profile;
    }
}
