package com.herokuapp.parkez.parkezfinal.models;



public class ParkingLocation {
    private Integer id;
    private Double latitude;
    private Double longitude;
    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public ParkingLocation(Integer id, Double latitude, Double longitude, String status) {

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
