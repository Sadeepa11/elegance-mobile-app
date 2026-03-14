package com.nexora.elegance.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.nexora.elegance.models.DirectionsResponse;

/**
 * DirectionsService defines the specific "Endpoint" we call on Google's servers.
 * It turns a web address into a Java function we can call.
 */
public interface DirectionsService {
    @GET("directions/json")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("mode") String mode,
            @Query("key") String apiKey
    );
}
