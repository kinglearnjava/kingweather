package com.kingweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kingweather.R;
import com.kingweather.app.db.KingWeatherDB;
import com.kingweather.app.model.City;
import com.kingweather.app.model.County;
import com.kingweather.app.model.Province;

public class ChooseAreaActivity extends Activity {
    
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    
    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private KingWeatherDB kingWeatherDB;
    private List<String> dataList = new ArrayList<String>();
    
    /**
     * ʡ���С����б�
     */
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    
    /**
     * ѡ�е�ʡ���С���
     */
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    
    /**
     * ��ǰѡ�еļ���
     */
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView)findViewById(R.id.list_view);
        titleText = (TextView)findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        kingWeatherDB = KingWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });
        // �ȼ���ʡ������
        queryProvinces();
    }
    
    /**
     * ��ѯȫ������ʡ�����ȴ����ݿ��ѯ�������txt�в�ѯ
     */
    private void queryProvinces(){
        provinceList = kingWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            // ����Ա���ʾ��������
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            // ����ListView
            adapter.notifyDataSetChanged();
            // �Ƶ���һ��
            listView.setSelection(0);
            // �ı���
            titleText.setText("�й�");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromTxt();
        }
    }
    
    /**
     * ��ѯ��ѡʡ�����е���
     */
    private void queryCities() {
        cityList = kingWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromTxt();
        }
    }
    
    /**
     * ��ѯ��ѡ�������е���
     */
    private void queryCounties(){
        countyList = kingWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromTxt();
        }
    }
    
    /**
     * ��txt�ĵ��в�ѯ����
     */
    private void queryFromTxt(){
        // ��һ�д��룬P502��ͷ
    }
}