package com.iot.smartlockerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ChangeEmailActivity extends AppCompatActivity {

    @BindView(R.id.input_new_email) EditText _textEmail;
    @BindView(R.id.input_new_email_conf) EditText _textConfEmail;
    @BindView(R.id.btn_change_email) Button _btnChangeEmail;

    String email;
    String conf_email;

    private static final String PREFS_NAME = "SmartLockSettings";
    private static final String TAG = "CHANGE_EMAIL";

    // Firebase db
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_email);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Change Email");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        _btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

                email = _textEmail.getText().toString();
                conf_email = _textConfEmail.getText().toString();

                String password = pref.getString("password", null);

                String credentials = email + ":" + password;
                String base64Credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

                if(!validate()) {
                    return;
                }

                JSONObject regForm = new JSONObject();

                try {
                    regForm.put("new_email", email);
                    regForm.put("new_pass", base64Credentials);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(regForm.toString(), MediaType.parse("application/json; charset=utf-8"));
                postRequest(MainActivity.url+"/changemail", body);

            }
        });

    }

    private boolean validate() {
        boolean valid = true;

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _textEmail.setError("enter a valid email address");
            valid = false;
        } else {
            _textEmail.setError(null);
        }

        if(conf_email.isEmpty() || !conf_email.equals(email)) {
            _textConfEmail.setError("emails don't match");
            valid = false;
        } else {
            _textConfEmail.setError(null);
        }

        return valid;
    }

    private void postRequest(String postUrl, RequestBody postBody) {
        Log.d(TAG, postUrl);

        HttpPostChangeEmailAsyncTask okHttpAsync = new HttpPostChangeEmailAsyncTask(postBody);
        okHttpAsync.execute(postUrl);

    }

    private class HttpPostChangeEmailAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpPostChangeEmailAsyncTask(RequestBody postBody) {
            this.postBody = postBody;
            resp = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {

            SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            String token = pref.getString("auth_token", null);

            Log.d(TAG, "request done");
            Log.d(TAG, "TOKEN: " + token);

            String postUrl = strings[0];
            Log.d(TAG, postUrl);

            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(postUrl)
                    .post(postBody)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", token)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                resp = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            Log.d(TAG, "RESPONSE" + resp);
            try {
                JSONObject json = new JSONObject(resp);
                String responseString = json.getString("response");
                Log.d(TAG, "Response from the server: " + responseString);
                if(responseString.equals("success")) {
                    Log.d(TAG, "success");

                    // HANDLE FIREBASE
                    changeFirebaseEmail();

                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putString("email", email)
                            .apply();

                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ChangeEmailActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Change Email");
                    alertDialog.setMessage("The email was successfully updated!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(i);
                                }
                            });
                    alertDialog.show();

                }
                else {
                    Log.d(TAG, "failure");
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ChangeEmailActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Change Email");
                    alertDialog.setMessage("The email was not updated!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            });
                    alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeFirebaseEmail() {
        // CHANGE BOOKINGS

        db = FirebaseFirestore.getInstance();

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String old_email = pref.getString("email", null);

        Query bookings = db.collection("bookings")
                .whereEqualTo("user", old_email);

        bookings.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {

                        // DELETE CURRENT DOCUMENT
                        boolean active = (boolean) document.get("active");
                        String city = (String) document.get("city");
                        String date = (String) document.get("date");
                        String leave = "";
                        if(document.get("leave") != null) {
                            leave = (String) document.get("leave");
                        }
                        String lockHash = (String) document.get("lockHash");
                        String park = (String) document.get("park");

                        db.collection("bookings").document(document.getId()).delete();

                        // CREATE NEW DOCUMENT WITH NEW ID

                        Map<String, Object> booking = new HashMap<>();
                        booking.put("user", email);
                        booking.put("active", active);
                        booking.put("city", city);
                        booking.put("park", park);
                        booking.put("date", date);
                        if(leave != "") {
                            booking.put("leave", leave);
                        }
                        booking.put("lockHash", lockHash);

                        String hc = email + " " + city + " " + park + " " + date + " " + lockHash;
                        int bookHash = hc.hashCode();

                        db.collection("bookings").document(Integer.toString(bookHash))
                                .set(booking)
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

                        // CHANGE EMAIL INTO LOCKER DOCUMENT
                        Map<String, Object> lock = new HashMap<>();
                        lock.put("user", email);

                        String cityPark = city + park;

                        db.collection("cities/"+city.hashCode()+"/parks/"+cityPark.hashCode()+"/lockers")
                                .document(lockHash)
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
                }
            }
        });

    }



}
