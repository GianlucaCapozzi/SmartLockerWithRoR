package com.iot.smartlockerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangeUsernameActivity extends AppCompatActivity {

    private final static String TAG = "CHANGE_USERNAME";

    private static final String PREFS_NAME = "SmartLockSettings";

    private String username;

    private String name;
    private String surname;

    @BindView(R.id.input_new_name) EditText _newName;
    @BindView(R.id.input_new_surname) EditText _newSurname;
    @BindView(R.id.btn_change_username) Button _changeUserBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_username);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Change Username");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        _changeUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = _newName.getText().toString();
                surname = _newSurname.getText().toString();

                if (!validate()) {
                    return;
                }
                username = name + " " + surname;

                JSONObject regForm = new JSONObject();

                try {
                    regForm.put("name", name);
                    regForm.put("surname", surname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(regForm.toString(), MediaType.parse("application/json; charset=utf-8"));
                postRequest(MainActivity.url+"/confprofile", body);

            }
        });

    }

    private void postRequest(String postUrl, RequestBody postBody) {

        Log.d(TAG, postUrl);

        HttpPostChangeUserAsyncTask okHttpAsync = new HttpPostChangeUserAsyncTask(postBody);
        okHttpAsync.execute(postUrl);

    }

    private class HttpPostChangeUserAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpPostChangeUserAsyncTask(RequestBody postBody) {
            this.postBody = postBody;
            resp = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {

            SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            String token = pref.getString("auth_token", null);

            Log.d(TAG, "request done");

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
                String loginResponseString = json.getString("response");
                Log.d(TAG, "Response from the server: " + loginResponseString);
                if(loginResponseString.equals("success")) {
                    Log.d(TAG, "success");

                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putString("user", username)
                        .apply();

                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ChangeUsernameActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Change Username");
                    alertDialog.setMessage("The username was successfully updated!");
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
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ChangeUsernameActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Change Username");
                    alertDialog.setMessage("The username was not updated!");
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

    private boolean validate() {
        boolean valid = true;

        if (name.isEmpty() || name.length() < 3) {
            _newName.setError("at least 3 characters");
            valid = false;
        } else {
            _newName.setError(null);
        }

        if (surname.isEmpty() || surname.length() < 3) {
            _newSurname.setError("at least 3 characters");
            valid = false;
        } else {
            _newSurname.setError(null);
        }

        return valid;
    }

}
