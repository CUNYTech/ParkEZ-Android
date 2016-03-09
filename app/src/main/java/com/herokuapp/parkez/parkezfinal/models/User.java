package com.herokuapp.parkez.parkezfinal.models;

/**
 * Created by pasan_000 on 3/8/2016.
 */
public class User {
    private String uid; // email
    private String token; // token that we use to authenticate
    private String clientId; // client id (multiple clients can use the same token)
    private String expiry; // when this token expires
}
