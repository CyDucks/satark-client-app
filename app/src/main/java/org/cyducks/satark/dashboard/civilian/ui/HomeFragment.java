package org.cyducks.satark.dashboard.civilian.ui;



import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import org.cyducks.satark.dashboard.viewmodel.DashboardViewModel;
import org.cyducks.satark.databinding.FragmentHomeDriverBinding;


public class HomeFragment extends Fragment {
    FragmentHomeDriverBinding viewBinding;
    DashboardViewModel dashboardViewModel;



    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentHomeDriverBinding.inflate(getLayoutInflater());
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);



        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

}