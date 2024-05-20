package com.example.masterphotos;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int REQUEST_PERMISSION = 123;
    private Uri imageUri;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String currentUserID;

    public UploadFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        Button selectImageButton = view.findViewById(R.id.selectimagebtn);
        Button uploadImageButton = view.findViewById(R.id.uploadimagebtn);
        ImageView imageView = view.findViewById(R.id.firebaseimage);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserID = user.getUid();
        } else {
            Toast.makeText(getContext(), (R.string.please_sign_in_to_upload_images), Toast.LENGTH_SHORT).show();
        }

        if (checkPermission()) {

        } else {
            requestPermission();
        }

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                user = mAuth.getCurrentUser();

                if (user != null) {
                    checkStorageAndUpload(imageView);
                } else {
                    Toast.makeText(getContext(), (R.string.please_sign_in_to_upload_images), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(requireContext(), (R.string.permission_granted), Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(requireContext(), (R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkStorageAndUpload(ImageView imageView) {
        if (imageUri != null) {
            long fileSize = getFileSize(imageUri);
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("GalleryPrefs", Context.MODE_PRIVATE);
            long totalStorageSize = sharedPreferences.getLong("totalStorageSize", 0);

            if (totalStorageSize + fileSize <= 20 * 1024 * 1024) {
                uploadImage(imageView);
            } else {
                Toast.makeText(getContext(), (R.string.storage_limit_exceeded), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), (R.string.please_select_an_image_first), Toast.LENGTH_SHORT).show();
        }
    }

    private long getFileSize(Uri uri) {
        long size = 0;
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (sizeIndex != -1) {
                size = cursor.getLong(sizeIndex);
            }
            cursor.close();
        }
        return size;
    }

    private void uploadImage(ImageView imageView) {
        if (imageUri != null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle(getString(R.string.uploading_file));
            progressDialog.show();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
            Date now = new Date();
            String fileName = formatter.format(now);
            storageReference = FirebaseStorage.getInstance().getReference("users").child(currentUserID).child("images").child(fileName);

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageView.setImageURI(null);
                            Toast.makeText(getContext(), (R.string.successfully_uploaded), Toast.LENGTH_SHORT).show();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                            taskSnapshot.getMetadata().getReference().getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {
                                    long uploadedFileSize = storageMetadata.getSizeBytes();
                                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("GalleryPrefs", Context.MODE_PRIVATE);
                                    long totalStorageSize = sharedPreferences.getLong("totalStorageSize", 0);
                                    totalStorageSize += uploadedFileSize;

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putLong("totalStorageSize", totalStorageSize);
                                    editor.apply();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            Toast.makeText(getContext(), (R.string.failed_to_upload), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), (R.string.please_select_an_image_first), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            imageUri = data.getData();
            ImageView imageView = getView().findViewById(R.id.firebaseimage);
            imageView.setImageURI(imageUri);
        }
    }
}
