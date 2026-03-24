package com.nexora.elegance.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nexora.elegance.MainActivity;
import com.nexora.elegance.R;
import com.nexora.elegance.BuildConfig;
import com.nexora.elegance.data.SessionManager;
import com.nexora.elegance.databinding.ActivityLoginBinding;
import com.nexora.elegance.models.UserModel;

/**
 * LoginActivity handles the user authentication process.
 * It allows existing users to sign in using their email and password,
 * validates inputs, authenticates with Firebase, fetches user profiles from
 * Firestore,
 * and manages session state.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private SessionManager sessionManager;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("608776197551-24bfjlgrois7dn8n869qgkblqm4erbab.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Google Sign-In Launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    }
                }
        );

        // Redirect to MainActivity if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
        }

        // Setup Login Button Click Listener
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEdit.getText().toString();
            String password = binding.passwordEdit.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                setLoading(true, false);
                // Authenticate with Firebase Auth
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String uid = mAuth.getCurrentUser().getUid();

                                // Success: Fetch detailed user data from Firestore
                                mFirestore.collection("users").document(uid).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            UserModel user = documentSnapshot.toObject(UserModel.class);
                                            if (user != null) {
                                                // Save session and proceed
                                                sessionManager.setLogin(true, email, user.getRole());
                                                navigateToMain();
                                            } else {
                                                setLoading(false, false);
                                                Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            setLoading(false, false);
                                            Toast.makeText(this, "Firestore Error: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                setLoading(false, false);
                                Toast.makeText(this, "Login Failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Setup Forgot Password Click Listener
        binding.forgotPassword.setOnClickListener(v -> {
            String email = binding.emailEdit.getText().toString();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Navigate to Registration Screen
        binding.registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Setup Google Sign-In Button Click Listener
        binding.googleSignIn.setOnClickListener(v -> {
            setLoading(true, true);
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
    }

    /**
     * Handles the result of the Google Sign-In intent.
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                setLoading(false, true);
            }
        } catch (ApiException e) {
            setLoading(false, true);
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(this, "Google Sign-In Failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Authenticates with Firebase using the Google ID Token.
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        String email = mAuth.getCurrentUser().getEmail();
                        String name = mAuth.getCurrentUser().getDisplayName();
                        String photoUrl = mAuth.getCurrentUser().getPhotoUrl() != null ? mAuth.getCurrentUser().getPhotoUrl().toString() : null;

                        // Check if user exists in Firestore
                        mFirestore.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (BuildConfig.DEBUG) {
                                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                                .setTitle("Debug: Login Success")
                                                .setMessage("Authenticated as: " + email)
                                                .setPositiveButton("OK", (dialog, which) -> {
                                                    handleFirestoreRedirect(documentSnapshot, uid, email, name, photoUrl);
                                                })
                                                .setCancelable(false)
                                                .show();
                                    } else {
                                        handleFirestoreRedirect(documentSnapshot, uid, email, name, photoUrl);
                                    }
                                });
                    } else {
                        setLoading(false, true);
                        Toast.makeText(this, "Firebase Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Handles the redirection logic after Firestore document retrieval.
     */
    private void handleFirestoreRedirect(com.google.firebase.firestore.DocumentSnapshot documentSnapshot, String uid, String email, String name, String photoUrl) {
        if (documentSnapshot.exists()) {
            UserModel user = documentSnapshot.toObject(UserModel.class);
            if (user != null) {
                sessionManager.setLogin(true, email, user.getRole());
                navigateToMain();
            }
        } else {
            // New user from Google Sign-In: Create profile in Firestore
            UserModel newUser = new UserModel(uid, name, email, "buyer");
            if (photoUrl != null) {
                newUser.setProfileImageUrl(photoUrl);
            }
            mFirestore.collection("users").document(uid).set(newUser)
                    .addOnSuccessListener(aVoid -> {
                        sessionManager.setLogin(true, email, "buyer");
                        navigateToMain();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false, true);
                        Toast.makeText(this, "Firestore Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Toggles the loading state of the UI.
     */
    private void setLoading(boolean isLoading, boolean isGoogle) {
        if (isGoogle) {
            binding.googleProgress.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.googleSignIn.setEnabled(!isLoading);
            binding.googleSignIn.setText(isLoading ? "" : "Continue With Google");
        } else {
            binding.loginProgress.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.loginButton.setEnabled(!isLoading);
            binding.loginButton.setText(isLoading ? "" : "Sign In");
        }
        
        // Also disable/enable other inputs during loading
        binding.emailEdit.setEnabled(!isLoading);
        binding.passwordEdit.setEnabled(!isLoading);
        binding.registerLink.setEnabled(!isLoading);
    }

    /**
     * Navigates to the main application dashboard and finishes the current
     * activity.
     */
    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
