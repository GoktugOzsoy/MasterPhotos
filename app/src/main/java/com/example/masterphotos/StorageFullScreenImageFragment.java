package com.example.masterphotos;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
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
                hideBottomNavigation();
            }
        });

        ImageButton downloadButton = view.findViewById(R.id.downloadButtonS);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImage();
            }
        });

        hideBottomNavigation();

        return view;
    }

    private void showDetailsDialog() {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);

        photoRef.getMetadata().addOnSuccessListener(metadata -> {
            long fileSizeInBytes = metadata.getSizeBytes();
            long fileSizeInKB = fileSizeInBytes / 1024;

            long creationTimeMillis = metadata.getCreationTimeMillis();

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String takenDate = dateFormat.format(new Date(creationTimeMillis));

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle((R.string.image_details));
            builder.setMessage(getString((R.string.size)) + fileSizeInKB + " KB\n"
                    + getString(R.string.date) + takenDate);
            builder.setPositiveButton((R.string.ok), null);
            builder.show();
        }).addOnFailureListener(exception -> {
            Toast.makeText(requireContext(), (R.string.failed_to_fetch_image_details), Toast.LENGTH_SHORT).show();
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.delete_image);
        hideBottomNavigation();

        builder.setMessage(R.string.are_you_sure_you_want_to_delete_this_image);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteImage();
                showBottomNavigation();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideBottomNavigation();
            }
        });
        builder.show();
    }

    private void deleteImage() {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
        photoRef.delete().addOnSuccessListener(task -> {
            Toast.makeText(getContext(), (R.string.image_deleted), Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), (R.string.failed_to_delete_image), Toast.LENGTH_SHORT).show();
        });
    }

    private void downloadImage() {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
        photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
            DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(getString(R.string.downloading_image));
            request.setDescription(getString(R.string.downloading_image_from_firebase_storage));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloaded_image.jpg");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            downloadManager.enqueue(request);

            Toast.makeText(getContext(), R.string.downloading_imagee, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), (R.string.failed_to_download_image), Toast.LENGTH_SHORT).show();
        });
    }

    private void hideBottomNavigation() {
        MainActivity activity = (MainActivity) requireActivity();
        activity.hideBottomNavigation();
    }

    private void showBottomNavigation() {
        MainActivity activity = (MainActivity) requireActivity();
        activity.showBottomNavigation();
    }
}
