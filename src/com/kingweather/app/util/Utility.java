package com.kingweather.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.kingweather.app.db.KingWeatherDB;
import com.kingweather.app.model.City;
import com.kingweather.app.model.County;
import com.kingweather.app.model.Province;

public class Utility {
    
    // �������ظ�
    private static Map<String, Integer> provinceCodes = new HashMap<String, Integer>();
    private static Map<String, Integer> cityCodes = new HashMap<String, Integer>();
    private static Map<String, Integer> countyCodes = new HashMap<String, Integer>();
   
    /**
     * ��area_id.txt�ļ��ж�ȡ���ݲ��������ݿ�
     */
    public static void txtToDatabase(KingWeatherDB kingWeatherDB){
        BufferedReader input = null;
        try {
            InputStream in = MyApplication.getContext().getResources()
                    .openRawResource(com.example.kingweather.R.raw.area_id);
            input = new BufferedReader(new InputStreamReader(in));
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
            String[] data = line.split("|");
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
        String cityCode = data[0].substring(3, 7);
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
        String countyCode = data[0].substring(3);
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
}
