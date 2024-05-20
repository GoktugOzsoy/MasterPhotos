package com.example.masterphotos;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FirebaseAuth örneğini al
        auth = FirebaseAuth.getInstance();

        // BottomNavigationView'ı bul ve seçilen öğeyi dinle
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationView.setSelectedItemId(R.id.nav_upload);

        // Uygulama başlangıcında ilk fragmenti yükle (GalleryFragment)
        if (savedInstanceState == null) {
            loadFragment(new UploadFragment());
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.night_black));
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
            } else if (itemId == R.id.nav_gallery) {
                selectedFragment = new GalleryFragment();
            } else if (itemId == R.id.nav_profile) {
                // Kullanıcı giriş yapmışsa SettingsFragment'e, aksi halde ProfileFragment'e git
                if (auth.getCurrentUser() == null) {
                    selectedFragment = new ProfileFragment();
                } else {
                    selectedFragment = new SettingsFragment();
                }
            } else if (itemId == R.id.nav_storage) {
                selectedFragment = new StorageFragment();
            }

            // Seçilen fragmenti yükle
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            updateBottomNavigationView();
            return true;
        }

        public void updateBottomNavigationView() {
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null && bottomNavigationView.getVisibility() == View.VISIBLE) {
                Menu menu = bottomNavigationView.getMenu();
                menu.findItem(R.id.nav_upload).setTitle(R.string.upload);
                menu.findItem(R.id.nav_storage).setTitle(R.string.storage);
                menu.findItem(R.id.nav_gallery).setTitle(R.string.gallery);
                menu.findItem(R.id.nav_profile).setTitle(R.string.profile);
            }
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

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

    public void selectGalleryItem() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_gallery);
        }
    }
}
