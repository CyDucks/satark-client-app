package org.cyducks.satark.dashboard.ui;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.WorkManager;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.cyducks.satark.AuthActivity;
import org.cyducks.satark.R;
import org.cyducks.satark.auth.viewmodel.AuthViewModel;
import org.cyducks.satark.dashboard.viewmodel.DashboardViewModel;
import org.cyducks.satark.databinding.FragmentDriverDashboardBinding;
import org.cyducks.satark.util.UserRole;

import java.util.ArrayList;
import java.util.List;


/**
 * Parent container fragment for the Driver's Dashboard
 * **/
public class DriverDashboardFragment extends Fragment {

    private static final String TAG = "MYAPP";
    private FragmentDriverDashboardBinding viewBinding;

    private DashboardViewModel dashboardViewModel;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private NavController navController;
    private BroadcastReceiver requestReceiver;

    private UserRole userRole;
    private String receivedTripId;

    private WorkManager workManager;

    private boolean isObserverAttached = false;


    public DriverDashboardFragment() {
        // Required empty public constructor
    }

    public static DriverDashboardFragment newInstance(UserRole userRole) {
        DriverDashboardFragment fragment = new DriverDashboardFragment();
        Bundle args = new Bundle();
        args.putString("role", userRole.name());
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentDriverDashboardBinding.inflate(getLayoutInflater());
        navController = NavHostFragment.findNavController(getChildFragmentManager().findFragmentById(R.id.driver_bottom_nav_container));
        AuthViewModel authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            if(firebaseUser == null) {
                Intent intent = new Intent(requireContext(), AuthActivity.class);
                startActivity(intent);
                requireActivity().finish();
                return;
            }
            String userId = firebaseUser.getUid();
            switch (userRole) {
                case CIVILIAN:

                    break;
                default:
                    throw new UnsupportedOperationException("not yet implemented.");
            }

        });




        // Inflate the layout for this fragment



        return viewBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(!hasLocationPermissions()) {
            List<String> permissions = new ArrayList<>();
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
            String[] permissionArray = permissions.toArray(new String[0]);

            ActivityCompat
                    .requestPermissions(requireActivity(), permissionArray, 111);
        }

        if(!hasGpsPermissions()) {
            LocationRequest dummyRequest = new LocationRequest.Builder(3000L)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build();

            LocationSettingsRequest.Builder settingsRequestBuilder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(dummyRequest)
                    .setAlwaysShow(true);

            LocationServices.getSettingsClient(requireContext())
                    .checkLocationSettings(settingsRequestBuilder.build())
                    .addOnCompleteListener(task -> {
                        try {
                            LocationSettingsResponse response = task.getResult(ApiException.class);
                        } catch (ApiException exception) {
                            switch (exception.getStatusCode()) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        ResolvableApiException resolvable = (ResolvableApiException) exception;

                                        resolvable.startResolutionForResult(requireActivity(), 113);

                                    } catch (IntentSender.SendIntentException sie) {

                                    } catch (ClassCastException cce) {

                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    break;
                            }
                        }
                    });
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if(requestReceiver != null) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(requireContext());
            broadcastManager.unregisterReceiver(requestReceiver);
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        assert data != null;
        final LocationSettingsStates settingsStates = LocationSettingsStates.fromIntent(data);

        if(requestCode == 113) {
            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    Toast.makeText(requireActivity(), "GPS is required to be enabled. Please enable GPS from the settings.", Toast.LENGTH_SHORT).show();
                    break;

            }
        }

    }

    private boolean hasLocationPermissions() {
        return
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasGpsPermissions() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}