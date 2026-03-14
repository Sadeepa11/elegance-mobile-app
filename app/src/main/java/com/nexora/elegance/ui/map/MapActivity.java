package com.nexora.elegance.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nexora.elegance.R;

/**
 * MapActivity handles the interactive map where users find shops and get directions.
 * Key Features for Beginners:
 * - GoogleMap Integration: Using ‘OnMapReadyCallback’ to know when the map is ready to use.
 * - Dynamic Direction Routing: Uses Google Directions API to draw paths on the map.
 * - Custom Bitmaps: Generates ‘modern’ marker pins using Java Canvas drawing.
 * - Real-time Location: Fetches the user's current GPS position.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userLocation;
    private LatLng selectedShop;
    
    private final LatLng rathmalanaShop = new LatLng(6.823653, 79.886595);
    private final LatLng kohuwalaShop = new LatLng(6.862040, 79.888064);

    private TextView tvShopName;
    private TextView tvTravelTime;
    private TextView tvDistance;
    private com.google.android.material.button.MaterialButtonToggleGroup modeToggleGroup;
    private String currentTransportMode = "driving";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Setup the top Toolbar with a back (home) button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize UI text fields for shop name and travel details
        tvShopName = findViewById(R.id.tvShopName);
        tvTravelTime = findViewById(R.id.tvTravelTime);
        tvDistance = findViewById(R.id.tvDistance);
        modeToggleGroup = findViewById(R.id.modeToggleGroup);

        // Listener for transport mode switches (Driving, Walking, Cycling)
        modeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnDriving) currentTransportMode = "driving";
                else if (checkedId == R.id.btnWalking) currentTransportMode = "walking";
                else if (checkedId == R.id.btnCycling) currentTransportMode = "bicycling";
                // Request a new route whenever the mode changes
                drawRouteToShop();
            }
        });

        // Initialize the fragment that actually contains the Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            // This starts the asynchronous process of loading the map
            mapFragment.getMapAsync(this);
        }

        // Setup the client that allows us to find the user's GPS coordinates
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup Quick-action buttons (Floating Action Buttons)
        findViewById(R.id.fabMyLocation).setOnClickListener(v -> getUserLocation());
        findViewById(R.id.fabDrawRoute).setOnClickListener(v -> drawRouteToShop());

        // Read specific shop details passed from the sidebar or product screens
        handleIntentData(getIntent());
    }

    private void handleIntentData(Intent intent) {
        if (intent != null && intent.hasExtra("shopName")) {
            String shopName = intent.getStringExtra("shopName");
            double lat = intent.getDoubleExtra("lat", 0);
            double lng = intent.getDoubleExtra("lng", 0);

            if (lat != 0 && lng != 0) {
                selectedShop = new LatLng(lat, lng);
                tvShopName.setText("Shop: " + shopName);
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (selectedShop != null) {
            // Show only the selected shop
            String shopNameStr = getIntent().getStringExtra("shopName");
            String title = (shopNameStr != null) ? "Elegance " + shopNameStr : "Selected Shop";
            mMap.addMarker(new MarkerOptions()
                    .position(selectedShop)
                    .title(title)
                    .icon(getModernMarker(R.mipmap.ic_launcher_round, "#E91E63")));
        } else {
            // Add both shops if no specific shop was selected
            mMap.addMarker(new MarkerOptions()
                    .position(rathmalanaShop)
                    .title("Elegance Rathmalana")
                    .icon(getModernMarker(R.mipmap.ic_launcher_round, "#E91E63")));
            mMap.addMarker(new MarkerOptions()
                    .position(kohuwalaShop)
                    .title("Elegance Kohuwala")
                    .icon(getModernMarker(R.mipmap.ic_launcher_round, "#E91E63")));
        }

        getUserLocation();
    }

    /**
     * This method renders a modern looking PIN marker on the map using pure Java code (Canvas).
     * @param iconResId The resource ID of the logo image to put in the circle
     * @param colorHex The color of the pin (e.g., "#E91E63" for shops)
     */
    private com.google.android.gms.maps.model.BitmapDescriptor getModernMarker(int iconResId, String colorHex) {
        int width = 120;
        int height = 160;
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);

        // 1. Draw the teardrop/pin shape manually using a Path
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(width / 2f, height);
        path.cubicTo(width / 2f, height, 0, height * 0.6f, 0, width / 2f);
        path.arcTo(new android.graphics.RectF(0, 0, width, width), 180, 180);
        path.cubicTo(width, width / 2f, width/2f, height * 0.6f, width / 2f, height);
        path.close();

        paint.setColor(android.graphics.Color.parseColor(colorHex));
        canvas.drawPath(path, paint);

        // 2. Draw a white inner circle where the icon will sit
        paint.setColor(android.graphics.Color.WHITE);
        canvas.drawCircle(width / 2f, width / 2f, (width / 2f) - 10, paint);

        // 3. Draw the actual logo on top of the white circle
        android.graphics.drawable.Drawable drawable = androidx.core.content.ContextCompat.getDrawable(this, iconResId);
        if (drawable != null) {
            int padding = 20;
            drawable.setBounds(padding, padding, width - padding, width - padding);
            drawable.draw(canvas);
        }

        // Convert the rendered Java Bitmap into a format Google Maps can use as an icon
        return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                
                // Add User Profile Marker
                loadUserProfileMarker();

                if (selectedShop != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedShop, 12));
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
                }
            } else {
                Toast.makeText(this, "Could not get current location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfileMarker() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null || userLocation == null) return;

        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            new Thread(() -> {
                                try {
                                    java.net.URL url = new java.net.URL(imageUrl);
                                    android.graphics.Bitmap profileBitmap = android.graphics.BitmapFactory.decodeStream(url.openStream());
                                    runOnUiThread(() -> {
                                        mMap.addMarker(new MarkerOptions()
                                                .position(userLocation)
                                                .title("Me")
                                                .icon(createUserMarker(profileBitmap)));
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        } else {
                            mMap.addMarker(new MarkerOptions().position(userLocation).title("Me").icon(getModernMarker(R.drawable.ic_person, "#2196F3")));
                        }
                    }
                });
    }

    private com.google.android.gms.maps.model.BitmapDescriptor createUserMarker(android.graphics.Bitmap profileBitmap) {
        int width = 120;
        int height = 160;
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);

        // Pin Shape (Blue for user)
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(width / 2f, height);
        path.cubicTo(width / 2f, height, 0, height * 0.6f, 0, width / 2f);
        path.arcTo(new android.graphics.RectF(0, 0, width, width), 180, 180);
        path.cubicTo(width, width / 2f, width/2f, height * 0.6f, width / 2f, height);
        path.close();
        paint.setColor(android.graphics.Color.parseColor("#2196F3"));
        canvas.drawPath(path, paint);

        // Circular Profile Image
        android.graphics.Bitmap circularBitmap = android.graphics.Bitmap.createBitmap(width, width, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas circCanvas = new android.graphics.Canvas(circularBitmap);
        android.graphics.Rect rect = new android.graphics.Rect(0, 0, width, width);
        paint.setColor(android.graphics.Color.WHITE);
        circCanvas.drawCircle(width / 2f, width / 2f, (width / 2f) - 10, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        circCanvas.drawBitmap(profileBitmap, null, rect, paint);
        
        paint.setXfermode(null);
        canvas.drawBitmap(circularBitmap, 0, 0, paint);

        return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void drawRouteToShop() {
        if (userLocation == null || selectedShop == null) {
            return;
        }
        
        tvTravelTime.setText("Updating...");

        String origin = userLocation.latitude + "," + userLocation.longitude;
        String destination = selectedShop.latitude + "," + selectedShop.longitude;
        String apiKey = getApiKey();

        com.nexora.elegance.api.DirectionsService service = com.nexora.elegance.api.RetrofitClient.getClient().create(com.nexora.elegance.api.DirectionsService.class);
        service.getDirections(origin, destination, currentTransportMode, apiKey).enqueue(new retrofit2.Callback<com.nexora.elegance.models.DirectionsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.nexora.elegance.models.DirectionsResponse> call, retrofit2.Response<com.nexora.elegance.models.DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.nexora.elegance.models.DirectionsResponse dirResponse = response.body();
                    if (dirResponse.routes != null && !dirResponse.routes.isEmpty()) {
                        com.nexora.elegance.models.DirectionsResponse.Route route = dirResponse.routes.get(0);
                        
                        if (route.legs != null && !route.legs.isEmpty()) {
                            com.nexora.elegance.models.DirectionsResponse.Leg leg = route.legs.get(0);
                            tvTravelTime.setText("Time: " + leg.duration.text);
                            tvDistance.setText("Distance: " + leg.distance.text);
                        }
                        
                        if (route.overviewPolyline != null) {
                            String polylineStr = route.overviewPolyline.points;
                            java.util.List<LatLng> decodedPath = com.nexora.elegance.utils.PolylineDecoder.decode(polylineStr);
                            
                            mMap.clear(); // Clear old paths and markers
                            onMapReady(mMap); // Redraw markers
                            
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(decodedPath)
                                    .width(12)
                                    .color(android.graphics.Color.parseColor("#E91E63"))
                                    .jointType(com.google.android.gms.maps.model.JointType.ROUND);

                            mMap.addPolyline(polylineOptions);
                            
                            com.google.android.gms.maps.model.LatLngBounds.Builder builder = new com.google.android.gms.maps.model.LatLngBounds.Builder();
                            for (LatLng latLng : decodedPath) builder.include(latLng);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
                        }
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.nexora.elegance.models.DirectionsResponse> call, Throwable t) {
                Toast.makeText(MapActivity.this, "Error fetching directions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getApiKey() {
        try {
            android.content.pm.ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), android.content.pm.PackageManager.GET_META_DATA);
            android.os.Bundle bundle = ai.metaData;
            return bundle.getString("com.google.android.geo.API_KEY");
        } catch (android.content.pm.PackageManager.NameNotFoundException | NullPointerException e) {
            return "";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        }
    }
}
