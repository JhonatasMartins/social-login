package br.com.jhonatasmartins.social;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by jhonatas on 6/12/15.
 */
public class StartActivity extends AppCompatActivity {

    boolean isLogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();

        if (!isLogged){
            intent.setClass(this, LoginActivity.class);
        }else{
            intent.setClass(this, MainActivity.class);
        }

        startActivity(intent);
    }
}
