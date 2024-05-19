package com.example.masterphotos;

import android.content.ContentResolver;
import androidx.exifinterface.media.ExifInterface;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

public class FullScreenImageFragment extends Fragment {

    private static final String ARG_IMAGE_PATH = "imagePath";

    public FullScreenImageFragment() {

    }

    public static FullScreenImageFragment newInstance(String imagePath) {
        FullScreenImageFragment fragment = new FullScreenImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_screen_image, container, false);

        ImageView imageView = view.findViewById(R.id.fullScreenImageView);
        if (getArguments() != null && getArguments().containsKey(ARG_IMAGE_PATH)) {
            String imagePath = getArguments().getString(ARG_IMAGE_PATH);
            Glide.with(requireContext()).load("file://" + imagePath).into(imageView);
        }

        ImageButton closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        ImageButton detailsButton = view.findViewById(R.id.detailsButton);
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailsDialog();
            }
        });

        ImageButton deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        return view;
    }

    private void showDetailsDialog() {
        if (getArguments() != null && getArguments().containsKey(ARG_IMAGE_PATH)) {
            String imagePath = getArguments().getString(ARG_IMAGE_PATH);

            if (imagePath != null) {
                File file = new File(imagePath);
                long fileSizeInBytes = file.length();
                long fileSizeInKB = fileSizeInBytes / 1024;

                String takenDate = null;
                try {
                    ExifInterface exifInterface = new ExifInterface(imagePath);
                    takenDate = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(R.string.image_details);
                builder.setMessage(getString(R.string.name) + file.getName() + "\n" +
                        getString(R.string.size) + fileSizeInKB + " KB" + "\n" +
                        getString(R.string.date) + takenDate);
                builder.setPositiveButton(R.string.ok, null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                Toast.makeText(requireContext(), R.string.image_path_not_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.delete_image);
        builder.setMessage(R.string.are_you_sure_you_want_to_delete_this_image);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteImageFromGallery();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private void deleteImageFromGallery() {
        if (getArguments() != null && getArguments().containsKey(ARG_IMAGE_PATH)) {
            String imagePath = getArguments().getString(ARG_IMAGE_PATH);

            if (imagePath != null) {
                ContentResolver contentResolver = requireContext().getContentResolver();
                Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String selection = MediaStore.Images.Media.DATA + " = ?";
                String[] selectionArgs = new String[]{imagePath};
                int deletedCount = contentResolver.delete(contentUri, selection, selectionArgs);

                if (deletedCount > 0) {
                    Toast.makeText(requireContext(), R.string.image_deleted_successfully, Toast.LENGTH_SHORT).show();
                    navigateToGalleryFragment();
                } else {
                    Toast.makeText(requireContext(), R.string.an_error_occurred_while_deleting_the_image, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), R.string.image_path_not_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToGalleryFragment() {
        requireActivity().getSupportFragmentManager().popBackStack("GalleryFragment", 0);
    }
}
