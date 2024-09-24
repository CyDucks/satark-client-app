package org.cyducks.satark.auth.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import org.cyducks.satark.R;
import org.cyducks.satark.databinding.FragmentTagLineBinding;

public class TagLineFragment extends Fragment {
    FragmentTagLineBinding viewBinding;

    public TagLineFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentTagLineBinding.inflate(getLayoutInflater());

        viewBinding.login.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_tagLineFragment_to_loginFragment);
        });

        return viewBinding.getRoot();
    }
}