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
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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


public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private static final String PREFS_NAME = "SmartLockSettings";
    private static final int IS_SIGNUP = 0;

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;

    private String base64Credentials;
    private String token;
    private String email;
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();

        String credentials = email + ":" + password;
        base64Credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        JSONObject regForm = new JSONObject();

        try {
            regForm.put("email", email);
            regForm.put("password", base64Credentials);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Log.d("PACKET", regForm.toString());

        RequestBody body = RequestBody.create(regForm.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(MainActivity.url + "/signup", body);


    }

    private void postRequest(String postUrl, RequestBody postBody) {

        Log.d("POSTURL", postUrl);

        HttpPostAsyncTask okHttpAsync = new HttpPostAsyncTask(postBody);
        okHttpAsync.execute(postUrl);

    }

    private class HttpPostAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpPostAsyncTask(RequestBody postBody) {
            this.postBody = postBody;
            resp = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {

            // DOUBLE-CHECK EMAIL

            Log.d("SIGNUP ACTIVITY", "request done");

            String postUrl = strings[0];
            Log.d("SIGNUP ACTIVITY", postUrl);

            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            final Request request = new Request.Builder()
                    .url(postUrl)
                    .post(postBody)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                resp = response.body().string();
                Log.d("SIGNUP ACTIVITY", resp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            Log.d("RESPONSE", "RESPONSE" + resp);
            try {
                JSONObject json = new JSONObject(resp);
                String responseString = json.getString("response");
                Log.d("RESPONSE", responseString);
                if (responseString.equals("success")) {
                    token = json.getString("conf_token");
                    Log.d(TAG, "TOKEN: " + token);
                    onSignupSuccess();
                } else {
                    Log.d("ERR", responseString);
                    Log.d("ERR", "onResponse failed");
                    onSignupFailed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void onSignupSuccess() {

        _signupButton.setEnabled(true);
        //setResult(RESULT_OK, null);

        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(SignupActivity.this, R.style.MyAlertDialog)).create();
        alertDialog.setTitle("Check");
        alertDialog.setMessage("Check your email and confirm by pressing on the link!");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                .edit()
                                .putString("auth_token", token)
                                .putString("password", password)
                                .apply();
                        Intent i = new Intent(getApplicationContext(), CompleteLoginActivity.class);
                        i.putExtra("fromActivity", IS_SIGNUP);
                        i.putExtra("email", email);
                        i.putExtra("base64credentials", base64Credentials);
                        startActivity(i);
                    }
                });
        alertDialog.show();
    }

    private void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);

    }

    private boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }



}
