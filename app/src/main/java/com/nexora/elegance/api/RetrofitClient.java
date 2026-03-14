package com.nexora.elegance.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitClient is a "Singleton" class that manages the app's connection to the internet.
 * It uses the Retrofit library to handle HTTP requests/responses.
 */
public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            // HttpLoggingInterceptor allows us to see the raw API requests and responses 
            // in the Logcat (handy for debugging).
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            // Create the Retrofit instance with our BASE_URL and JSON converter (GSON)
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create()) // Automatically converts JSON -> Java Objects
                    .build();
        }
        return retrofit;
    }
}
