package com.iot.smartlockerapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangeProfileActivity extends AppCompatActivity {

    private static final String TAG = "ChangeProfileActivity";

    private final static int RESULT_LOAD_IMAGE = 1;

    private static final String PREFS_NAME = "SmartLockSettings";

    private String age;
    private String weight;
    private String imageUri = "R.drawable.com_facebook_profile_picture_blank_portrait";

    @BindView(R.id.input_new_image) CircleImageView _profilePict;
    @BindView(R.id.input_new_age) EditText _ageText;
    @BindView(R.id.input_new_weight) EditText _weightText;
    @BindView(R.id.btn_changeProfile) Button _changeProfileButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_profile);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Change Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        _profilePict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(ChangeProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChangeProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESULT_LOAD_IMAGE);
                }
                else {
                    Intent i = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
            }
        });

        _changeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try {
                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(selectedImage, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();
                imageUri = Base64.encodeToString(b, Base64.DEFAULT);

                parcelFileDescriptor.close();
                _profilePict.setImageBitmap(image);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void updateProfile() {

        age = _ageText.getText().toString();
        weight = _weightText.getText().toString();

        _changeProfileButton.setEnabled(false);

        if(age == null) {
            age = "";
        }
        if(weight == null) {
            weight = "";
        }

        if (imageUri.equals("R.drawable.com_facebook_profile_picture_blank_portrait")) {

            imageUri = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString("image", null);
        }

        JSONObject regForm = new JSONObject();

        try {
            regForm.put("age", age);
            regForm.put("weight", weight);
            regForm.put("img", imageUri);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(regForm.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(MainActivity.url+"/confprofile", body);

    }

    private void postRequest(String postUrl, RequestBody postBody) {

        Log.d("POSTURL", postUrl);

        HttpPostUpdateProfileAsyncTask okHttpAsync = new HttpPostUpdateProfileAsyncTask(postBody);
        okHttpAsync.execute(postUrl);

    }

    private class HttpPostUpdateProfileAsyncTask extends AsyncTask<String, Void, byte[]> {

        RequestBody postBody;
        private String resp;

        private HttpPostUpdateProfileAsyncTask(RequestBody postBody) {
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
            Log.d(TAG, "RESPONSE: " + resp);
            try {
                JSONObject json = new JSONObject(resp);
                String responseString = json.getString("response");
                Log.d(TAG, "Response from the server: " + responseString);
                if(responseString.equals("success")) {
                    Log.d(TAG, "success");
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putString("image", imageUri)
                            .apply();

                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ChangeProfileActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Update Profile");
                    alertDialog.setMessage("The profile was successfully updated!");
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
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(ChangeProfileActivity.this, R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("Update Profile");
                    alertDialog.setMessage("The profile was not updated!");
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

}
