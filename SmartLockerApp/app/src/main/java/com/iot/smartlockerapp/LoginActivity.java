package com.iot.smartlockerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    private String token;
    private String gender;
    private String base64Credentials;

    SharedPreferences pref;

    private boolean value;

    // Facebook login
    private CallbackManager callbackManager;
    private String fb_email;
    private String fb_name;
    private AccessToken fb_accessToken;

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.rememberBox) CheckBox _checkRem;
    @BindView(R.id.link_signup) TextView _signupLink;
    @BindView(R.id.link_forgot) TextView _forgotLink;
    @BindView(R.id.fb_login_button) LoginButton fbLogButton;


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

        pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        int fromActivity = pref.getInt("fromActivity", 0);
        boolean rem_me;

        if(fromActivity == 1) {
            rem_me = pref.getBoolean("remember", true);
            if (rem_me == false) {
                getSharedPreferences(PREFS_NAME, 0).edit().remove("user").apply();
                getSharedPreferences(PREFS_NAME, 0).edit().remove("email").apply();
                getSharedPreferences(PREFS_NAME, 0).edit().remove("image").apply();
                getSharedPreferences(PREFS_NAME, 0).edit().remove("gender").apply();
                getSharedPreferences(PREFS_NAME, 0).edit().remove("password").apply();
            }
            else {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if(accessToken != null && !accessToken.isExpired()) {
                    Log.d(TAG, "In FACEBOOK LOGIN");
                    useLoginInformation(accessToken);
                }
                else if(accessToken.isExpired()) {
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("LOGIN INFORMATION");
                    alertDialog.setMessage("Session expired, you must login again!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if(accessToken == null) {
                    String get_email = pref.getString("email", null);
                    _emailText.setText(get_email);
                    _emailText.setFocusable(false);
                    _passwordText.setText("EXAMPLE PASSWORD");
                    _passwordText.setFocusable(false);
                    login();
                }
            }
            getSharedPreferences(PREFS_NAME, 0).edit().remove("auth_token").apply();
        }

        else if(fromActivity == 2 || fromActivity == 3 || fromActivity == 4) {
            getSharedPreferences(PREFS_NAME, 0).edit().remove("user").apply();
            getSharedPreferences(PREFS_NAME, 0).edit().remove("email").apply();
            getSharedPreferences(PREFS_NAME, 0).edit().remove("image").apply();
            getSharedPreferences(PREFS_NAME, 0).edit().remove("gender").apply();
            getSharedPreferences(PREFS_NAME, 0).edit().remove("auth_token").apply();
            getSharedPreferences(PREFS_NAME, 0).edit().remove("password").apply();
        }

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });



        fbLogButton.setPermissions(Arrays.asList("email", "public_profile"));

        callbackManager = CallbackManager.Factory.create();

        // Facebook callback registration
        fbLogButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken access_token = loginResult.getAccessToken();
                useLoginInformation(access_token);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, error.getCause().toString());
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void login() {
        Log.d(TAG, "Login");

        _loginButton.setEnabled(false);

        value = _checkRem.isChecked();

        String get_email = pref.getString("email", null);
        String get_psw = pref.getString("password", null);

        String email;
        String password;

        if(get_email != null && get_psw != null) {
            email = get_email;
            String credentials = get_email + ":" + get_psw;
            base64Credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        }

        else {
            if (!validate()) {
                return;
            }
            email = _emailText.getText().toString();
            password = _passwordText.getText().toString();

            Log.d(TAG, email + " " + password);

            String credentials = email + ":" + password;
            base64Credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        }

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

            Log.d(TAG, "LOGIN request done");

            String postUrl = strings[0];
            Log.d(TAG, postUrl);

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
                    user = json.getString("name") + " " + json.getString("surname");
                    image = json.getString("photo");
                    token = json.getString("auth_token");
                    gender = json.getString("gender");
                    Log.d(TAG, "TOKEN: " + token);
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

            Log.d(TAG, "RESET request done");

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
            Log.d(TAG + " RESET", "RESPONSE" + resp);
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
    }

    private void onLoginSuccess() {

        String name = pref.getString("user", null);
        String email = pref.getString("email", null);


        if(name != null && email != null) {
            // Remember me is true
            Intent i = new Intent(this, MainActivity.class);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString("image", image)
                    .putString("auth_token", token)
                    .apply();
            startActivity(i);
        }

        else {
            // Remember me is false
            Intent i = new Intent(this, MainActivity.class);

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("user", user)
                .putString("email", _emailText.getText().toString())
                .putString("image", image)
                .putString("gender", gender)
                .putString("auth_token", token)
                .putInt("fromActivity", IS_LOG)
                .putBoolean("remember", value)
                .putString("password", _passwordText.getText().toString())
                .apply();

            startActivity(i);
        }

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
                        i.putExtra("email", _emailText.getText().toString());
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

    private void useLoginInformation(final AccessToken accessToken) {
        fb_accessToken = accessToken;
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(TAG, object.toString());
                        try {
                            fb_email = object.getString("email");
                            fb_name = object.getString("name");
                            //Log.d(TAG, "token: " + accessToken.toString());
                            Log.d(TAG, "email: " + fb_email);
                            Log.d(TAG, "name: " + fb_name);
                            String userID = object.getString("id");
                            final JSONObject data = response.getJSONObject();
                            if(data.has("picture")) {
                                String url = "https://graph.facebook.com/" + userID + "/picture?type=large";
                                DownloadProfilePicture downProPict = new DownloadProfilePicture();
                                downProPict.execute(url);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private class DownloadProfilePicture extends AsyncTask<String, Void, byte[]> {

        String profile_img;

        public DownloadProfilePicture() {
            profile_img = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {
            try {
                String postUrl = strings[0];
                URL profilePicUrl = new URL(postUrl);
                Bitmap profilePic = BitmapFactory.decodeStream(profilePicUrl.openConnection().getInputStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                profilePic.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();
                profile_img = Base64.encodeToString(b, Base64.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            image = profile_img;
            Log.d(TAG, "FACEBOOK SIGNUP");
            JSONObject fbloginForm = new JSONObject();
            try {
                fbloginForm.put("email", fb_email);
                fbloginForm.put("token_oauth", fb_accessToken.getToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(fbloginForm.toString(), MediaType.parse("application/json; charset=utf-8"));
            postLoginFacebookRequest(MainActivity.url + "/oauth", body);
        }
    }

    private void postLoginFacebookRequest(String postUrl, RequestBody body) {

        HttpLogFacebookAsyncTask okHttpAsync = new HttpLogFacebookAsyncTask(body);
        okHttpAsync.execute(postUrl);

    }

    private class HttpLogFacebookAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpLogFacebookAsyncTask(RequestBody postBody) {
            this.postBody = postBody;
            resp = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {

            String postUrl = strings[0];
            Log.d(TAG, postUrl);

            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            Request request;

            // First login with facebook
            Log.d(TAG, "FACEBOOK registration request done");
            request = new Request.Builder()
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
                String accType = json.getString("type");
                token = json.getString("auth_token");
                if (responseString.equals("success")) {
                    Log.d(TAG, responseString);
                    if(accType.equals("signup")) {
                        onSignupFacebookSuccess();
                    }
                    else if(accType.equals("login")){
                        user = json.getString("name") + " " + json.getString("surname");
                        image = json.getString("photo");
                        token = json.getString("auth_token");
                        gender = json.getString("gender");
                        onLogFacebookSuccess();
                    }
                } else {
                    Log.d(TAG, responseString);
                    JSONObject error = json.getJSONObject("error");
                    if(accType.equals("signup") && error.has("user_registration")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.MyAlertDialog)).create();
                        alertDialog.setTitle("Facebook signup");
                        alertDialog.setMessage("Email already used");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    Log.d(TAG, "onResponse failed");
                    onLogFacebookFailed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void onLogFacebookSuccess() {
        Log.d(TAG, "onLogFacebookSuccess");
        Intent i = new Intent(this, MainActivity.class);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("user", user)
                .putString("email", fb_email)
                .putString("image", image)
                .putString("gender", gender)
                .putString("auth_token", token)
                .putInt("fromActivity", IS_LOG)
                .putBoolean("remember", true)
                .apply();
        startActivity(i);
    }

    private void onSignupFacebookSuccess() {
        Log.d(TAG, "onSignupFacebookSuccess");
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("auth_token", fb_accessToken.getToken())
                .putBoolean("remember", true)
                .apply();
        Intent i = new Intent(getApplicationContext(), CompleteLoginActivity.class);
        i.putExtra("fromActivity", IS_LOG);
        i.putExtra("email", fb_email);
        i.putExtra("image", image);
        i.putExtra("username", fb_name);
        startActivity(i);
    }

    private void onLogFacebookFailed() {
        Log.d(TAG, "onLogFacebookFailed");

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
