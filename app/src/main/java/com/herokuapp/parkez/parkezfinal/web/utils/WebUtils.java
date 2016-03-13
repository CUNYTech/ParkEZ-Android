package com.herokuapp.parkez.parkezfinal.web.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.herokuapp.parkez.parkezfinal.activities.SignupActivity;
import com.herokuapp.parkez.parkezfinal.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * This is a generic utility class -- every method is written in a generic manner in that it can be used easily
 */
public class WebUtils {

    private static final String BASE_URL = "http://parkez.herokuapp.com/api/v1/"; // base url
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static OkHttpClient getClient() {
        return client;
    }
    /**
     * Represents the okhttp Call. Will be called asynchronously
     * @param request the request with all headers filled out
     * @return a call ready to be passed off.
     */
    public static Call initiateAPICall(Request request) {
        return client.newCall(request);
    }
    /**
     * Utility function to add headers for auth
     *
     * @param resource
     * @param user
     * @return
     */
    public static Request.Builder addTokenAuthHeaders(String resource, User user) {
        return getRequest(resource).addHeader("Access-Token", user.getToken())
                .addHeader("Client", user.getClientId())
                .addHeader("Expiry", user.getExpiry())
                .addHeader("Token-type", "Bearer")
                .addHeader("Uid", user.getUid());
    }

    /**
     * Gets an instance of the okhtp RequestBody
     *
     * @param mediaType The Media Type
     * @param json      The data as JSON
     * @return the RequestBody
     */
    public static RequestBody getBody(MediaType mediaType, String json) {
        return RequestBody.create(JSON, json);
    }

    /**
     * Returns an instance of the Request Builder
     *
     * @param resource the resource we are requesting
     * @return an instance of the Request Builder
     */
    public static Request.Builder getRequest(String resource) {
        return new Request.Builder().url(BASE_URL + resource);

    }

    public static User getTokenAuthenticationDetails(Response response) {
        String uid = response.header("Uid");
        String token = response.header("Access-Token");
        String clientId = response.header("Client");
        String expiry = response.header("Expiry");
        String name = "";
        try {
            // {"data":{"id":11,"email":"robby.oconnor@gmail.com","provider":"email","name":null,"uid":"robby.oconnor@gmail.com","nickname":null,"image":null}}
            // based on the above it should be clear what this does.
            JSONObject jsonObject = new JSONObject(response.body().string()).getJSONObject("data");
            name = jsonObject.get("name").toString();
            name = name.equals("null") ? "" : name;
            Log.d("[fuck] ", name);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new User(uid, token, clientId, expiry, name);

    }


}
