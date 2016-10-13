package com.newtouch.ym.coolweather.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ym on 2016/10/13.
 */
public class HttpUtil {
    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();
                    if (connection == null) {
                        Log.d("CoolWeather","connecton == null");
                        return;
                    }
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while ( (line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.d("CoolWeather","获取数据");
                    Log.d("CoolWeather",response.toString());
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    listener.OnError(e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
