package org.cyducks.satark.network.repository;

import android.util.Log;

import org.cyducks.satark.network.request.SafetyScoreRequest;
import org.cyducks.satark.network.service.SafetyScoreService;

import java.util.Calendar;
import java.util.Locale;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SafetyScoreRepository {
    private final SafetyScoreService safetyScoreService;

    public SafetyScoreRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://satark-module-1-backend.onrender.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        safetyScoreService = retrofit.create(SafetyScoreService.class);
    }

    private String formatRequestParams(double latitude, double longitude) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;

        return String.format(Locale.ENGLISH, "%d,%d,%.6f,%.6f,%d,%d", hour, minute, latitude, longitude, day, month);
    }

    public Single<Integer> getSafetyScore(double latitude, double longitude) {
        String params = formatRequestParams(latitude, longitude);
        SafetyScoreRequest request = new SafetyScoreRequest(params);
        return safetyScoreService.getSafetyScore(request)
                .map(safetyScoreResponse -> {
                    try {
                        return Integer.parseInt(safetyScoreResponse.getScore());
                    } catch (NumberFormatException e) {
                        double score = Double.parseDouble(safetyScoreResponse.getScore());
                        Log.d("TAG", "getSafetyScore: " + score);
                        return (int) (score * 100);
                    }
                });
    }
}
