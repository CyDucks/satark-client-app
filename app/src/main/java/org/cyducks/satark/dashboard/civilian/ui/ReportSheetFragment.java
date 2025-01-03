package org.cyducks.satark.dashboard.civilian.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.cyducks.satark.R;
import org.cyducks.satark.core.MapConstants;
import org.cyducks.satark.core.heatmap.domain.viewmodel.SettingsViewModel;
import org.cyducks.satark.databinding.FragmentReportSheetBinding;
import org.cyducks.satark.grpc.GrpcWorker;
import org.cyducks.satark.util.LocationUtil;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


public class ReportSheetFragment extends BottomSheetDialogFragment {
    FragmentReportSheetBinding viewBinding;
    private String city;
    private Context context;

    private static final String[] INCIDENT_TYPES = new String[] {
            "Theft",
            "Burglary",
            "Suspicious Activity",
            "Vandalism",
            "Public Disturbance",
            "Other"
    };

    @Override
    public void onStart() {
        super.onStart();


    }

    public ReportSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        if(preferences.contains("user_city")) {
            city = preferences.getString("user_city", null);
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_BottomSheetDialog);
        viewBinding = FragmentReportSheetBinding.inflate(getLayoutInflater());
        dialog.setContentView(viewBinding.getRoot());
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
        context = requireContext();
        LatLngBounds bounds = getCityBounds();
        LatLng centerPoint = getRandomPoint(bounds);

        SettingsViewModel settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        AtomicBoolean simulationMode = new AtomicBoolean(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown,
                INCIDENT_TYPES
        );

        settingsViewModel.getOperationMode().observe(this, simulationMode::set);

        viewBinding.reportTypeAutoComplete.setAdapter(adapter);

        viewBinding.btnSubmitReport.setOnClickListener(v -> {
            if(simulationMode.get()) {
                simulateRandomReportLocation(bounds, centerPoint);
            } else {
                submitActualReportLocation();
                dismiss();
            }
        });

        return dialog;
    }

    private LatLng getRandomPoint(LatLngBounds bounds) {
        Random random = new Random();
        double lat = bounds.southwest.latitude +
                (bounds.northeast.latitude - bounds.southwest.latitude) * random.nextDouble();
        double lng = bounds.southwest.longitude +
                (bounds.northeast.longitude - bounds.southwest.longitude) * random.nextDouble();

        return new LatLng(lat, lng);
    }

    private void submitActualReportLocation() {
        LocationUtil locationUtil = new LocationUtil(requireActivity());

        locationUtil.getCurrentLocation(new LocationUtil.LocationListener() {
            @Override
            public void onResult(Location location) {
                String selectedType = viewBinding.reportTypeAutoComplete.getText().toString();
                submitReport(selectedType, new LatLng(location.getLatitude(), location.getLongitude()));
            }

            @Override
            public void onError(String exception) {
                Toast.makeText(requireContext(), exception, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void simulateRandomReportLocation(LatLngBounds bounds, LatLng centerPoint) {
        Random random = new Random();

        double r = 0.01 * Math.sqrt(random.nextDouble());
        double theta = 2 * Math.PI * random.nextDouble();

        double latitude = centerPoint.latitude + r * Math.cos(theta);
        double longitude = centerPoint.longitude + r * Math.sin(theta);

        latitude = Math.min(Math.max(latitude, bounds.southwest.latitude), bounds.northeast.latitude);
        longitude = Math.min(Math.max(longitude, bounds.southwest.longitude), bounds.northeast.longitude);

        String selectedType = viewBinding.reportTypeAutoComplete.getText().toString();
        submitReport(selectedType, new LatLng(latitude, longitude));
    }

    private @NonNull LatLngBounds getCityBounds() {
        LatLngBounds bounds = null;

        if(city != null) {
            switch (city) {
                case "Nagpur":
                    bounds = MapConstants.NAGPUR_BOUNDS;
                    break;

                case "Pune":
                    bounds = MapConstants.PUNE_BOUNDS;
                    break;

                case "Mumbai":
                    bounds = MapConstants.MUMBAI_BOUNDS;
                    break;

                case "Delhi":
                    bounds = MapConstants.DELHI_BOUNDS;
                    break;

                default:
                    throw new IllegalArgumentException("Invalid/Unsupported city");
            }
        }

        // Generate random latitude and longitude within the specified range
        assert bounds != null;
        return bounds;
    }

    private void submitReport(String selectedType, LatLng latLng) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Confirm Report")
                .setMessage("Are you sure you want to report a " + selectedType.toLowerCase() + " incident at this location?")
                .setPositiveButton("Submit", (dialog, which) -> {
                    sendReport(selectedType, latLng.latitude, latLng.longitude);
//                    Snackbar.make(viewBinding.getRoot(), "Incident Reported Successfully", BaseTransientBottomBar.LENGTH_LONG).show();
                }).show();
    }

    private void sendReport(String selectedType, double latitude, double longitude) {
        WorkManager workManager = WorkManager.getInstance(context);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        OneTimeWorkRequest sendReportRequest = new OneTimeWorkRequest.Builder(GrpcWorker.class)
                .setInputData(new Data.Builder()
                        .putString("request_type", "sendReport")
                        .putString("zone_id", "A")
                        .putString("mod_id", "H7trkTwIVzOHMKYLryOgIkZ8vn23")
                        .putFloat("lat", (float) latitude)
                        .putFloat("lng", (float) longitude)
                        .putString("type", selectedType)
                        .build())
                .build();

        workManager.enqueue(sendReportRequest);

        workManager
                .getWorkInfoByIdLiveData(sendReportRequest.getId())
                .observe(this, workInfo -> {
                    if(workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        Toast.makeText(requireContext(), "Report Sent", Toast.LENGTH_SHORT).show();
                        Log.d("MYAPP", "onCreateView: " + workInfo.getOutputData());
                    } else if(workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.FAILED) {
                        Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                        Log.d("MYAPP", "onCreateView: " + workInfo.getOutputData());
                    }
                });
    }
}