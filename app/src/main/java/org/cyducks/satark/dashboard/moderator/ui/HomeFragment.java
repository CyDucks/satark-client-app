package org.cyducks.satark.dashboard.moderator.ui;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;

import org.cyducks.satark.AuthActivity;
import org.cyducks.satark.R;
import org.cyducks.satark.databinding.FragmentHomeBinding;
import org.cyducks.satark.util.MassReportLocations;

public class HomeFragment extends Fragment {

    private static final String MASS_REPORT_EVENT = "org.cyducks.satark.MASS_REPORT_EVENT";
    FragmentHomeBinding viewBinding;

    private final BroadcastReceiver massReportReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("locations")) {
                String locations = intent.getStringExtra("locations");
                MassReportLocations.getInstance().setLocations(locations);
            }
            enableLiveReportsView();
        }
    };

    private void enableLiveReportsView() {
        if(viewBinding != null) {
            viewBinding.liveReportButton.setVisibility(View.VISIBLE);
            viewBinding.createConflictZoneButton.setVisibility(View.VISIBLE);
        }
    }


    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @SuppressLint({"DefaultLocale", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentHomeBinding.inflate(getLayoutInflater());

        if(requireActivity().getIntent().hasExtra("event_type") &&
                requireActivity().getIntent().getStringExtra("event_type").equals(MASS_REPORT_EVENT)) {
            // Check if locations data exists in intent
            if (requireActivity().getIntent().hasExtra("locations")) {
                String locations = requireActivity().getIntent().getStringExtra("locations");
                MassReportLocations.getInstance().setLocations(locations);
            }
            enableLiveReportsView();
        }

        viewBinding.mapButton.setOnClickListener(v -> {
            Toast.makeText(requireActivity(), "Hello World", Toast.LENGTH_SHORT).show();
        });


        if(requireActivity().getIntent().hasExtra("event_type") && requireActivity().getIntent().getStringExtra("event_type").equals(MASS_REPORT_EVENT)) {
            enableLiveReportsView();
        }

        viewBinding.liveReportButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_moderatorHomeFragment_to_reportsViewFragment);
        });

        viewBinding.createConflictZoneButton.setOnClickListener(v -> {
            MassReportLocations.getInstance().shouldShowMarkers();
            Navigation.findNavController(v).navigate(R.id.action_moderatorHomeFragment_to_zoneCreationFragment);
        });

        viewBinding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return viewBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                massReportReceiver,
                new IntentFilter(MASS_REPORT_EVENT)
        );
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(massReportReceiver);
    }
}