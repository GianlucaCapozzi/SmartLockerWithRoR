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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonSerializer;

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

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SmartLockSettings";
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private static final int IS_LOG = 1;

    private String user;
    private String image;
    private boolean value;

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.rememberBox) CheckBox _checkRem;
    @BindView(R.id.link_signup) TextView _signupLink;
    @BindView(R.id.link_forgot) TextView _forgotLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = pref.getString("user", null);
        String email = pref.getString("email", null);


        if(name != null && email != null) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }


        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                //overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        _forgotLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!validateForForgot()) {
                    onResetFailed();
                    return;
                }
                String email = _emailText.getText().toString();

                JSONObject forgotForm = new JSONObject();
                try {
                    forgotForm.put("email", email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(forgotForm.toString(), MediaType.parse("application/json; charset=utf-8"));
                postResetRequest(MainActivity.url+"/forgetpass", body);
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        value = _checkRem.isChecked();


        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        String credentials = email + ":" + password;
        String base64Credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        JSONObject loginForm = new JSONObject();
        try {
            loginForm.put("email", email);
            loginForm.put("password", base64Credentials);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(loginForm.toString(), MediaType.parse("application/json; charset=utf-8"));

        postLoginRequest(MainActivity.url+"/login", body);

    }

    private void postResetRequest(String postUrl, RequestBody postBody) {

        HttpResetPostAsyncTask okHttpAsync = new HttpResetPostAsyncTask(postBody);
        okHttpAsync.execute(postUrl);

    }

    private void postLoginRequest(String postUrl, RequestBody postBody) {

        HttpLoginPostAsyncTask okHttpAsync = new HttpLoginPostAsyncTask(postBody);
        okHttpAsync.execute(postUrl);

    }

    private class HttpLoginPostAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpLoginPostAsyncTask(RequestBody postBody) {
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
            Log.d("RESPONSE", "RESPONSE" + resp);
            try {
                JSONObject json = new JSONObject(resp);
                String responseString = json.getString("response");
                Log.d("RESPONSE", responseString);
                if (responseString.equals("success")) {
                    //Log.d("RESPONSE", "AOOOOOOOOOOETER");
                    user = json.getString("name") + " " + json.getString("surname");
                    image = json.getString("photo");
                    onLoginSuccess();
                } else {
                    Log.d("ERR", responseString);
                    Log.d("ERR", "onResponse failed");
                    onLoginFailed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class HttpResetPostAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpResetPostAsyncTask(RequestBody postBody) {
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

            Request request = new Request.Builder()
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
                Log.d(TAG + " RESET", responseString);
                if (responseString.equals("success")) {
                    Log.d(TAG + " RESET", responseString);
                    onResetSuccess();
                } else {
                    Log.d(TAG + " RESET", responseString);
                    Log.d(TAG + " RESET", "onResponse failed");
                    onResetFailed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back
        moveTaskToBack(true);
    }

    private void onLoginSuccess() {

        Intent i = new Intent(this, MainActivity.class);



        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("user", user)
                .putString("email", _emailText.getText().toString())
                .putString("image", image)
                .commit();

        i.putExtra("fromActivity", IS_LOG);
        i.putExtra("remember", value);
        startActivity(i);

        finish();
    }

    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    private void onResetSuccess() {
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.MyAlertDialog)).create();
        alertDialog.setTitle("Reset Password");
        alertDialog.setMessage("Check your email for temporary password");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent i = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                        startActivity(i);
                    }
                });
        alertDialog.show();
    }

    private void onResetFailed(){
        Toast.makeText(getBaseContext(), "Reset failed", Toast.LENGTH_LONG).show();
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.MyAlertDialog)).create();
        alertDialog.setTitle("Reset Password");
        alertDialog.setMessage("An error has occurred");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private boolean validateForForgot() {
        boolean valid = true;

        String email = _emailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        return valid;

    }

    private boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

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

        return valid;
    }

}
