package org.cyducks.satark.util;

public class MassReportLocations {
    private static MassReportLocations instance;
    private String locationsJson;
    private boolean shouldShowMarkers = false;

    private MassReportLocations() {}

    public static synchronized MassReportLocations getInstance() {
        if (instance == null) {
            instance = new MassReportLocations();
        }
        return instance;
    }

    public void setLocations(String locations) {
        this.locationsJson = locations;
        this.shouldShowMarkers = true; // Reset whenever new locations come in
    }

    public String getLocations() {
        return locationsJson;
    }

    public boolean shouldShowMarkers() {
        return shouldShowMarkers;
    }

    public void markMarkersShown() {
        shouldShowMarkers = false;
    }

    public void clear() {
        locationsJson = null;
        shouldShowMarkers = false;
    }
}
