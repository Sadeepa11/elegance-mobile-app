package com.nexora.elegance.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.nexora.elegance.R;
import com.nexora.elegance.utils.NetworkUtils;

public class NoInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        findViewById(R.id.retryButton).setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                // Connection restored, go back to splash or just finish and let the app proceed
                startActivity(new Intent(this, SplashActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Still no connection. Please check your settings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to a potentially broken state
        moveTaskToBack(true);
    }
}
