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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookActivity extends AppCompatActivity {

    private RecyclerView toBookRV;

    private FirestoreRecyclerAdapter toBookAdapter;

    private String user;
    private String city;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book);

        getSupportActionBar().setTitle("Book your locker");

        toBookRV = (RecyclerView) findViewById(R.id.toBookRV);
        toBookRV.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        user = getIntent().getStringExtra("user");
        city = getIntent().getStringExtra("city");

        Log.d("BOOKACTIVITY", city);

        getAllParks();

    }

    @Override
    public void onStart() {
        super.onStart();
        toBookAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        toBookAdapter.stopListening();
    }

    private void getAllParks(){
        Query query = db.collection("cities/"+city.hashCode()+"/parks");

        Log.d("BOOK", "cities/"+city.hashCode()+"/parks");

        FirestoreRecyclerOptions<ToBook> response = new FirestoreRecyclerOptions.Builder<ToBook>()
                .setQuery(query, ToBook.class)
                .build();

        toBookAdapter = new FirestoreRecyclerAdapter<ToBook, ToBookHolder>(response) {

            @NonNull
            @Override
            public ToBookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.tobook_card, parent, false);
                return new ToBookHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ToBookHolder toBookHolder, int i, @NonNull final ToBook toBook) {
                toBookHolder.parkName.setText(toBook.getParkName());
                toBookHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), CardToBookActivity.class);
                        i.putExtra("user", user);
                        i.putExtra("city", city);
                        i.putExtra("parkAddress", toBook.getParkAddress());
                        i.putExtra("parkName", toBook.getParkName());
                        v.getContext().startActivity(i);
                        finish();
                    }
                });
            }
        };
        toBookAdapter.notifyDataSetChanged();
        toBookRV.setAdapter(toBookAdapter);
    }


    public class ToBookHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.parkNameTV)
        TextView parkName;

        public ToBookHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
