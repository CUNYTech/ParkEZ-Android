package com.herokuapp.parkez.parkezfinal.models;

/**
 * Created by pasan_000 on 3/8/2016.
 */
public class User {
    private String uid; // email
    private String token; // token that we use to authenticate
    private String clientId; // client id (multiple clients can use the same token)
    private String expiry; // when this token expires

    public User(String uid, String token, String clientId, String expiry) {
        this.uid = uid;
        this.token = token;
        this.clientId = clientId;
        this.expiry = expiry;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!getUid().equals(user.getUid())) return false;
        if (!getToken().equals(user.getToken())) return false;
        if (!getClientId().equals(user.getClientId())) return false;
        return getExpiry().equals(user.getExpiry());

    }

    @Override
    public int hashCode() {
        int result = getUid().hashCode();
        result = 31 * result + getToken().hashCode();
        result = 31 * result + getClientId().hashCode();
        result = 31 * result + getExpiry().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", token='" + token + '\'' +
                ", clientId='" + clientId + '\'' +
                ", expiry='" + expiry + '\'' +
                '}';
    }
}
