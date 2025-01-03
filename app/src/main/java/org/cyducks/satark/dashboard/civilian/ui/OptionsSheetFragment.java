package org.cyducks.satark.dashboard.civilian.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.cyducks.satark.AppConstants;
import org.cyducks.satark.R;
import org.cyducks.satark.core.heatmap.domain.viewmodel.SettingsViewModel;
import org.cyducks.satark.databinding.FragmentOptionsSheetBinding;


public class OptionsSheetFragment extends BottomSheetDialogFragment {
    FragmentOptionsSheetBinding viewBinding;
    private SettingsViewModel viewModel;
    private static boolean flag = true;


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


        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        viewModel.getHeatMapVisibility().observe(this, visible -> viewBinding.enableSwitch.setChecked(visible));
        viewModel.getOperationMode().observe(this, simulationMode -> {
            viewBinding.modeSwitch.setChecked(simulationMode);
            viewBinding.cityDropDown.setEnabled(simulationMode);
        });
        viewModel.getUserCity().observe(this, city -> viewBinding.cityAutocomplete.setText(city, false));
        viewModel.getSelectedCrimeTypes().observe(this, selectedTypes -> {
            for (int i = 0; i < viewBinding.chipGroupFilters.getChildCount(); i++) {
                Chip chip = (Chip) viewBinding.chipGroupFilters.getChildAt(i);
                chip.setOnCheckedChangeListener(null);
                chip.setChecked(selectedTypes.contains(chip.getText().toString()));
                setupChipListener(chip);
            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdown, AppConstants.SUPPORTED_CITIES);
        viewBinding.cityAutocomplete.setThreshold(0);
        viewBinding.cityAutocomplete.setAdapter(adapter);

        viewBinding.cityAutocomplete.setOnItemClickListener((parent, view, position, id) -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            prefs.edit().putString("user_city", AppConstants.SUPPORTED_CITIES.get(position)).apply();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("App Restart Required")
                    .setMessage("Changing the city requires the app to restart. The app will stop now and needs to be reopened.")
                    .setCancelable(false)
                    .setPositiveButton("Okay", (dlg, which) -> {
                        Process.killProcess(Process.myPid());
                        System.exit(0);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        viewBinding.enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                viewBinding.filterContainer.setVisibility(View.VISIBLE);
            } else {
                viewBinding.filterContainer.setVisibility(View.GONE);
            }
            viewModel.setVisible(isChecked);
        });

        viewBinding.modeSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> viewModel.toggleOperationMode(isChecked)));

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