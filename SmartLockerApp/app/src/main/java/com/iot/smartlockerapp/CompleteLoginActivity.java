package com.iot.smartlockerapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mikhaellopez.circularimageview.CircularImageView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CompleteLoginActivity extends AppCompatActivity {

    private static final String TAG = "ConfSignupActivity";

    @BindView(R.id.input_image) CircleImageView _profilePict;
    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_surname) EditText _surnText;
    @BindView(R.id.input_age) EditText _ageText;
    @BindView(R.id.input_weight) EditText _weightText;
    @BindView(R.id.btn_confsignup) Button _signupButton;

    private String picture;
    private String name;
    private String surname;
    private String age;
    private String weight;
    private String imageUri = "R.drawable.com_facebook_profile_picture_blank_portrait";

    private String base64Credentials;
    private String email;

    private final static int RESULT_LOAD_IMAGE = 1;

    private static final String PREFS_NAME = "SmartLockSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_complogin);
        ButterKnife.bind(this);

        _profilePict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(CompleteLoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CompleteLoginActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESULT_LOAD_IMAGE);
                }
                else {
                    Intent i = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
            }
        });

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
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
        email = getIntent().getStringExtra("email");

        Log.d(TAG, email);

        if(imageUri == null) {
            imageUri = "R.drawable.com_facebook_profile_picture_blank_portrait";
        }

        Log.d(TAG, base64Credentials);
        Log.d(TAG, imageUri);

        JSONObject regForm = new JSONObject();

        try {
            regForm.put("name", name);
            regForm.put("surname", surname);
            regForm.put("age", age);
            regForm.put("weight", weight);
            regForm.put("img", imageUri);
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
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String loginResponseString = json.getString("response");
                            Log.d("LOGIN", "Response from the server: " + loginResponseString);
                            if(loginResponseString.equals("success")) {
                                onSignupSuccess();
                            }
                            else {
                                onSignupFailed();
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

    private void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);

        String user = name + " " + surname;

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("user", user);
        i.putExtra("email", email);
        i.putExtra("image", imageUri);

        startActivity(i);
    }

    private void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);

        String username = name + " " + surname;

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("user", username);
        i.putExtra("email", email);
        i.putExtra("image", imageUri);

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("user", username)
                .putString("email", email)
                .putString("image", imageUri)
                .commit();

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
