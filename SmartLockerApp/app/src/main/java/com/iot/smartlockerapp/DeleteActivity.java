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

public class DeleteActivity extends AppCompatActivity {

    private static final int IS_DEL = 4;

    private String token;
    private String email;

    @BindView(R.id.input_delete_pwd) EditText _deletePwd;
    @BindView(R.id.input_conf_delete_pwd) EditText _confDeletePwd;
    @BindView(R.id.btn_delete_account) Button _deleteAccount;

    private static final String PREFS_NAME = "SmartLockSettings";
    private static final String TAG = "DELETE_ACT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delete);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Delete Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        _deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(DeleteActivity.this, R.style.MyAlertDialog)).create();
                alertDialog.setTitle("Delete account");
                alertDialog.setMessage("Are you sure you want to delete the account?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAcc();
                            }
                        });
                alertDialog.show();
            }
        });

    }

    private void deleteAcc() {
        _deleteAccount.setEnabled(false);

        if(!validate()) {
            _deleteAccount.setEnabled(true);
            return;
        }

        SharedPreferences pref = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = pref.getString("email", null);
        String pwd = _deletePwd.getText().toString();

        token = pref.getString("auth_token", null);

        String credentials = email + ":" + pwd;
        String base64Credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        try {
            JSONObject deleteForm = new JSONObject();

            deleteForm.put("password", base64Credentials);

            RequestBody body = RequestBody.create(deleteForm.toString(), MediaType.parse("application/json; charset=utf-8"));
            sendDeleteRequest(MainActivity.url+"/deleteaccount", body);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void sendDeleteRequest(String deleteUrl, RequestBody deleteBody) {
        HttpDeleteAsyncTask okHttpAsync = new HttpDeleteAsyncTask(deleteBody);
        okHttpAsync.execute(deleteUrl);
    }

    private class HttpDeleteAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody deleteBody;
        private String resp;

        private HttpDeleteAsyncTask(RequestBody deleteBody) {
            this.deleteBody = deleteBody;
            resp = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {
            Log.d(TAG, "DELETE request done");

            String deleteUrl = strings[0];
            Log.d(TAG, deleteUrl);

            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(deleteUrl)
                    .delete(deleteBody)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", token)
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
            try {
                JSONObject json = new JSONObject(resp);
                String responseString = json.getString("response");
                Log.d(TAG, responseString);
                if(responseString.equals("success")) {
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putInt("fromActivity", IS_DEL)
                            .apply();
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(DeleteActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Delete account");
                    alertDialog.setMessage("The account was successfully deleted!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(i);
                                }
                            });
                    alertDialog.show();
                }
                else {
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(DeleteActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Delete account");
                    alertDialog.setMessage("An error has occured, try again!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
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

        String pwd = _deletePwd.getText().toString();
        String conf_pwd = _confDeletePwd.getText().toString();

        if(pwd.isEmpty() || pwd.length() < 4 || pwd.length() > 10) {
            _deletePwd.setError("Insert password between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _deletePwd.setError(null);
        }
        if(!(conf_pwd.equals(pwd))) {
            _confDeletePwd.setError("Password Do not match");
            valid = false;
        } else {
            _confDeletePwd.setError(null);
        }
        return valid;
    }

}
