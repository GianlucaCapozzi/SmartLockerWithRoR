package com.iot.smartlockerapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class SearchFragment extends Fragment {

    private String TAG = "SEARCH";

    private SearchView search_edit_text;
    private RecyclerView recyclerView;
    private ParkAdapter parkAdapter;

    private String user;
    private String username;

    private ArrayList<Park> cities;

    private FirebaseFirestore db;

    public SearchFragment() {
    }

    public static SearchFragment newInstance(String user, String username) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString("user", user);
        args.putString("username", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            user = getArguments().getString("user");
            username = getArguments().getString("username");
            Log.d(TAG, user);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_search_park, container, false);

        search_edit_text = v.findViewById(R.id.searchView);
        recyclerView = v.findViewById(R.id.searchRV);

        db = FirebaseFirestore.getInstance();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        cities = new ArrayList<Park>();

        parkAdapter = new ParkAdapter(cities, user, username);
        recyclerView.setAdapter(parkAdapter);

        getCities();

        search_edit_text.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, newText);
                parkAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return v;

    }

    private void getCities() {
        db.collection("cities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot doc : task.getResult()) {
                            cities.add(doc.toObject(Park.class));
                        }
                    }
                });
    }

}
