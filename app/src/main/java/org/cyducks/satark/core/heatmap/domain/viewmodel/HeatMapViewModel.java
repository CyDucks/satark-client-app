package org.cyducks.satark.core.heatmap.domain.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import org.cyducks.satark.core.heatmap.HeatMapManager;
import org.cyducks.satark.core.heatmap.model.CrimeFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HeatMapViewModel extends ViewModel {
    private final SavedStateHandle savedStateHandle;
    private HeatMapManager heatMapManager;

    public HeatMapViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;

        if(!savedStateHandle.contains("heatmap_visible")) {
            savedStateHandle.set("heatmap_visible", false);
        }

        if(!savedStateHandle.contains("selected_crime_types")) {
            CrimeFilter filter = new CrimeFilter();
            savedStateHandle.set("selected_crime_types", filter.getSelectedCrimeTypes());
        }
    }

    public LiveData<Boolean> getVisibility() {
        return savedStateHandle.getLiveData("heatmap_visible");
    }

    public LiveData<Set<String>> getSelectedCrimeTypes() {
        return savedStateHandle.getLiveData("selected_crime_types");
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

    public void setHeatMapManager(HeatMapManager mapManager) {
        this.heatMapManager = mapManager;
    }
}
