package org.cyducks.satark.dashboard.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.cyducks.satark.databinding.CityInputDialogBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SelectCityDialog {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String PREF_NAME = "app_prefs";
    private static final String CITY_KEY = "user_city";
    private static final AtomicBoolean expanded = new AtomicBoolean(false);

    public interface CitySelectionCallback {
        void onCitySelected(String city);
    }

    public static boolean isCityStored(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.contains(CITY_KEY);
    }

    public static void setCityPref(Context context, String city) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(CITY_KEY, city).apply();
    }


    public static void show(Activity activity, CitySelectionCallback callback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        CityInputDialogBinding binding = CityInputDialogBinding.inflate(activity.getLayoutInflater());

        builder.setTitle("Select your city");
        builder.setCancelable(false);


        binding.cityInput.setThreshold(0);

        List<String> cities = Arrays.asList("Nagpur", "Mumbai", "Pune", "Delhi");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, cities);
        binding.cityInput.setAdapter(adapter);
        adapter.notifyDataSetChanged();



        binding.inputLayout.setEndIconOnClickListener(v -> {
            if(!expanded.get()) {
                binding.cityInput.showDropDown();
                expanded.set(true);
            } else {
                binding.cityInput.dismissDropDown();
                expanded.set(false);
            }
        });


        builder.setView(binding.getRoot());

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String selectedCity = binding.cityInput.getText().toString();
            if(!selectedCity.isEmpty()) {
                setCityPref(activity, selectedCity);
                callback.onCitySelected(selectedCity);
            } else {
                binding.cityInput.setError("Please select a city");
                dialog.dismiss();
                show(activity, callback);
            }
        });

        builder.show();

    }

    private static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 1001);
    }

    private static boolean checkLocationPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }



    private static void getCurrentCity(Activity activity, AutoCompleteTextView cityInput) {
        FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        locationProviderClient.getLastLocation().addOnSuccessListener(activity, location -> {
            if(location != null) {
                reverseGeocode(activity, location, cityInput);
            }
        });
    }

    private static void reverseGeocode(Activity activity, Location location, AutoCompleteTextView cityInput) {
        executor.execute(() -> {
            try {
                URL url = new URL(String.format(Locale.US, "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", location.getLatitude(), location.getLongitude()));


                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "SATARK");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject result = new JSONObject(response.toString());
                JSONObject address = result.getJSONObject("address");
                final String city = address.optString("city", address.optString("town", address.optString("village", "Unknown")));

                mainHandler.post(() -> cityInput.setText(city));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void setAutoComplete(Context context, AutoCompleteTextView cityInput) {
        cityInput.setThreshold(0);

        List<String> cities = Arrays.asList("Nagpur", "Mumbai", "Pune", "Delhi");

        mainHandler.post(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, cities);
            cityInput.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });


    }

    private static void searchCities(Context context, String query, AutoCompleteTextView cityInput) {
        executor.execute(() -> {
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                URL url = new URL("https://nominatim.openstreetmap.org/search?format=json&q=" + encodedQuery + "&featuretype=city");


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static @NonNull List<String> retreiveCityList(URL url) throws IOException, JSONException {
        JSONArray results = getResults(url);
        List<String> cities = new ArrayList<>();

        for(int i=0; i<results.length(); i++) {
            JSONObject result = results.getJSONObject(i);

            if(result.has("display_name")) {
                String displayName = result.getString("display_name");
                String[] parts = displayName.split(",");

                if(parts.length > 0) {
                    cities.add(parts[0].trim());
                }
            }
        }
        return cities;
    }

    private static @NonNull JSONArray getResults(URL url) throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("User-Agent", "SATARK");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;

        while((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }

        return new JSONArray(responseBuilder.toString());
    }

}
