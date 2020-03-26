package com.iot.smartlockerapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ChangeEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_email);

        getSupportActionBar().setTitle("Change Email");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


    }
}
