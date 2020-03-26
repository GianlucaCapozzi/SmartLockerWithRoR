package com.iot.smartlockerapp;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ParkAdapter extends RecyclerView.Adapter<ParkAdapter.MyViewHolder> implements Filterable {

    private List<Park> parkList;
    private List<Park> filteredData;
    private String user; // email
    private String username; // username

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ParkAdapter(List<Park> list, String user, String username) {

        parkList = list;
        filteredData = list;
        this.user = user;
        this.username = username;
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
        final Park p = filteredData.get(position);
        holder.cityTV.setText(p.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                db.collection("cities")
                        .whereEqualTo("name", p.getName())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for(QueryDocumentSnapshot doc : task.getResult()) {
                                    Intent i = new Intent(v.getContext(), BookActivity.class);
                                    i.putExtra("user", user);
                                    i.putExtra("city", p.getName());
                                    v.getContext().startActivity(i);

                                }
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public Filter getFilter() {
        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                //Log.d("PARKADAPTER", constraint.toString());

                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();

                final List<Park> list = parkList;

                int count = list.size();
                final ArrayList<Park> nlist = new ArrayList<Park>(count);

                String filterableString;

                for (int i = 0; i < count; i++) {
                    filterableString = list.get(i).getName();
                    if(filterableString.toLowerCase().startsWith(filterString)) {
                        //Log.d("PARKADAPTER", filterableString);
                        nlist.add(list.get(i));
                    }
                }
                results.values = nlist;
                results.count = nlist.size();

                return results;

            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (ArrayList<Park>) results.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView cityTV;

        public MyViewHolder(View view) {
            super(view);
            cityTV = view.findViewById(R.id.cityTV);
        }

    }

}