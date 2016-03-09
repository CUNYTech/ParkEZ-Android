package com.herokuapp.parkez.parkezfinal.models;

/**
 * Created by pasan_000 on 3/8/2016.
 */
public class ParkingLocation {
    // class members
    private Integer id;
    private Float latitude;
    private Float longitude;
    private User user;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ParkingLocation(Integer id, Float latitude, Float longitude, User user) {

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParkingLocation that = (ParkingLocation) o;

        if (!getId().equals(that.getId())) return false;
        if (!getLatitude().equals(that.getLatitude())) return false;
        if (!getLongitude().equals(that.getLongitude())) return false;
        return getUser().equals(that.getUser());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getLatitude().hashCode();
        result = 31 * result + getLongitude().hashCode();
        result = 31 * result + getUser().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ParkingLocation{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", user=" + user +
                '}';
    }
}
