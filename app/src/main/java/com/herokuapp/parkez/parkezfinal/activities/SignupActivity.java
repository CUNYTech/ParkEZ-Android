package com.herokuapp.parkez.parkezfinal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.herokuapp.parkez.parkezfinal.R;
import com.herokuapp.parkez.parkezfinal.web.utils.WebUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignupActivity extends BaseActivity {
    private EditText fullName; // password
    private EditText email; // email address
    private EditText password1; // password
    private EditText password2; // password
    private Button button; // le login button
    private OkHttpClient client = WebUtils.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isConnected()) {
            Toast.makeText(getApplicationContext(), "You are not currently connected to the imternetam .", Toast.LENGTH_LONG);
        }
        setContentView(R.layout.activity_signup);
        fullName = (EditText) findViewById(R.id.et_name);
        email = (EditText) findViewById(R.id.et_email);
        password1 = (EditText) findViewById(R.id.et_password);
        password2 = (EditText) findViewById(R.id.et_pw_confirm);
        button = (Button) findViewById(R.id.btn_signup);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected()) {
                    Toast.makeText(getApplicationContext(), "You are not currently connected.", Toast.LENGTH_LONG);
                    return;

                }
                if (validate()) {
                    Toast.makeText(getApplicationContext(), "Processing Sign-up...", Toast.LENGTH_LONG).show();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("name", (Object) fullName.getText().toString());
                        jsonObject.put("email", (Object) email.getText().toString());
                        jsonObject.put("password", (Object) password1.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Request request = WebUtils.getRequest("/auth/").
                            post(WebUtils.getBody(WebUtils.JSON, jsonObject.toString())).build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("[signup]", e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                SignupActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                SignupActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
                                        startActivity(loginIntent);
                                        Toast.makeText(getApplicationContext(), "Successfully registered. Please Log In.", Toast.LENGTH_LONG).show();
                                    }
                                });
                                response.body().close();
                            }
                        }
                    });
                }
            }

            private boolean validate() {
                boolean valid = true;
                if (!(fullName.length() > 0)) {
                    fullName.setError("Name is required.");
                    valid = false;
                }
                if (!(email.length() > 0)) {
                    email.setError("E-mail is required.");
                    valid = false;
                } else if (!isValidEmail(email.getText().toString())) {
                    email.setError("Not a valid E-mail");
                    valid = false;
                }
                valid = validatePassword();
                return valid;
            }

            // validating email id
            private boolean isValidEmail(String email) {
                Pattern pattern = Patterns.EMAIL_ADDRESS;
                return pattern.matcher(email).matches();
            }

            private boolean validatePassword() {
                boolean valid = true;
                String pw1 = password1.getText().toString();
                String pw2 = password2.getText().toString();
                Log.d("[signup]", "pw=" + pw2 + "\n" + "pw2=" + pw2);
                if (pw1.isEmpty()) {
                    password1.setError("Password is required.");
                    valid = false;
                }
                if (pw2.isEmpty()) {
                    password2.setError("You must confirm your password.");
                    valid = false;

                }

                if (!pw1.matches(pw2)) {
                    password1.setError("Passwords do not match");
                    password2.setError("Passwords do not match");
                    valid = false;

                }
                return valid;
            }
        });
    }
}
