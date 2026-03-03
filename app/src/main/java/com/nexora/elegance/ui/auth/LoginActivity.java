package com.nexora.elegance.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexora.elegance.MainActivity;
import com.nexora.elegance.data.SessionManager;
import com.nexora.elegance.databinding.ActivityLoginBinding;
import com.nexora.elegance.models.UserModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            navigateToMain();
        }

        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEdit.getText().toString();
            String password = binding.passwordEdit.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                // Firebase Login
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String uid = mAuth.getCurrentUser().getUid();

                                // Fetch user data from Firestore
                                mFirestore.collection("users").document(uid).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            UserModel user = documentSnapshot.toObject(UserModel.class);
                                            if (user != null) {
                                                sessionManager.setLogin(true, email, user.getRole());
                                                navigateToMain();
                                            } else {
                                                Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Firestore Error: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(this, "Login Failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        binding.registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
