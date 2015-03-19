package com.kingweather.app.service;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import com.kingweather.app.receiver.AutoUpdateReceiver;
import com.kingweather.app.util.CallbackListener;
import com.kingweather.app.util.EncodeUtil;
import com.kingweather.app.util.HttpUtil;
import com.kingweather.app.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service {
    
    /**
     * 气象台更新数据的三个时间点
     */
    private static final int[] ALARM_HOURS = {8, 11, 18};
    /**
     * 延迟五分，确保气象台数据已经全部更新
     */
    private static final int MINUTE = 5;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 发条广播让AutoUpdateReceiver马上更新一下天气
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        sendBroadcast(i);
        // 设置定时
        setUpdateWeatherAlarm();
        return super.onStartCommand(intent, flags, startId);
    }
    
    /**
     * 设置闹钟
     * 气象台API更新天气的时间为每天8:00, 11:00, 18:00
     * 设定三个定时闹钟
     */
    private void setUpdateWeatherAlarm() {
        long intervalMillis = 24 * 60 *60 * 1000;
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AutoUpdateReceiver.class);
        for (int i = 0; i < ALARM_HOURS.length; i++){
            long triggerAtTime = getAlarmTimeMillis(ALARM_HOURS[i], MINUTE);
            PendingIntent pi = PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, intervalMillis, pi);
        }
    }
    
    /**
     * 获取指定时间的敲钟设定毫秒数
     */
    private long getAlarmTimeMillis(int hour, int minute) {
        long elapsedTime = SystemClock.elapsedRealtime();
        long systemTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long selectedTime = calendar.getTimeInMillis();
        // 如果现在的时间大于设置的时间，则推迟一天
        if (systemTime > selectedTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectedTime = calendar.getTimeInMillis();
        }
        long firstAlarmTime = elapsedTime + (selectedTime - systemTime);
        return firstAlarmTime;
    }
    
    
    
}
