package com.newtouch.ym.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.newtouch.ym.coolweather.R;
import com.newtouch.ym.coolweather.db.City;
import com.newtouch.ym.coolweather.db.CoolWeatherDB;
import com.newtouch.ym.coolweather.db.County;
import com.newtouch.ym.coolweather.db.Province;
import com.newtouch.ym.coolweather.util.HttpCallbackListener;
import com.newtouch.ym.coolweather.util.HttpUtil;
import com.newtouch.ym.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String>dataList = new ArrayList<String>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    private static final String TAG = "CoolWeather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);

        listView = (ListView)findViewById(R.id.list_view);
        titleView = (TextView)findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = coolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCounties();
                }
            }
        });

        queryProvinces();
    }

    /*
    查询所有的省，先熊数据库查询，再从服务器查询
    */
    private void queryProvinces() {
        provinceList = coolWeatherDB.loadProvinces();

        if (provinceList.size() > 0) {
            dataList.clear();

            for (Province p : provinceList) {
                dataList.add(p.getProvinceName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleView.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    /*
    查询所有的市，先熊数据库查询，再从服务器查询
    */
    private void queryCities() {
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());

        if (cityList.size() > 0) {
            dataList.clear();

            for (City c : cityList) {
                dataList.add(c.getCityName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleView.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /*
    查询所有的县，先熊数据库查询，再从服务器查询
     */
    private void queryCounties() {
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());

        if (countyList.size() > 0) {
            dataList.clear();

            for (County c : countyList) {
                dataList.add(c.getCountyName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleView.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /*
    查询所有的省、市、县，先熊数据库查询，再从服务器查询
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        Log.d(TAG, address);
        //
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {

                Log.d(TAG, response);
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(coolWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
                } else if ("county".equals(type)){
                    result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
                }
                Log.d(TAG, String.format("结果 %b",result));

                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //
                            closeProgrssDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void OnError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 
                        closeProgrssDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*
    打开进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*
    关闭进度对话框
     */
    private void closeProgrssDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            finish();
        }
    }
}
