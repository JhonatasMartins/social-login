package br.com.jhonatasmartins.social;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.jhonatasmartins.social.login.Auth;
import br.com.jhonatasmartins.social.login.FacebookAuth;
import br.com.jhonatasmartins.social.login.GoogleAuth;
import br.com.jhonatasmartins.social.login.SimpleAuthListener;
import br.com.jhonatasmartins.social.login.Social;


public class MainActivity extends AppCompatActivity{

    Toolbar toolbar;
    NavigationView navigationView;
    TextView name;
    TextView email;
    ImageView cover;
    ImageView photo;
    Social socialNetwork;

    Auth auth;

    private SimpleAuthListener authListener = new SimpleAuthListener() {
        @Override
        public void onRevoke() {
            logoutUser();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.social_toolbar);
        navigationView = (NavigationView) findViewById(R.id.social_menu);
        name = (TextView) findViewById(R.id.social_name);
        email = (TextView) findViewById(R.id.social_email);
        cover = (ImageView) findViewById(R.id.social_cover);
        photo = (ImageView) findViewById(R.id.social_photo);

        setSupportActionBar(toolbar);
        setupUserInfo();

        //create correct auth manager according user account
        if (socialNetwork == Social.FACEBOOK){
            auth = new FacebookAuth(this, authListener);
        }else{
            auth = new GoogleAuth(this, authListener);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_logout){
            if(socialNetwork != null){
                auth.revoke();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupUserInfo(){
        Picasso picasso = Picasso.with(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String socialNetworkName = preferences.getString(LoginActivity.USER_SOCIAL, null);
        socialNetwork = Social.valueOf(socialNetworkName);

        name.setText(preferences.getString(LoginActivity.PROFILE_NAME, ""));
        email.setText(preferences.getString(LoginActivity.PROFILE_EMAIL, ""));

        picasso.load(preferences.getString(LoginActivity.PROFILE_COVER, ""))
                .into(cover);

        picasso.load(preferences.getString(LoginActivity.PROFILE_IMAGE, ""))
                .transform(new RoundedTransformation(getResources()))
                .into(photo);
    }

    private void logoutUser(){
        //clear share preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                         .clear()
                         .apply();

        //clear back stack activity and back to login activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        finishAffinity();
    }
}
