package com.iot.smartlockerapp;

import android.content.DialogInterface;
import android.content.Intent;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SmartLockSettings";
    private static final String TAG = "ResetPasswordActivity";
    private static final int IS_RESET = 3;

    @BindView(R.id.input_temp_pwd) EditText _tempPwd;
    @BindView(R.id.input_new_pwd) EditText _newPwd;
    @BindView(R.id.input_conf_new_pwd) EditText _confNewPwd;
    @BindView(R.id.btn_reset) Button _resetBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_forgot_password);

        ButterKnife.bind(this);

        _resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }

    private void reset() {

        if(!validate()) {
            return;
        }

        _resetBtn.setEnabled(true);

        String email = getIntent().getStringExtra("email");

        Log.d(TAG, email);

        String credentials = email + ":" + _newPwd.getText().toString();
        String newPwd = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        JSONObject resForm = new JSONObject();

        try {
            resForm.put("temp_pass", _tempPwd.getText().toString());
            resForm.put("new_pass", newPwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(resForm.toString(), MediaType.get("application/json; charset=utf-8"));
        postRequest(MainActivity.url+"/recoverypass", body);

    }

    private void postRequest(String postUrl, RequestBody postBody) {

        HttpNewPassPostAsyncTask okHttpAsync = new HttpNewPassPostAsyncTask(postBody);
        okHttpAsync.execute(postUrl);

    }

    private class HttpNewPassPostAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpNewPassPostAsyncTask(RequestBody postBody) {
            this.postBody = postBody;
            resp = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {

            // DOUBLE-CHECK EMAIL

            Log.d(TAG, "LOGIN request done");

            String postUrl = strings[0];
            Log.d(TAG, postUrl);

            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(postUrl)
                    .post(postBody)
                    .header("Accept", "application/json")
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

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            Log.d(TAG, "RESPONSE" + resp);
            try {
                JSONObject json = new JSONObject(resp);
                String responseString = json.getString("response");
                Log.d(TAG, responseString);
                if (responseString.equals("success")) {
                    onResetSuccess();
                } else {
                    Log.d(TAG + " ERR", responseString);
                    Log.d(TAG + " ERR", "onResponse failed");
                    onResetFailed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void onResetSuccess() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt("fromActivity", IS_RESET)
                .apply();

        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ForgotPasswordActivity.this, R.style.MyAlertDialog)).create();
        alertDialog.setTitle("Reset Password");
        alertDialog.setMessage("Your password has been reset");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
        alertDialog.show();
    }

    private void onResetFailed() {
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ForgotPasswordActivity.this, R.style.MyAlertDialog)).create();
        alertDialog.setTitle("Reset Password");
        alertDialog.setMessage("An error has occured");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private boolean validate() {

        boolean valid = true;

        String tempPwd = _tempPwd.getText().toString();
        String newPwd = _newPwd.getText().toString();
        String confNewPwd = _confNewPwd.getText().toString();

        if(tempPwd.isEmpty()) {
            _tempPwd.setError("insert temporary password");
            valid = false;
        } else {
            _tempPwd.setError(null);
        }

        if(newPwd.isEmpty() || newPwd.length() < 4 || newPwd.length() > 10) {
            _newPwd.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _newPwd.setError(null);
        }

        if(confNewPwd.isEmpty() || confNewPwd.length() < 4 || confNewPwd.length() > 10 || !(confNewPwd.equals(newPwd))) {
            _confNewPwd.setError("password don't match");
            valid = false;
        } else {
            _confNewPwd.setError(null);
        }

        return valid;
    }

}
