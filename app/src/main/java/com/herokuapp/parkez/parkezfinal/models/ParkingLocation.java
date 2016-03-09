package com.herokuapp.parkez.parkezfinal.models;



public class ParkingLocation {
    private Integer id;
    private Float latitude;
    private Float longitude;
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public ParkingLocation(Integer id, Float latitude, Float longitude, String status) {

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParkingLocation that = (ParkingLocation) o;

        if (!getId().equals(that.getId())) return false;
        if (!getLatitude().equals(that.getLatitude())) return false;
        if (!getLongitude().equals(that.getLongitude())) return false;
        return getStatus().equals(that.getStatus());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getLatitude().hashCode();
        result = 31 * result + getLongitude().hashCode();
        result = 31 * result + getStatus().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ParkingLocation{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", status=" + status +
                '}';
    }
}
