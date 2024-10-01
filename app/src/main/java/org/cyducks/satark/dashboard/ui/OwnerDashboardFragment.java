package org.cyducks.satark.dashboard.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.cyducks.satark.R;
import org.cyducks.satark.databinding.FragmentOwnerDashboardBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class OwnerDashboardFragment extends Fragment {

    FragmentOwnerDashboardBinding viewBinding;

    private static final String TAG = "MYAPP";

    NavController navController;

    public OwnerDashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFcmToken();

    }

    private void initializeFcmToken() {
        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = requireActivity().getSharedPreferences("fcm_token", Context.MODE_PRIVATE);

        if(currentUser == null) {
            return;
        }

        if(preferences != null && preferences.contains("token")) {

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("token", preferences.getString("token", null));
            inputMap.put("timestamp", FieldValue.serverTimestamp());
            dbInstance
                    .collection("fcm_tokens")
                    .document(Objects.requireNonNull(currentUser.getUid()))
                    .set(inputMap)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "FCM token created/updated: " + preferences.getString("token", null));
                        }
                        else {
                            Log.e(TAG, "FCM token creation/updation failed", task.getException());
                        }
                    });
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewBinding = FragmentOwnerDashboardBinding.inflate(getLayoutInflater());



        return viewBinding.getRoot();
    }

    private void navigateToDestination(@IdRes int destinationId) {
        int destinationTag = Integer.parseInt(navController.getGraph().findNode(destinationId).getLabel().toString());
        int currentDestinationTag = Integer.parseInt(navController.getCurrentDestination().getLabel().toString());

        int enterAnim;
        int exitAnim;

        if(destinationTag != currentDestinationTag) {
            if(destinationTag > currentDestinationTag) {
                enterAnim = R.anim.slide_in_right;
                exitAnim = R.anim.slide_out_left;
            }
            else {
                enterAnim = android.R.anim.slide_in_left;
                exitAnim = android.R.anim.slide_out_right;
            }

            navController.navigate(destinationId, null, new NavOptions.Builder().setEnterAnim(enterAnim).setExitAnim(exitAnim).build());
        }

    }


}