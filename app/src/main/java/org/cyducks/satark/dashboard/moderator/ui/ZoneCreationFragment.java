package org.cyducks.satark.dashboard.moderator.ui;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.maps.android.PolyUtil;

import org.cyducks.satark.core.grid.GridManager;
import org.cyducks.satark.databinding.FragmentZoneCreationBinding;


public class ZoneCreationFragment extends Fragment implements OnMapReadyCallback {
    FragmentZoneCreationBinding viewBinding;

    public ZoneCreationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentZoneCreationBinding.inflate(getLayoutInflater());


        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = viewBinding.zoneCreationMap.getFragment();
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setLatLngBoundsForCameraTarget(
                new LatLngBounds(
                        new LatLng(21.076, 78.926),
                        new LatLng(21.214, 79.186)
                )
        );

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(21.14158, 79.0882), 15f));
        googleMap.setMinZoomPreference(15f);

        GridManager gridManager = new GridManager();
        gridManager.initializeGrid(googleMap);

        viewBinding.selectionToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            googleMap.getUiSettings()
                    .setScrollGesturesEnabled(isChecked);
        });

        viewBinding.dragOverlay.setOnTouchListener((v, event) -> {
            int x = Math.round(event.getX());
            int y = Math.round(event.getY());

            Projection projection = googleMap.getProjection();
            Point point = new Point(x, y);

            LatLng latLng = projection.fromScreenLocation(point);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    for(Polygon polygon : gridManager.getGridCells()) {
                        if(PolyUtil.containsLocation(latLng, polygon.getPoints(), true)) {
                            gridManager.handleCellSelection(polygon);
                        }
                    }
                    break;
            }


            return !viewBinding.selectionToggle.isChecked();
        });
    }
}