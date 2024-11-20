package org.cyducks.satark.core.heatmap.domain.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.room.Room;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.cyducks.satark.core.heatmap.data.dao.CrimeLocationDao;
import org.cyducks.satark.core.heatmap.data.dao.FileMetaDataDao;
import org.cyducks.satark.core.heatmap.data.entity.CrimeLocation;
import org.cyducks.satark.core.heatmap.data.entity.FIleMetadata;
import org.cyducks.satark.core.heatmap.model.CrimeClusterItem;
import org.cyducks.satark.data.AppDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HeatMapRepository {
    private static final String GEOJSON_FILENAME = "crimes.geojson";
    private static final String FIREBASE_PATH = "heatmap/crimes.geojson";
    private static final String TAG = "HeatMapRepository";
    private static final int BATCH_SIZE = 1000;

    private final Context context;
    private final FirebaseStorage firebaseStorage;
    private final AppDatabase database;
    private final FileMetaDataDao fileMetaDataDao;
    private final CrimeLocationDao crimeLocationDao;
    private final ExecutorService executorService;

    public HeatMapRepository(Context context) {
        this.context = context;
        this.firebaseStorage = FirebaseStorage.getInstance();
        this.database = Room.databaseBuilder(context, AppDatabase.class, "heatmap-db")
                .enableMultiInstanceInvalidation().build();
        this.fileMetaDataDao = database.fileMetaDataDao();
        this.crimeLocationDao = database.crimeLocationDao();
        this.executorService = Executors.newFixedThreadPool(5);
    }

    public Single<List<CrimeClusterItem>> getLocationsInRegion(LatLngBounds bounds) {
        return Single.create(emitter -> {
           executorService.execute(() -> {
               List<CrimeLocation> locations = crimeLocationDao.getLocationsInBounds(
                       bounds.southwest.latitude,
                       bounds.northeast.latitude,
                       bounds.southwest.longitude,
                       bounds.northeast.longitude
               );

               List<CrimeClusterItem> clusterItems = new ArrayList<>();
               for(CrimeLocation location : locations) {
                   JSONObject properties = new JSONObject();

                   try {
                       properties.put("crime_type", location.getCrimeType());
                       properties.put("timestamp", location.getTimestamp());
                       properties.put("crime_description", location.getCrimeDescription());
                   } catch (JSONException e) {
                       emitter.onError(e);
                   }

                   clusterItems.add(new CrimeClusterItem(
                           location.getLatitude(),
                           location.getLongitude(),
                           properties
                   ));
               }

               emitter.onSuccess(clusterItems);
           });
        });
    }

    @SuppressLint("CheckResult")
    public Single<Boolean> syncHeatmapData() {
        return Single.create(emitter -> {
            executorService.execute(() -> {
                try {
                    // Check if file exists and is up to date
                    if (!isLocalFileUpToDate()) {
                        // Download file from Firebase
                        downloadAndSaveFile()
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .subscribe(success -> {
                                    if (success) {
                                        // Parse and update database
                                        updateDatabase();
                                        emitter.onSuccess(true);
                                    } else {
                                        emitter.onError(new Exception("Failed to download file"));
                                    }
                                }, emitter::onError);
                    } else {
                        emitter.onSuccess(true);
                    }
                } catch (Exception e) {
                    emitter.onError(e);
                }
            });
        });
    }

    private boolean isLocalFileUpToDate() {
        Log.d("HeatMapRepository", "isLocalFileUpToDate: HELODGPOFJSDPOGOJDISD");
        File localFile = new File(context.getFilesDir(), GEOJSON_FILENAME);
        if (!localFile.exists()) return false;

        FIleMetadata metadata = null;
        try {
            Future<FIleMetadata> future = executorService.submit(() -> fileMetaDataDao.getMetadata(GEOJSON_FILENAME));
            metadata = future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (metadata == null) return false;

        // Check Firebase metadata
        StorageReference ref = firebaseStorage.getReference(FIREBASE_PATH);

        try {
            Task<StorageMetadata> metadataTask = ref.getMetadata();
            StorageMetadata firebaseMetadata = Tasks.await(metadataTask);
            return metadata.getLastUpdated() >= firebaseMetadata.getUpdatedTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    private Single<Boolean> downloadAndSaveFile() {
        return Single.create(emitter -> {
            StorageReference ref = firebaseStorage.getReference(FIREBASE_PATH);
            File localFile = new File(context.getFilesDir(), GEOJSON_FILENAME);

            ref.getFile(localFile)
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        Log.d(TAG, "downloadAndSaveFile: download progress - " + progress);
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "downloadAndSaveFile: download ho gya");
                        try {
                            executorService.execute(() -> {
                                FIleMetadata metadata = new FIleMetadata();
                                metadata.setFileName(GEOJSON_FILENAME);
                                metadata.setLastUpdated(System.currentTimeMillis());
                                fileMetaDataDao.insert(metadata);

                                emitter.onSuccess(true);
                            });
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                        // Update metadata

                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    private void updateDatabase() throws Exception {
        File localFile = new File(context.getFilesDir(), GEOJSON_FILENAME);
        String jsonStr = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            jsonStr = new String(Files.readAllBytes(localFile.toPath()));
        }

        assert jsonStr != null;
        JSONObject geoJson = new JSONObject(jsonStr);
        JSONArray features = geoJson.getJSONArray("features");

        List<CrimeLocation> locations = new ArrayList<>();

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject geometry = feature.getJSONObject("geometry");
            JSONArray coordinates = geometry.getJSONArray("coordinates");
            JSONObject properties = feature.getJSONObject("properties");

            CrimeLocation location = new CrimeLocation();
            location.setLongitude(coordinates.getDouble(0));
            location.setLatitude(coordinates.getDouble(1));
            location.setTimestamp(properties.getString("timestamp"));
            location.setCrimeType(properties.getInt("crime_type"));
            location.setCrimeDescription(properties.getString("crime_description"));

            locations.add(location);
        }

        // Update database
        crimeLocationDao.deleteAll();
        crimeLocationDao.insertAll(locations);
    }

    @SuppressLint("CheckResult")
    public Single<GeoJsonLayer> getHeatmapLayer(GoogleMap map) {
        Log.d(TAG, "getHeatmapLayer: inside getHeatmap layer");
        return Single.create(emitter -> {
            Log.d(TAG, "getHeatmapLayer: inside single.create()");
            executorService.execute(() -> {
                try {
                    Log.d(TAG, "getHeatmapLayer: inside executor");
                    List<CrimeLocation> locations = crimeLocationDao.getAllLocations();
                    JSONObject geoJson = new JSONObject();
                    geoJson.put("type", "FeatureCollection");

                    JSONArray features = new JSONArray();
                    for (CrimeLocation location : locations) {
                        JSONObject feature = new JSONObject();
                        feature.put("type", "Feature");

                        JSONObject geometry = new JSONObject();
                        geometry.put("type", "Point");
                        JSONArray coordinates = new JSONArray();
                        coordinates.put(location.getLongitude());
                        coordinates.put(location.getLatitude());
                        geometry.put("coordinates", coordinates);

                        JSONObject properties = new JSONObject();
                        properties.put("timestamp", location.getTimestamp());
                        properties.put("crime_type", location.getCrimeType());
                        properties.put("crime_description", location.getCrimeDescription());

                        feature.put("geometry", geometry);
                        feature.put("properties", properties);
                        features.put(feature);
                    }

                    geoJson.put("features", features);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            GeoJsonLayer layer = new GeoJsonLayer(map, geoJson);
                            emitter.onSuccess(layer);
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    });


                } catch (Exception e) {
                    emitter.onError(e);
                }
            });
        });
    }

    public Single<List<LatLng>> getFilteredHeatMapData(LatLngBounds bounds, Set<String> filters) {
        return Single.create(emitter -> {
            executorService.execute(() -> {
                try {
                    List<CrimeLocation> locations = crimeLocationDao.getFilteredLocations(
                            bounds.southwest.latitude,
                            bounds.northeast.latitude,
                            bounds.southwest.longitude,
                            bounds.northeast.longitude,
                            new ArrayList<>(filters)
                    );

                    List<LatLng> heatMapData = new ArrayList<>();
                    for (CrimeLocation location : locations) {
                        heatMapData.add(new LatLng(location.getLatitude(), location.getLongitude()));
                    }

                    emitter.onSuccess(heatMapData);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            });
        });
    }

    public void cleanup() {
        executorService.shutdown();
    }
}
