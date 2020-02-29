package com.iot.smartlockerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";

    @BindView(R.id.input_temp_pwd) EditText _tempPwd;
    @BindView(R.id.input_new_pwd) EditText _newPwd;
    @BindView(R.id.input_conf_new_pwd) EditText _confNewPwd;
    @BindView(R.id.btn_reset) Button _resetBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            onResetFailed();
            return;
        }

        _resetBtn.setEnabled(true);

        String tempPwd = Base64.encodeToString(_tempPwd.getText().toString().getBytes(), Base64.NO_WRAP);
        String newPwd = Base64.encodeToString(_newPwd.getText().toString().getBytes(), Base64.NO_WRAP);
        String confNewPwd = Base64.encodeToString(_confNewPwd.getText().toString().getBytes(), Base64.NO_WRAP);

        JSONObject resForm = new JSONObject();

        try {
            resForm.put("temp_pass", tempPwd);
            resForm.put("new_pass", newPwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(resForm.toString(), MediaType.get("application/json; charset=utf-8"));
        postRequest(MainActivity.url+"/recoverypass", body);

    }

    private void postRequest(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        // DOUBLE-CHECK EMAIL

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
                        onResetFailed();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                final String responseString = response.body().string().trim();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String resetResponseString = json.getString("response");
                            Log.d("LOGIN", "Response from the server: " + resetResponseString);
                            if(resetResponseString.equals("success")) {
                                onResetSuccess();
                            }
                            else {
                                onResetFailed();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void onResetSuccess() {
        AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this).create();
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
        AlertDialog alertDialog = new AlertDialog.Builder(ForgotPasswordActivity.this).create();
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
