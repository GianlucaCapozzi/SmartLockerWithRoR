package com.iot.smartlockerapp;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class AccountFragment extends Fragment {

    private static final String PREFS_NAME = "SmartLockSettings";

    private CircleImageView profilePict;
    private TextView usernameTV;

    public AccountFragment() {
    }

    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Account");

        SharedPreferences pref = this.getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = pref.getString("user", null);
        String email = pref.getString("email", null);
        String image = pref.getString("image", null);

        if(!image.equals("R.drawable.com_facebook_profile_picture_blank_portrait")) {
            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            profilePict = v.findViewById(R.id.profile_image);
            profilePict.setImageBitmap(decodedByte);
        }

        usernameTV = v.findViewById(R.id.usernameTV);
        usernameTV.setText(name);

        return v;

    }
}
