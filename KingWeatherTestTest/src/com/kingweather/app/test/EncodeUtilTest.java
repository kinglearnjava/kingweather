package com.kingweather.app.test;

import com.kingweather.app.util.EncodeUtil;

import android.test.AndroidTestCase;

public class EncodeUtilTest extends AndroidTestCase {

    private String areaId = "101210101";    
    private String type = EncodeUtil.FORECAST_ROUTINE;
    private String date = "201503171817";
    private String expectStr = "http://open.weather.com.cn/data/?areaid=101210101&type=forecast_v&date=201503171817&appid=8dc4de&key=sc8Gf%2FRe%2F3eVjH3XAfmMMLRr9%2F4%3D";
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * 测试获取查询天气用的网址
     */
    public void testGetQueryURLStr() {
        assertEquals(expectStr, EncodeUtil.getQueryURLStr(areaId, type, date));
    }
}
