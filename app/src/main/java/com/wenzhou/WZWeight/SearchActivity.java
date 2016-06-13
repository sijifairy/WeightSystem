package com.wenzhou.WZWeight;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.application.HttpClientUtil;
import com.wenzhou.WZWeight.log.MyLog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends Activity {
    private static final String TAG = "Activity_search";

    private static List<Map<String, Object>> yesterdayData = null;
    private static List<SpinnerData> mRegionData = null;
    private static List<SpinnerData> mRubbishSourceData = null;
    private static List<SpinnerData> mPoundsData = null;
    private static List<SpinnerData> mDataTypeInfo = null;
    private static String operatingRegionID = "";
    Spinner regionSpinner = null;
    Spinner rubbishsourceSpinner = null;
    Spinner poundsSpinner = null;
    Spinner datatypeSpinner = null;
    TextView back;
    ProgressDialog dialogMine;
    TextView yesterday_statistics;
    Button button_datestart;
    Button button_dateend;
    Button button_search;
    TextView edit_datestart;
    TextView edit_dateend;
    TextView edit_plateno;
    TextView edit_carno;
    Calendar nowdate;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.search);

        back = (TextView) findViewById(R.id.tool_textview_top);
        yesterday_statistics = (TextView) findViewById(R.id.yesterday_statistics);
        regionSpinner = (Spinner) findViewById(R.id.linear_carsearch_Region);
        rubbishsourceSpinner = (Spinner) findViewById(R.id.linear_carsearch_RubbishSource);
        poundsSpinner = (Spinner) findViewById(R.id.linear_carsearch_Pound);
        datatypeSpinner = (Spinner) findViewById(R.id.linear_carsearch_DataType);
        button_datestart = (Button) findViewById(R.id.button_datestart);
        button_dateend = (Button) findViewById(R.id.button_dateend);
        button_search = (Button) findViewById(R.id.button_search);
        edit_datestart = (TextView) findViewById(R.id.linear_carsearch_DateStart);
        edit_dateend = (TextView) findViewById(R.id.linear_carsearch_DateEnd);
        edit_plateno = (TextView) findViewById(R.id.linear_carsearch_PlateNo);
        edit_carno = (TextView) findViewById(R.id.linear_carsearch_CarNo);
        nowdate = Calendar.getInstance();
        edit_datestart.setText(nowdate.get(Calendar.YEAR) + "-" + (nowdate.get(Calendar.MONTH) + 1) + "-" + "1");
        edit_dateend.setText(nowdate.get(Calendar.YEAR) + "-" + (nowdate.get(Calendar.MONTH) + 1) + "-" + nowdate.get(Calendar.DAY_OF_MONTH));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, IndexActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                SearchActivity.this.finish();
            }
        });


        regionSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                operatingRegionID = mRegionData.get(arg2).getValue();
                new GetRubbishSourceTask().execute(Constant.serverUrl + Constant.getrubbishsourceinfo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });


        button_datestart.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DatePickerDialog(SearchActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        monthOfYear = monthOfYear + 1;
                        edit_datestart.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
                        MyLog.d(TAG, year + "-" + monthOfYear + "-" + dayOfMonth);
                    }
                }, nowdate.get(Calendar.YEAR), nowdate.get(Calendar.MONTH), 1).show();
            }
        });


        button_dateend.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DatePickerDialog(SearchActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        monthOfYear = monthOfYear + 1;
                        edit_dateend.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
                        MyLog.d(TAG, year + "-" + monthOfYear + "-" + dayOfMonth);
                    }
                }, nowdate.get(Calendar.YEAR), nowdate.get(Calendar.MONTH), nowdate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        button_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date st = StringToDate(edit_datestart.getText().toString(), "yyyy-MM-dd");
                Date et = StringToDate(edit_dateend.getText().toString(), "yyyy-MM-dd");
                try {
                    if (daysBetween(st, et) > 93) {
                        Toast.makeText(SearchActivity.this, "时间间隔过长，请缩小范围！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent1 = new Intent(SearchActivity.this, SearchResultActivity.class);
                    Bundle data1 = new Bundle();
                    data1.putString("plateno", edit_plateno.getText().toString());
                    data1.putString("carno", edit_carno.getText().toString());
                    data1.putString("datestart", edit_datestart.getText().toString());
                    data1.putString("dateend", edit_dateend.getText().toString());
                    data1.putString("regionid", ((SpinnerData) regionSpinner.getSelectedItem()).getValue());
                    data1.putString("rubbishsourceid", ((SpinnerData) rubbishsourceSpinner.getSelectedItem()).getValue());
                    data1.putString("poundid", ((SpinnerData) poundsSpinner.getSelectedItem()).getValue());
                    data1.putString("datatypeinfo", ((SpinnerData) datatypeSpinner.getSelectedItem()).getValue());
                    intent1.putExtras(data1);
                    startActivity(intent1);
                } catch (ParseException e) {

                    e.printStackTrace();
                }
            }
        });

        new YesterdayWeightTask().execute(Constant.serverUrl + Constant.yesterdayweight);

    }

    public static Date StringToDate(String dateStr, String formatStr) {
        DateFormat sdf = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static int daysBetween(Date smdate, Date bdate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    class YesterdayWeightTask extends AsyncTask<String, Integer, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogMine = new ProgressDialog(SearchActivity.this);
            dialogMine.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialogMine.setTitle("请等待...");
            dialogMine.setMessage("正在获取昨日统计信息，请稍后...");
            dialogMine.setIcon(R.drawable.ic);
            dialogMine.setCancelable(true);
            dialogMine.setIndeterminate(false);
            dialogMine.show();
        }


        @Override
        protected Boolean doInBackground(String... params) {

            try {
                MyLog.d(TAG, "params[0] is :" + params[0]);
                JSONObject obj = new JSONObject();
                try {
                    obj.put("SessionID", Constant.getSessionID());
                    MyLog.i(TAG, "obj" + obj.toString());
                } catch (JSONException e) {
                    MyLog.e(TAG, e.getMessage());
                }
                List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("para", obj.toString()));
                JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
                int result = item.getInt("Result");
                if (result == 1) {
                    yesterdayData = getYersterdayData(item.getString("TableJson"));
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {

                e.printStackTrace();
                MyLog.d(TAG, e.toString());
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result == true) {
                String str = "";
                if (yesterdayData.size() == 0) {
                    str += "\t\t无数据。";
                }
                for (int i = 0; i < yesterdayData.size(); i++) {
                    Map<String, Object> record = yesterdayData.get(i);
                    str += ("\t\t" + record.get("Classname").toString() + record.get("Info").toString() + "共称重 " + record.get("NumCount").toString() + " 次" + "总净重 "
                            + record.get("JingZhong").toString() + " 公斤.\n\r");
                }
                yesterday_statistics.setText(str);
                new GetRegionTask().execute(Constant.serverUrl + Constant.getregioninfo);
            } else {

            }
        }
    }


    class GetRegionTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogMine.setMessage("正在获取区域列表，请稍后...");
        }


        @Override
        protected Boolean doInBackground(String... params) {
            try {
                MyLog.d(TAG, "params[0] is :" + params[0]);

                JSONObject objRegionPara = new JSONObject();
                try {
                    objRegionPara.put("SessionID", Constant.getSessionID());
                    MyLog.i(TAG, "objRegionPara" + objRegionPara.toString());
                } catch (JSONException e) {
                    MyLog.e(TAG, e.getMessage());
                }
                List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("para", objRegionPara.toString()));
                JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
                int result = item.getInt("Result");
                if (result == 1) {
                    String tablejson = item.getString("TableJson");
                    try {
                        tablejson = tablejson.replaceAll("\\\"", "\"");
                        JSONArray resultJson = new JSONArray(tablejson);
                        mRegionData = new ArrayList<SpinnerData>();
                        mRegionData.add(new SpinnerData("", "全部"));
                        for (int i = 0; i < resultJson.length(); i++) {
                            SpinnerData tmp = new SpinnerData(((JSONObject) resultJson.get(i)).getString("ID"),
                                    ((JSONObject) resultJson.get(i)).getString("RegionName"));
                            mRegionData.add(tmp);
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                        MyLog.d(TAG, e.toString());
                        return null;
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {

                e.printStackTrace();
                MyLog.d(TAG, e.toString());
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                BindSpinnerData(mRegionData, regionSpinner);
                operatingRegionID = mRegionData.get(0).getValue();
                new GetRubbishSourceTask().execute(Constant.serverUrl + Constant.getrubbishsourceinfo);
            }
        }
    }


    class GetRubbishSourceTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogMine.setMessage("正在获取垃圾亭列表，请稍后...");
            if (!dialogMine.isShowing()) {
                dialogMine.show();
            }
        }


        @Override
        protected Boolean doInBackground(String... params) {
            try {
                MyLog.d(TAG, "params[0] is :" + params[0]);

                JSONObject objRegionPara = new JSONObject();
                try {
                    objRegionPara.put("SessionID", Constant.getSessionID());
                    objRegionPara.put("RegionID", operatingRegionID);
                    MyLog.i(TAG, "objRegionPara" + objRegionPara.toString());
                } catch (JSONException e) {
                    MyLog.e(TAG, e.getMessage());
                }
                List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("para", objRegionPara.toString()));
                JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
                int result = item.getInt("Result");
                if (result == 1) {
                    String tablejson = item.getString("TableJson");
                    try {
                        tablejson = tablejson.replaceAll("\\\"", "\"");
                        JSONArray resultJson = new JSONArray(tablejson);
                        mRubbishSourceData = new ArrayList<SpinnerData>();
                        mRubbishSourceData.add(new SpinnerData("", "全部"));
                        for (int i = 0; i < resultJson.length(); i++) {
                            SpinnerData tmp = new SpinnerData(((JSONObject) resultJson.get(i)).getString("ID"),
                                    ((JSONObject) resultJson.get(i)).getString("Sourcename"));
                            mRubbishSourceData.add(tmp);
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                        MyLog.d(TAG, e.toString());
                        return null;
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {

                e.printStackTrace();
                MyLog.d(TAG, e.toString());
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {

                BindSpinnerData(mRubbishSourceData, rubbishsourceSpinner);
                new GetPoundTask().execute(Constant.serverUrl + Constant.getpoundinfo);
            }
        }
    }


    class GetPoundTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogMine.setMessage("正在获取地磅站列表，请稍后...");
            if (!dialogMine.isShowing()) {
                dialogMine.show();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                MyLog.d(TAG, "params[0] is :" + params[0]);

                JSONObject objPoundsPara = new JSONObject();
                try {
                    objPoundsPara.put("SessionID", Constant.getSessionID());
                    MyLog.i(TAG, "objPoundsPara" + objPoundsPara.toString());
                } catch (JSONException e) {
                    MyLog.e(TAG, e.getMessage());
                }
                List<NameValuePair> params1 = new ArrayList<NameValuePair>();
                params1.add(new BasicNameValuePair("para", objPoundsPara.toString()));
                JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
                int result = item.getInt("Result");
                if (result == 1) {
                    String tablejson = item.getString("TableJson");
                    try {
                        tablejson = tablejson.replaceAll("\\\"", "\"");
                        JSONArray resultJson = new JSONArray(tablejson);
                        mPoundsData = new ArrayList<SpinnerData>();
                        mPoundsData.add(new SpinnerData("", "全部"));
                        for (int i = 0; i < resultJson.length(); i++) {
                            SpinnerData tmp = new SpinnerData(((JSONObject) resultJson.get(i)).getString("ID"),
                                    ((JSONObject) resultJson.get(i)).getString("Classname"));
                            mPoundsData.add(tmp);
                        }
                        mDataTypeInfo = new ArrayList<SpinnerData>();
                        mDataTypeInfo.add(new SpinnerData("", "全部"));
                        mDataTypeInfo.add(new SpinnerData("1期", "一期"));
                        mDataTypeInfo.add(new SpinnerData("2期", "二期"));
                    } catch (Exception e) {

                        e.printStackTrace();
                        MyLog.d(TAG, e.toString());
                        return null;
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {

                e.printStackTrace();
                MyLog.d(TAG, e.toString());
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                dialogMine.dismiss();
                BindSpinnerData(mPoundsData, poundsSpinner);
                BindSpinnerData(mDataTypeInfo, datatypeSpinner);
            }
        }
    }


    private List<Map<String, Object>> getYersterdayData(String tablejson) {
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        try {
            tablejson = tablejson.replaceAll("\\\"", "\"");
            JSONArray item = new JSONArray(tablejson);

            for (int i = 0; i < item.length(); i++) {
                JSONObject obj = item.getJSONObject(i);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("Poundid", obj.getString("Poundid"));
                map.put("NumCount", obj.getString("NumCount"));
                map.put("MaoZhong", obj.getString("MaoZhong"));
                map.put("PiZhong", obj.getString("PiZhong"));
                map.put("JingZhong", obj.getString("JingZhong"));
                map.put("Classname", obj.getString("Classname"));
                map.put("Info", obj.getString("info"));
                dataList.add(map);
            }
            return dataList;
        } catch (Exception e) {

            e.printStackTrace();
            MyLog.d(TAG, e.toString());
            return new ArrayList<Map<String, Object>>();
        }
    }


    private void BindSpinnerData(List<SpinnerData> data, Spinner sp) {
        ArrayAdapter<SpinnerData> adapter = new ArrayAdapter<SpinnerData>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(R.layout.drop_down_item);
        sp.setAdapter(adapter);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(SearchActivity.this, IndexActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            SearchActivity.this.finish();
        }
        return false;
    }
}
