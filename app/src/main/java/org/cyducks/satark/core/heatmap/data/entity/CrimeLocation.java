package org.cyducks.satark.core.heatmap.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "crime_location")
public class CrimeLocation {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private double latitude;
    private double longitude;
    private String timestamp;
    private int crimeType;
    private String crimeDescription;


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getCrimeType() {
        return crimeType;
    }

    public void setCrimeType(int crimeType) {
        this.crimeType = crimeType;
    }

    public String getCrimeDescription() {
        return crimeDescription;
    }

    public void setCrimeDescription(String crimeDescription) {
        this.crimeDescription = crimeDescription;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
