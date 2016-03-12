package com.herokuapp.parkez.parkezfinal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.herokuapp.parkez.parkezfinal.MapsActivity;
import com.herokuapp.parkez.parkezfinal.R;
import com.herokuapp.parkez.parkezfinal.models.User;
import com.herokuapp.parkez.parkezfinal.web.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LoginActivity extends BaseActivity {
    private EditText email; // email address
    private EditText password; // password
    private Button button; // le login button
    private OkHttpClient client = WebUtils.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.et_email);
        password = (EditText) findViewById(R.id.et_password);
        button = (Button) findViewById(R.id.btn_login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get user input for login
                final String userEmail = email.getText().toString();
                final String userPassword = password.getText().toString();

                // check for empty fields for email and password
                if(userEmail.isEmpty()) {
                    email.setError("Email is required to proceed.");
                }
                if(userPassword.isEmpty() || userPassword.length()<8) {
                    password.setError("Password is required.");
                }

                Toast.makeText(getApplicationContext(), "Processing Login...",
                        Toast.LENGTH_LONG).show();
                // disable button after login click
                button.setEnabled(false);

                // create JSON object
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email",(Object)userEmail);
                    jsonObject.put("password",(Object)userPassword);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Request request = WebUtils.getRequest("/auth/sign_in").
                            post(WebUtils.getBody(WebUtils.JSON, jsonObject.toString())).build();
                client.newCall(request).enqueue(new Callback() {
                    // login fails, log the exception
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("[login api call]",e.getMessage());
                       button.setEnabled(true);
                    }

                    // server responses,
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(!response.isSuccessful()) {
                            // show message to user
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Username and/or password is incorrect.",Toast.LENGTH_LONG).show();
                                    button.setEnabled(true);
                                }
                            });
                        } else {
                            User user = WebUtils.getTokenAuthenticationDetails(response); //TODO: persist this.

                            Log.d("[login]:", user.getToken() + "\n" + user.getClientId());

                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Logged in", Toast.LENGTH_LONG).show();
                                    // enable login button
                                    button.setEnabled(true);
                                }
                            });
                            // load map here when successfully logged in
                            // start google map
                            Intent mapIntent = new Intent(LoginActivity.this, MapsActivity.class);
                            startActivity(mapIntent);
                        }
                        response.body().close();
                    }
                });
            }
        });
    }
}

