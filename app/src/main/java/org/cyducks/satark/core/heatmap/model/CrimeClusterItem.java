package org.cyducks.satark.core.heatmap.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONException;
import org.json.JSONObject;

public class CrimeClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final JSONObject properties;

    public CrimeClusterItem(double lat, double lng, JSONObject properties) {
        this.position = new LatLng(lat, lng);
        this.properties = properties;
        try {
            this.title = properties.getString("crime_description");
            this.snippet = properties.getString("timestamp");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0.0F;
    }

    public JSONObject getProperties() {
        return properties;
    }
}
