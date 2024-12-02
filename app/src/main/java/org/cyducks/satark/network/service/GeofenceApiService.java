package org.cyducks.satark.network.service;

import org.cyducks.satark.core.conflictzone.model.ConflictZone;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GeofenceApiService {
    @POST("api/v1/zones/conflict")
    Call<ConflictZone> createConflictZone(@Body ConflictZone zone);

    @GET("api/v1/zones")
    Call<List<ConflictZone>> getAllZones();

    @DELETE("api/v1/zones/{zoneId}")
    Call<Void> deleteZone(@Path("zoneId") String zoneId);

    @GET("api/v1/zones/{zoneId}")
    Call<ConflictZone> getZoneById(@Path("zoneId") String zoneId);
}
