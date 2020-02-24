package com.iot.smartlockerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CompleteLoginActivity extends AppCompatActivity {

    private static final String TAG = "ConfSignupActivity";

    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_surname) EditText _surnText;
    @BindView(R.id.input_age) EditText _ageText;
    @BindView(R.id.input_weight) EditText _weightText;
    @BindView(R.id.btn_confsignup) Button _signupButton;

    private String name;
    private String surname;
    private String age;
    private String weight;

    private String base64Credentials;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complogin);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

    }

    public void signup() {
        Log.d(TAG, "Signup");

        name = _nameText.getText().toString();
        surname = _surnText.getText().toString();
        age = _ageText.getText().toString();
        weight = _weightText.getText().toString();

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        base64Credentials = getIntent().getStringExtra("token");

        Log.d(TAG, base64Credentials);

        JSONObject regForm = new JSONObject();

        try {
            regForm.put("name", name);
            regForm.put("surname", surname);
            regForm.put("age", age);
            regForm.put("weight", weight);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Log.d("PACKET", regForm.toString());

        RequestBody body = RequestBody.create(regForm.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(MainActivity.url+"/confsignup", body);

    /*
    new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    // On complete call either onSignupSuccess or onSignupFailed
                    // depending on success
                    onSignupSuccess();
                    // onSignupFailed();
                }
            }, 3000);
    */
    }

    private void postRequest(String postUrl, RequestBody postBody) {

        //Log.d("ERR", postBody.toString());

        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", base64Credentials)
                .build();

        Log.d("OK", "request done");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, final @NotNull IOException e) {
                call.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ERR", "in onFailure");
                        e.printStackTrace();
                        onSignupFailed();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                final String responseString = response.body().string().trim();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(responseString.equals("success")){
                            onSignupSuccess();
                        }
                        else{
                            Log.d("ERR", response.body().toString());
                            Log.d("ERR", "onResponse failed");
                            onSignupFailed();
                        }
                    }
                });
            }
        });
    }

    private void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);

        String user = name + " " + surname;

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("user", user);

        startActivity(i);
    }

    private void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);

        String username = name + " " + surname;

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("user", username);

        startActivity(i);


    }

    private boolean validate() {
        boolean valid = true;

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (surname.isEmpty() || surname.length() < 3) {
            _surnText.setError("at least 3 characters");
            valid = false;
        } else {
            _surnText.setError(null);
        }

        return valid;
    }
}
