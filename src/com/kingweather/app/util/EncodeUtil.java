package com.kingweather.app.util;

import java.io.InputStream;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.util.Properties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.kingweather.app.R;

/**
 * 生成查询用的网址
 */
public class EncodeUtil {
    
    /**
     * 数据类型参数
     * 常规提供的地区比基础多
     */
    public static final String INDEX_FOUNDATION = "index_f"; // 基础指数
    public static final String INDEX_ROUTINE = "index_v"; // 常规指数
    public static final String FORECAST_FOUNDATION = "forecast_f"; // 基础三天预报
    public static final String FORECAST_ROUTINE = "forecast_v"; // 常规三天预报
    
    /**
     * 气象局获取的开发者key等数据
     */
    private static String privateKey;
    private static String appId;
    static {
        try {
            InputStream in = MyApplication.getContext().getResources()
                    .openRawResource(R.raw.weather_api_key);
            Properties prop = new Properties();
            prop.loadFromXML(in);
            privateKey = prop.getProperty("private_key");
            appId = prop.getProperty("app_id");
            LogUtil.v("测试读取api_key", "privateKey: " + privateKey + "\n" + "appId: " + appId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static final String URL_PREFIX = "http://open.weather.com.cn/data/?";

    private static final char last2byte = (char) Integer.parseInt("00000011", 2);
    private static final char last4byte = (char) Integer.parseInt("00001111", 2);
    private static final char last6byte = (char) Integer.parseInt("00111111", 2);
    private static final char lead6byte = (char) Integer.parseInt("11111100", 2);
    private static final char lead4byte = (char) Integer.parseInt("11110000", 2);
    private static final char lead2byte = (char) Integer.parseInt("11000000", 2);
    private static final char[] encodeTable = new char[] { 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/' 
    };
    
    /**
     * 获取查询天气用的网址 
     */
    public static String getQueryURLStr (String areaId, String type, String date) {
        String data = URL_PREFIX + "areaid=" + areaId + "&type=" + type + "&date=" + date + "&appid=";
        String publicKey = standardURLEncoder(data + appId, privateKey);
        String queryURLStr = data + appId.substring(0, 6) + "&key=" + publicKey;
        return queryURLStr;
    }
    

    private static String standardURLEncoder(String data, String key) {
        byte[] byteHMAC = null;
        String urlEncoder = "";
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            mac.init(spec);
            byteHMAC = mac.doFinal(data.getBytes());
            if (byteHMAC != null) {
                String oauth = encode(byteHMAC);
                if (oauth != null) {
                    urlEncoder = URLEncoder.encode(oauth, "utf8");
                }
            }
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return urlEncoder;
    }

    private static String encode(byte[] from) {
        StringBuffer to = new StringBuffer((int) (from.length * 1.34) + 3);
        int num = 0;
        char currentByte = 0;
        for (int i = 0; i < from.length; i++) {
            num = num % 8;
            while (num < 8) {
                switch (num) {
                case 0:
                    currentByte = (char) (from[i] & lead6byte);
                    currentByte = (char) (currentByte >>> 2);
                    break;
                case 2:
                    currentByte = (char) (from[i] & last6byte);
                    break;
                case 4:
                    currentByte = (char) (from[i] & last4byte);
                    currentByte = (char) (currentByte << 2);
                    if ((i + 1) < from.length) {
                        currentByte |= (from[i + 1] & lead2byte) >>> 6;
                    }
                    break;
                case 6:
                    currentByte = (char) (from[i] & last2byte);
                    currentByte = (char) (currentByte << 4);
                    if ((i + 1) < from.length) {
                        currentByte |= (from[i + 1] & lead4byte) >>> 4;
                    }
                    break;
                }
                to.append(encodeTable[currentByte]);
                num += 6;
            }
        }
        if (to.length() % 4 != 0) {
            for (int i = 4 - to.length() % 4; i > 0; i--) {
                to.append("=");
            }
        }
        return to.toString();
    }
    
    
    public static void main(String[] args) {
        try {
            
            //需要加密的数据  
            String data = "http://open.weather.com.cn/data/?areaid=xxxxxxxxxx&type=xxxxxxxx&date=xxxxxxxxx&appid=xxxxxxx";  
//            String data = "http://open.weather.com.cn/data/?areaid=101210101&type=forecast_v&date=201503171817&appid=8dc4de10af5d44cf";
            //密钥  
            String key = "xxxxx_SmartWeatherAPI_xxxxxxx";  
            
            String str = standardURLEncoder(data, key);

            System.out.println(str);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}