package org.cyducks.satark.core.heatmap;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.cyducks.satark.core.heatmap.domain.repository.HeatMapRepository;
import org.cyducks.satark.core.heatmap.model.CrimeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HeatMapManager {
    private static final String TAG = "HeatMapManager";
    private final Context context;
    private final GoogleMap googleMap;
    private final HeatMapRepository repository;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private boolean loadComplete = false;
    private TileOverlay heatMapOverlay;
    private HeatmapTileProvider provider;
    private Set<String> filters;



    private boolean visible;

    public HeatMapManager(Context context, GoogleMap googleMap, HeatMapRepository repository) {
        this.context = context;
        this.googleMap = googleMap;
        this.repository = repository;
        visible = false;
        setupListener();
        loadInitialData();
    }

    private void setupListener() {
        googleMap.setOnCameraIdleListener(() -> {
           handleVisibility(visible);
        });
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        handleVisibility(visible);
    }

    private void handleVisibility(boolean visible) {
        if(!visible && heatMapOverlay != null) {
            heatMapOverlay.remove();
        }
        if(visible && loadComplete) {
            if(filters != null && filters.size() > 1) {
                loadFilteredRegion();
            } else {
                Log.d(TAG, "handleVisibility: Calling loadVisibleRegion()");
                loadVisibleRegion();
            }
        }
    }



    public void setFilter(Set<String> filters) {
        this.filters = filters;
    }

    private void loadVisibleRegion() {
        LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;


        disposable.add(repository.getLocationsInRegion(bounds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(items -> {
                    List<LatLng> locations = new ArrayList<>();
                    for(ClusterItem item : items) {
                        locations.add(item.getPosition());
                    }
                    updateHeatMap(locations);
                    }, error -> {
                    Log.e(TAG, "loadVisibleRegion: Something went wrong", error);
                }));

    }

    private void loadFilteredRegion() {
        LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        disposable.add(repository.getFilteredHeatMapData(bounds, filters)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateHeatMap, error -> Log.e(TAG, "loadFilteredRegion: Something went wrong", error)));
    }

    private void updateHeatMap(List<LatLng> locations) {
        if(locations.isEmpty()) {
            setVisible(false);
            return;
        }

        int[] colors = {
                Color.rgb(102, 155, 0),
                Color.rgb(255 , 128, 0),
                Color.rgb(255, 0, 0)
        };

        float[] startPoints = {
                0.2f, 0.5f, 1.0f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        if(provider == null){
            provider = new HeatmapTileProvider.Builder()
                    .data(locations)
                    .radius(50)
                    .gradient(gradient)
                    .build();
        } else {
            provider.setData(locations);
        }

        if(heatMapOverlay != null) {
            heatMapOverlay.clearTileCache();
            heatMapOverlay.remove();
        }



        heatMapOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));


    }

    private void loadInitialData() {
        disposable.add(repository.syncHeatmapData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            Log.d(TAG, "onCreate: Data sync success");
                            loadComplete = true;
                        },
                        error -> Log.e(TAG, "onCreate: Data sync failed", error)
                ));
    }

    public boolean getVisibility() {
        return visible;
    }

}
