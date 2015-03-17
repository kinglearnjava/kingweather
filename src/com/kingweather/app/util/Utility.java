package com.kingweather.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
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
    
    // ֱϽ��
    private static String[] municipalities = {"����", "�Ϻ�", "����", "���"};
   
    /**
     * ��area_id.txt�ļ��ж�ȡ���ݲ��������ݿ�
     */
    public static void txtToDatabase(KingWeatherDB kingWeatherDB){
        BufferedReader input = null;
        try {
            InputStream in = MyApplication.getContext().getResources()
                    .openRawResource(com.example.kingweather.R.raw.area_id);
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
}
