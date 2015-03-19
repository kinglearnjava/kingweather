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
     * 切换城市按钮
     */
    private Button switchCity;
    
    /**
     * 更新天气按钮
     */
    private Button refreshWeather;

    private LinearLayout weatherInfoLayout;
    
    /**
     * 用于显示城市名
     */
    private TextView cityNameText;
    
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    
    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    
    /**
     * 用于显示气温1、2
     */
    private TextView temp1Text;
    private TextView temp2Text;
    
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        // 初始化各种控件
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
            // 有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherInfo(countyCode);
        } else {
            // 没有县级代码就直接查询本地天气
            showWeather();
        }
        switchCity = (Button)findViewById(R.id.switch_city);
        refreshWeather = (Button)findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }
    
    
    /**
     * 服务器查询天气信息
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
                        LogUtil.v("同步失败调试", "异常跟踪栈：" + e.getMessage());
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }
    
    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name", ""));
        temp1Text.setText(prefs.getString("temp1", ""));
        temp2Text.setText(prefs.getString("temp2", ""));
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
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
            publishText.setText("同步中...");
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
