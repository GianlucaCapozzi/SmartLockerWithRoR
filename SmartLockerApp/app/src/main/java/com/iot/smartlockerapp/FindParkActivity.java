package com.iot.smartlockerapp;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FindParkActivity extends AppCompatActivity {

    private final static String TAG = "FIND_PARK";

    // Firebase db
    private FirebaseFirestore db;

    private MapView mapView;
    private GoogleMap map;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_park);

        final String city = getIntent().getStringExtra("city");
        final String park = getIntent().getStringExtra("parkName");

        String cityPark = city + park;

        getSupportActionBar().setTitle(city + " - " + park);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // DB
        db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("cities/"+city.hashCode()+"/parks").document(Integer.toString(cityPark.hashCode()));

        mapView = (MapView) findViewById(R.id.findParkMap);
        mapView.onCreate(savedInstanceState);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        String address = document.get("parkAddress").toString();
                        Log.d(TAG, address);
                        final double[] coordinates = getCoordinates(address);

                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                map = googleMap;
                                map.getUiSettings().setMyLocationButtonEnabled(false);
                                map.setBuildingsEnabled(true);
                                CameraPosition cameraPosition = new CameraPosition(
                                        new LatLng(coordinates[0], coordinates[1]),
                                        18,
                                        45,
                                        67
                                );
                                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(coordinates[0], coordinates[1]))
                                        .title(park + " entrance"));
                            }
                        });

                        mapView.onResume();
                    }
                }
            }
        });

    }

    private double[] getCoordinates(String address){
        double[] coordinates = new double[2];
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 5);
            if (addresses.size() > 0) {
                Double lat = (double) (addresses.get(0).getLatitude());
                Double lon = (double) (addresses.get(0).getLongitude());
                coordinates[0] = lat;
                coordinates[1] = lon;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return coordinates;
    }

}
