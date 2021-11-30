package com.example.atlanticbakery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class notification_class {
    Activity activity;

    public notification_class(Activity m){
        activity = m;
    }

    public int notif(String sURL){
        int result = 0;
        try {
            OkHttpClient client;
            client = new OkHttpClient();
            SharedPreferences sharedPreferences2 = activity.getSharedPreferences("CONFIG", MODE_PRIVATE);
            String IPAddress = sharedPreferences2.getString("IPAddress", "");

            SharedPreferences sharedPreferences0 = activity.getSharedPreferences("TOKEN", MODE_PRIVATE);
            String token = sharedPreferences0.getString("token", "");
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(IPAddress +  sURL)
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = null;

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            response = client.newCall(request).execute();
            String sResponse = response.body().string();
//            System.out.println(sResponse);
            if(sResponse.substring(0,1).equals("{")) {
                JSONObject jsonObjectResponse = new JSONObject(sResponse);
                if (!jsonObjectResponse.isNull("success")) {
                    if (jsonObjectResponse.getBoolean("success")) {
                        JSONArray jsonArrayData = jsonObjectResponse.getJSONArray("data");
                        for (int i = 0; i < jsonArrayData.length(); i++) {
                            result += 1;
                        }
//                        System.out.println("resu " + result);
                        return  result;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return  0;
        }
        return result;
    }

}
