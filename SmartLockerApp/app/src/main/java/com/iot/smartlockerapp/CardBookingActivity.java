package com.iot.smartlockerapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CardBookingActivity extends AppCompatActivity implements SensorEventListener{

    private Button authenticate;
    private Button leave;
    private Button delete;
    private Button findPark;
    private Button startTrain;
    private Button stopTrain;

    private ImageView weatherIV;
    private TextView weatherTV;
    private TextView dateTV;
    private TextView lockNameTV;

    private String username;
    private String user;
    private String gender;

    private int pace;
    private float km;

    private float ratingValue = -1;

    private String city;
    private String lockName;
    private String lockHash;
    private String parkName;
    private String date;
    private Date bookDate;
    private boolean lockState;

    private final static String openAPI = "06a255a9b7dbe88bf33ec3e5ddb34c18";
    private final static String WeatherURL = "http://api.openweathermap.org/data/2.5/forecast?q=";
    private final static String WeatherImageUrl = "http://openweathermap.org/img/wn/";

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
            weatherIV = (ImageView) findViewById(R.id.idWeatherIcon);
            weatherTV = (TextView) findViewById(R.id.idWeatherLbl);

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

            Log.d(TAG, sensorManager.getSensorList(Sensor.TYPE_STEP_DETECTOR).toString());

            // Find weather
            WeatherAPI weatherTask = new WeatherAPI(new WeatherAPI.WeatherResponse() {
                @Override
                public void processFinish(String output) {
                    //Log.d(TAG, output);
                    String[] forecast = processWeather(output);
                    if (!forecast[0].equals("No available forecasts")) {
                        String imageUri = WeatherImageUrl + forecast[1] + "@2x.png";
                        Picasso.get().load(imageUri).into(weatherIV);
                    }
                    weatherTV.setText(forecast[0]);
                }
            });

            weatherTask.execute(WeatherURL+city+",it&appid="+openAPI);

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
            bookDate = dateFormat.parse(date);

            long diff = getDifference(currFormDate, bookDate);

            stopTrain.setEnabled(false);

            if(diff < 0) {
                leave.setEnabled(false);
                authenticate.setEnabled(false);
                startTrain.setEnabled(false);
                stopTrain.setEnabled(false);
                delete.setEnabled(true);
                findPark.setEnabled(true);
            }
            else {
                leave.setEnabled(true);
                authenticate.setEnabled(true);
                startTrain.setEnabled(true);
                stopTrain.setEnabled(false);
                delete.setEnabled(false);
                findPark.setEnabled(true);
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
                    leave.setEnabled(false);
                    numSteps = 0;
                    sensorManager.registerListener(CardBookingActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
                }
            });

            stopTrain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTrain.setEnabled(true);
                    stopTrain.setEnabled(false);
                    leave.setEnabled(true);
                    sensorManager.unregisterListener(CardBookingActivity.this);
                    km = (float)(numSteps*pace)/(float)100000;
                    Log.d(TAG, Float.toString(km));
                }
            });

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private long getDifference(Date leave, Date booking){
        long difference = leave.getTime() - booking.getTime();

        //Log.d(TAG, "difference : " + difference);

        return difference;

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

    private void leaveLocker(final String bookID, final String city, final String parkName, final String lockHash){

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(CardBookingActivity.this, R.style.MyAlertDialog));
        View layout = null;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.rating, null);
        final RatingBar ratingBar = layout.findViewById(R.id.ratingBar);
        builder.setTitle("Rate your experience!");
        builder.setMessage("Thank you for rating your experience, it will help us to improve the system!");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ratingValue = ratingBar.getRating();


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
                booking.put("rating", Float.toString(ratingValue));
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

                JSONObject ratingForm = new JSONObject();

                try {
                    ratingForm.put("cityHash", city.hashCode());
                    ratingForm.put("parkHash", cityPark.hashCode());
                    ratingForm.put("rating", ratingValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(ratingForm.toString(), MediaType.parse("application/json; charset=utf-8"));
                postRatingRequest(MainActivity.url+"/updaterating", body);

                finish();
            }
        });
        builder.setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.setView(layout);
        builder.show();

    }

    private void postRatingRequest(String postUrl, RequestBody postBody) {
        HttpRatingPostAsyncTask okHttpAsync = new HttpRatingPostAsyncTask(postBody);
        okHttpAsync.execute(postUrl);
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

    private class HttpRatingPostAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpRatingPostAsyncTask(RequestBody postBody) {
            this.postBody = postBody;
            resp = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {
            Log.d(TAG, "RATING request done");
            String postUrl = strings[0];

            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(postUrl)
                    .post(postBody)
                    .header("Content-Type", "application/json")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                resp = response.body().string();
                Log.d(TAG, resp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private String[] processWeather(String forecast) {
        String[] forecastWithImage = new String[2];
        try {
            JSONObject forecastJson = new JSONObject(forecast);
            String res = forecastJson.getString("cod");
            if(res.equals("200")) {
                JSONArray forecastList = forecastJson.getJSONArray("list");
                forecastWithImage[0] = "No available forecasts";
                forecastWithImage[1] = "";
                long bestDiff = 3;
                for(int i = 0; i < forecastList.length(); i++) {
                    JSONObject el = forecastList.getJSONObject(i);

                    String forecastDate = el.getString("dt_txt");

                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date forecastDateForm = dateFormat.parse(forecastDate);

                    long forecastDiff = getDifference(forecastDateForm, bookDate);
                    long diffHours = Math.abs(forecastDiff / (60 * 60 * 1000) % 24);
                    long diffDays = forecastDiff / (24 * 60 * 60 * 1000);

                    if(diffDays == 0) {
                        if(diffHours < bestDiff) {
                            Log.d(TAG, el.getString("dt_txt") + " " + diffDays + " " + diffHours);
                            JSONArray weatherArray = el.getJSONArray("weather");
                            Log.d(TAG, weatherArray.getJSONObject(0).getString("main"));
                            forecastWithImage[0] = weatherArray.getJSONObject(0).getString("main");
                            forecastWithImage[1] = weatherArray.getJSONObject(0).getString("icon");
                            bestDiff = diffHours;
                        }
                    }
                }
                return forecastWithImage;
            }
            else {
                Log.d(TAG, "ELSE BRANCH");
                forecastWithImage[0] = "No available forecasts";
                forecastWithImage[1] = "";
                return forecastWithImage;
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        return forecastWithImage;
    }

}
