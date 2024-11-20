package org.cyducks.satark.dashboard.civilian.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.cyducks.satark.R;
import org.cyducks.satark.databinding.FragmentReportSheetBinding;
import org.cyducks.satark.grpc.GrpcWorker;

import java.util.Random;


public class ReportSheetFragment extends BottomSheetDialogFragment {
    FragmentReportSheetBinding viewBinding;

    private static final String[] INCIDENT_TYPES = new String[] {
            "Theft",
            "Burglary",
            "Suspicious Activity",
            "Vandalism",
            "Public Disturbance",
            "Other"
    };


    public ReportSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_BottomSheetDialog);
        viewBinding = FragmentReportSheetBinding.inflate(getLayoutInflater());
        dialog.setContentView(viewBinding.getRoot());
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown,
                INCIDENT_TYPES
        );

        viewBinding.reportTypeAutoComplete.setAdapter(adapter);

        viewBinding.btnSubmitReport.setOnClickListener(v -> {
            Random random = new Random();

            // Latitude range for Nagpur
            float minLatitude = 21.0725f;
            float maxLatitude = 21.2050f;

            // Longitude range for Nagpur
            float minLongitude = 79.0020f;
            float maxLongitude = 79.1690f;


            // Generate random latitude and longitude within the specified range
            float latitude = minLatitude + (maxLatitude - minLatitude) * random.nextFloat();
            float longitude = minLongitude + (maxLongitude - minLongitude) * random.nextFloat();

            String selectedType = viewBinding.reportTypeAutoComplete.getText().toString();
            submitReport(selectedType, new LatLng(latitude, longitude));
        });

        return dialog;
    }

    private void submitReport(String selectedType, LatLng latLng) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Report")
                .setMessage("Are you sure you want to report a " + selectedType.toLowerCase() + " incident at this location?")
                .setPositiveButton("Submit", (dialog, which) -> {
                    sendReport(selectedType, latLng.latitude, latLng.longitude);
                    Snackbar.make(viewBinding.getRoot(), "Incident Reported Successfully", BaseTransientBottomBar.LENGTH_LONG).show();
                }).show();
    }

    private void sendReport(String selectedType, double latitude, double longitude) {
        WorkManager workManager = WorkManager.getInstance(requireContext());

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