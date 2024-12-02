package org.cyducks.satark.dashboard.moderator.ui;

import static org.cyducks.satark.AppConstants.REST_SERVER_BASE_URL;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import org.cyducks.satark.core.conflictzone.ZoneManager;
import org.cyducks.satark.core.conflictzone.model.ConflictZone;
import org.cyducks.satark.core.conflictzone.model.GeoPoint;
import org.cyducks.satark.core.grid.GridManager;
import org.cyducks.satark.databinding.FragmentZoneCreationBinding;
import org.cyducks.satark.network.service.GeofenceApiService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ZoneCreationFragment extends Fragment implements OnMapReadyCallback {
    private static final class MapConstants {
        static final LatLngBounds NAGPUR_BOUNDS = new LatLngBounds(
                new LatLng(21.076, 78.926),
                new LatLng(21.214, 79.186)
        );
        static final LatLng INITIAL_CENTER = new LatLng(21.14158, 79.0882);
        static final float INITIAL_ZOOM = 15f;
        static final float MIN_ZOOM = 15f;
    }

    private FragmentZoneCreationBinding viewBinding;
    private GridManager gridManager;
    private ZoneManager zoneManager;
    private GoogleMap googleMap;
    private boolean isConfirmationMode = false;
    private GeofenceApiService apiService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBackPressHandler();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(REST_SERVER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        apiService = retrofit.create(GeofenceApiService.class);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (zoneManager != null) {
            zoneManager.clearPreviews(gridManager.getSelectedPolygons());
        }
        viewBinding = null;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        setupMap();
        initializeManagers();
        setupUIListeners();
        setupTouchListener();
    }

    // Setup Methods
    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(viewBinding.getRoot()).navigateUp();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setupMap() {
        googleMap.setLatLngBoundsForCameraTarget(MapConstants.NAGPUR_BOUNDS);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                MapConstants.INITIAL_CENTER, MapConstants.INITIAL_ZOOM));
        googleMap.setMinZoomPreference(MapConstants.MIN_ZOOM);
    }

    private void initializeManagers() {
        gridManager = new GridManager();
        gridManager.initializeGrid(googleMap);
        zoneManager = new ZoneManager(googleMap);
    }

    private void setupUIListeners() {
        viewBinding.selectionToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startSelectionMode();
            } else {
                endSelectionMode();
            }
        });

        viewBinding.confirmButton.setOnClickListener(v -> {
            if (!isConfirmationMode) {
                showSelectionConfirmation();
            } else {
                processConfirmedSelection();
            }
        });

        viewBinding.cancelButton.setOnClickListener(v -> {
            if (isConfirmationMode) {
                exitConfirmationMode();
            } else {
                clearSelection();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener() {
        viewBinding.dragOverlay.setOnTouchListener((v, event) -> {
            if (!viewBinding.selectionToggle.isChecked() || isConfirmationMode) {
                return false;
            }

            handleTouchEvent(event);
            return true;
        });
    }

    // Selection Mode Methods
    private void startSelectionMode() {
        gridManager.toggleSelectionMode(true);
        viewBinding.dragOverlay.setVisibility(View.VISIBLE);
        viewBinding.confirmButton.setEnabled(true);
        viewBinding.confirmButton.setText("Confirm Selection");
    }

    private void endSelectionMode() {
        gridManager.toggleSelectionMode(false);
        viewBinding.confirmButton.setEnabled(false);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
    }

    // Confirmation Mode Methods
    private void showSelectionConfirmation() {
        isConfirmationMode = true;
        viewBinding.selectionToggle.setEnabled(false);
        zoneManager.showZonePreviews(gridManager.getSelectedPolygons());
        updateConfirmationUI(true);
        googleMap.getUiSettings().setAllGesturesEnabled(false);
    }

    private void exitConfirmationMode() {
        isConfirmationMode = false;
        viewBinding.selectionToggle.setEnabled(true);
        zoneManager.clearPreviews(gridManager.getSelectedPolygons());
        updateConfirmationUI(false);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        viewBinding.dragOverlay.setVisibility(View.VISIBLE);
    }

    private void updateConfirmationUI(boolean isConfirmation) {
        viewBinding.confirmButton.setText(isConfirmation ? "Confirm Zone" : "Confirm Selection");
        viewBinding.cancelButton.setText(isConfirmation ? "Edit Selection" : "Clear Selection");
    }

    // Touch Handling
    private void handleTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_MOVE) {
            return;
        }

        Point touchPoint = new Point(Math.round(event.getX()), Math.round(event.getY()));
        LatLng latLng = googleMap.getProjection().fromScreenLocation(touchPoint);

        gridManager.getGridCells().stream()
                .filter(polygon -> PolyUtil.containsLocation(latLng, polygon.getPoints(), true))
                .forEach(gridManager::handleCellSelection);
    }

    // Selection Methods
    private void clearSelection() {
        gridManager.clearSelection();
    }

    private void processConfirmedSelection() {
        gridManager.logSelectedRegion();
        // Add your zone processing logic here

        List<LatLng> redZone = zoneManager.getRedZone();
        List<LatLng> orangeZone = zoneManager.getOrangeZone();
        List<LatLng> yellowZone = zoneManager.getYellowZone();

        ConflictZone conflictZone = new ConflictZone();
        conflictZone.setId(UUID.randomUUID().toString());
        conflictZone.setRedZone(convertToGeoPoints(redZone));
        conflictZone.setOrangeZone(convertToGeoPoints(orangeZone));
        conflictZone.setYellowZone(convertToGeoPoints(yellowZone));
        conflictZone.setName("Conflict Zone " + System.currentTimeMillis());

        // Send to server and register geofences
        sendZonesToServer(conflictZone);
    }

    private List<GeoPoint> convertToGeoPoints(List<LatLng> latLngs) {
        return latLngs.stream()
                .map(latLng -> new GeoPoint(latLng.latitude, latLng.longitude))
                .collect(Collectors.toList());
    }

    private void sendZonesToServer(ConflictZone conflictZone) {
        // Show progress dialog

        apiService.createConflictZone(conflictZone).enqueue(new Callback<ConflictZone>() {
            @Override
            public void onResponse(Call<ConflictZone> call, Response<ConflictZone> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireActivity(), "Zone created successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireActivity(), "Zone creation failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ConflictZone> call, Throwable t) {
                Log.d("TAG", "Network error: " + t.getMessage());
            }
        });
    }
}