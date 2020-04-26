package com.iot.smartlockerapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
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
    @BindView(R.id.genderRadioGroup) RadioGroup _radioGroup;

    private String name;
    private String surname;
    private String age;
    private String weight;
    private String gender;
    private String imageUri = "R.drawable.com_facebook_profile_picture_blank_portrait";

    private String token;
    private String email;

    private int setFromAct;

    private final static int IS_SIGNUP = 2;

    private final static int RESULT_LOAD_IMAGE = 1;
    private final static int RESULT_CAMERA = 2;

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

        int fromAct = getIntent().getIntExtra("fromActivity", 0);
        if(fromAct == 1) {
            // FACEBOOK OAUTH LOGIN

            setFromAct = 1;

            imageUri = getIntent().getStringExtra("image");
            byte[] decodedString = Base64.decode(imageUri, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            _profilePict.setImageBitmap(decodedByte);

            List<String> usname = new LinkedList(Arrays.asList(getIntent().getStringExtra("username").trim().split("\\s+")));
            name = usname.get(0);
            usname.remove(0);
            surname = String.join(" ", usname);
            _nameText.setText(name);
            _surnText.setText(surname);

        }

        else {
            setFromAct = IS_SIGNUP;
        }

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        token = pref.getString("auth_token", null);
        Log.d(TAG, token);

        email = getIntent().getStringExtra("email");

        _profilePict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = { "Take Photo", "Choose from Gallery", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.MyAlertDialog));
                builder.setTitle("Choose your profile picture");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(options[which].equals("Take Photo")) {
                            if(ActivityCompat.checkSelfPermission(CompleteLoginActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(CompleteLoginActivity.this, new String[]{Manifest.permission.CAMERA}, RESULT_CAMERA);
                            }
                            else {
                                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                startActivityForResult(i, RESULT_CAMERA);
                            }
                        }
                        else if(options[which].equals("Choose from Gallery")) {
                            if (ActivityCompat.checkSelfPermission(CompleteLoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(CompleteLoginActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESULT_LOAD_IMAGE);
                            }
                            else {
                                Intent i = new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                                startActivityForResult(i, RESULT_LOAD_IMAGE);
                            }
                        }
                        else if(options[which].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();

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
            case RESULT_CAMERA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(i, RESULT_CAMERA);
                }
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
        if(requestCode == RESULT_CAMERA && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            imageUri = Base64.encodeToString(b, Base64.DEFAULT);
            Log.d(TAG, imageUri);
            _profilePict.setImageBitmap(image);
        }

    }

    @Override
    public void onBackPressed() {
    }

    public void signup() {
        Log.d(TAG, "Signup");

        name = _nameText.getText().toString();
        surname = _surnText.getText().toString();
        age = _ageText.getText().toString();
        weight = _weightText.getText().toString();

        if (!validate()) {
            onCompleteSignFailed();
            return;
        }

        _signupButton.setEnabled(false);

        Log.d(TAG, email);

        if(imageUri == null) {
            imageUri = "R.drawable.com_facebook_profile_picture_blank_portrait";
        }

        Log.d(TAG, token);
        Log.d(TAG, imageUri);

        int selectedG = _radioGroup.getCheckedRadioButtonId();

        RadioButton selGend = findViewById(selectedG);
        gender = selGend.getText().toString();

        JSONObject regForm = new JSONObject();

        try {
            regForm.put("name", name);
            regForm.put("surname", surname);
            regForm.put("age", age);
            regForm.put("weight", weight);
            regForm.put("gender", gender);
            regForm.put("img", imageUri);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(regForm.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(MainActivity.url+"/confprofile", body);

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

            Log.d(TAG, "request done");

            String postUrl = strings[0];
            Log.d(TAG, postUrl);

            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(postUrl)
                    .post(postBody)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", email)
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
            Log.d(TAG, "RESPONSE" + resp);
            try {
                JSONObject json = new JSONObject(resp);
                String responseString = json.getString("response");
                Log.d(TAG, "Response from the server: " + responseString);
                if(responseString.equals("success")) {
                    Log.d(TAG, "success");
                    onCompleteSignSuccess();
                }
                else {
                    Log.d(TAG, "failure");
                    onCompleteSignFailed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void onCompleteSignSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);

        String username = name + " " + surname;

        Intent i = new Intent(this, MainActivity.class);

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString("user", username)
                .putString("email", email)
                .putString("gender", gender)
                .putString("image", imageUri)
                .putInt("fromActivity", setFromAct)
                .apply();

        startActivity(i);
    }

    private void onCompleteSignFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);

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
