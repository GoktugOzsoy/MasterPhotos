package com.example.masterphotos;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class StorageFragment extends Fragment implements StorageAdapter.OnImageClickListener {
    private FirebaseStorage storage;
    private StorageReference storageRef;
    FirebaseUser user;
    private RecyclerView recyclerView;
    private StorageAdapter adapter;
    private List<String> imageURLs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            String currentUserID = user.getUid();
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference().child("users").child(currentUserID).child("images");

            recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

            imageURLs = new ArrayList<>();
            adapter = new StorageAdapter(imageURLs, this);
            recyclerView.setAdapter(adapter);
            getImagesFromFirebaseStorage();
        } else {
            Toast.makeText(getContext(), (R.string.please_sign_in_to_access_storage), Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void getImagesFromFirebaseStorage() {
        storageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        AtomicLong totalSize = new AtomicLong(0);

                        for (StorageReference item : listResult.getItems()) {
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageURLs.add(uri.toString());
                                    adapter.notifyDataSetChanged();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle any errors
                                }
                            });

                            item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {
                                    totalSize.addAndGet(storageMetadata.getSizeBytes());

                                    // Eğer tüm öğeleri gezdiyseniz totalSize değerini settings fragment'a aktarın
                                    Context context = getContext();
                                    if (context != null) {
                                        SharedPreferences sharedPreferences = context.getSharedPreferences("GalleryPrefs", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putLong("totalStorageSize", totalSize.get());
                                        editor.apply();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle any errors
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                    }
                });
    }



    @Override
    public void onImageClick(String imageURL) {
        openFullScreenImage(imageURL);
    }

    private void openFullScreenImage(String imageURL) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, StorageFullScreenImageFragment.newInstance(imageURL));
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
