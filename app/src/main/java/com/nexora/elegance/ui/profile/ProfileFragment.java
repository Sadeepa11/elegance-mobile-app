package com.nexora.elegance.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nexora.elegance.data.SessionManager;
import com.nexora.elegance.databinding.FragmentProfileBinding;
import com.nexora.elegance.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;
import com.nexora.elegance.R;

/**
 * ProfileFragment displays high-level user information and provides access
 * to logout and profile settings.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load user session details
        SessionManager sessionManager = new SessionManager(requireContext());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        if (user != null) {
            binding.userEmail.setText(user.getEmail());
            
            // Fetch real-time updates from Firestore
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;
                    
                    String name = snapshot.getString("name");
                    String imageUrl = snapshot.getString("profileImageUrl");
                    
                    if (name != null) binding.userName.setText(name);
                    if (imageUrl != null && !imageUrl.isEmpty() && isAdded()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.profile_user)
                            .into(binding.profileImage);
                    }
                });
        }

        // Handle Logout
        binding.logoutButton.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
