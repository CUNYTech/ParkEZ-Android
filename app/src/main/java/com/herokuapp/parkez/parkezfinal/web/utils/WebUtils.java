package com.herokuapp.parkez.parkezfinal.web.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This is a generic utility class -- every method is written in a generic manner in that it can be used easily
 */
public class WebUtils {
    private Map<String, String> headers = new HashMap<>(); //

    private static final String BASE_URL = "http://parkez.herokuapp.com/api/v1/"; // base url

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client = new OkHttpClient();

    public static Response post(String resource, String json) throws IOException {
        Request request = getRequest(resource).post(getBody(JSON, json)).build();
        Response response = makeCall(request);
        if (!response.isSuccessful()) {
            return null;
        }

        return response;
    }

    public static Response get(String resource, String json) throws IOException {
        Request request = getRequest(resource).post(getBody(JSON, json)).build();
        Response response = makeCall(request);
        if (!response.isSuccessful()) {
            return null;
        }
        return response;

    }

    /**
     * Gets an instance of the okhtp RequestBody
     *
     * @param mediaType The Media Type
     * @param json      The data as JSON
     * @return the RequestBody
     */
    private static RequestBody getBody(MediaType mediaType, String json) {
        return RequestBody.create(JSON, json);
    }

    /**
     * Returns an instance of the Request Builder
     *
     * @param resource the resource we are requesting
     * @return an instance of the Request Builder
     */
    private static Request.Builder getRequest(String resource) {
        return new Request.Builder().url(BASE_URL + resource);


    }

    /**
     * Returns the okhttp {@Response} object after making the API Call
     *
     * @param request the  okhttp {@Request} obkect
     * @return the Response
     * @throws IOException if the shit hits the fan
     */
    private static Response makeCall(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        return response;
    }
}
