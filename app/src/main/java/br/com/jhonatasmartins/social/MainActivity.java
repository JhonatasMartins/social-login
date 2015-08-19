package br.com.jhonatasmartins.social;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.jhonatasmartins.social.login.Auth;
import br.com.jhonatasmartins.social.login.FacebookAuth;
import br.com.jhonatasmartins.social.login.GoogleAuth;
import br.com.jhonatasmartins.social.login.SimpleAuthListener;
import br.com.jhonatasmartins.social.login.SocialProfile;
import br.com.jhonatasmartins.social.login.TwitterAuth;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static final String DEFAULT_COVER = "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcQN9FVv2cnfoEW7pRKDh0OubDnHEG_BCg-jTEzYHzeaO7vIGbNo";
    static final String DEFAULT_IMAGE = "http://static.squarespace.com/static/523ce201e4b0cd883dbb8bbf/t/53bf2302e4b06e125c374401/1405035267633/profile-icon.png";
    final int REQUEST_CODE = 1000;

    Toolbar toolbar;
    NavigationView navigationView;
    TextView name;
    TextView email;
    ImageView cover;
    ImageView photo;
    EditText shareContent;
    TextView pickFilename;
    Button pickFile;
    Button share;
    String socialNetwork;

    Uri imageOrVideo;
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
        shareContent = (EditText) findViewById(R.id.social_share_content);
        pickFilename= (TextView) findViewById(R.id.pick_filename);
        pickFile = (Button) findViewById(R.id.pick_file);
        share = (Button) findViewById(R.id.share);

        pickFile.setOnClickListener(this);
        share.setOnClickListener(this);

        setSupportActionBar(toolbar);
        setupUserInfo();

        //create correct auth manager according user account
        if (socialNetwork.equals(SocialProfile.FACEBOOK)){
            auth = new FacebookAuth(this, authListener);
        } else if(socialNetwork.equals(SocialProfile.GOOGLE)){
            auth = new GoogleAuth(this, authListener);
            auth.login();
        }else{
            auth = new TwitterAuth(this, authListener);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (auth instanceof GoogleAuth){
            //disconnect google client api
            auth.logout();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            imageOrVideo = data.getData();
            pickFilename.setText(imageOrVideo.getPath());
        }
    }

    @Override
    public void onClick(View view) {

        int viewId = view.getId();

        if(viewId == R.id.pick_file){
            pickFile();
        }else if(viewId == R.id.share){
            auth.share(shareContent.getText().toString(), imageOrVideo);
        }

    }

    private void pickFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*, video/*");

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    private void setupUserInfo(){
        Picasso picasso = Picasso.with(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String socialNetworkName = preferences.getString(LoginActivity.USER_SOCIAL, null);
        socialNetwork = socialNetworkName;

        String profileName =  preferences.getString(LoginActivity.PROFILE_NAME, "");

        if (socialNetwork.equals(SocialProfile.TWITTER)){
            name.setText("@" + profileName);
        }else{
            name.setText(profileName);
        }

        email.setText(preferences.getString(LoginActivity.PROFILE_EMAIL, ""));

        picasso.load(preferences.getString(LoginActivity.PROFILE_COVER, DEFAULT_COVER))
                .into(cover);

        picasso.load(preferences.getString(LoginActivity.PROFILE_IMAGE, DEFAULT_IMAGE))
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
