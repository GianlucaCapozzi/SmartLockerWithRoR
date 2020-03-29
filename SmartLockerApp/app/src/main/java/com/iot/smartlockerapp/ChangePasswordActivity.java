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

public class ChangePasswordActivity extends AppCompatActivity {

    @BindView(R.id.input_old_pwd) EditText _textOldPass;
    @BindView(R.id.input_new_change_pwd) EditText _textNewPass;
    @BindView(R.id.input_conf_new_change_pwd) EditText _textConfNewPass;
    @BindView(R.id.btn_change_password) Button _changePass;

    private static final String PREFS_NAME = "SmartLockSettings";
    private static final String TAG = "CHANGE_PASSWORD";

    private String oldPass;
    private String newPass;
    private String confNewPass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        _changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

                oldPass = _textOldPass.getText().toString();
                newPass = _textNewPass.getText().toString();
                confNewPass = _textConfNewPass.getText().toString();

                if(!validate()) {
                    return;
                }

                String email = pref.getString("email", null);

                String oldCredentials = email + ":" + oldPass;
                String oldBase64Credentials = Base64.encodeToString(oldCredentials.getBytes(), Base64.NO_WRAP);

                String newCredentials = email + ":" + newPass;
                String newBase64Credentials = Base64.encodeToString(newCredentials.getBytes(), Base64.NO_WRAP);

                JSONObject regForm = new JSONObject();

                try {
                    regForm.put("old_pass", oldBase64Credentials);
                    regForm.put("new_pass", newBase64Credentials);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(regForm.toString(), MediaType.parse("application/json; charset=utf-8"));
                postRequest(MainActivity.url+"/changepass", body);

            }
        });

    }

    private void postRequest(String postUrl, RequestBody postBody) {
        Log.d(TAG, postUrl);

        HttpPostChangePassAsyncTask okHttpAsync = new HttpPostChangePassAsyncTask(postBody);
        okHttpAsync.execute(postUrl);

    }

    private class HttpPostChangePassAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpPostChangePassAsyncTask(RequestBody postBody) {
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

                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putString("password", newPass)
                            .apply();

                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ChangePasswordActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Change Password");
                    alertDialog.setMessage("The password was successfully updated!");
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
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ChangePasswordActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Change Password");
                    alertDialog.setMessage("The password was not updated!");
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

        if (oldPass.isEmpty() || oldPass.length() < 4 || oldPass.length() > 10) {
            _textOldPass.setError("enter a valid email address");
            valid = false;
        } else {
            _textOldPass.setError(null);
        }

        if (newPass.isEmpty() || newPass.length() < 4 || newPass.length() > 10) {
            _textNewPass.setError("enter a valid email address");
            valid = false;
        } else {
            _textNewPass.setError(null);
        }

        if (confNewPass.isEmpty() || confNewPass.length() < 4 || confNewPass.length() > 10 || !(confNewPass.equals(newPass))) {
            _textConfNewPass.setError("Password Do not match");
            valid = false;
        } else {
            _textConfNewPass.setError(null);
        }

        return valid;
    }

}
