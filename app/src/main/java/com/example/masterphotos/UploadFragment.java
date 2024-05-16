package com.example.masterphotos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri imageUri;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    FirebaseUser user;
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
            // Kullanıcı giriş yapmamışsa veya Firebase kimlik doğrulaması yoksa, null olduğu için gerekli işlemleri yapamazsınız.
            // Bu durumu uygun şekilde işleyin, örneğin bir hata mesajı gösterin veya kullanıcıyı giriş yapmaya yönlendirin.
            // Burada currentUserID'yi null olarak ayarlamak yerine, uygun bir şekilde işlem yapın.
            // Örneğin:
            Toast.makeText(getContext(), "Please sign in to upload images", Toast.LENGTH_SHORT).show();
            // Veya
            // startActivity(new Intent(getContext(), LoginActivity.class));
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
                    uploadImage(imageView);
                } else {
                    Toast.makeText(getContext(), "Please sign in to upload images", Toast.LENGTH_SHORT).show();
                    // Veya kullanıcıyı giriş yapmaya yönlendirin:
                    // startActivity(new Intent(getContext(), LoginActivity.class));
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

    private void uploadImage(ImageView imageView) {
        if (imageUri != null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading File....");
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
                            Toast.makeText(getContext(), "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
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
