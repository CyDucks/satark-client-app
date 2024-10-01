package org.cyducks.satark.dashboard.civilian.ui;



import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.cyducks.satark.dashboard.viewmodel.DashboardViewModel;
import org.cyducks.satark.databinding.FragmentHomeDriverBinding;
import org.cyducks.satark.grpc.GrpcWorker;

import java.util.Random;


public class HomeFragment extends Fragment {
    FragmentHomeDriverBinding viewBinding;
    DashboardViewModel dashboardViewModel;

    WorkManager workManager;



    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        workManager = WorkManager.getInstance(requireContext());
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentHomeDriverBinding.inflate(getLayoutInflater());
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        viewBinding.reportButton.setOnClickListener(v -> {

            Random random = new Random();

            // Generate random latitude between -90 and 90
            float latitude = (float) (-90.0 + (90.0 - (-90.0)) * random.nextFloat());

            // Generate random longitude between -180 and 180
            float longitude = (float) (-180.0 + (180.0 - (-180.0)) * random.nextFloat());


            assert user != null;
            OneTimeWorkRequest sendReportRequest = new OneTimeWorkRequest.Builder(GrpcWorker.class)
                    .setInputData(new Data.Builder()
                            .putString("request_type", "sendReport")
                            .putString("zone_id", "A")
                            .putString("mod_id", "H7trkTwIVzOHMKYLryOgIkZ8vn23")
                            .putFloat("lat", latitude)
                            .putFloat("lng", longitude)
                            .putString("type", "dange")
                            .build())
                    .build();
            ;


            workManager
                    .enqueue(sendReportRequest);


            workManager
                    .getWorkInfoByIdLiveData(sendReportRequest.getId())
                    .observe(getViewLifecycleOwner(), workInfo -> {
                        if(workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Toast.makeText(requireContext(), "Report Sent", Toast.LENGTH_SHORT).show();
                            Log.d("MYAPP", "onCreateView: " + workInfo.getOutputData());
                        } else if(workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.FAILED) {
                            Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                            Log.d("MYAPP", "onCreateView: " + workInfo.getOutputData());
                        }
                    });
        });

        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

}