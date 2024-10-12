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

import org.cyducks.generated.Report;
import org.cyducks.generated.ReportServiceGrpc;
import org.cyducks.satark.dashboard.viewmodel.DashboardViewModel;
import org.cyducks.satark.databinding.FragmentHomeDriverBinding;
import org.cyducks.satark.grpc.GrpcRunnable;
import org.cyducks.satark.grpc.GrpcWorker;
import org.cyducks.satark.grpc.ReportStreamRunnable;

import java.util.Random;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;


public class HomeFragment extends Fragment {
    private static final String TAG = "CivilianHomeFragment";
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

        viewBinding.liveReportButton.setOnClickListener(v -> {
            GrpcRunnable runnable = new ReportStreamRunnable("H7trkTwIVzOHMKYLryOgIkZ8vn23", new StreamObserver<Report>() {
                @Override
                public void onNext(Report value) {

                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });
            ManagedChannel channel = ManagedChannelBuilder.forAddress("10.0.2.2", 9000).usePlaintext().build();

            try {
                runnable.run(ReportServiceGrpc.newBlockingStub(channel), ReportServiceGrpc.newStub(channel));
            } catch (Exception e) {
                Log.d(TAG, "onCreateView: " + e);
            }
        });


        viewBinding.reportButton.setOnClickListener(v -> {

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