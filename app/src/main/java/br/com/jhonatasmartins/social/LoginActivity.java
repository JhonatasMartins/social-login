package br.com.jhonatasmartins.social;

import android.content.Intent;
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

import java.util.Arrays;


public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener, FacebookCallback<LoginResult>{

    Button facebookButton;
    Button googleButton;

    LoginManager facebookLoginManager;
    CallbackManager facebookCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FacebookSdk.sdkInitialize(getApplicationContext());

        facebookButton = (Button)findViewById(R.id.login_facebook);
        googleButton = (Button)findViewById(R.id.login_google);

        facebookButton.setOnClickListener(this);
        googleButton.setOnClickListener(this);

        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginManager = LoginManager.getInstance();

        facebookLoginManager.registerCallback(facebookCallbackManager, this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.login_facebook){
            facebookLoginManager.logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        }

    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        //save on shared preferences user is logged with facebook
    }

    @Override
    public void onCancel() {}

    @Override
    public void onError(FacebookException e) {}
}
