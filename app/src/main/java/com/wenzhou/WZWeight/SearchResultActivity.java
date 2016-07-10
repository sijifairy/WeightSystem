package com.wenzhou.WZWeight;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.application.HttpClientUtil;
import com.wenzhou.WZWeight.log.MyLog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultActivity extends Activity {
	private static final String TAG = "Activity_searchresult";

	Button firstpage = null;
	Button lastpage = null;
	Button prevpage = null;
	Button nextpage = null;
	TextView currentpage = null;
	TextView statistic = null;
	ProgressDialog dialogMine;
	JSONObject obj = null;
	ListView weightListView;

	Handler refreshWeightListHandler = null;
	Handler showWeightDetailHandler = null;
	Runnable refreshWeightListRunnable = null;
	Runnable showWeightDetailRunnable = null;
	private static List<Map<String, Object>> mData = null;
	private static List<Map<String, Object>> mDataCurrent = null;
	private static int mPagerCount = 0;
	private static int mPagerCurrent = 0;
	private static int mCarCount = 0;
	private static double mAllNet = 0;
	private static String operatingCarID = "";
	private static String plateno;
	private static String carno;
	private static String datestart;
	private static String dateend;
	private static String regionid;
	private static String rubbishsourceid;
	private static String poundid;
	private static String datatypeinfo;
	ImageView back = null;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_result);

		Intent intent = getIntent();
		plateno = intent.getStringExtra("plateno");
		carno = intent.getStringExtra("carno");
		datestart = intent.getStringExtra("datestart");
		dateend = intent.getStringExtra("dateend");
		regionid = intent.getStringExtra("regionid");
		rubbishsourceid = intent.getStringExtra("rubbishsourceid");
		poundid = intent.getStringExtra("poundid");
		datatypeinfo = intent.getStringExtra("datatypeinfo");

		weightListView = (ListView) findViewById(R.id.lv);
		firstpage = (Button) findViewById(R.id.button_StartPage);
		prevpage = (Button) findViewById(R.id.button_PrevPage);
		nextpage = (Button) findViewById(R.id.button_NextPage);
		lastpage = (Button) findViewById(R.id.button_LastPage);
		currentpage = (TextView) findViewById(R.id.currentPage);
		statistic = (TextView) findViewById(R.id.statistic);
		back = (ImageView) findViewById(R.id.linear_imageview_back);


		refreshWeightListHandler = new Handler();
		refreshWeightListRunnable = new Runnable() {
			public void run() {
				refreshCarListUI();
			}
		};

		showWeightDetailHandler = new Handler();
		showWeightDetailRunnable = new Runnable() {
			public void run() {
				showCarDetailUI();
			}
		};

		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Runtime runtime = Runtime.getRuntime();
					runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					e.printStackTrace();
					MyLog.d(TAG, e.toString());
				}
			}
		});

		firstpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(1);
				new GetWeightDetailTask().execute(Constant.serverUrl + Constant.getweightdetail);
			}
		});

		prevpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(mPagerCurrent - 1);
				new GetWeightDetailTask().execute(Constant.serverUrl + Constant.getweightdetail);
			}
		});

		nextpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(mPagerCurrent + 1);
				new GetWeightDetailTask().execute(Constant.serverUrl + Constant.getweightdetail);
			}
		});

		lastpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(mPagerCount);
				new GetWeightDetailTask().execute(Constant.serverUrl + Constant.getweightdetail);
			}
		});

		weightListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mDataCurrent = new ArrayList<Map<String, Object>>();
				Map<String, Object> current = mData.get(position);
				String[] labels = { "车牌号", "车辆编号", "垃圾亭", "区域", "称重时间", "毛重", "皮重", "净重" };
				String[] contents = { "PlateNo", "CarNo", "RubbishSourceName", "Region", "TimeWeight", "AllWeight", "Weightleave", "JingZhong" };

				for (int i = 0; i < labels.length; i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("Label", labels[i]);
					map.put("Content", current.get(contents[i]));
					mDataCurrent.add(map);
				}
				showWeightDetailHandler.post(showWeightDetailRunnable);
			}
		});

		objFilter(1);
		new GetWeightDetailTask().execute(Constant.serverUrl + Constant.getweightdetail);
	}


	public void objFilter(int Pager) {
		obj = new JSONObject();
		try {
			obj.put("SessionID", Constant.getSessionID());
			obj.put("Pager", Pager);
			if (plateno != "") {
				obj.put("PlateNo", plateno);
			}
			if (carno != "") {
				obj.put("CarNo", carno);
			}
			if (datestart != "") {
				obj.put("DateStart", datestart);
			}
			if (dateend != "") {
				obj.put("DateEnd", dateend);
			}
			if (regionid != "") {
				obj.put("RegionID", regionid);
			}
			if (rubbishsourceid != "") {
				obj.put("RubbishSourceID", rubbishsourceid);
			}
			if (poundid != "") {
				obj.put("PoundID", poundid);
			}
			if (datatypeinfo != "") {
				obj.put("Info", datatypeinfo);
			}
			MyLog.i(TAG, "obj" + obj.toString());
		} catch (JSONException e) {
			MyLog.e(TAG, e.getMessage());
		}
	}


	class GetWeightDetailTask extends AsyncTask<String, Integer, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine = new ProgressDialog(SearchResultActivity.this);
			dialogMine.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialogMine.setTitle("请等待...");
			dialogMine.setMessage("正在获取称重信息列表，请稍后...");
			dialogMine.setIcon(R.drawable.ic);
			dialogMine.setCancelable(true);
			dialogMine.setIndeterminate(false);
			dialogMine.show();
		}


		@Override
		protected Boolean doInBackground(String... params) {

			try {
				MyLog.d(TAG, "params[0] is :" + params[0]);

				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("para", obj.toString()));
				JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
				int result = item.getInt("Result");
				if (result == 1) {
					if (item.getInt("DataSize") != 0) {
						mPagerCount = Integer.parseInt(item.getString("PagerCount").substring(0, item.getString("PagerCount").indexOf(".")));
						mPagerCurrent = item.getInt("PagerCurrent");
						mCarCount = item.getInt("CarCount");
						mAllNet = item.getDouble("AllNet");
					} else {
						mPagerCount = 1;
						mPagerCurrent = 1;
					}
					mData = getData(item.getString("TableJson"));
					refreshWeightListHandler.post(refreshWeightListRunnable);
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
				dialogMine.dismiss();
			} else {

			}
		}
	}


	private List<Map<String, Object>> getData(String tablejson) {
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		try {
			tablejson = tablejson.replaceAll("\\\"", "\"");
			JSONArray item = new JSONArray(tablejson);

			for (int i = 0; i < item.length(); i++) {
				JSONObject obj = item.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("No", i + (mPagerCurrent - 1) * 10 + 1);
				map.put("PlateNo", obj.getString("PlateNo"));
				map.put("CarNo", obj.getString("CarNo"));
				map.put("Region", obj.getString("Region"));
				map.put("CarID", obj.getString("CarID"));
				map.put("AllWeight", Double.parseDouble(obj.getString("AllWeight")) / 1000);
				map.put("RubbishSourceName", obj.getString("RubbishSourceName"));
				map.put("Weightleave", Double.parseDouble(obj.getString("Weightleave")) / 1000);
				map.put("TimeWeight", obj.getString("TimeWeight"));
				map.put("TimeLeave", obj.getString("TimeLeave"));
				map.put("JingZhong", (Double.parseDouble(obj.getString("AllWeight")) - Double.parseDouble(obj.getString("Weightleave"))) / 1000);
				dataList.add(map);
			}
			return dataList;
		} catch (Exception e) {

			e.printStackTrace();
			MyLog.d(TAG, e.toString());
			return new ArrayList<Map<String, Object>>();
		}
	}


	private void refreshCarListUI() {
		SimpleAdapter mSimpleAdapter = new SimpleAdapter(SearchResultActivity.this, mData, R.layout.search_result_list_item, new String[] { "No", "PlateNo",
				"TimeWeight", "RubbishSourceName", "JingZhong" }, new int[] { R.id.linear_item_No, R.id.linear_item_PlateNo, R.id.linear_item_WeightTime,
				R.id.linear_item_RubbishSourceName, R.id.linear_item_JingZhong });
		weightListView.setAdapter(mSimpleAdapter);
		currentpage.setText(mPagerCurrent + "/" + mPagerCount);
		statistic.setText("\t共\t" + mCarCount + "车次,\t\t" + mAllNet + "吨.");

		prevpage.setEnabled(true);
		firstpage.setEnabled(true);
		nextpage.setEnabled(true);
		lastpage.setEnabled(true);

		if (mPagerCurrent == 1) {
			prevpage.setEnabled(false);
			firstpage.setEnabled(false);
		}
		if (mPagerCurrent == mPagerCount) {
			nextpage.setEnabled(false);
			lastpage.setEnabled(false);
		}
	}


	private void showCarDetailUI() {
		SimpleAdapter showCarDetailAdapter = new SimpleAdapter(SearchResultActivity.this, mDataCurrent, R.layout.car_list_item_current, new String[] { "Label",
				"Content" }, new int[] { R.id.linear_item_Label, R.id.linear_item_Content });
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("详细信息").setAdapter(showCarDetailAdapter, null);
		builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		builder.create().show();
	}

}
