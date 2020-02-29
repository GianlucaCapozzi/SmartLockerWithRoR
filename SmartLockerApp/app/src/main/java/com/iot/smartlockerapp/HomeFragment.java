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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragment extends Fragment {

    private String TAG = "HOME";

    private TextView usernameTV;
    private RecyclerView bookedRV;

    private FirestoreRecyclerAdapter bookingAdapter;

    private String user;
    private String email;
    private String lockName;
    private boolean lockState;

    private FirebaseFirestore db;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String user, String email) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("user", user);
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            user = getArguments().getString("user");
            email = getArguments().getString("email");
            Log.d(TAG, user);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        Log.d(TAG, user);

        db = FirebaseFirestore.getInstance();

        usernameTV = (TextView) v.findViewById(R.id.usernameView);
        usernameTV.setText("Welcome back, " + user);

        bookedRV = (RecyclerView) v.findViewById(R.id.bookedRV);
        bookedRV.setLayoutManager(new LinearLayoutManager(getActivity()));

        getUserActiveBookings();

        return v;
    }

    private void getUserActiveBookings(){
        Query query = db.collection("bookings")
                .whereEqualTo("user", email)
                .whereEqualTo("active", true);

        FirestoreRecyclerOptions<Booking> response = new FirestoreRecyclerOptions.Builder<Booking>()
                .setQuery(query, Booking.class)
                .build();

        bookingAdapter = new FirestoreRecyclerAdapter<Booking, BookingActiveHolder>(response) {

            @NonNull
            @Override
            public BookingActiveHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.booking_card, parent, false);
                return new BookingActiveHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull BookingActiveHolder bookingHolder, int i, @NonNull final Booking booking) {
                Log.d(TAG, booking.getPark());
                Log.d(TAG, booking.getLockHash());
                getLockInfo(booking.getCity(), booking.getPark(), booking.getLockHash());
                bookingHolder.parkB.setText(booking.getPark());
                bookingHolder.dateB.setText(booking.getDate());
                bookingHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), CardBookingActivity.class);
                        i.putExtra("park", booking.getPark());
                        i.putExtra("date", booking.getDate());
                        i.putExtra("lockHash", booking.getLockHash());
                        i.putExtra("lockName", lockName);
                        i.putExtra("lockState", lockState);
                        v.getContext().startActivity(i);
                    }
                });
            }
        };
        bookingAdapter.notifyDataSetChanged();
        bookedRV.setAdapter(bookingAdapter);
    }

    public class BookingActiveHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.parkTV)
        TextView parkB;
        @BindView(R.id.dateTV)
        TextView dateB;

        public BookingActiveHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        bookingAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        bookingAdapter.stopListening();
    }

    private void getLockInfo(String city, String parkName, String lockHash){
        String cityPark = city + parkName;
        Log.d(TAG, "cities/"+city+"/parks/"+cityPark.hashCode()+"/lockers");
        DocumentReference docRef = db.collection("cities/"+city+"/parks/"+cityPark.hashCode()+"/lockers").document(lockHash);

        Log.d(TAG, docRef.toString());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Locker lock = documentSnapshot.toObject(Locker.class);
                Log.d(TAG, lock.toString());
                lockName = lock.getLockName();
                Log.d(TAG, "LockName: " + lockName);
                lockState = lock.isOpen();
                Log.d(TAG, "LockState: " + lockState);
            }
        });
    }

}
