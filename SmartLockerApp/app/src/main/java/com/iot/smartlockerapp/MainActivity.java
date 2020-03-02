package com.iot.smartlockerapp;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    static String url = "http://10.0.2.2:3000";
    private static final String PREFS_NAME = "SmartLockSettings";

    private String user;
    private String email;

    private TextView drawerName;
    private CircleImageView profilePict;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    static String TAG = "MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dl = (DrawerLayout) findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl, R.string.Open, R.string.Close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu();
            }
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("Bookings");
                invalidateOptionsMenu();
            }
        };

        t.setDrawerIndicatorEnabled(true);
        dl.addDrawerListener(t);
        t.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        nv = (NavigationView) findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.account:
                        Toast.makeText(MainActivity.this, "My Account", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.settings:
                        Toast.makeText(MainActivity.this, "Settings",Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

        Log.d("MAIN", "Before navigation");
        BottomNavigationView bottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Log.d("MAIN", "After navigation");

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = pref.getString("user", null);
        String email = pref.getString("email", null);
        String image = pref.getString("image", null);

        View header = nv.getHeaderView(0);

        if(!image.equals("R.drawable.com_facebook_profile_picture_blank_portrait")) {
            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            profilePict = header.findViewById(R.id.nav_profile_pict);
            profilePict.setImageBitmap(decodedByte);
        }


        drawerName = header.findViewById(R.id.nav_header_textView);
        drawerName.setText(name);


        openFragment(HomeFragment.newInstance(name, email));

    }

    @Override
    protected void onStop() {
        super.onStop();
        getSharedPreferences(PREFS_NAME, 0).edit().clear().commit();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        t.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        t.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(t.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    user = pref.getString("user", null);
                    email = pref.getString("email", null);
                    Log.d("MAIN", user);
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            openFragment(HomeFragment.newInstance(user, email));
                            return true;
                        case R.id.navigation_book:
                            openFragment(SearchFragment.newInstance(email, user));
                            return true;
                        case R.id.navigation_prev_bookings:
                            openFragment(PrevBookingsFragment.newInstance(email));
                    }
                    return false;
                }
            };

    private void openFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
