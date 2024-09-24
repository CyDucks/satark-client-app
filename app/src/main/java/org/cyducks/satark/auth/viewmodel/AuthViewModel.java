package org.cyducks.satark.auth.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.cyducks.satark.auth.model.UserInfo;


public class AuthViewModel extends ViewModel {
    private MutableLiveData<UserInfo> userInfoLiveData;
    private MutableLiveData<FirebaseUser> currentUserLiveData;

    private FirebaseAuth auth;

    private static final String TAG = "MYAPP";

    public AuthViewModel() {
        userInfoLiveData = new MutableLiveData<>(null);
        currentUserLiveData = new MutableLiveData<>(FirebaseAuth.getInstance().getCurrentUser());

        auth = FirebaseAuth.getInstance();

        auth.addAuthStateListener(firebaseAuth -> {
            this.auth = firebaseAuth;
            this.setCurrentUser(firebaseAuth.getCurrentUser());
            Log.d(TAG, "AuthViewModel: auth state changed " + ((firebaseAuth.getCurrentUser() == null) ? "null" : firebaseAuth.getCurrentUser().getEmail()));
        });
    }

    public void setUser(UserInfo userInfo) {
        this.userInfoLiveData.setValue(userInfo);
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return this.currentUserLiveData;
    }

    public void setCurrentUser(FirebaseUser user) {
        this.currentUserLiveData.setValue(user);
    }

    public LiveData<UserInfo> getUserInfoLiveData() {
        return this.userInfoLiveData;
    }
}
