package com.iot.smartlockerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BookedNearYouActivity extends AppCompatActivity {

    private SwitchDateTimeDialogFragment dateTimeFragment;

    private Button bookBtn;
    private TextView calView;

    private RecyclerView friendsRV;
    private BookedNearYouAdapter bookedNearAdapter;
    private List<Booking> nearYou;

    private String parkName;
    private String user;
    private String city;

    // Firebase db
    private FirebaseFirestore db;

    private final String TAG = "NearYou";

    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nearyou);

        getSupportActionBar().setTitle("Choose Hour");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        parkName = getIntent().getStringExtra("parkName");
        user = getIntent().getStringExtra("user");
        city = getIntent().getStringExtra("city");

        friendsRV = (RecyclerView) findViewById(R.id.idFriendsRV);
        friendsRV.setLayoutManager(new LinearLayoutManager(this));

        // DB
        db = FirebaseFirestore.getInstance();

        // Near bookings

        nearYou = (List<Booking>) getIntent().getSerializableExtra("nearYou");

        if(nearYou.isEmpty()) {
            TextView noFriends = findViewById(R.id.noFriendsTV);
            noFriends.setText("No friends near you :(");
        }
        else {
            TextView nearYouLbl = findViewById(R.id.nearYouTV);
            nearYouLbl.setText("Friends near you:");
        }

        Log.d(TAG, "Size near you: " + nearYou.size());

        bookedNearAdapter = new BookedNearYouAdapter(nearYou, user);
        friendsRV.setAdapter(bookedNearAdapter);

        // CALENDAR WIDGET

        calView = (TextView) findViewById(R.id.idCalendarView);

        // Construct SwitchDateTimePicker
        dateTimeFragment = (SwitchDateTimeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
        if(dateTimeFragment == null) {
            dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    getString(R.string.label_datetime_dialog),
                    getString(R.string.ok),
                    getString(R.string.cancel),
                    getString(R.string.clean) // Optional
            );
        }

        // Optionally define a timezone
        dateTimeFragment.setTimeZone(TimeZone.getDefault());

        // Init format
        // Assign unmodifiable values

        Date minDate = new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime();
        Date maxDate = new GregorianCalendar(2200, Calendar.DECEMBER, 31).getTime();

        DateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

        final Date currDate = new Date();

        dateTimeFragment.set24HoursMode(true);
        dateTimeFragment.setHighlightAMPMSelection(false);
        dateTimeFragment.setMinimumDateTime(minDate);
        dateTimeFragment.setMaximumDateTime(maxDate);

        dateTimeFragment.setAlertStyle(R.style.MyAlertCalendar);

        // Define new day and month format
        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        // Set listener for date
        // Or use dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
        dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                Intent i = new Intent(getApplicationContext(), LockerActivity.class);  // Go to lockerActivity
                i.putExtra("parkName", parkName);
                Log.d(TAG, user);
                i.putExtra("user", user);
                i.putExtra("city", city);
                DateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
                String strDate = dateFormat.format(date);

                if(getDifference(date, currDate)) {
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(BookedNearYouActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Book locker");
                    alertDialog.setMessage("Invalid date!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            });
                    alertDialog.show();
                    return;
                }

                i.putExtra("date", strDate);
                startActivity(i);
                finish();
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Do nothing
            }

            @Override
            public void onNeutralButtonClick(Date date) {
                // Optional if neutral button does'nt exists
                calView.setText("");
            }
        });

        bookBtn = (Button) findViewById(R.id.idBookBtn);

        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Re-init each time
                dateTimeFragment.startAtCalendarView();
                dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
            }
        });

    }

    private boolean getDifference(Date leave, Date booking){
        long difference = leave.getTime() - booking.getTime();

        Log.d(TAG, "difference : " + difference);

        if(difference < 0) {
            return true;
        }
        return false;
    }

}
