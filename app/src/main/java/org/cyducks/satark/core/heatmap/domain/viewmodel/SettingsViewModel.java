package org.cyducks.satark.core.heatmap.domain.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import org.cyducks.satark.AppConstants;
import org.cyducks.satark.core.heatmap.HeatMapManager;
import org.cyducks.satark.core.heatmap.model.CrimeFilter;

import java.util.Set;

public class SettingsViewModel extends ViewModel {
    private final SavedStateHandle savedStateHandle;
    private HeatMapManager heatMapManager;

    public SettingsViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;

        if(!savedStateHandle.contains("heatmap_visible")) {
            savedStateHandle.set("heatmap_visible", false);
        }

        if(!savedStateHandle.contains("simulation_mode")) {
            savedStateHandle.set("simulation_mode", false);
        }

        if(!savedStateHandle.contains("selected_crime_types")) {
            CrimeFilter filter = new CrimeFilter();
            savedStateHandle.set("selected_crime_types", filter.getSelectedCrimeTypes());
        }

        if(!savedStateHandle.contains("user_city")) {
            savedStateHandle.set("user_city", null);
        }
    }

    public LiveData<Boolean> getHeatMapVisibility() {
        return savedStateHandle.getLiveData("heatmap_visible");
    }

    public LiveData<Set<String>> getSelectedCrimeTypes() {
        return savedStateHandle.getLiveData("selected_crime_types");
    }

    public LiveData<Boolean> getOperationMode() {
        return savedStateHandle.getLiveData("simulation_mode");
    }

    public LiveData<String> getUserCity() {
        return savedStateHandle.getLiveData("user_city");
    }

    @SuppressWarnings({"unchecked"})
    public void applyFilters() {
        Boolean visible = savedStateHandle.get("heatmap_visible");
        Set<String> filters = (Set<String>) savedStateHandle.get("selected_crime_types");
        if(heatMapManager != null && Boolean.TRUE.equals(visible)) {
            heatMapManager.setFilter(filters);
        }
    }


    @SuppressWarnings({"unchecked"})
    public void toggleCrimeType(String crimeType, boolean selected) {
        Set<String> filters = (Set<String>) savedStateHandle.get("selected_crime_types");
        assert filters != null;
        if(selected) {
            filters.add(crimeType);
        } else {
            filters.remove(crimeType);
        }
        savedStateHandle.set("selected_crime_types", filters);

    }

    public void setVisible(boolean visible) {
        savedStateHandle.set("heatmap_visible", visible);
        if(heatMapManager != null) {
            heatMapManager.setVisible(visible);
        }
    }

    public void toggleOperationMode(boolean simulationMode) {
        savedStateHandle.set("simulation_mode", simulationMode);
    }

    public void setUserCity(String city) {
        if(!AppConstants.SUPPORTED_CITIES.contains(city)) {
            throw new IllegalArgumentException("Invalid City name: " + city);
        }
        savedStateHandle.set("user_city", city);
    }

    public void setHeatMapManager(HeatMapManager mapManager) {
        this.heatMapManager = mapManager;
    }
}
