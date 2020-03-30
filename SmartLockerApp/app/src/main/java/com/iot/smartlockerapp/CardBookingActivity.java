package com.iot.smartlockerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

public class CardBookingActivity extends AppCompatActivity implements SensorEventListener{

    private Button authenticate;
    private Button leave;
    private Button delete;
    private Button findPark;
    private Button startTrain;
    private Button stopTrain;

    private TextView dateTV;
    private TextView lockNameTV;

    private String username;
    private String user;
    private String gender;

    private int pace;
    private float km;

    private String city;
    private String lockName;
    private String lockHash;
    private String parkName;
    private String date;
    private boolean lockState;

    private final static String TAG = "FPrintAct";

    // Firebase db
    private FirebaseFirestore db;

    // Pedometer
    private SensorManager sensorManager;
    private Sensor accel;
    private int numSteps;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            db = FirebaseFirestore.getInstance();

            setContentView(R.layout.activity_booking_card);

            dateTV = (TextView) findViewById(R.id.idDateTV);
            lockNameTV = (TextView) findViewById(R.id.idLockName);

            username = getIntent().getStringExtra("user");
            user = getIntent().getStringExtra("email");
            gender = getIntent().getStringExtra("gender");

            if(gender.equals("M")) {
                pace = 78;
            }
            else {
                pace = 70;
            }

            Log.d(TAG, user);


            city = getIntent().getStringExtra("city");
            parkName = getIntent().getStringExtra("park");
            lockHash = getIntent().getStringExtra("lockHash");
            lockName = getIntent().getStringExtra("lockName");
            lockState = getIntent().getBooleanExtra("lockState", true);
            date = getIntent().getStringExtra("date");

            Log.d(TAG, "LockName: " + lockName);
            Log.d(TAG, "LockState: " + lockState);

            String lockN = lockName.substring(0, lockName.length()-1);
            String lockID = lockName.substring(lockName.length()-1);

            getSupportActionBar().setTitle(city + " - " + parkName);

            String styledDateText = "<strong> Date: </strong>" + date;
            dateTV.setText(Html.fromHtml(styledDateText, Html.FROM_HTML_MODE_LEGACY));

            String styledLockerText = "<strong> Locker: </strong>" + lockN + " " + lockID;
            lockNameTV.setText(Html.fromHtml(styledLockerText, Html.FROM_HTML_MODE_LEGACY));

            authenticate = (Button) findViewById(R.id.idAuthButt);
            leave = (Button) findViewById(R.id.idLeaveBtn);
            delete = (Button) findViewById(R.id.idDeleteBtn);
            findPark = (Button) findViewById(R.id.idFindParkBtn);
            startTrain = (Button) findViewById(R.id.idStartTrainBtn);
            stopTrain = (Button) findViewById(R.id.idStopTrainBtn);

            // Get an instance of the SensorManager
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

            findPark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), FindParkActivity.class);
                    i.putExtra("city", city);
                    i.putExtra("parkName", parkName);
                    startActivity(i);
                }
            });

            String lockPark = city + parkName + lockName;

            final String bookID = user + " " + city + " " + parkName + " " + date + " " + lockPark.hashCode();
            Log.d(TAG, "BookID: " + bookID);

            DateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

            Date currDate = new Date();
            String dateFormatted = dateFormat.format(currDate);
            Log.d(TAG, dateFormatted);

            Date currFormDate = dateFormat.parse(dateFormatted);
            Date bookDate = dateFormat.parse(date);

            boolean diff = getDifference(currFormDate, bookDate);

            stopTrain.setEnabled(false);

            if(diff) {
                leave.setEnabled(false);
                authenticate.setEnabled(false);
                startTrain.setEnabled(false);
            }

            leave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leaveLocker(bookID, city, parkName, lockHash);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.MyAlertDialog));
                    builder.setTitle("Confirm delete !");
                    builder.setMessage("You are about to delete your booking. Do you really want to proceed ?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteBooking(bookID, city, parkName, lockHash);
                            Toast.makeText(getApplicationContext(), "You've choosen to delete you booking", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            getApplicationContext().startActivity(i);
                            finish();
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "You've changed your mind", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.show();
                }
            });

            Executor executor = Executors.newSingleThreadExecutor();

            FragmentActivity activity = this;

            final BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        // user clicked negative button
                    } else {
                        //TODO: Called when an unrecoverable error has been encountered and the operation is complete.
                    }
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);

                    setLockFull(city, parkName, lockHash);

                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    //TODO: Called when a biometric is valid but not recognized.
                }
            });

            final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Put your finger over the sensor")
                    .setSubtitle("Set the subtitle to display.")
                    .setDescription("Set the description to display")
                    .setNegativeButtonText("Dismiss")
                    .build();

            authenticate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    biometricPrompt.authenticate(promptInfo);
                }
            });

            startTrain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopTrain.setEnabled(true);
                    startTrain.setEnabled(false);
                    numSteps = 0;
                    sensorManager.registerListener(CardBookingActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
                }
            });

            stopTrain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTrain.setEnabled(true);
                    stopTrain.setEnabled(false);
                    sensorManager.unregisterListener(CardBookingActivity.this);
                    km = (float)(numSteps*pace)/(float)100000;
                    Log.d(TAG, Float.toString(km));
                }
            });

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private boolean getDifference(Date leave, Date booking){
        long difference = leave.getTime() - booking.getTime();

        Log.d(TAG, "difference : " + difference);

        if(difference < 0) {
            return true;
        }
        return false;
    }

    private void setLockFull(String city, String parkName, String lockHash){
        Map<String, Object> lock = new HashMap<>();
        if(lockState == true) {
            lock.put("open", false);
            lockState = false;
        }
        else{
            lock.put("open", true);
            lockState = true;
        }

        String cityPark = city+parkName;

        db.collection("cities/"+city.hashCode()+"/parks/"+cityPark.hashCode()+"/lockers").document(lockHash)
                .set(lock, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void leaveLocker(String bookID, String city, String parkName, String lockHash){
        Map<String, Object> lock = new HashMap<>();
        if(lockState == true) {
            lock.put("open", false);
        }
        lock.put("available", true);
        lock.put("user", "");
        lockState = false;

        String cityPark = city + parkName;

        db.collection("cities/"+city.hashCode()+"/parks/"+cityPark.hashCode()+"/lockers").document(lockHash)
                .set(lock, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        DateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        Date currentTime = Calendar.getInstance().getTime();
        String leaveTime = dateFormat.format(currentTime);
        Map<String, Object> booking = new HashMap<>();
        booking.put("active", false);
        booking.put("leave", leaveTime);
        booking.put("km", Float.toString(km));

        db.collection("bookings").document(Integer.toString(bookID.hashCode()))
                .set(booking, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("email", user);
        i.putExtra("user", username);
        finish();
    }

    private void deleteBooking(String bookID, String parkName, String city, String lockHash){

        Map<String, Object> lock = new HashMap<>();
        if(lockState == true) {
            lock.put("open", false);
        }
        lock.put("available", true);
        lock.put("user", "");
        lockState = false;

        String cityPark = city+parkName;

        db.collection("cities/"+city.hashCode()+"/parks/"+cityPark.hashCode()+"/lockers").document(lockHash)
                .set(lock, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        db.collection("bookings").document(Integer.toString(bookID.hashCode()))
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            numSteps++;
            Toast.makeText(this, TEXT_NUM_STEPS + numSteps, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
