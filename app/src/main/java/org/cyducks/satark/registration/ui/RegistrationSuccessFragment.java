package org.cyducks.satark.registration.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.cyducks.satark.R;
import org.cyducks.satark.databinding.FragmentRegistrationSuccessBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegistrationSuccessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegistrationSuccessFragment extends Fragment {

    FragmentRegistrationSuccessBinding viewBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RegistrationSuccessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegistrationSuccessFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegistrationSuccessFragment newInstance(String param1, String param2) {
        RegistrationSuccessFragment fragment = new RegistrationSuccessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewBinding = FragmentRegistrationSuccessBinding.inflate(getLayoutInflater());

        // Inflate the layout for this fragment

        viewBinding.btnToDashboard.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_registrationSuccessFragment_to_mainActivity2);
            requireActivity().finish();
        });


        return viewBinding.getRoot();
    }
}