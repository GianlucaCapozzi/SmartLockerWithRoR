package com.iot.smartlockerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private String park, dateS, locker, leaveTime, km;

    private String durationS;

    private TextView parkTV;
    private TextView dateTV;
    private TextView lockerTV;
    private TextView durationTV;
    private TextView kmTV;

    private final static String TAG = "profAct";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_card);

        getSupportActionBar().setTitle("Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        try {
            park = getIntent().getStringExtra("park");
            dateS = getIntent().getStringExtra("date");
            locker = getIntent().getStringExtra("locker");
            leaveTime = getIntent().getStringExtra("leaveTime");
            km = getIntent().getStringExtra("km");

            parkTV = (TextView) findViewById(R.id.idParkTxt);
            String styledParkText = "<strong> Park: </strong>" + park;
            parkTV.setText(Html.fromHtml(styledParkText, Html.FROM_HTML_MODE_LEGACY));

            dateTV = (TextView) findViewById(R.id.idDateTxt);
            String styledDateText = "<strong> Date: </strong>" + dateS;
            dateTV.setText(Html.fromHtml(styledDateText, Html.FROM_HTML_MODE_LEGACY));

            lockerTV = (TextView) findViewById(R.id.idLockerTxt);
            String styledLockerText = "<strong> Locker: </strong>" + locker;
            lockerTV.setText(Html.fromHtml(styledLockerText, Html.FROM_HTML_MODE_LEGACY));

            DateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

            Date leaveDate = dateFormat.parse(leaveTime);
            Date bookingDate = dateFormat.parse(dateS);
            durationS = getDifference(leaveDate, bookingDate);

            durationTV = (TextView) findViewById(R.id.idDurationTxt);
            String styledDurationText = "<strong> Duration: </strong>" + durationS;
            durationTV.setText(Html.fromHtml(styledDurationText, Html.FROM_HTML_MODE_LEGACY));

            kmTV = (TextView) findViewById(R.id.idKmTxt);
            String styledKmText = "<strong> KM: </strong>" + km;
            kmTV.setText(Html.fromHtml(styledKmText, Html.FROM_HTML_MODE_LEGACY));

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private String getDifference(Date leave, Date booking){
        long difference = leave.getTime() - booking.getTime();

        Log.d(TAG, "startDate : " + leave);
        Log.d(TAG, "endDate : "+ booking);
        Log.d(TAG, "different : " + difference);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = difference / daysInMilli;
        difference = difference % daysInMilli;

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long elapsedMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        long elapsedSeconds = difference / secondsInMilli;

        String diff = elapsedHours + "hours, " + elapsedMinutes + "minutes";
        return diff;
    }


}
