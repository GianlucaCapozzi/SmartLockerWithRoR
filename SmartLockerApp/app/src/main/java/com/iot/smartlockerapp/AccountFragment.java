package com.iot.smartlockerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class AccountFragment extends Fragment {

    private static final String PREFS_NAME = "SmartLockSettings";

    private static final String TAG = "ACCOUNT";

    private CircleImageView profilePict;
    private TextView usernameTV;
    private TextView ageTV;
    private TextView weightTV;

    private String age;
    private String weight;
    private String token;

    public AccountFragment() {
    }

    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);


        SharedPreferences pref = this.getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = pref.getString("user", null);
        String image = pref.getString("image", null);

        token = pref.getString("auth_token", null);

        //Log.d("TAG", token);

        if(!image.equals("R.drawable.com_facebook_profile_picture_blank_portrait")) {
            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            profilePict = v.findViewById(R.id.profile_image);
            profilePict.setImageBitmap(decodedByte);
        }

        usernameTV = v.findViewById(R.id.usernameTV);
        usernameTV.setText(name);

        String postUrl = MainActivity.url + "/getinfo";
        HttpGetInfoAsyncTask okHttpAsync = new HttpGetInfoAsyncTask();
        okHttpAsync.execute(postUrl);

        return v;

    }

    private class HttpGetInfoAsyncTask extends AsyncTask<String, Void, byte[]> {

        private String resp;

        private HttpGetInfoAsyncTask() {
            resp = "";
        }

        @Override
        protected byte[] doInBackground(String... strings) {

            Log.d(TAG, "request done");

            String postUrl = strings[0];
            Log.d(TAG, postUrl);

            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(postUrl)
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
                if (responseString.equals("success")) {

                    age = json.getString("age");
                    ageTV = getView().findViewById(R.id.ageTV);
                    String styledAgeText = "<strong> Age: </strong>" + age;
                    ageTV.setText(Html.fromHtml(styledAgeText, Html.FROM_HTML_MODE_LEGACY));

                    weight = json.getString("weight");
                    weightTV = getView().findViewById(R.id.weightTV);
                    String styledWeightText = "<strong> Weight: </strong>" + weight;
                    weightTV.setText(Html.fromHtml(styledWeightText, Html.FROM_HTML_MODE_LEGACY));

                    String image = json.getString("photo");

                    if(!image.equals("R.drawable.com_facebook_profile_picture_blank_portrait")) {
                        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        profilePict = getView().findViewById(R.id.profile_image);
                        profilePict.setImageBitmap(decodedByte);
                    }

                } else {
                    Log.d(TAG, responseString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
