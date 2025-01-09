package org.cyducks.satark.core.heatmap.model;

import java.util.HashSet;
import java.util.Set;

public class CrimeFilter {
    private Set<String> selectedCrimeTypes;

    public CrimeFilter() {
        selectedCrimeTypes = new HashSet<>();
        selectedCrimeTypes.add("theft");
    }

    public void toggleCrimeType(String crimeType, boolean selected) {
        if(selected) {
            selectedCrimeTypes.add(crimeType);
        } else {
            selectedCrimeTypes.remove(crimeType);
        }
    }

    public Set<String> getSelectedCrimeTypes() {
        return selectedCrimeTypes;
    }

    public boolean isTypeSelected(String crimeType) {
        return selectedCrimeTypes.contains(crimeType);
    }
}
