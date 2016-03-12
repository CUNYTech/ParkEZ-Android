package com.herokuapp.parkez.parkezfinal.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.herokuapp.parkez.parkezfinal.R;

public class MainActivity extends FragmentActivity {

    private Button login_btn = null;
    private Button signup_btn = null;
    protected SharedPreferences sharedpreferences;// Shared preference variable
    private static final String USER_PREFS = "USER PREFS";
    protected static final String UID = "Uid";
    protected static final String ClientID = "Client";
    protected static final String TOKEN = "Token";
    protected static final String EXPIRY = "Expiry";
    protected static final String NAME = "Name";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check for orientation
        setContentView(R.layout.activity_main_port);
        /*
        if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_land);
        }
        */
        setContentView(R.layout.activity_main);
        sharedpreferences = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        login_btn = (Button) findViewById(R.id.btn_login);
        signup_btn = (Button) findViewById(R.id.btn_signup);

        // listen to login button to enter into login page
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
        // listen to sign up button to enter into sign up page
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signupIntent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(signupIntent);
            }
        });
    }

    public SharedPreferences getSharedpreferences() {
        return sharedpreferences;
    }

    public void setSharedpreferences(SharedPreferences sharedpreferences) {
        this.sharedpreferences = sharedpreferences;
    }

    public static String getUserPrefs() {
        return USER_PREFS;
    }

    public static String getUID() {
        return UID;
    }

    public static String getClientID() {
        return ClientID;
    }

    public static String getTOKEN() {
        return TOKEN;
    }

    public static String getEXPIRY() {
        return EXPIRY;
    }

    public static String getNAME() {
        return NAME;
    }


    /*
    // change layout when user switches orientation
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // check orientation and set the respective layout
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main_port);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_land);
        }
    }
    */
}
