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
    
    // 用来查重复
    private static Map<String, Integer> provinceCodes = new HashMap<String, Integer>();
    private static Map<String, Integer> cityCodes = new HashMap<String, Integer>();
    private static Map<String, Integer> countyCodes = new HashMap<String, Integer>();
    
    // 直辖市
    private static String[] municipalities = {"北京", "上海", "重庆", "天津"};
   
    /**
     * 从area_id.txt文件中读取数据并存入数据库
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
     * 解析和处理area_id.txt的每一行数据
     */
    private static void handleLine(String line, KingWeatherDB kingWeatherDB){
        if (!TextUtils.isEmpty(line)) {
            LogUtil.v("读取文本", "读取出的文本文件每一行内容为:" + line);
            String[] data = line.split("\\|");
            LogUtil.v("读取文本", "data数组长度为：" + data.length + "\nDATA数组为：" + Arrays.toString(data));
            if (data != null && data.length > 0) {
                int provinceId = handleProvince(data, kingWeatherDB);
                int cityId = handleCity(data, provinceId, kingWeatherDB);
                handleCounty(data, cityId, kingWeatherDB);
            }
        }
    }
    
    /**
     * 从分割后的数组中获取省级数据
     */
    private static int handleProvince(String[] data, KingWeatherDB kingWeatherDB){
        String provinceName = data[6];
        String provinceCode = data[0].substring(3, 5);
        // 数据库中的id
        int id = -1;
        if (!provinceCodes.containsKey(provinceCode)) {
            // Map中的value与数据库中的id对应
            id = provinceCodes.size() + 1;
            LogUtil.v("提取省", "省：" + provinceName+ "|省Code："  + provinceCode + " ID" + id + "数组第一个元素" + data[0] + " 长度：" + data[0].length()  );
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
     * 从分割后的数组中解析市级数据
     */
    private static int handleCity(String[] data, int provinceId, 
            KingWeatherDB kingWeatherDB) {
        String cityName = data[4];
        // 如果是直辖市则取二位市区代码
        String cityCode = null;
        if (isMunicipality(cityName)) {
            cityCode = data[0].substring(3, 5); 
        } else {
            cityCode = data[0].substring(3, 7);
        }
        // 数据库中的id
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
     * 从分割数组中解析县级数据
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
     * 判断是否直辖市
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
