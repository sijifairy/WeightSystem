package com.wenzhou.WZWeight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wenzhou.WZWeight.CarActivity.GetCarDetailTask;
import com.wenzhou.WZWeight.CarActivity.GetCarTypeTask;
import com.wenzhou.WZWeight.CarActivity.GetRegionTask;
import com.wenzhou.WZWeight.CarActivity.GetRubbishSourceTask;
import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.application.HttpClientUtil;
import com.wenzhou.WZWeight.log.MyLog;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CarEditActivity extends Activity {
	private static final String TAG = "Activity_caredit";

	ProgressDialog dialogMine;
	JSONObject obj = null;
	JSONObject objCarPara = null;
	private static List<Map<String, Object>> mData = null;
	private static List<SpinnerData> mRegionData = null;
	private static List<SpinnerData> mRubbishSourceData = null;
	private static List<SpinnerData> mCarTypeData = null;
	private static List<SpinnerData> mDepartmentData = null;
	private static List<SpinnerData> mRubbishTypeData = null;
	private static String operatingRegionID = "";
	private static String type = "";
	private static String carid = "";
	private static Boolean isFirst = true;
	private static Boolean firstTriggered = true;
	private static int saveStatus = -1;
	TextView title;
	ImageView back = null;
	EditText platenoInput = null;
	EditText carnoInput = null;
	EditText carweightInput = null;
	EditText carcheckweightInput = null;
	Spinner regionSpinner = null;
	Spinner rubbishsourceSpinner = null;
	Spinner cartypeSpinner = null;
	Spinner departmentSpinner = null;
	Spinner rubbishtypeSpinner = null;
	Button savebtn = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.car_edit);
		Intent intent = getIntent();
		type = intent.getStringExtra("type");
		title = (TextView) findViewById(R.id.tool_textview_top);
		back = (ImageView) findViewById(R.id.linear_imageview_back);
		platenoInput = (EditText) findViewById(R.id.linear_carsearch_PlateNo);
		carnoInput = (EditText) findViewById(R.id.linear_carsearch_CarNo);
		regionSpinner = (Spinner) findViewById(R.id.linear_carsearch_Region);
		rubbishsourceSpinner = (Spinner) findViewById(R.id.linear_carsearch_RubbishSource);
		cartypeSpinner = (Spinner) findViewById(R.id.linear_carsearch_CarType);
		departmentSpinner = (Spinner) findViewById(R.id.linear_carsearch_Department);
		rubbishtypeSpinner = (Spinner) findViewById(R.id.linear_carsearch_RubbishType);
		carweightInput = (EditText) findViewById(R.id.linear_carsearch_CarWeight);
		carcheckweightInput = (EditText) findViewById(R.id.linear_carsearch_CarCheckWeight);
		savebtn = (Button) findViewById(R.id.button_save);

		if (type.equals("edit")) {
			carid = intent.getStringExtra("carid");
			title.setText("�޸ĳ���");
		}


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


		savebtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String plateno = platenoInput.getText().toString();
				String carno = carnoInput.getText().toString();
				String carweight = carweightInput.getText().toString();
				String carcheckweight = carcheckweightInput.getText().toString();
				String rubbishsourceid = ((SpinnerData) rubbishsourceSpinner.getSelectedItem()).getValue();
				String clientid = ((SpinnerData) departmentSpinner.getSelectedItem()).getValue();
				String cartypeid = ((SpinnerData) cartypeSpinner.getSelectedItem()).getValue();
				String rubbishtypeid = ((SpinnerData) rubbishtypeSpinner.getSelectedItem()).getValue();
				if (plateno.equals("") || carno.equals("") || carweight.equals("") || carcheckweight.equals("")) {
					Toast.makeText(CarEditActivity.this, "�뽫������Ϣ��д����", Toast.LENGTH_SHORT).show();
					return;
				}
				objCarPara = new JSONObject();
				try {
					objCarPara.put("SessionID", Constant.getSessionID());
					objCarPara.put("PlateNo", plateno);
					objCarPara.put("CarNo", carno);
					objCarPara.put("ClientID", clientid);
					objCarPara.put("CarTypeID", cartypeid);
					objCarPara.put("RubbishTypeID", rubbishtypeid);
					objCarPara.put("CarWeight", carweight);
					objCarPara.put("CarCheckWeight", carcheckweight);
					objCarPara.put("RubbishSourceID", rubbishsourceid);
					objCarPara.put("CarID", carid);
					MyLog.i(TAG, "objRegionPara" + objCarPara.toString());
				} catch (JSONException e) {
					MyLog.e(TAG, e.getMessage());
				}
				if (type.equals("new")) {
					new NewCarTask().execute(Constant.serverUrl + Constant.insertcar);
				} else if (type.equals("edit")) {
					new EditCarTask().execute(Constant.serverUrl + Constant.editcar);
				}
			}
		});


		regionSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				operatingRegionID = mRegionData.get(arg2).getValue();
				if (!firstTriggered) {
					new GetRubbishSourceTask().execute(Constant.serverUrl + Constant.getrubbishsourceinfo);
				} else {
					if (!isFirst) {
						firstTriggered = false;
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		isFirst = true;
		firstTriggered = true;
		new GetRegionTask().execute(Constant.serverUrl + Constant.getregioninfo);
	}


	class GetCarDetailTask extends AsyncTask<String, Integer, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine.setMessage("���ڻ�ȡ������Ϣ�����Ժ�...");
		}


		@Override
		protected Boolean doInBackground(String... params) {

			try {
				MyLog.d(TAG, "params[0] is :" + params[0]);

				JSONObject objCarPara = new JSONObject();
				try {
					objCarPara.put("SessionID", Constant.getSessionID());
					objCarPara.put("Pager", "1");
					objCarPara.put("CarID", carid);
					MyLog.i(TAG, "objRegionPara" + objCarPara.toString());
				} catch (JSONException e) {
					MyLog.e(TAG, e.getMessage());
				}

				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("para", objCarPara.toString()));
				JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
				int result = item.getInt("Result");
				if (result == 1) {
					mData = getData(item.getString("TableJson"));
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
				if (!mData.isEmpty()) {
					Map<String, Object> objCar = mData.get(0);
					operatingRegionID = objCar.get("RegionID").toString();
				}
				new GetRubbishSourceTask().execute(Constant.serverUrl + Constant.getrubbishsourceinfo);
			} else {

			}
		}
	}


	class NewCarTask extends AsyncTask<String, Integer, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}


		@Override
		protected Boolean doInBackground(String... params) {

			try {
				MyLog.d(TAG, "params[0] is :" + params[0]);

				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("para", objCarPara.toString()));
				JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
				saveStatus = item.getInt("Result");
				if (saveStatus == 1) {
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
				Toast.makeText(CarEditActivity.this, "�½������ɹ���", Toast.LENGTH_SHORT).show();
				try {
					Runtime runtime = Runtime.getRuntime();
					runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					e.printStackTrace();
					MyLog.d(TAG, e.toString());
				}
			} else {
				if (saveStatus == 3) {
					Toast.makeText(CarEditActivity.this, "���ƺ��ظ���", Toast.LENGTH_SHORT).show();
				}
				if (saveStatus == 4) {
					Toast.makeText(CarEditActivity.this, "�Ա���ظ���", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}


	class EditCarTask extends AsyncTask<String, Integer, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}


		@Override
		protected Boolean doInBackground(String... params) {

			try {
				MyLog.d(TAG, "params[0] is :" + params[0]);

				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("para", objCarPara.toString()));
				JSONObject item = new HttpClientUtil(params[0], params1).httpClientCreate();
				saveStatus = item.getInt("Result");
				if (saveStatus == 1) {
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
				Toast.makeText(CarEditActivity.this, "�޸ĳ�����Ϣ�ɹ���", Toast.LENGTH_SHORT).show();
				try {
					Runtime runtime = Runtime.getRuntime();
					runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					e.printStackTrace();
					MyLog.d(TAG, e.toString());
				}
			} else {
				if (saveStatus == 3) {
					Toast.makeText(CarEditActivity.this, "���ƺ��ظ���", Toast.LENGTH_SHORT).show();
				}
				if (saveStatus == 4) {
					Toast.makeText(CarEditActivity.this, "�Ա���ظ���", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}


	class GetRegionTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine = new ProgressDialog(CarEditActivity.this);
			dialogMine.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialogMine.setTitle("��ȴ�...");
			dialogMine.setMessage("���ڻ�ȡ�����б?���Ժ�...");
			dialogMine.setIcon(R.drawable.ic);
			dialogMine.setCancelable(true);
			dialogMine.setIndeterminate(false);
			dialogMine.show();
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
				if (type.equals("edit")) {
					new GetCarDetailTask().execute(Constant.serverUrl + Constant.getcardetail);
				} else {
					new GetRubbishSourceTask().execute(Constant.serverUrl + Constant.getrubbishsourceinfo);
				}
			}
		}
	}


	class GetRubbishSourceTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine.setMessage("���ڻ�ȡ����ͤ�б?���Ժ�...");
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
				if (isFirst) {
					new GetCarTypeTask().execute(Constant.serverUrl + Constant.getcartypeinfo);
					isFirst = false;
				} else {
				}
			}
		}
	}


	class GetCarTypeTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine.setMessage("���ڻ�ȡ�����б?���Ժ�...");
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
			if (result) {
				BindSpinnerData(mCarTypeData, cartypeSpinner);
				new GetDepartmentTask().execute(Constant.serverUrl + Constant.getclientinfo);
			}
		}
	}


	class GetDepartmentTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine.setMessage("���ڻ�ȡ��λ�б?���Ժ�...");
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
						mDepartmentData = new ArrayList<SpinnerData>();
						for (int i = 0; i < resultJson.length(); i++) {
							SpinnerData tmp = new SpinnerData(((JSONObject) resultJson.get(i)).getString("ID"),
									((JSONObject) resultJson.get(i)).getString("ClientName"));
							mDepartmentData.add(tmp);
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
				BindSpinnerData(mDepartmentData, departmentSpinner);
				new GetRubbishTypeTask().execute(Constant.serverUrl + Constant.getrubbishtypeinfo);
			}
		}
	}


	class GetRubbishTypeTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine.setMessage("���ڻ�ȡ���������б?���Ժ�...");
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
						mRubbishTypeData = new ArrayList<SpinnerData>();
						for (int i = 0; i < resultJson.length(); i++) {
							SpinnerData tmp = new SpinnerData(((JSONObject) resultJson.get(i)).getString("ID"),
									((JSONObject) resultJson.get(i)).getString("RubbishTypeName"));
							mRubbishTypeData.add(tmp);
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
				BindSpinnerData(mRubbishTypeData, rubbishtypeSpinner);
				if (type.equals("edit")) {
					if (!mData.isEmpty()) {
						Map<String, Object> objCar = mData.get(0);
						platenoInput.setText(objCar.get("PlateNo").toString());
						carnoInput.setText(objCar.get("CarNo").toString());
						carweightInput.setText(objCar.get("CarWeight").toString());
						carcheckweightInput.setText(objCar.get("CarCheckWeight").toString());
						SelectSpinnerByText(objCar, "RegionName", mRegionData, regionSpinner);
						SelectSpinnerByText(objCar, "RubbishSourceName", mRubbishSourceData, rubbishsourceSpinner);
						SelectSpinnerByText(objCar, "ClientName", mDepartmentData, departmentSpinner);
						SelectSpinnerByText(objCar, "CarTypeName", mCarTypeData, cartypeSpinner);
						SelectSpinnerByText(objCar, "RubbishTypeName", mRubbishTypeData, rubbishtypeSpinner);
					}
				}
				dialogMine.dismiss();
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
				map.put("ID", obj.getString("ID"));
				map.put("PlateNo", obj.getString("PlateNo"));
				map.put("CarID", obj.getString("ID"));
				map.put("CarNo", obj.getString("CarNo"));
				map.put("ClientName", obj.getString("ClientName"));
				map.put("RegionID", obj.getString("RegionID"));
				map.put("RegionName", obj.getString("RegionName"));
				map.put("CarTypeName", obj.getString("CarTypeName"));
				map.put("RubbishSourceName", obj.getString("RubbishSourceName"));
				map.put("RubbishTypeName", obj.getString("RubbishTypeName"));
				map.put("PoundName", obj.getString("PoundName"));
				map.put("CarWeight", obj.getString("CarWeight"));
				map.put("CarCheckWeight", obj.getString("CarCheckWeight"));
				map.put("CardId", obj.getString("CardId"));
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


	private void BindSpinnerData(List<SpinnerData> data, Spinner sp) {
		ArrayAdapter<SpinnerData> adapter = new ArrayAdapter<SpinnerData>(this, android.R.layout.simple_spinner_item, data);
		adapter.setDropDownViewResource(R.layout.drop_down_item);
		sp.setAdapter(adapter);
	}


	private void SelectSpinnerByText(Map<String, Object> obj, String field, List<SpinnerData> spinnerData, Spinner spinner) {
		if (obj.get(field).toString() != "") {
			for (int i = 0; i < spinnerData.size(); i++) {
				if (spinnerData.get(i).getText().equals(obj.get(field).toString())) {
					spinner.setSelection(i);
				}
			}
		}
	}
}
