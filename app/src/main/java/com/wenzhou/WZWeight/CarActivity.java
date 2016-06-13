package com.wenzhou.WZWeight;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ContextMenu.ContextMenuInfo;

import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.application.HttpClientUtil;
import com.wenzhou.WZWeight.log.MyLog;

public class CarActivity extends Activity {
	private static final String TAG = "Activity_car";

	ImageView back;
	ImageView search;
	ListView carListView;
	Button firstpage = null;
	Button lastpage = null;
	Button prevpage = null;
	Button nextpage = null;
	TextView currentpage = null;
	ProgressDialog dialogMine;
	JSONObject obj = null;
	Handler refreshCarListHandler = null;
	Handler showCarDetailHandler = null;
	Runnable refreshCarListRunnable = null;
	Runnable showCarDetailRunnable = null;

	private static List<Map<String, Object>> mData = null;
	private static List<Map<String, Object>> mDataCurrent = null;
	private static List<SpinnerData> mRegionData = null;
	private static List<SpinnerData> mRubbishSourceData = null;
	private static List<SpinnerData> mCarTypeData = null;
	private static int mPagerCount = 0;
	private static int mPagerCurrent = 0;
	private static String operatingCarID = "";
	private static String operatingRegionID = "";
	private static Spinner operatingRubbishSourceSpinner = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.car);


		refreshCarListHandler = new Handler();
		refreshCarListRunnable = new Runnable() {
			public void run() {
				refreshCarListUI();
			}
		};

		showCarDetailHandler = new Handler();
		showCarDetailRunnable = new Runnable() {
			public void run() {
				showCarDetailUI();
			}
		};

		back = (ImageView) findViewById(R.id.linear_imageview_back);
		search = (ImageView) findViewById(R.id.linear_imageview_search);
		carListView = (ListView) findViewById(R.id.lv);
		firstpage = (Button) findViewById(R.id.button_StartPage);
		prevpage = (Button) findViewById(R.id.button_PrevPage);
		nextpage = (Button) findViewById(R.id.button_NextPage);
		lastpage = (Button) findViewById(R.id.button_LastPage);
		currentpage = (TextView) findViewById(R.id.currentPage);

		getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_plateno", "").commit();
		getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_carno", "").commit();
		getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_cardid", "").commit();
		getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_region", "").commit();
		getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_rubbishsource", "").commit();
		getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_cartype", "").commit();


		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(CarActivity.this, IndexActivity.class));
				overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
				CarActivity.this.finish();
			}
		});

		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSearchDialogUI(CarActivity.this);
			}
		});

		firstpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(1);
				new GetCarDetailTask().execute(Constant.getcardetail);
			}
		});

		prevpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(mPagerCurrent - 1);
				new GetCarDetailTask().execute(Constant.getcardetail);
			}
		});

		nextpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(mPagerCurrent + 1);
				new GetCarDetailTask().execute(Constant.getcardetail);
			}
		});

		lastpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(mPagerCount);
				new GetCarDetailTask().execute(Constant.getcardetail);
			}
		});

		carListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mDataCurrent = new ArrayList<Map<String, Object>>();
				Map<String, Object> current = mData.get(position);
				String[] labels = { "车牌号", "自编号", "车型", "皮重(吨)", "载重(吨)", "垃圾亭", "区域", "载质类型", "适用地磅站", "卡号" };
				String[] contents = { "PlateNo", "CarNo", "CarTypeName", "CarWeight", "CarCheckWeight", "RubbishSourceName", "RegionName", "RubbishTypeName",
						"PoundName", "CardNumber" };

				for (int i = 0; i < labels.length; i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("Label", labels[i]);
					map.put("Content", current.get(contents[i]));
					mDataCurrent.add(map);
				}
				showCarDetailHandler.post(showCarDetailRunnable);
			}
		});

		if (getSharedPreferences("constant", Context.MODE_PRIVATE).getString("userClass", "").equals("1")) {
			carListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.add(0, 0, 0, "新建车辆");
					menu.add(0, 1, 0, "修改车辆");
					menu.add(0, 2, 0, "删除车辆");
				}
			});
		}

		objFilter(1);
		new GetCarDetailTask().execute(Constant.getcardetail);
		new GetRegionTask().execute(Constant.getregioninfo);
		new GetRubbishSourceTask().execute(Constant.getrubbishsourceinfo);
		new GetCarTypeTask().execute(Constant.getcartypeinfo);
	}


	public void objFilter(int Pager) {
		obj = new JSONObject();
		try {
			obj.put("SessionID", Constant.getSessionID());
			obj.put("Pager", Pager);
			String plateno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_plateno", "");
			String carno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_carno", "");
			String cardid = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_cardid", "");
			String region = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_region", "");
			String rubbishsource = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_rubbishsource", "");
			String cartype = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_cartype", "");
			if (plateno != "") {
				obj.put("PlateNo", plateno);
			}
			if (carno != "") {
				obj.put("CarNo", carno);
			}
			if (cardid != "") {
				obj.put("CardID", cardid);
			}
			if (region != "") {
				obj.put("RegionID", region);
			}
			if (rubbishsource != "") {
				obj.put("RubbishSourceID", rubbishsource);
			}
			if (cartype != "") {
				obj.put("CarTypeID", cartype);
			}


			MyLog.i(TAG, "obj" + obj.toString());
		} catch (JSONException e) {
			MyLog.e(TAG, e.getMessage());
		}
	}


	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		String carid = mData.get((int) info.id).get("CarID").toString();
		switch (item.getItemId()) {
		case 0:

			Intent intent = new Intent(CarActivity.this, CarEditActivity.class);
			Bundle data = new Bundle();
			data.putString("type", "new");
			intent.putExtras(data);
			startActivity(intent);
			break;
		case 1:

			Intent intent1 = new Intent(CarActivity.this, CarEditActivity.class);
			Bundle data1 = new Bundle();
			data1.putString("type", "edit");
			data1.putString("carid", carid);
			intent1.putExtras(data1);
			startActivity(intent1);
			break;
		case 2:

			if (carid != "") {
				operatingCarID = carid;
				AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("详细信息").setMessage("确定删除该车辆?");
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						new DeleteCarTask().execute(Constant.deletecar);
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
				builder.create().show();
			}
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}


	class GetCarDetailTask extends AsyncTask<String, Integer, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine = new ProgressDialog(CarActivity.this);
			dialogMine.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialogMine.setTitle("请等待...");
			dialogMine.setMessage("正在获取车辆列表，请稍后...");
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
					} else {
						mPagerCount = 1;
						mPagerCurrent = 1;
					}
					mData = getData(item.getString("TableJson"));
					refreshCarListHandler.post(refreshCarListRunnable);
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


	class GetRegionTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
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
		}
	}


	class GetRubbishSourceTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
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
				if (operatingRubbishSourceSpinner != null) {
					BindSpinnerData(mRubbishSourceData, operatingRubbishSourceSpinner);
					String rubbishsource = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_rubbishsource", "");
					if (rubbishsource != "") {
						for (int i = 0; i < mRubbishSourceData.size(); i++) {
							if (rubbishsource.equals(mRubbishSourceData.get(i).getValue())) {
								operatingRubbishSourceSpinner.setSelection(i);
							}
						}
					}
				}
			}
		}
	}


	class GetCarTypeTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
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
						mCarTypeData = new ArrayList<SpinnerData>();
						mCarTypeData.add(new SpinnerData("", "全部"));
						for (int i = 0; i < resultJson.length(); i++) {
							SpinnerData tmp = new SpinnerData(((JSONObject) resultJson.get(i)).getString("ID"),
									((JSONObject) resultJson.get(i)).getString("CarTypeName"));
							mCarTypeData.add(tmp);
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
		}
	}


	class DeleteCarTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}


		@Override
		protected Boolean doInBackground(String... params) {
			try {
				MyLog.d(TAG, "params[0] is :" + params[0]);

				JSONObject objRegionPara = new JSONObject();
				try {
					objRegionPara.put("SessionID", Constant.getSessionID());
					objRegionPara.put("CarID", operatingCarID);
				} catch (JSONException e) {
					MyLog.e(TAG, e.getMessage());
				}
				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("para", objRegionPara.toString()));
				JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
				int result = item.getInt("Result");
				if (result == 1) {
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
				Toast.makeText(CarActivity.this, "删除车辆成功！", Toast.LENGTH_SHORT).show();
				new GetCarDetailTask().execute(Constant.getcardetail);
			}
		}
	}


	private void refreshCarListUI() {
		SimpleAdapter mSimpleAdapter = new SimpleAdapter(CarActivity.this, mData, R.layout.car_list_item, new String[] { "No", "PlateNo", "CarNo",
				"RubbishSourceName" }, new int[] { R.id.linear_item_No, R.id.linear_item_PlateNo, R.id.linear_item_CarTypeName,
				R.id.linear_item_RubbishSourceName });
		carListView.setAdapter(mSimpleAdapter);
		currentpage.setText(mPagerCurrent + "/" + mPagerCount);

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
		SimpleAdapter showCarDetailAdapter = new SimpleAdapter(CarActivity.this, mDataCurrent, R.layout.car_list_item_current, new String[] { "Label",
				"Content" }, new int[] { R.id.linear_item_Label, R.id.linear_item_Content });
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("详细信息").setAdapter(showCarDetailAdapter, null);
		builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		builder.create().show();
	}


	private void showSearchDialogUI(Context context) {
		LayoutInflater inflater = LayoutInflater.from(this);
		final View carSearchView = inflater.inflate(R.layout.car_search, null);
		final EditText platenoInput = (EditText) carSearchView.findViewById(R.id.linear_carsearch_PlateNo);
		final EditText carnoInput = (EditText) carSearchView.findViewById(R.id.linear_carsearch_CarNo);
		final Spinner regionSpinner = (Spinner) carSearchView.findViewById(R.id.linear_carsearch_Region);
		final Spinner rubbishsourceSpinner = (Spinner) carSearchView.findViewById(R.id.linear_carsearch_RubbishSource);
		operatingRubbishSourceSpinner = rubbishsourceSpinner;
		final Spinner cartypeSpinner = (Spinner) carSearchView.findViewById(R.id.linear_carsearch_CarType);
		final EditText cardidInput = (EditText) carSearchView.findViewById(R.id.linear_carsearch_CardID);
		BindSpinnerData(mRegionData, regionSpinner);
		BindSpinnerData(mRubbishSourceData, rubbishsourceSpinner);
		BindSpinnerData(mCarTypeData, cartypeSpinner);

		regionSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				operatingRegionID = mRegionData.get(arg2).getValue();
				new GetRubbishSourceTask().execute(Constant.getrubbishsourceinfo);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		String plateno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_plateno", "");
		String carno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_carno", "");
		String cardid = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_cardid", "");
		String region = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_region", "");
		String rubbishsource = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_rubbishsource", "");
		String cartype = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("carfilter_cartype", "");

		platenoInput.setText(plateno);
		carnoInput.setText(carno);
		cardidInput.setText(cardid);

		if (region != "") {
			for (int i = 0; i < mRegionData.size(); i++) {
				if (mRegionData.get(i).getValue() == region) {
					regionSpinner.setSelection(i);
				}
			}
		}
		if (rubbishsource != "") {
			for (int i = 0; i < mRubbishSourceData.size(); i++) {
				if (mRubbishSourceData.get(i).getValue() == rubbishsource) {
					rubbishsourceSpinner.setSelection(i);
				}
			}
		}
		if (cartype != "") {
			for (int i = 0; i < mCarTypeData.size(); i++) {
				if (mCarTypeData.get(i).getValue() == cartype) {
					cartypeSpinner.setSelection(i);
				}
			}
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setTitle("车辆查询");
		builder.setView(carSearchView);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_plateno", platenoInput.getText().toString()).commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_carno", carnoInput.getText().toString()).commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_cardid", cardidInput.getText().toString()).commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit()
							.putString("carfilter_region", ((SpinnerData) regionSpinner.getSelectedItem()).getValue()).commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit()
							.putString("carfilter_rubbishsource", ((SpinnerData) rubbishsourceSpinner.getSelectedItem()).getValue()).commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit()
							.putString("carfilter_cartype", ((SpinnerData) cartypeSpinner.getSelectedItem()).getValue()).commit();

					objFilter(1);
					new GetCarDetailTask().execute(Constant.getcardetail);
					Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
					field.setAccessible(true);
					field.set(dialog, true);
				} catch (Exception e) {
				}
			}
		});
		builder.setNeutralButton("清空", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_plateno", "").commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_carno", "").commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_cardid", "").commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_region", "").commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_rubbishsource", "").commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("carfilter_cartype", "").commit();
					platenoInput.setText("");
					carnoInput.setText("");
					cardidInput.setText("");
					regionSpinner.setSelection(0);
					rubbishsourceSpinner.setSelection(0);
					cartypeSpinner.setSelection(0);
					Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
					field.setAccessible(true);
					field.set(dialog, false);
				} catch (Exception e) {
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
					field.setAccessible(true);
					field.set(dialog, true);
				} catch (Exception e) {
				}
			}
		});
		builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
					dialog.dismiss();
				}
				return false;
			}
		});
		builder.create().show();
	}


	private void BindSpinnerData(List<SpinnerData> data, Spinner sp) {
		ArrayAdapter<SpinnerData> adapter = new ArrayAdapter<SpinnerData>(this, android.R.layout.simple_spinner_item, data);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp.setAdapter(adapter);
	}


	private List<Map<String, Object>> getData(String tablejson) {
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		try {
			tablejson = tablejson.replaceAll("\\\"", "\"");
			JSONArray item = new JSONArray(tablejson);

			for (int i = 0; i < item.length(); i++) {
				JSONObject obj = item.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("ID", obj.getString("ID"));
				map.put("No", i + (mPagerCurrent - 1) * 10 + 1);
				map.put("PlateNo", obj.getString("PlateNo"));
				map.put("CarID", obj.getString("ID"));
				map.put("CarNo", obj.getString("CarNo"));
				map.put("ClientName", obj.getString("ClientName"));
				map.put("CarTypeName", obj.getString("CarTypeName"));
				map.put("RubbishSourceName", obj.getString("RubbishSourceName"));
				map.put("RegionName", obj.getString("RegionName"));
				map.put("RubbishTypeName", obj.getString("RubbishTypeName"));
				map.put("PoundName", obj.getString("PoundName"));
				map.put("CarWeight", obj.getString("CarWeight"));
				map.put("CarCheckWeight", obj.getString("CarCheckWeight"));
				map.put("CardId", obj.getString("CardId"));
				map.put("CardNumber", obj.getString("CardNumber"));
				map.put("BuyTime", obj.getString("BuyTime"));
				dataList.add(map);
			}
			return dataList;
		} catch (Exception e) {

			e.printStackTrace();
			MyLog.d(TAG, e.toString());
			return new ArrayList<Map<String, Object>>();
		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startActivity(new Intent(CarActivity.this, IndexActivity.class));
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			CarActivity.this.finish();
		}
		return false;
	}
}
