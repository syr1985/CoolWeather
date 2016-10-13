package com.newtouch.ym.coolweather.util;

/**
 * Created by ym on 2016/10/13.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void OnError(Exception e);
}
