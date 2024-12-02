package org.cyducks.satark.core.conflictzone.model;


import java.util.List;

public class ConflictZone {
    private String id;
    private String name;
    private List<GeoPoint> redZone;
    private List<GeoPoint> orangeZone;
    private List<GeoPoint> yellowZone;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GeoPoint> getRedZone() {
        return redZone;
    }

    public void setRedZone(List<GeoPoint> redZone) {
        this.redZone = redZone;
    }

    public List<GeoPoint> getOrangeZone() {
        return orangeZone;
    }

    public void setOrangeZone(List<GeoPoint> orangeZone) {
        this.orangeZone = orangeZone;
    }

    public List<GeoPoint> getYellowZone() {
        return yellowZone;
    }

    public void setYellowZone(List<GeoPoint> yellowZone) {
        this.yellowZone = yellowZone;
    }
}
