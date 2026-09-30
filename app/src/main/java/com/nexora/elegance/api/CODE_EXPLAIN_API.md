# 🌐 Network Layer: Google Maps Directions API

This package handles the communication between the **Elegance** app and **Google's Directions API**. It uses **Retrofit 2**, the industry-standard library for Android networking.

---

## 1. [RetrofitClient.java](file:///d:/Elegance%20full/Elegance/app/src/main/java/com/nexora/elegance/api/RetrofitClient.java) - Line-by-Line Explanation

This class is a **Singleton**. Its only job is to create and provide a single, reusable "engine" to make web requests.

| Line # | Code Fragment | Explanation |
| :--- | :--- | :--- |
| **13** | `private static Retrofit retrofit = null;` | Holds the single instance of Retrofit. Being `static` ensures it's shared across the whole app. |
| **14** | `BASE_URL = "https://maps..."` | The root address for all Google Maps API calls. |
| **17** | `if (retrofit == null) { ... }` | **The Singleton Pattern.** It only initializes the engine the *first* time it's called. |
| **20** | `HttpLoggingInterceptor interceptor = ...` | Creates a tool that "listens" to the network traffic. |
| **21** | `interceptor.setLevel(BODY);` | Configures the listener to show the *entire* message (headers + data) in your Logcat. |
| **22** | `OkHttpClient client = ...` | Builds the underlying "browser" engine and adds our logger to it. |
| **25** | `new Retrofit.Builder()` | Starts the construction of the Retrofit engine. |
| **26** | `.baseUrl(BASE_URL)` | Tells Retrofit where Google's servers are located. |
| **27** | `.client(client)` | Plugs in our custom `OkHttpClient` (with logging enabled). |
| **28** | `.addConverterFactory(...)` | Uses **GSON** to automatically convert JSON text into Java Objects. |
| **29** | `.build();` | Finalizes the construction. |

---

## 2. [DirectionsService.java](file:///d:/Elegance%20full/Elegance/app/src/main/java/com/nexora/elegance/api/DirectionsService.java) - Line-by-Line Explanation

This is an **Interface**. It maps a simple Java function to a complex web URL.

| Line # | Code Fragment | Explanation |
| :--- | :--- | :--- |
| **13** | `@GET("directions/json")` | Annotation telling Retrofit this is a **GET** request to the `/directions/json` path. |
| **14** | `Call<DirectionsResponse>` | A `Call` object represents a task that will eventually return a `DirectionsResponse` object. |
| **14** | `getDirections(...)` | The name of the Java function you will call in your Activities. |
| **15** | `@Query("origin") String origin` | Takes your `origin` variable and adds `?origin=your_value` to the web URL. |
| **16** | `@Query("destination") ...` | Adds the destination location to the URL query string. |
| **17** | `@Query("mode") ...` | Specifies transport mode (e.g., "driving" or "walking"). |
| **18** | `@Query("key") String apiKey` | Injects your secret Google API Key into the request for authentication. |

---

## 🛠️ How to use this together
In your `MapActivity.java`, you fetch a route using only a few lines:

```java
// 1. Get the Service (the "Instruction Manual")
DirectionsService service = RetrofitClient.getClient().create(DirectionsService.class);

// 2. Make the call (the "Request")
service.getDirections(origin, destination, mode, apiKey).enqueue(new retrofit2.Callback<com.nexora.elegance.models.DirectionsResponse>() {
    @Override
    public void onResponse(retrofit2.Call<com.nexora.elegance.models.DirectionsResponse> call, retrofit2.Response<com.nexora.elegance.models.DirectionsResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            // Here, the 'response.body()' is already a Java object! 
            // No manual JSON parsing required.
        }
    }

    @Override
    public void onFailure(retrofit2.Call<com.nexora.elegance.models.DirectionsResponse> call, Throwable t) {
        // Handle network error
    }
});
```
