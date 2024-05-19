package com.example.masterphotos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    ImageButton  btn_logout, btn_goBack, btn_trlang, btn_enlang;

    TextView tv_email, tv_storagesize;

    TextView tv_photocount;

    SwitchCompat switchtheme;

    ProgressBar storageprogressbar;

    FirebaseAuth auth;
    FirebaseUser user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        btn_logout = view.findViewById(R.id.btn_logout);
        tv_photocount = view.findViewById(R.id.tv_photoCount);
        btn_goBack = view.findViewById(R.id.btnimage_goBack);
        switchtheme = view.findViewById(R.id.switch_theme);
        tv_email = view.findViewById(R.id.tv_userEmail);
        btn_enlang = view.findViewById(R.id.btn_enlang);
        btn_trlang = view.findViewById(R.id.btn_trlang);
        storageprogressbar = view.findViewById(R.id.storageProgressBar);
        tv_storagesize = view.findViewById(R.id.tv_showstoragesize);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            tv_email.setText(userEmail);
        } else {
            tv_email.setText(R.string.there_is_no_existing_email);
        }

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySettings", Context.MODE_PRIVATE);
        boolean switchState = sharedPreferences.getBoolean("switch_state", false);
        switchtheme.setChecked(switchState);

        FirebaseUser currentUser2 = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser2 != null) {
            btn_logout.setVisibility(View.VISIBLE);
            btn_goBack.setVisibility(View.INVISIBLE);
        } else {
            btn_logout.setVisibility(View.INVISIBLE);
            btn_goBack.setVisibility(View.VISIBLE);
        }

        // SharedPreferences'den resim sayısını oku
        SharedPreferences galleryPrefs = requireContext().getSharedPreferences("GalleryPrefs", Context.MODE_PRIVATE);
        int photoCount = galleryPrefs.getInt("imageCount", 0);
        tv_photocount.setText(getString(R.string.total_photos) + photoCount);

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

        btn_trlang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocale("tr");
                updateBottomNavigationView(); // Alt gezinme çubuğunu güncelle
            }
        });


        btn_enlang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocale("en");
                updateBottomNavigationView(); // Alt gezinme çubuğunu güncelle
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



        SharedPreferences galleryPrefs2 = requireContext().getSharedPreferences("GalleryPrefs", Context.MODE_PRIVATE);
        long totalSize = galleryPrefs2.getLong("totalStorageSize", 0);
        int totalStorageLimit = 20 * 1024 * 1024; // 20 MB
        int progress = (int) ((totalSize * 100) / totalStorageLimit);
        storageprogressbar.setProgress(progress);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStorageTextView();
    }


    private void updateStorageTextView() {
        SharedPreferences sharedPreferences3 = getActivity().getSharedPreferences("GalleryPrefs", Context.MODE_PRIVATE);
        long totalStorageSize = sharedPreferences3.getLong("totalStorageSize", 0);

        // Total storage in MB
        double totalStorageSizeMB = totalStorageSize / (1024.0 * 1024.0);

        // Assuming max storage limit is 20 MB
        double maxStorageMB = 20.0;

        String storageText = String.format("%.2f MB / %.2f MB", totalStorageSizeMB, maxStorageMB);
        tv_storagesize.setText(storageText);
    }


    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        saveLanguagePreference(languageCode);
        // Yeniden başlatma işlemi
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }

    private void saveSwitchState(boolean isChecked) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("switch_state", isChecked);
        editor.apply();
    }

    private void saveLanguagePreference(String languageCode) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selected_language", languageCode);
        editor.apply();
    }

    private void updateBottomNavigationView() {
        // MainActivity'deki bottomNavigationView'ı bul
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            Menu menu = bottomNavigationView.getMenu();
            menu.findItem(R.id.nav_upload).setTitle(R.string.upload);
            menu.findItem(R.id.nav_storage).setTitle(R.string.storage);
            menu.findItem(R.id.nav_gallery).setTitle(R.string.gallery);
            menu.findItem(R.id.nav_profile).setTitle(R.string.profile);
        }
    }
}