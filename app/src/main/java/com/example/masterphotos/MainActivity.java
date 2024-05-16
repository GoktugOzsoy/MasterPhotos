package com.example.masterphotos;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    FirebaseAuth auth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new UploadFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            auth = FirebaseAuth.getInstance();


            if (item.getItemId() == R.id.nav_upload) {
                selectedFragment = new UploadFragment();
            } else if (item.getItemId() == R.id.nav_gallery) {
                selectedFragment = new GalleryFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                if (auth.getCurrentUser() == null) {
                    selectedFragment = new ProfileFragment();
                } else {
                    selectedFragment = new SettingsFragment();
                }
            } else if (item.getItemId() == R.id.nav_storage) {
                selectedFragment = new StorageFragment();
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

            return true;
        }
    };
}
