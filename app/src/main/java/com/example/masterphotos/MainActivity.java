package com.example.masterphotos;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FirebaseAuth örneğini al
        auth = FirebaseAuth.getInstance();

        // BottomNavigationView'ı bul ve seçilen öğeyi dinle
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Uygulama başlangıcında ilk fragmenti yükle (UploadFragment)
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new UploadFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            // FirebaseAuth örneğini al
            auth = FirebaseAuth.getInstance();

            // Seçilen öğeye göre ilgili fragmenti belirle
            int itemId = item.getItemId();
            if (itemId == R.id.nav_upload) {
                selectedFragment = new UploadFragment();
            } else {
                if (itemId == R.id.nav_gallery) {
                    selectedFragment = new GalleryFragment();
                } else {
                    if (itemId == R.id.nav_profile) {
                        // Kullanıcı giriş yapmışsa SettingsFragment'e, aksi halde ProfileFragment'e git
                        if (auth.getCurrentUser() == null) {
                            selectedFragment = new ProfileFragment();
                        } else {
                            selectedFragment = new SettingsFragment();
                        }
                    } else {
                        if (itemId == R.id.nav_storage) {
                            selectedFragment = new StorageFragment();
                        }
                    }
                }
            }

            // Seçilen fragmenti yükle
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            updateBottomNavigationView();
            return true;
        }

        public void updateBottomNavigationView() {
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            Menu menu = bottomNavigationView.getMenu();
            menu.findItem(R.id.nav_upload).setTitle(R.string.upload);
            menu.findItem(R.id.nav_storage).setTitle(R.string.storage);
            menu.findItem(R.id.nav_gallery).setTitle(R.string.gallery);
            menu.findItem(R.id.nav_profile).setTitle(R.string.profile);
        }
    };


    // BottomNavigationView'ın görünürlüğünü gizlemek için metot
    public void hideBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);
    }

    // BottomNavigationView'ın görünürlüğünü göstermek için metot
    public void showBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.VISIBLE);
    }
}
