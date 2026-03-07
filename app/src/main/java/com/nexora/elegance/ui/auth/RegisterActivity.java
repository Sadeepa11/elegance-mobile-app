package com.nexora.elegance.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexora.elegance.MainActivity;
import com.nexora.elegance.data.SessionManager;
import com.nexora.elegance.databinding.ActivityRegisterBinding;
import com.nexora.elegance.models.UserModel;

/**
 * RegisterActivity manages new user registration.
 * It provides a form for users to provide their name, email, and password,
 * performs client-side validation, handles account creation via Firebase Auth,
 * and saves initial user profiles to Firestore.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Go back to login
        binding.backButton.setOnClickListener(v -> finish());

        // Setup Registration Logic
        binding.registerButton.setOnClickListener(v -> {
            String name = binding.nameEdit.getText().toString();
            String email = binding.emailEdit.getText().toString();
            String password = binding.passwordEdit.getText().toString();
            String confirmPassword = binding.confirmPasswordEdit.getText().toString();
            boolean isAgreed = binding.termsCheckbox.isChecked();

            // Client-side validation: Check for empty fields
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Client-side validation: Password strength check
            if (password.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Client-side validation: Password matching check
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure terms are accepted
            if (!isAgreed) {
                Toast.makeText(this, "Please agree to Terms & Conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start Firebase Authentication process for new user creation
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();
                            // Create a UserModel to represent the new user's profile
                            UserModel user = new UserModel(uid, name, email, "buyer");

                            // Store the user profile in Firestore
                            mFirestore.collection("users").document(uid).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                        // Initialize session and navigate to main
                                        sessionManager.setLogin(true, email, "buyer");
                                        navigateToMain();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                                                .show();
                                    });
                        } else {
                            Toast.makeText(this, "Auth Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        });

        // Switch to Login Screen
        binding.loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Navigates to the main application dashboard and clears the activity stack.
     */
    private void navigateToMain() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finishAffinity();
    }
}
