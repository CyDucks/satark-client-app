package org.cyducks.satark.dashboard.viewmodel;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.cyducks.satark.util.UserRole;


public class DashboardViewModel extends ViewModel {
    private final MutableLiveData<UserRole> userRole;

    public DashboardViewModel() {
        userRole = new MutableLiveData<>(UserRole.UNASSIGNED);
    }

    public MutableLiveData<UserRole> getUserRole() {
        return this.userRole;
    }

    public void setUserRole(UserRole role) {
        this.userRole.setValue(role);
    }
}
