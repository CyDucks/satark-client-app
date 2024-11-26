package org.cyducks.satark.dashboard.moderator.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.cyducks.generated.Point;
import org.cyducks.generated.Report;
import org.cyducks.generated.ReportServiceGrpc;
import org.cyducks.satark.R;
import org.cyducks.satark.databinding.FragmentReportsViewBinding;
import org.cyducks.satark.grpc.GrpcRunnable;
import org.cyducks.satark.grpc.ReportStreamRunnable;
import org.cyducks.satark.util.DynamicClusterManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;


public class ReportsViewFragment extends Fragment implements OnMapReadyCallback {


    private FragmentReportsViewBinding viewBinding;
    private static final String TAG = "myapp";
    private static final AtomicReference<Boolean> requestSent = new AtomicReference<>(false);
    private GoogleMap googleMap;
    private DynamicClusterManager clusterManager;

    private final StreamObserver<Report> reportStreamObserver = new StreamObserver<Report>() {
        @Override
        public void onNext(Report value) {
            Log.d(TAG, "report received: " + value.getTimestamp());
            requireActivity().runOnUiThread(() -> {
                addMarker(value.getLocation());
            });

        }

        @Override
        public void onError(Throwable t) {
            Log.e(TAG, "onError: Something went wrong", t);
        }

        @Override
        public void onCompleted() {

        }
    };

    private void addMarker(Point location) {

        double lat =  location.getLat();
        double lng = location.getLng();

        if(googleMap != null && clusterManager != null) {
//            googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
            clusterManager.addItem(lat, lng);
        }

    }

    public ReportsViewFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(viewBinding.getRoot()).navigateUp();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentReportsViewBinding.inflate(getLayoutInflater());

        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SupportMapFragment map = viewBinding.reportsViewMap.getFragment();
        map.getMapAsync(this);

        if(!requestSent.get()) {
            GrpcRunnable fetchReportsRunnable = new ReportStreamRunnable("H7trkTwIVzOHMKYLryOgIkZ8vn23", reportStreamObserver);
            ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("10.0.2.2", 9000).usePlaintext().build();

            try {
                fetchReportsRunnable.run(ReportServiceGrpc.newBlockingStub(managedChannel), ReportServiceGrpc.newStub(managedChannel));
                requestSent.set(true);
            } catch (Exception e) {
                Log.d(TAG, "onViewCreated: " + e);
            }
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
          this.googleMap = googleMap;

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(21.1458,79.0882), 12f));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(22.1948,76.8392)));

        clusterManager = new DynamicClusterManager(requireContext(), googleMap);

    }

    class ReportMarker implements ClusterItem {
        private final LatLng position;


        public ReportMarker(double lat, double lng) {
            this.position = new LatLng(lat, lng);
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return position;
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