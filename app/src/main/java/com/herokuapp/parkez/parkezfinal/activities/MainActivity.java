package com.herokuapp.parkez.parkezfinal.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.herokuapp.parkez.parkezfinal.R;
import com.herokuapp.parkez.parkezfinal.models.User;
import com.herokuapp.parkez.parkezfinal.receivers.NetworkStateReceiver;

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
    private NetworkStateReceiver receiver;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check the network connection
        sharedpreferences = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        login_btn = (Button) findViewById(R.id.btn_login);
        signup_btn = (Button) findViewById(R.id.btn_signup);
        if (!isConnected()) {
            Toast.makeText(getApplicationContext(), "You are not currently connected.",
                    Toast.LENGTH_LONG).show();
            login_btn.setEnabled(false);
            signup_btn.setEnabled(false);
            return;
        }

        final User user = getUser();
        if (user != null) {
            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapIntent);
            Toast.makeText(getApplicationContext(), "Logged in as " + user.getUid(), Toast.LENGTH_SHORT).show();
        }

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

    protected boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null) &&
                activeNetwork.isConnectedOrConnecting();
    }

    public SharedPreferences getSharedpreferences() {
        return sharedpreferences;
    }


    @Override
    protected void onResume() {
        this.receiver = new NetworkStateReceiver();
        registerReceiver(
                this.receiver,
                new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(this.receiver);
        super.onPause();
    }


    protected void saveUserAuthenticationData(User user, SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(UID, user.getUid());
        editor.putString(ClientID, user.getClientId());
        editor.putString(TOKEN, user.getToken());
        editor.putString(EXPIRY, user.getExpiry());
        if (!user.getName().isEmpty()) {
            editor.putString(NAME, user.getName());
        }
        editor.apply();

    }

    protected User getUser() {
        String uid = sharedpreferences.getString(UID, "");
        String token = sharedpreferences.getString(TOKEN, "");
        String expiry = sharedpreferences.getString(EXPIRY, "");
        String clientId = sharedpreferences.getString(ClientID, "");
        String name = sharedpreferences.getString(NAME, "");
        if (uid.isEmpty() || token.isEmpty() || expiry.isEmpty() || clientId.isEmpty()) {
            return null;
        } else {
            return new User(uid, token, clientId, expiry, name);
        }
    }

}
