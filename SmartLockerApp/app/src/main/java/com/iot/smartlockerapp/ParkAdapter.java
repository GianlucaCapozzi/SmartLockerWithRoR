package com.iot.smartlockerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ParkAdapter extends RecyclerView.Adapter<ParkAdapter.MyViewHolder> {

    private List<Park> parkList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ParkAdapter(List<Park> list) {
        parkList = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.park_search_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Park p = parkList.get(position);
        holder.cityTV.setText(p.getName());
    }

    @Override
    public int getItemCount() {
        return parkList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView cityTV;

        public MyViewHolder(View view) {
            super(view);
            cityTV = view.findViewById(R.id.cityTV);
        }

    }

}