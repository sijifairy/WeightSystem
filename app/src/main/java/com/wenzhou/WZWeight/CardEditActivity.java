package com.wenzhou.WZWeight;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wenzhou.WZWeight.CarEditActivity.GetCarDetailTask;
import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.application.HttpClientUtil;
import com.wenzhou.WZWeight.log.MyLog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CardEditActivity extends Activity {
	private static final String TAG = "Activity_caredit";

	ProgressDialog dialogMine;
	JSONObject obj = null;
	JSONObject objCardPara = null;
	private static List<Map<String, Object>> mData = null;
	private static List<SpinnerData> mPlateNoData = null;
	private static List<SpinnerData> mPoundsData = null;
	private static List<CheckBox> checkboxPounds=null;
	private static String type = "";
	private static String cardid = "";
	private static Boolean isFirst = true;
	private static int saveStatus = -1;
	TextView title;
	ViewGroup pounds_group;
	ImageView back = null;
	EditText cardnoInput = null;
	Spinner platenoSpinner = null;
	Button savebtn = null;
	Button button_dateend;
	TextView edit_dateend;
	Calendar nowdate;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.card_edit);
		Intent intent = getIntent();
		type = intent.getStringExtra("type");
		title = (TextView) findViewById(R.id.tool_textview_top);
		back = (ImageView) findViewById(R.id.linear_imageview_back);
		button_dateend = (Button) findViewById(R.id.button_dateend);
		cardnoInput = (EditText) findViewById(R.id.linear_carsearch_CardNo);
		edit_dateend = (TextView) findViewById(R.id.linear_carsearch_DateEnd);
		platenoSpinner = (Spinner) findViewById(R.id.linear_carsearch_PlateNo);
		savebtn = (Button) findViewById(R.id.button_save);
		pounds_group = (ViewGroup) findViewById(R.id.linear_pounds_group);
		nowdate = Calendar.getInstance();
		checkboxPounds = new ArrayList <CheckBox>();
		if (type.equals("edit")) {
			cardid = intent.getStringExtra("cardid");
			title.setText("�޸�IC��");
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
				String cardno = cardnoInput.getText().toString();
				String dateend = edit_dateend.getText().toString();
				String carid = ((SpinnerData) platenoSpinner.getSelectedItem()).getValue();
				if (carid.equals("") || cardno.equals("") || dateend.equals("")) {
					Toast.makeText(CardEditActivity.this, "�뽫IC����Ϣ��д����", Toast.LENGTH_SHORT).show();
					return;
				}
				String poundids = "";
				for(int  i=0;i<checkboxPounds.size();i++){
					if(checkboxPounds.get(i).isChecked()){
						if(poundids.equals("")){
							poundids+=((SpinnerData)checkboxPounds.get(i).getTag()).getValue();
						}else
						{
							poundids+=(","+((SpinnerData)checkboxPounds.get(i).getTag()).getValue());
						}
					}
				}
				objCardPara = new JSONObject();
				try {
					objCardPara.put("SessionID", Constant.getSessionID());
					objCardPara.put("CarID", carid);
					objCardPara.put("CardID", cardid);
					objCardPara.put("CardNo", cardno);
					objCardPara.put("CardTimeEnd", dateend);
					objCardPara.put("PoundIDs", poundids);
					MyLog.i(TAG, "objRegionPara" + objCardPara.toString());
				} catch (JSONException e) {
					MyLog.e(TAG, e.getMessage());
				}
				if (type.equals("new")) {
					new NewCardTask().execute(Constant.insertcard);
				} else if (type.equals("edit")) {
					new EditCardTask().execute(Constant.editcard);
				}
			}
		});


		button_dateend.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				new DatePickerDialog(CardEditActivity.this, new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

						monthOfYear = monthOfYear + 1;
						edit_dateend.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
						MyLog.d(TAG, year + "-" + monthOfYear + "-" + dayOfMonth);
					}
				}, nowdate.get(Calendar.YEAR) + 1, nowdate.get(Calendar.MONTH), nowdate.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
		isFirst = true;
		new GetPoundsTask().execute(Constant.getpoundinfo);
	}


	class GetPoundsTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine = new ProgressDialog(CardEditActivity.this);
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
						mPoundsData = new ArrayList<SpinnerData>();
						for (int i = 0; i < resultJson.length(); i++) {
							SpinnerData tmp = new SpinnerData(((JSONObject) resultJson.get(i)).getString("ID"),
									((JSONObject) resultJson.get(i)).getString("Classname"));
							mPoundsData.add(tmp);
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
				for (int i = 0; i < mPoundsData.size(); i++) {
					SpinnerData poundinfo = mPoundsData.get(i);
					CheckBox ck = new CheckBox(CardEditActivity.this);
					ck.setId(Integer.parseInt(poundinfo.getValue()));
					ck.setText(poundinfo.getText());
					ck.setTag(poundinfo);
					pounds_group.addView(ck);
					checkboxPounds.add(ck);
				}
				if (type.equals("edit")) {


				}
				new GetNoCardCarTask().execute(Constant.getnocardcar);
			}
		}

	}


	class GetNoCardCarTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine.setMessage("���ڻ�ȡδ�ɿ������б?���Ժ�...");
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
						mPlateNoData = new ArrayList<SpinnerData>();
						for (int i = 0; i < resultJson.length(); i++) {
							SpinnerData tmp = new SpinnerData(((JSONObject) resultJson.get(i)).getString("ID"),
									((JSONObject) resultJson.get(i)).getString("PlateNo"));
							mPlateNoData.add(tmp);
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
				BindSpinnerData(mPlateNoData, platenoSpinner);
				if (type.equals("new")) {
					dialogMine.hide();
				} else if (type.equals("edit")) {
					new GetCardDetailTask().execute(Constant.getcarddetail);
				}
			}
		}
	}


	class GetCardDetailTask extends AsyncTask<String, Integer, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine.setMessage("���ڻ�ȡIC����Ϣ�����Ժ�...");
		}


		@Override
		protected Boolean doInBackground(String... params) {

			try {
				MyLog.d(TAG, "params[0] is :" + params[0]);

				JSONObject objCardPara = new JSONObject();
				try {
					objCardPara.put("SessionID", Constant.getSessionID());
					objCardPara.put("Pager", "1");
					objCardPara.put("CardID", cardid);
					MyLog.i(TAG, "objRegionPara" + objCardPara.toString());
				} catch (JSONException e) {
					MyLog.e(TAG, e.getMessage());
				}

				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("para", objCardPara.toString()));
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
					Map<String, Object> objCard = mData.get(0);
					if (!objCard.get("PlateNo").toString().equals("")) {
						SpinnerData tmp = new SpinnerData(objCard.get("CarID").toString(), objCard.get("PlateNo").toString());
						mPlateNoData.add(tmp);
						BindSpinnerData(mPlateNoData, platenoSpinner);
						SelectSpinnerByText(objCard, "PlateNo", mPlateNoData, platenoSpinner);
					}
					String poundnames = objCard.get("PoundName").toString();
					if(!poundnames.equals("")){
						for(int i=0;i<checkboxPounds.size();i++){
							if(poundnames.contains(checkboxPounds.get(i).getText())){
								checkboxPounds.get(i).setChecked(true);
							}
						}
					}
					cardnoInput.setText(objCard.get("CardNo").toString());
					edit_dateend.setText(objCard.get("CardTimeEnd").toString());
				}
				dialogMine.dismiss();
			} else {

			}
		}
	}


	class NewCardTask extends AsyncTask<String, Integer, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}


		@Override
		protected Boolean doInBackground(String... params) {

			try {
				MyLog.d(TAG, "params[0] is :" + params[0]);

				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("para", objCardPara.toString()));
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
				Toast.makeText(CardEditActivity.this, "�½�IC���ɹ���", Toast.LENGTH_SHORT).show();
				try {
					Runtime runtime = Runtime.getRuntime();
					runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					e.printStackTrace();
					MyLog.d(TAG, e.toString());
				}
			} else {
				if (saveStatus == 3) {
					Toast.makeText(CardEditActivity.this, "�����ظ���", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}


	class EditCardTask extends AsyncTask<String, Integer, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}


		@Override
		protected Boolean doInBackground(String... params) {

			try {
				MyLog.d(TAG, "params[0] is :" + params[0]);

				List<NameValuePair> params1 = new ArrayList<NameValuePair>();
				params1.add(new BasicNameValuePair("para", objCardPara.toString()));
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
				Toast.makeText(CardEditActivity.this, "�޸�IC����Ϣ�ɹ���", Toast.LENGTH_SHORT).show();
				try {
					Runtime runtime = Runtime.getRuntime();
					runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					e.printStackTrace();
					MyLog.d(TAG, e.toString());
				}
			} else {
				if (saveStatus == 3) {
					Toast.makeText(CardEditActivity.this, "�����ظ���", Toast.LENGTH_SHORT).show();
				}
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
				map.put("ID", obj.getString("id"));
				map.put("CarID", obj.getString("CarID"));
				map.put("PlateNo", obj.getString("PlateNo"));
				map.put("CardNo", obj.getString("CardNo"));
				map.put("CardTimeEnd", obj.getString("CardTimeEnd"));
				map.put("PoundName", obj.getString("PoundName"));
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
