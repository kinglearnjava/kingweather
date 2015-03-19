package com.kingweather.app.receiver;

import com.kingweather.app.service.AutoUpdateService;
import com.kingweather.app.util.CallbackListener;
import com.kingweather.app.util.EncodeUtil;
import com.kingweather.app.util.HttpUtil;
import com.kingweather.app.util.MyApplication;
import com.kingweather.app.util.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AutoUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        updateWeather();
    }
    
    /**
     * 更新天气信息
     */
    private void updateWeather() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                String weatherCode = prefs.getString("weather_code", "");
                String address = EncodeUtil.getQueryURLStr(weatherCode, EncodeUtil.FORECAST_ROUTINE);
                HttpUtil.sendHttpRequest(address, new CallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        Utility.handleWeatherResponse(MyApplication.getContext(), response);
                    }
                    
                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }).start();
    }

}
