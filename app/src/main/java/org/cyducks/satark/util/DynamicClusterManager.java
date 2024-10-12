package org.cyducks.satark.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

public class DynamicClusterManager {
    private final ClusterManager<ReportMarker> clusterManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long CLUSTER_UPDATE_INTERVAL = 100;
    private boolean needsUpdate = false;


    public DynamicClusterManager(Context context, GoogleMap googleMap) {
        this.clusterManager = new ClusterManager<>(context, googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        clusterManager.setAlgorithm(new NonHierarchicalDistanceBasedAlgorithm<>());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(needsUpdate) {
                    clusterManager.cluster();
                    needsUpdate = false;
                }
                handler.postDelayed(this, CLUSTER_UPDATE_INTERVAL);
            }
        }, CLUSTER_UPDATE_INTERVAL);
    }

    public void addItem(final double lat, final double lng) {
        ReportMarker marker = new ReportMarker(lat, lng);
        clusterManager.addItem(marker);
        needsUpdate = true;
    }
    private static class ReportMarker implements ClusterItem {
        private final LatLng latLng;

        public ReportMarker(double lat, double lng) {
            this.latLng = new LatLng(lat, lng);
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return latLng;
        }

        @Nullable
        @Override
        public String getTitle() {
            return "";
        }

        @Nullable
        @Override
        public String getSnippet() {
            return "";
        }

        @Nullable
        @Override
        public Float getZIndex() {
            return 0f;
        }
    }
}
