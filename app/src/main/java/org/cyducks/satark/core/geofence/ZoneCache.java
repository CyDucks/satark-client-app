package org.cyducks.satark.core.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.cyducks.satark.core.conflictzone.model.ConflictZone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZoneCache {
    private static final String PREF_NAME = "zone_cache";
    private static final String KEY_LAST_ZONE = "last_zone";
    private static final String KEY_LAST_LEVEL = "last_level";
    
    private final SharedPreferences prefs;
    private final Gson gson;

    public ZoneCache(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LatLng.class, new LatLngTypeAdapter())
            .create();
    }

    public void saveZone(ConflictZone zone) {
        String json = gson.toJson(zone);
        prefs.edit()
            .putString("zone_" + zone.getId(), json)
            .apply();
    }

    public ConflictZone getZone(String zoneId) {
        String json = prefs.getString("zone_" + zoneId, null);
        if (json == null) return null;
        return gson.fromJson(json, ConflictZone.class);
    }

    public List<ConflictZone> getAllZones() {
        List<ConflictZone> zones = new ArrayList<>();
        Map<String, ?> all = prefs.getAll();
        
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith("zone_")) {
                try {
                    ConflictZone zone = gson.fromJson(
                        (String) entry.getValue(), 
                        ConflictZone.class
                    );
                    zones.add(zone);
                } catch (JsonSyntaxException e) {
                    Log.e("ZoneCache", "Error parsing zone: " + e.getMessage());
                }
            }
        }
        return zones;
    }

    public void deleteZone(String zoneId) {
        prefs.edit()
            .remove("zone_" + zoneId)
            .apply();
    }

    public void saveLastZoneStatus(String zoneId, String level) {
        prefs.edit()
            .putString(KEY_LAST_ZONE, zoneId)
            .putString(KEY_LAST_LEVEL, level)
            .apply();
    }

    public Pair<String, String> getLastZoneStatus() {
        String zoneId = prefs.getString(KEY_LAST_ZONE, null);
        String level = prefs.getString(KEY_LAST_LEVEL, null);
        if (zoneId == null || level == null) return null;
        return new Pair<>(zoneId, level);
    }

    // Custom TypeAdapter for LatLng
    private static class LatLngTypeAdapter extends TypeAdapter<LatLng> {
        @Override
        public void write(JsonWriter out, LatLng value) throws IOException {
            out.beginObject();
            out.name("latitude").value(value.latitude);
            out.name("longitude").value(value.longitude);
            out.endObject();
        }

        @Override
        public LatLng read(JsonReader in) throws IOException {
            double lat = 0, lng = 0;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "latitude":
                        lat = in.nextDouble();
                        break;
                    case "longitude":
                        lng = in.nextDouble();
                        break;
                    default:
                        in.skipValue();
                        break;
                }
            }
            in.endObject();
            return new LatLng(lat, lng);
        }
    }
}