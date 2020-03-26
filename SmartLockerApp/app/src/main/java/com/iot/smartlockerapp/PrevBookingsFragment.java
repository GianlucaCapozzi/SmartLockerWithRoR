package com.iot.smartlockerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrevBookingsFragment extends Fragment {

    private FirebaseFirestore db;

    private final static String TAG = "PrevBookings";

    private String user;
    private RecyclerView profileRV;
    private FirestoreRecyclerAdapter profileAdapter;

    private String cityName;

    private String lockName;
    private boolean lockState;

    public PrevBookingsFragment() {
    }

    public static PrevBookingsFragment newInstance(String user) {
        PrevBookingsFragment fragment = new PrevBookingsFragment();
        Bundle args = new Bundle();
        args.putString("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            user = getArguments().getString("user");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_prevbook, container, false);

        profileRV = v.findViewById(R.id.profileRV);
        profileRV.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        getUserDisabledBookings();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        profileAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        profileAdapter.stopListening();
    }


    private void getUserDisabledBookings(){
        Query query = db.collection("bookings")
                .whereEqualTo("user", user)
                .whereEqualTo("active", false);

        FirestoreRecyclerOptions<Booking> response = new FirestoreRecyclerOptions.Builder<Booking>()
                .setQuery(query, Booking.class)
                .build();

        profileAdapter = new FirestoreRecyclerAdapter<Booking, BookingDisabledHolder>(response) {

            @NonNull
            @Override
            public BookingDisabledHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.profile_card, parent, false);
                return new BookingDisabledHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final BookingDisabledHolder bookingDisabledHolder, int i, @NonNull final Booking booking) {
                getLockInfo(booking.getCity(), booking.getPark(), booking.getLockHash());
                //getCity(booking.getCity());
                bookingDisabledHolder.parkBD.setText(booking.getCity() + "-" + booking.getPark());
                bookingDisabledHolder.dateBD.setText(booking.getDate());
                bookingDisabledHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), ProfileActivity.class);
                        i.putExtra("park", booking.getPark());
                        i.putExtra("date", booking.getDate());
                        i.putExtra("locker", lockName);
                        i.putExtra("leaveTime", booking.getLeave());
                        v.getContext().startActivity(i);
                    }
                });
            }
        };
        profileAdapter.notifyDataSetChanged();
        profileRV.setAdapter(profileAdapter);
    }

    private void getLockInfo(String city, String parkName, String lockHash){
        String cityPark = city + parkName;
        Log.d(TAG, "cities/"+city.hashCode()+"/parks/"+cityPark.hashCode()+"/lockers");
        DocumentReference docRef = db.collection("cities/"+city.hashCode()+"/parks/"+cityPark.hashCode()+"/lockers").document(lockHash);

        Log.d(TAG, docRef.toString());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Locker lock = documentSnapshot.toObject(Locker.class);
                //Log.d(TAG, lock.toString());
                lockName = lock.getLockName();
                //Log.d(TAG, "LockName: " + lockName);
                lockState = lock.isOpen();
                //Log.d(TAG, "LockState: " + lockState);
            }
        });
    }

    private void getCity(String cityID) {
        String c = "";
        DocumentReference docRef = db.collection("cities").document(cityID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document != null) {
                        cityName = document.getString("name");
                        Log.d(TAG, cityName);
                    }
                }
                else{
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public class BookingDisabledHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.parkTV1)
        TextView parkBD;
        @BindView(R.id.dateTV1)
        TextView dateBD;

        public BookingDisabledHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
