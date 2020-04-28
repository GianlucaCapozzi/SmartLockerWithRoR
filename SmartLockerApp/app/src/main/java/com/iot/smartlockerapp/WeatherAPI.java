package com.iot.smartlockerapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherAPI extends AsyncTask<String, Void, byte[]> {

    public interface WeatherResponse {
        void processFinish(String output);
    }


    private WeatherResponse delegate;
    private String resp;

    private final static String TAG = "WEATHER_API";

    public WeatherAPI(WeatherResponse delegate) {
        this.delegate = delegate;
        this.resp = "";
    }

    @Override
    protected byte[] doInBackground(String... strings) {
        String postUrl = strings[0];

        Log.d(TAG, postUrl);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(postUrl)
                .get()
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
        delegate.processFinish(resp);
    }
}
