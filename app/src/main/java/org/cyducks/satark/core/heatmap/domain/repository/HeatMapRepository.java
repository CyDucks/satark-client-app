package org.cyducks.satark.core.heatmap.domain.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private static final int BATCH_SIZE = Runtime.getRuntime().maxMemory() < 100 * 1024 * 1024 ?
            500 : 100;

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
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(localFile));
            JsonReader jsonReader = new JsonReader(bufferedReader)) {

            List<CrimeLocation> locationBatch = new ArrayList<>(BATCH_SIZE);

            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if("features".equals(name)) {
                    jsonReader.beginArray();

                    while(jsonReader.hasNext()) {
                        CrimeLocation location = parseFeature(jsonReader);
                        locationBatch.add(location);

                        if(locationBatch.size() >= BATCH_SIZE) {
                            crimeLocationDao.insertAll(locationBatch);
                            locationBatch.clear();
                        }
                    }

                    jsonReader.endArray();
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();

            if(!locationBatch.isEmpty()) {
                crimeLocationDao.insertAll(locationBatch);
            }

        } catch (IOException e) {
            Log.e(TAG, "updateDatabase: error reading json file", e);
        }
    }

    private CrimeLocation parseFeature(JsonReader reader) throws IOException {
        CrimeLocation location = new CrimeLocation();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "geometry":
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String geometryName = reader.nextName();
                        if ("coordinates".equals(geometryName)) {
                            reader.beginArray();
                            location.setLongitude(reader.nextDouble());
                            location.setLatitude(reader.nextDouble());
                            reader.endArray();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                    break;
                case "properties":
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String propName = reader.nextName();
                        switch (propName) {
                            case "timestamp":
                                location.setTimestamp(reader.nextString());
                                break;
                            case "crime_type":
                                location.setCrimeType(reader.nextInt());
                                break;
                            case "crime_description":
                                location.setCrimeDescription(reader.nextString());
                                break;
                            default:
                                reader.skipValue();
                        }
                    }
                    reader.endObject();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();

        return location;
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
