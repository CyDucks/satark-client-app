package org.cyducks.satark.dashboard.civilian.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import org.cyducks.satark.core.heatmap.HeatMapManager;
import org.cyducks.satark.core.heatmap.domain.repository.HeatMapRepository;
import org.cyducks.satark.core.heatmap.domain.viewmodel.HeatMapViewModel;
import org.cyducks.satark.databinding.FragmentOptionsSheetBinding;


public class OptionsSheetFragment extends BottomSheetDialogFragment {
    FragmentOptionsSheetBinding viewBinding;
    private HeatMapViewModel viewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public OptionsSheetFragment() {

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), com.google.android.material.R.style.ThemeOverlay_Material3_BottomSheetDialog);
        viewBinding = FragmentOptionsSheetBinding.inflate(getLayoutInflater());
        dialog.setContentView(viewBinding.getRoot());


        viewModel = new ViewModelProvider(requireActivity()).get(HeatMapViewModel.class);

        viewModel.getVisibility().observe(this, visible -> viewBinding.enableSwitch.setChecked(visible));
        viewModel.getSelectedCrimeTypes().observe(this, selectedTypes -> {
            for (int i = 0; i < viewBinding.chipGroupFilters.getChildCount(); i++) {
                Chip chip = (Chip) viewBinding.chipGroupFilters.getChildAt(i);
                chip.setOnCheckedChangeListener(null);
                chip.setChecked(selectedTypes.contains(chip.getText().toString()));
                setupChipListener(chip);
            }
        });


        viewBinding.enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                viewBinding.filterContainer.setVisibility(View.VISIBLE);
            } else {
                viewBinding.filterContainer.setVisibility(View.GONE);
            }
            viewModel.setVisible(isChecked);
        });

        viewBinding.btnApplyFilters.setOnClickListener(v -> {
            viewModel.applyFilters();
        });

        return dialog;
    }

    private void setupChipListener(Chip chip) {
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleCrimeType(chip.getText().toString(), isChecked);
        });
    }

}