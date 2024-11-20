package org.cyducks.satark.core.safetyscore.domain;

import android.graphics.Color;
import android.util.Log;

import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.cyducks.satark.network.repository.SafetyScoreRepository;

import java.net.UnknownHostException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SafetyScoreViewModel extends ViewModel {
    private final SafetyScoreRepository repository;
    private final MutableLiveData<Integer> safetyScore;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> error;
    private final CompositeDisposable disposable;


    private double lastLatitude;
    private double lastLongitude;

    public SafetyScoreViewModel() {
        repository = new SafetyScoreRepository();
        safetyScore = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        error = new MutableLiveData<>();
        disposable = new CompositeDisposable();
    }

    public LiveData<Integer> getSafetyScore() {
        return safetyScore;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<String> getError() {
        return error;
    }

    public void fetchSafetyScore(double latitude, double longitude) {
        lastLatitude = latitude;
        lastLongitude = longitude;
        fetchSafetyScore();
    }

    private void fetchSafetyScore() {
        isLoading.setValue(true);
        error.setValue(null);

        disposable.add(repository.getSafetyScore(lastLatitude, lastLongitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        score -> {
                            Log.d("TAG", "fetchSafetyScore: " + score);
                            safetyScore.setValue(score);
                            isLoading.setValue(false);
                        },
                        error -> {
                            String errorMessage = "Failed to fetch safety score";
                            if(error instanceof UnknownHostException) {
                                errorMessage = "No Internet Connection";
                            } else if(error instanceof IllegalStateException) {
                                errorMessage = "Invalid data reeceived";
                            }
                            this.error.setValue(errorMessage);
                            isLoading.setValue(false);
                        }
                ));
    }

    public void refreshScore() {
        fetchSafetyScore();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

    public int getScoreColor(int score) {
        float fraction = score / 100f;

        return ColorUtils.blendARGB(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#F44336"),
                fraction
        );
    }
}
