package com.iot.smartlockerapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    //@BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;

    private String base64Credentials;

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
                //overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

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
                    //Log.d("RESPONSE", "AOOOOOOOOOOETER");
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
        setResult(RESULT_OK, null);

        Intent i = new Intent(this, CompleteLoginActivity.class);
        i.putExtra("token", base64Credentials);
        i.putExtra("email", _emailText.getText().toString());

        startActivity(i);
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
