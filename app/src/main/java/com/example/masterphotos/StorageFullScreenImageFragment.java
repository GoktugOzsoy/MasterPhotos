package com.example.masterphotos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StorageFullScreenImageFragment extends Fragment {

    private String imageURL;

    public static StorageFullScreenImageFragment newInstance(String imageURL) {
        StorageFullScreenImageFragment fragment = new StorageFullScreenImageFragment();
        Bundle args = new Bundle();
        args.putString("imageURL", imageURL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage_full_screen_image, container, false);

        if (getArguments() != null) {
            imageURL = getArguments().getString("imageURL");
        }

        ImageView fullScreenImageView = view.findViewById(R.id.fullScreenImageViewS);
        Glide.with(this).load(imageURL).into(fullScreenImageView);

        ImageButton closeButton = view.findViewById(R.id.closeButtonS);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
                // Geri butonuna basıldığında alt gezinme çubuğunu göster
                showBottomNavigation();
            }
        });

        ImageButton detailsButton = view.findViewById(R.id.detailsButtonS);
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailsDialog();
            }
        });

        ImageButton deleteButton = view.findViewById(R.id.deleteButtonS);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
                showBottomNavigation();
            }
        });

        // Alt gezinme çubuğunu gizle
        hideBottomNavigation();

        return view;
    }

    private void showDetailsDialog() {
        // Firebase Storage'dan resmin detaylarını almak için referans oluştur
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);

        // Resmin metadata'sını al
        photoRef.getMetadata().addOnSuccessListener(metadata -> {
            long fileSizeInBytes = metadata.getSizeBytes();
            long fileSizeInKB = fileSizeInBytes / 1024;

            // Resmin tarih bilgisini al
            long creationTimeMillis = metadata.getCreationTimeMillis();

            // Tarihi biçimlendirme
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String takenDate = dateFormat.format(new Date(creationTimeMillis));

            // Detayları göstermek için bir iletişim kutusu oluştur
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Image Details");
            builder.setMessage("Size: " + fileSizeInKB + " KB\n"
                    + "Date: " + takenDate);
            builder.setPositiveButton("OK", null);
            builder.show();
        }).addOnFailureListener(exception -> {
            // Başarısız olursa kullanıcıya hata mesajı göster
            Toast.makeText(requireContext(), "Failed to fetch image details", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Image");

        builder.setMessage("Are you sure you want to delete this image?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteImage();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void deleteImage() {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
        photoRef.delete().addOnSuccessListener(task -> {
            Toast.makeText(getContext(), "Image deleted", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to delete image", Toast.LENGTH_SHORT).show();
        });
    }

    private void hideBottomNavigation() {
        // MainActivity'nin bir referansını al
        MainActivity activity = (MainActivity) requireActivity();

        // MainActivity'nin alt gezinme çubuğunu gizle
        activity.hideBottomNavigation();
    }
    private void showBottomNavigation() {
        // MainActivity'nin bir referansını al
        MainActivity activity = (MainActivity) requireActivity();

        // MainActivity'nin alt gezinme çubuğunu göster
        activity.showBottomNavigation();
    }

}
