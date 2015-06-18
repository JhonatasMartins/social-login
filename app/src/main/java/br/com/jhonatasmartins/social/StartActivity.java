package br.com.jhonatasmartins.social;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by jhonatas on 6/12/15.
 */
public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();

        if (isLogged()){
            intent.setClass(this, MainActivity.class);
        }else{
            intent.setClass(this, LoginActivity.class);
        }

        startActivity(intent);
    }

    private boolean isLogged(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(LoginActivity.USER_AUTHENTICATED, false);
    }
}
