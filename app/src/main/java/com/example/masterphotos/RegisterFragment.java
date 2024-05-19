package com.example.masterphotos;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterFragment extends Fragment {

    TextInputEditText edittext_email, edittext_password;
    Button btn_register;
    ImageButton btn_settings, rgbtn_showpassword;
    FirebaseAuth mAuth;
    FirebaseStorage mStorage;
    StorageReference mUserStorageRef;
    String currentUserID;
    TextView loginnow;

    boolean isPasswordVisible = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        edittext_email = view.findViewById(R.id.rg_email);
        edittext_password = view.findViewById(R.id.rg_password);
        btn_register = view.findViewById(R.id.btn_register);
        loginnow = view.findViewById(R.id.txt_loginnow);
        btn_settings = view.findViewById(R.id.btn_settingsReg);
        rgbtn_showpassword = view.findViewById(R.id.rgbtn_showpassword);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

        loginnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                ProfileFragment profileFragment = new ProfileFragment();
                transaction.replace(R.id.fragment_container, profileFragment);
                transaction.commit();
            }
        });

        rgbtn_showpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // If password is visible, hide it
                    edittext_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    rgbtn_showpassword.setImageResource(R.drawable.img_hidepassword); // Change icon to "hide password" icon
                    isPasswordVisible = false;
                } else {
                    // If password is hidden, show it
                    edittext_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    rgbtn_showpassword.setImageResource(R.drawable.img_showpassword); // Change icon to "show password" icon
                    isPasswordVisible = true;
                }

                edittext_password.setTypeface(Typeface.DEFAULT);
                // Move the cursor to the end of the text
                edittext_password.setSelection(edittext_password.getText().length());
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

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = edittext_email.getText().toString();
                password = edittext_password.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(getActivity(), (R.string.please_fill_all_the_fields), Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    currentUserID = mAuth.getCurrentUser().getUid();
                                    mUserStorageRef = mStorage.getReference().child("users").child(currentUserID).child("images");
                                    Toast.makeText(getActivity(), (R.string.register_successful), Toast.LENGTH_SHORT).show();
                                    FirebaseAuth.getInstance().signOut();
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, new ProfileFragment())
                                            .commit();

                                } else {
                                    Toast.makeText(getActivity(), (R.string.register_failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });



        return view;
    }
}