package com.example.masterphotos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class StorageFragment extends Fragment {
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
            adapter = new StorageAdapter(imageURLs);
            recyclerView.setAdapter(adapter);
            getImagesFromFirebaseStorage();
            // currentUserID'yi kullanarak diğer işlemleri gerçekleştirin
        } else {
            // Kullanıcı giriş yapmamışsa veya Firebase kimlik doğrulaması yoksa, null olduğu için gerekli işlemleri yapamazsınız.
            // Bu durumu uygun şekilde işleyin, örneğin bir hata mesajı gösterin veya kullanıcıyı giriş yapmaya yönlendirin.
            // Burada currentUserID'yi null olarak ayarlamak yerine, uygun bir şekilde işlem yapın.
            // Örneğin:
            Toast.makeText(getContext(), "Please sign in to access storage", Toast.LENGTH_SHORT).show();
            // Veya
            // startActivity(new Intent(getContext(), LoginActivity.class));
        }



        return view;
    }

    private void getImagesFromFirebaseStorage() {
        storageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference item : listResult.getItems()) {
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<android.net.Uri>() {
                                @Override
                                public void onSuccess(android.net.Uri uri) {
                                    imageURLs.add(uri.toString());
                                    adapter.notifyDataSetChanged();
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
}
