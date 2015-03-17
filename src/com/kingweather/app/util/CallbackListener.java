package com.kingweather.app.util;

public interface CallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
