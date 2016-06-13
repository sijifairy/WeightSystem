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

import com.wenzhou.WZWeight.CarActivity.DeleteCarTask;
import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.application.HttpClientUtil;
import com.wenzhou.WZWeight.log.MyLog;

public class CardActivity extends Activity {
	private static final String TAG = "Activity_car";

	ImageView back;
	ImageView search;
	ListView cardListView;
	Button firstpage = null;
	Button lastpage = null;
	Button prevpage = null;
	Button nextpage = null;
	TextView currentpage = null;
	ProgressDialog dialogMine;
	JSONObject obj = null;
	Handler refreshCardListHandler = null;
	Handler showCardDetailHandler = null;
	Runnable refreshCardListRunnable = null;
	Runnable showCardDetailRunnable = null;

	private static List<Map<String, Object>> mData = null;
	private static List<Map<String, Object>> mDataCurrent = null;
	private static List<SpinnerData> mRegionData = null;
	private static int mPagerCount = 0;
	private static int mPagerCurrent = 0;
	private static String operatingCardID = "";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.card);


		refreshCardListHandler = new Handler();
		refreshCardListRunnable = new Runnable() {
			public void run() {
				refreshCarListUI();
			}
		};

		showCardDetailHandler = new Handler();
		showCardDetailRunnable = new Runnable() {
			public void run() {
				showCarDetailUI();
			}
		};
		search = (ImageView) findViewById(R.id.linear_imageview_search);
		cardListView = (ListView) findViewById(R.id.lv);
		firstpage = (Button) findViewById(R.id.button_StartPage);
		prevpage = (Button) findViewById(R.id.button_PrevPage);
		nextpage = (Button) findViewById(R.id.button_NextPage);
		lastpage = (Button) findViewById(R.id.button_LastPage);
		currentpage = (TextView) findViewById(R.id.currentPage);
		back = (ImageView) findViewById(R.id.linear_imageview_back);

		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSearchDialogUI(CardActivity.this);
			}
		});

		firstpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(1);
				new GetCardDetailTask().execute(Constant.serverUrl + Constant.getcarddetail);
			}
		});

		prevpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(mPagerCurrent - 1);
				new GetCardDetailTask().execute(Constant.serverUrl + Constant.getcarddetail);
			}
		});

		nextpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(mPagerCurrent + 1);
				new GetCardDetailTask().execute(Constant.serverUrl + Constant.getcarddetail);
			}
		});

		lastpage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				objFilter(mPagerCount);
				new GetCardDetailTask().execute(Constant.serverUrl + Constant.getcarddetail);
			}
		});

		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(CardActivity.this, IndexActivity.class));
				overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
				CardActivity.this.finish();
			}
		});


		cardListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mDataCurrent = new ArrayList<Map<String, Object>>();
				Map<String, Object> current = mData.get(position);
				String[] labels = { "IC������", "���ƺ�", "�Ա��", "���õذ�վ", "IC����Ч��" };
				String[] contents = { "CardNo", "PlateNo", "CarNo", "PoundName", "CardTimeEnd" };

				for (int i = 0; i < labels.length; i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("Label", labels[i]);
					map.put("Content", current.get(contents[i]));
					mDataCurrent.add(map);
				}
				showCardDetailHandler.post(showCardDetailRunnable);
			}
		});

		if (getSharedPreferences("constant", Context.MODE_PRIVATE).getString("userClass", "").equals("1")) {
			cardListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.add(0, 0, 0, "�½�IC��");
					menu.add(0, 1, 0, "�޸�IC��");
					menu.add(0, 2, 0, "ɾ��IC��");
				}
			});
		}

		getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("cardfilter_plateno", "").commit();
		getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("cardfilter_carno", "").commit();
		getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("cardfilter_cardno", "").commit();

		objFilter(1);
		new GetCardDetailTask().execute(Constant.serverUrl + Constant.getcarddetail);
	}


	public void objFilter(int Pager) {
		obj = new JSONObject();
		try {
			obj.put("SessionID", Constant.getSessionID());
			obj.put("Pager", Pager);
			String plateno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("cardfilter_plateno", "");
			String carno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("cardfilter_carno", "");
			String cardno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("cardfilter_cardno", "");
			if (plateno != "") {
				obj.put("PlateNo", plateno);
			}
			if (carno != "") {
				obj.put("CarNo", carno);
			}
			if (cardno != "") {
				obj.put("CardNo", cardno);
			}
			MyLog.i(TAG, "obj" + obj.toString());
		} catch (JSONException e) {
			MyLog.e(TAG, e.getMessage());
		}
	}


	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		String cardid = mData.get((int) info.id).get("ID").toString();
		switch (item.getItemId()) {
		case 0:

			Intent intent = new Intent(CardActivity.this, CardEditActivity.class);
			Bundle data = new Bundle();
			data.putString("type", "new");
			intent.putExtras(data);
			startActivity(intent);
			break;
		case 1:

			Intent intent1 = new Intent(CardActivity.this, CardEditActivity.class);
			Bundle data1 = new Bundle();
			data1.putString("type", "edit");
			data1.putString("cardid", cardid);
			intent1.putExtras(data1);
			startActivity(intent1);
			break;
		case 2:

			if (cardid != "") {
				operatingCardID = cardid;
				AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("��ϸ��Ϣ").setMessage("ȷ��ɾ���IC��?");
				builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						new DeleteCardTask().execute(Constant.serverUrl + Constant.deletecard);
					}
				});
				builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
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


	class GetCardDetailTask extends AsyncTask<String, Integer, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialogMine = new ProgressDialog(CardActivity.this);
			dialogMine.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialogMine.setTitle("��ȴ�...");
			dialogMine.setMessage("���ڻ�ȡIC���б?���Ժ�...");
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
					refreshCardListHandler.post(refreshCardListRunnable);
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
						mRegionData.add(new SpinnerData("", "ȫ��"));
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


	class DeleteCardTask extends AsyncTask<String, Integer, Boolean> {

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
					objRegionPara.put("CardID", operatingCardID);
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
				Toast.makeText(CardActivity.this, "ɾ��IC���ɹ���", Toast.LENGTH_SHORT).show();
				new GetCardDetailTask().execute(Constant.serverUrl + Constant.getcarddetail);
			}
		}
	}


	private void refreshCarListUI() {
		SimpleAdapter mSimpleAdapter = new SimpleAdapter(CardActivity.this, mData, R.layout.card_list_item, new String[] { "No", "CarNo", "CardNo" },
				new int[] { R.id.linear_item_No, R.id.linear_item_PlateNo, R.id.linear_item_CardNo });
		cardListView.setAdapter(mSimpleAdapter);
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
		SimpleAdapter showCarDetailAdapter = new SimpleAdapter(CardActivity.this, mDataCurrent, R.layout.card_list_item_current, new String[] { "Label",
				"Content" }, new int[] { R.id.linear_item_Label, R.id.linear_item_Content });
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("��ϸ��Ϣ").setAdapter(showCarDetailAdapter, null);
		builder.setPositiveButton("�ر�", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		builder.create().show();
	}


	private void showSearchDialogUI(Context context) {
		LayoutInflater inflater = LayoutInflater.from(this);
		final View cardSearchView = inflater.inflate(R.layout.card_search, null);
		final EditText platenoInput = (EditText) cardSearchView.findViewById(R.id.linear_carsearch_PlateNo);
		final EditText carnoInput = (EditText) cardSearchView.findViewById(R.id.linear_carsearch_CarNo);
		final EditText cardnoInput = (EditText) cardSearchView.findViewById(R.id.linear_carsearch_CardNo);

		String plateno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("cardfilter_plateno", "");
		String carno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("cardfilter_carno", "");
		String cardno = getSharedPreferences("constant", Context.MODE_PRIVATE).getString("cardfilter_cardno", "");

		platenoInput.setText(plateno);
		carnoInput.setText(carno);
		cardnoInput.setText(cardno);

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setTitle("IC����ѯ");
		builder.setView(cardSearchView);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("cardfilter_plateno", platenoInput.getText().toString()).commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("cardfilter_carno", carnoInput.getText().toString()).commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("cardfilter_cardno", cardnoInput.getText().toString()).commit();

					objFilter(1);
					new GetCardDetailTask().execute(Constant.serverUrl + Constant.getcarddetail);
					Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
					field.setAccessible(true);
					field.set(dialog, true);
				} catch (Exception e) {
				}
			}
		});
		builder.setNeutralButton("���", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("cardfilter_plateno", "").commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("cardfilter_carno", "").commit();
					getSharedPreferences("constant", Context.MODE_PRIVATE).edit().putString("cardfilter_cardno", "").commit();
					platenoInput.setText("");
					carnoInput.setText("");
					cardnoInput.setText("");
					Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
					field.setAccessible(true);
					field.set(dialog, false);
				} catch (Exception e) {
				}
			}
		});
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
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


	private List<Map<String, Object>> getData(String tablejson) {
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		try {
			tablejson = tablejson.replaceAll("\\\"", "\"");
			JSONArray item = new JSONArray(tablejson);

			for (int i = 0; i < item.length(); i++) {
				JSONObject obj = item.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("ID", obj.getString("id"));
				map.put("No", i + (mPagerCurrent - 1) * 10 + 1);
				map.put("PlateNo", obj.getString("PlateNo"));
				map.put("CarNo", obj.getString("CarNo"));
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


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startActivity(new Intent(CardActivity.this, IndexActivity.class));
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			CardActivity.this.finish();
		}
		return false;
	}
}
