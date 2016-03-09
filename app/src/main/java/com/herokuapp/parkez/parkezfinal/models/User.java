package com.herokuapp.parkez.parkezfinal.models;

public class User {

    private String uid; // email
    private String token; // token that we use to authenticate
    private String clientId; // client id (multiple clients can use the same token)
    private String expiry; // when this token expires
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!getUid().equals(user.getUid())) return false;
        if (getToken() != null ? !getToken().equals(user.getToken()) : user.getToken() != null)
            return false;
        if (!getClientId().equals(user.getClientId())) return false;
        if (!getExpiry().equals(user.getExpiry())) return false;
        return getName().equals(user.getName());

    }

    @Override
    public int hashCode() {
        int result = getUid().hashCode();
        result = 31 * result + (getToken() != null ? getToken().hashCode() : 0);
        result = 31 * result + getClientId().hashCode();
        result = 31 * result + getExpiry().hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }

    public User(String uid, String token, String clientId, String expiry, String name) {
        this.uid = uid;
        this.token = token;
        this.clientId = clientId;
        this.expiry = expiry;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", token='" + token + '\'' +
                ", clientId='" + clientId + '\'' +
                ", expiry='" + expiry + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
