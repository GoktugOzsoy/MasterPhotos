package com.example.masterphotos;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {
    public ProfileFragment() {

    }

    TextInputEditText edittext_email, edittext_password;

    Button btn_login;

    ImageButton btn_settings, btn_goBack;

    FirebaseAuth mAuth;

    TextView registernow;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        edittext_email = view.findViewById(R.id.lg_email);
        edittext_password = view.findViewById(R.id.lg_password);
        btn_login = view.findViewById(R.id.btn_login);
        mAuth = FirebaseAuth.getInstance();
        registernow = view.findViewById(R.id.txt_registernow);
        btn_settings = view.findViewById(R.id.btn_settingsPro);


        registernow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                RegisterFragment registerFragment = new RegisterFragment();

                transaction.replace(R.id.fragment_container, registerFragment);

                transaction.commit();
            }
        });

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .commit();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = edittext_email.getText().toString();
                password = edittext_password.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(getActivity(), "Please fill all the blanks.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String userEmail = edittext_email.getText().toString();

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user != null) {
                                        String userID = user.getUid();
                                        String storagePath = "users/" + userID + "/images";
                                        StorageReference userStorageRef = FirebaseStorage.getInstance().getReference(storagePath);
                                    } else {
                                        // Kullanıcı giriş yapmamışsa veya Firebase kimlik doğrulaması yoksa, null değer dönecektir
                                    }

                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, new SettingsFragment())
                                            .commit();

                                    Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();

                                } else {

                                    Toast.makeText(getActivity(), "Login failed.", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });



        return view;
    }
    }



