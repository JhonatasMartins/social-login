package br.com.jhonatasmartins.social;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.Arrays;


public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener, FacebookCallback<LoginResult>,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    Button facebookButton;
    Button googleButton;

    LoginManager facebookLoginManager;
    CallbackManager facebookCallbackManager;
    GoogleApiClient googleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FacebookSdk.sdkInitialize(getApplicationContext());

        facebookButton = (Button)findViewById(R.id.login_facebook);
        googleButton = (Button)findViewById(R.id.login_google);

        facebookButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);

        setupFacebookLogin();
        setupGoogleLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token != null){
            Log.e("LoginActivity", "user is logged with facebook");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient.isConnected()){
            Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            //clear shared preferences info
                        }
                    }
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.login_facebook){
            facebookLoginManager.logInWithReadPermissions(this,
                                                          Arrays.asList("public_profile", "email"));
        }else{
            if(!googleApiClient.isConnected()){
                googleApiClient.connect();
            }
        }

    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        //facebook
        //save on shared preferences user is logged with facebook
        //TODO: onSuccess
    }

    @Override
    public void onCancel() {
        //facebook
        //TODO: onCancel
    }

    @Override
    public void onError(FacebookException e) {
        //facebook
        //TODO: onConnected
    }

    @Override
    public void onConnected(Bundle bundle) {
        //google plus
        Person person = Plus.PeopleApi.getCurrentPerson(googleApiClient);

        if (person != null){
            Log.e("teste",  " person name "+ person.getDisplayName());
            if (person.hasCover()){
                Person.Cover cover = person.getCover();

                if (cover.hasCoverPhoto()){
                    Log.e("teste", " cover url" + cover.getCoverPhoto().getUrl());
                }
            }

            if(person.hasImage()){
                Log.e("teste", " image url" + person.getImage().getUrl());
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        //google plus
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //google plus
        if(connectionResult.hasResolution()){
            try {
                startIntentSenderForResult(connectionResult.getResolution().getIntentSender(),
                        10001, null, 0, 0, 0);
            }catch (IntentSender.SendIntentException e){
                Log.e("Social", e.getMessage());
                googleApiClient.connect();
            }
        }
    }

    private void setupFacebookLogin() {
        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginManager = LoginManager.getInstance();

        facebookLoginManager.registerCallback(facebookCallbackManager, this);
    }

    private void setupGoogleLogin() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }
}
