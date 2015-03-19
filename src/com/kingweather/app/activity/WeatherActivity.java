package com.kingweather.app.activity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kingweather.app.R;
import com.kingweather.app.service.AutoUpdateService;
import com.kingweather.app.util.CallbackListener;
import com.kingweather.app.util.EncodeUtil;
import com.kingweather.app.util.HttpUtil;
import com.kingweather.app.util.LogUtil;
import com.kingweather.app.util.Utility;


public class WeatherActivity extends Activity implements OnClickListener {
    
    /**
     * �л����а�ť
     */
    private Button switchCity;
    
    /**
     * ����������ť
     */
    private Button refreshWeather;

    private LinearLayout weatherInfoLayout;
    
    /**
     * ������ʾ������
     */
    private TextView cityNameText;
    
    /**
     * ������ʾ����ʱ��
     */
    private TextView publishText;
    
    /**
     * ������ʾ����������Ϣ
     */
    private TextView weatherDespText;
    
    /**
     * ������ʾ����1��2
     */
    private TextView temp1Text;
    private TextView temp2Text;
    
    /**
     * ������ʾ��ǰ����
     */
    private TextView currentDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        // ��ʼ�����ֿؼ�
        initWidget();
    }
    
    private void initWidget() {
        weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
        cityNameText = (TextView)findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            // ���ؼ�����ʱ��ȥ��ѯ����
            publishText.setText("ͬ����...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherInfo(countyCode);
        } else {
            // û���ؼ������ֱ�Ӳ�ѯ��������
            showWeather();
        }
        switchCity = (Button)findViewById(R.id.switch_city);
        refreshWeather = (Button)findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }
    
    
    /**
     * ��������ѯ������Ϣ
     */
    private void queryWeatherInfo(final String areaId) {
        String address = EncodeUtil.getQueryURLStr(areaId, EncodeUtil.FORECAST_ROUTINE);
        HttpUtil.sendHttpRequest(address, new CallbackListener() {
            @Override
            public void onFinish(String response) {
                if (!TextUtils.isEmpty(response)) {
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }
            
            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.v("ͬ��ʧ�ܵ���", "�쳣����ջ��" + e.getMessage());
                        publishText.setText("ͬ��ʧ��");
                    }
                });
            }
        });
    }
    
    /**
     * ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ��������
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name", ""));
        temp1Text.setText(prefs.getString("temp1", ""));
        temp2Text.setText(prefs.getString("temp2", ""));
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        publishText.setText("����" + prefs.getString("publish_time", "") + "����");
        currentDateText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.switch_city:
            Intent intent = new Intent(this, ChooseAreaActivity.class);
            intent.putExtra("from_weather_activity", true);
            startActivity(intent);
            finish();
            break;
        case R.id.refresh_weather:
            publishText.setText("ͬ����...");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String weatherCode = prefs.getString("weather_code", "");
            if (!TextUtils.isEmpty(weatherCode)) {
                queryWeatherInfo(weatherCode);
            }
            break;
        default:
            break;
        }
    }
}
