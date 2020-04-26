package com.iot.smartlockerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    static String url = "https://smart-locker-macc.herokuapp.com";

    private static final String PREFS_NAME = "SmartLockSettings";


    private String user;
    private String email;
    private String token;

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

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = pref.getString("user", null);
        String image = pref.getString("image", null);

        token = pref.getString("auth_token", null);
        Log.d(TAG, "Auth_token: " + token);

        final BottomNavigationView bottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        dl = (DrawerLayout) findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl, R.string.Open, R.string.Close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
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
                    case R.id.homeDrw:
                        getSupportActionBar().setTitle("Booking");
                        openFragment(HomeFragment.newInstance());
                        bottomNavigation.setVisibility(View.VISIBLE);
                        dl.closeDrawers();
                        return true;
                    case R.id.account:
                        getSupportActionBar().setTitle("Account");
                        bottomNavigation.setVisibility(View.GONE);
                        openFragment(AccountFragment.newInstance());
                        dl.closeDrawers();
                        return true;
                    case R.id.settings:
                        getSupportActionBar().setTitle("Settings");
                        bottomNavigation.setVisibility(View.GONE);
                        openFragment(SettingsFragment.newInstance());
                        dl.closeDrawers();
                        return true;
                    case R.id.logout:
                        logout();
                        return true;
                }

                return false;
            }
        });

        View header = nv.getHeaderView(0);

        if(!image.equals("R.drawable.com_facebook_profile_picture_blank_portrait")) {
            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            profilePict = header.findViewById(R.id.nav_profile_pict);
            profilePict.setImageBitmap(decodedByte);
        }


        drawerName = header.findViewById(R.id.nav_header_textView);
        drawerName.setText(name);


        openFragment(HomeFragment.newInstance());

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
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

    private void logout() {

        //CHECK FOR FACEBOOK LOGOUT
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null && !accessToken.isExpired()) {
            Log.d(TAG, "In FACEBOOK LOGOUT");
            LoginManager.getInstance().logOut();
        }

        // SEND TOKEN TO SERVER FOR BLACKLIST
        String postUrl = url + "/logout";
        HttpLogoutGetAsyncTask okHttpAsync = new HttpLogoutGetAsyncTask();
        okHttpAsync.execute(postUrl);

    }

    private class HttpLogoutGetAsyncTask extends AsyncTask<String, Void, byte[]> {

        private String resp;

        private HttpLogoutGetAsyncTask() {
            resp = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {

            Log.d(TAG + " logout", "request done");

            String postUrl = strings[0];
            Log.d(TAG + " logout", postUrl);

            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(postUrl)
                    .header("Authorization", token)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                resp = response.body().string();
                Log.d(TAG + " logout", resp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            try {
                JSONObject json = new JSONObject(resp);
                String responseString = json.getString("response");
                Log.d(TAG + " logout", responseString);
                if (responseString.equals("success")) {
                    getSharedPreferences(PREFS_NAME, 0).edit().clear().apply();
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                } else {
                    Log.d(TAG + " logout" + " ERR", responseString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
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
                            openFragment(HomeFragment.newInstance());
                            return true;
                        case R.id.navigation_book:
                            getSupportActionBar().setTitle("Search Park");
                            openFragment(SearchFragment.newInstance(email, user));
                            return true;
                        case R.id.navigation_prev_bookings:
                            getSupportActionBar().setTitle("Previous bookings");
                            openFragment(PrevBookingsFragment.newInstance(email));
                            return true;
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
