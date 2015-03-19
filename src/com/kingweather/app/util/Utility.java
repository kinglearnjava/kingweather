package com.kingweather.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.kingweather.app.db.KingWeatherDB;
import com.kingweather.app.model.City;
import com.kingweather.app.model.County;
import com.kingweather.app.model.Province;

public class Utility {
    
    
    /**
     * ������������
     */
    private static Map<Integer, String> weatherCodes = new HashMap<Integer, String>();
    static {
        String[] weatherCodeArray = {"��","����","��","����","������","��������б���"
                ,"���ѩ","С��","����","����","����","����","�ش���","��ѩ"
                ,"Сѩ","��ѩ","��ѩ","��ѩ","��","����","ɳ����","С������","�е�����"
                ,"�󵽱���","���굽����","���굽�ش���","С����ѩ","�е���ѩ"
                ,"�󵽱�ѩ","����","��ɳ","ǿɳ����"};
        for (int i = 0; i < weatherCodeArray.length; i++) {
            weatherCodes.put(i, weatherCodeArray[i]);
        }
        weatherCodes.put(53, "��");
        weatherCodes.put(99, "δ֪");
    }
    
    /**
     * �������ظ�
     */
    private static Map<String, Integer> provinceCodes = new HashMap<String, Integer>();
    private static Map<String, Integer> cityCodes = new HashMap<String, Integer>();
    private static Map<String, Integer> countyCodes = new HashMap<String, Integer>();
    
    /**
     * ֱϽ��
     */
    private static String[] municipalities = {"����", "�Ϻ�", "����", "���"};
    
    
   
    /**
     * ��area_id.txt�ļ��ж�ȡ���ݲ��������ݿ�
     */
    public static void txtToDatabase(KingWeatherDB kingWeatherDB){
        BufferedReader input = null;
        try {
            InputStream in = MyApplication.getContext().getResources()
                    .openRawResource(com.kingweather.app.R.raw.area_id);
            input = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String line = null;
            while ((line = input.readLine()) != null) {
                handleLine(line, kingWeatherDB);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * �����ʹ���area_id.txt��ÿһ������
     */
    private static void handleLine(String line, KingWeatherDB kingWeatherDB){
        if (!TextUtils.isEmpty(line)) {
            LogUtil.v("��ȡ�ı�", "��ȡ�����ı��ļ�ÿһ������Ϊ:" + line);
            String[] data = line.split("\\|");
            if (data.length < 9 ) return;
            LogUtil.v("��ȡ�ı�", "data���鳤��Ϊ��" + data.length + "\nDATA����Ϊ��" + Arrays.toString(data));
            if (data != null && data.length > 0) {
                int provinceId = handleProvince(data, kingWeatherDB);
                int cityId = handleCity(data, provinceId, kingWeatherDB);
                handleCounty(data, cityId, kingWeatherDB);
            }
        }
    }
    
    /**
     * �ӷָ��������л�ȡʡ������
     */
    private static int handleProvince(String[] data, KingWeatherDB kingWeatherDB){
        String provinceName = data[6];
        String provinceCode = data[0].substring(3, 5);
        // ���ݿ��е�id
        int id = -1;
        if (!provinceCodes.containsKey(provinceCode)) {
            // Map�е�value�����ݿ��е�id��Ӧ
            id = provinceCodes.size() + 1;
            LogUtil.v("��ȡʡ", "ʡ��" + provinceName+ "|ʡCode��"  + provinceCode + " ID" + id + "�����һ��Ԫ��" + data[0] + " ���ȣ�" + data[0].length()  );
            provinceCodes.put(provinceCode, id);
            Province province = new Province();
            province.setProvinceCode(provinceCode);
            province.setProvinceName(provinceName);
            kingWeatherDB.saveProvince(province);
        } else {
            id = provinceCodes.get(provinceCode);
        }
        return id;
    }
    
    /**
     * �ӷָ��������н����м�����
     */
    private static int handleCity(String[] data, int provinceId, 
            KingWeatherDB kingWeatherDB) {
        String cityName = data[4];
        // �����ֱϽ����ȡ��λ��������
        String cityCode = null;
        if (isMunicipality(cityName)) {
            cityCode = data[0].substring(3, 5); 
        } else {
            cityCode = data[0].substring(3, 7);
        }
        // ���ݿ��е�id
        int id = -1;
        if (!cityCodes.containsKey(cityCode)) {
            id = cityCodes.size() + 1;
            cityCodes.put(cityCode, id);
            City city = new City();
            city.setCityCode(cityCode);
            city.setCityName(cityName);
            city.setProvinceId(provinceId);
            kingWeatherDB.saveCity(city);
        } else {
            id = cityCodes.get(cityCode);
        }
        return id;
    }
    
    /**
     * �ӷָ������н����ؼ�����
     */
    private static void handleCounty(String[] data, int cityId,
            KingWeatherDB kingWeatherDB) {
        String countyName = data[2];
        String countyCode = data[0]; // ���ڲ�����
        if (!countyCodes.containsKey(countyCode)) {
            int id = countyCodes.size() + 1;
            countyCodes.put(countyCode, id);
            County county = new County();
            county.setCountyName(countyName);
            county.setCountyCode(countyCode);
            county.setCityId(cityId);
            kingWeatherDB.saveCounty(county);
        }
    }
    
    /**
     * �ж��Ƿ�ֱϽ��
     */
    private static boolean isMunicipality(String name) {
        for (int i = 0; i < municipalities.length; i++) {
            if (municipalities[i].equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * �������������ص�JSON���ݣ����������������ݴ洢������
     */
    public static void handleWeatherResponse(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            // ������Ϣ
            JSONObject city = jsonObject.getJSONObject("c");
            String cityName = city.getString("c3");
            String weatherCode = city.getString("c1");
            // ������Ϣ ����δ������
            JSONObject weatherInfo = jsonObject.getJSONObject("f");
            JSONArray weatherInfos = weatherInfo.getJSONArray("f1");
            // ��һ��
            JSONObject firstDayWeather = weatherInfos.getJSONObject(0);
            String temp1 = firstDayWeather.getString("fd") + "��";
            String temp2 = firstDayWeather.getString("fc") + "��";
            // ��������������������룬���ϸ�������ʱ���������ݻᱻ���
            String weatherDayCode = firstDayWeather.getString("fa");
            String weatherNightCode = firstDayWeather.getString("fb");
            String weatherDesp = "";
            if (!"".equals(weatherDayCode)) {
                weatherDesp = weatherCodes.get(Integer.valueOf(weatherDayCode)) 
                        + "ת" + weatherCodes.get(Integer.valueOf(weatherNightCode));
            } else {
                weatherDesp = weatherCodes.get(Integer.valueOf(weatherNightCode));
            }
            String publishTimeAll = weatherInfo.getString("f0"); //201503181100��ʽ
            String publishTime = publishTimeAll.substring(8, 10) + ":" + publishTimeAll.substring(10);
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * �����������ص�����������Ϣ�洢��SharedPreferences�ļ���
     */
    public static void saveWeatherInfo(Context context, String cityName, String weatherCode
            , String temp1, String temp2, String weatherDesp, String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        if (!TextUtils.isEmpty(temp1)) {
            editor.putString("temp1", temp1);
        }
        if (!TextUtils.isEmpty(temp2)) {
            editor.putString("temp2", temp2);
        }
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }
}
