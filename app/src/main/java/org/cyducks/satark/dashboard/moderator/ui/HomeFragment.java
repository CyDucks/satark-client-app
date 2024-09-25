package org.cyducks.satark.dashboard.moderator.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import org.cyducks.satark.R;
import org.cyducks.satark.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    FragmentHomeBinding viewBinding;



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
        viewBinding.mapButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_moderatorHomeFragment_to_reportsViewFragment);
        });



        return viewBinding.getRoot();
    }
}