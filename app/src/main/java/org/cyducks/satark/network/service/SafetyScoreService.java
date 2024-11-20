package org.cyducks.satark.network.service;

import org.cyducks.satark.network.request.SafetyScoreRequest;
import org.cyducks.satark.network.response.SafetyScoreResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SafetyScoreService {
    @POST("/sentiment_score")
    Single<SafetyScoreResponse> getSafetyScore(@Body SafetyScoreRequest request);
}
