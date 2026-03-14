package com.nexora.elegance.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.nexora.elegance.models.DirectionsResponse;

public interface DirectionsService {
    @GET("directions/json")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("mode") String mode,
            @Query("key") String apiKey
    );
}
