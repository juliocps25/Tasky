package com.julio.tasky.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.julio.tasky.Activities.LoginActivity;
import com.julio.tasky.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CircleImageView ivProfile;
    private ImageView ivEditIcon;
    private TextView tvName;
    private TextView tvEmail;
    private TextView cerrarSesion;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public ProfileFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri imageUri = data.getData();
                            uploadImageToFirebase(imageUri);
                        }
                    }
                });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                selectImage();
            } else {
                Toast.makeText(getContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivEditIcon = view.findViewById(R.id.ivEditIcon);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        cerrarSesion = view.findViewById(R.id.cerrarSesion);

        displayerUserInfo();

        ivProfile.setOnClickListener( v -> selectImage());

        cerrarSesion.setOnClickListener(v -> {
            signOut();
        });

        return view;
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void displayerUserInfo() {
        if (currentUser != null) {
            String userName = currentUser.getDisplayName();
            if (userName != null && !userName.isBlank()) {
                tvName.setText(userName);
            } else {
                tvName.setText("Usuario sin nombre");
            }

            String userEmail = currentUser.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                tvEmail.setText(userEmail);
            } else {
                tvEmail.setText("Usuario sin correo");
            }

            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.ic_profile);
            }
        } else {
            tvName.setText("No autenticado");
            tvEmail.setText("");
            ivProfile.setImageResource(R.drawable.ic_profile);
        }
    }
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_pictures")
                .child(currentUser.getUid() + ".jpg");

        storageReference.putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            // Obtener la URL de descarga
                            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> downloadUrlTask) {
                                    if (downloadUrlTask.isSuccessful()) {
                                        Uri downloadUrl = downloadUrlTask.getResult();
                                        updateUserProfilePicture(downloadUrl);
                                    } else {
                                        Toast.makeText(getContext(), "Fallo al obtener URL de descarga: " + downloadUrlTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Fallo al subir imagen: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserProfilePicture(Uri photoUrl) {
        if (currentUser == null || photoUrl == null) {
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUrl)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show();
                            Glide.with(ProfileFragment.this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(ivProfile);
                        } else {
                            Toast.makeText(getContext(), "Fallo al actualizar imagen de perfil: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}