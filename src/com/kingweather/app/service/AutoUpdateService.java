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
     * ����̨�������ݵ�����ʱ���
     */
    private static final int[] ALARM_HOURS = {8, 11, 18};
    /**
     * �ӳ���֣�ȷ������̨�����Ѿ�ȫ������
     */
    private static final int MINUTE = 5;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // �����㲥��AutoUpdateReceiver���ϸ���һ������
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        sendBroadcast(i);
        // ���ö�ʱ
        setUpdateWeatherAlarm();
        return super.onStartCommand(intent, flags, startId);
    }
    
    /**
     * ��������
     * ����̨API����������ʱ��Ϊÿ��8:00, 11:00, 18:00
     * �趨������ʱ����
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
     * ��ȡָ��ʱ��������趨������
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
        // ������ڵ�ʱ��������õ�ʱ�䣬���Ƴ�һ��
        if (systemTime > selectedTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectedTime = calendar.getTimeInMillis();
        }
        long firstAlarmTime = elapsedTime + (selectedTime - systemTime);
        return firstAlarmTime;
    }
    
    
    
}
