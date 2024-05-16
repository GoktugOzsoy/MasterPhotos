package com.example.masterphotos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    ImageButton  btn_logout, btn_goBack, btn_settings;

    TextView tv_email, tv_photoCount;

    SwitchCompat switchtheme;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        btn_logout = view.findViewById(R.id.btn_logout);
        btn_goBack = view.findViewById(R.id.btnimage_goBack);
        tv_photoCount = view.findViewById(R.id.tv_photoCount);
        switchtheme = view.findViewById(R.id.switch_theme);
        tv_email = view.findViewById(R.id.tv_userEmail);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySettings", Context.MODE_PRIVATE);

        boolean switchState = sharedPreferences.getBoolean("switch_state", false);
        switchtheme.setChecked(switchState);

        switchtheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSwitchState(isChecked);
            }
        });

        btn_goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
            }
        });

        return view;
    }

    private void saveSwitchState(boolean isChecked) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("switch_state", isChecked);
        editor.apply();
    }

}