package com.wenzhou.WZWeight;

import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.wenzhou.WZWeight.adapter.ExpandableAdapter;
import com.wenzhou.WZWeight.adapter.ExpandableAdapterFromme;
import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.log.MyLog;
import com.wenzhou.WZWeight.sqlite.DataBaseAdapter;
import com.wenzhou.WZWeight.sqlite.InfoColumn;
import com.wenzhou.WZWeight.widget.NewDataToastButtom;
import com.wenzhou.WZWeight.widget.NewDataToastTop;
import com.wenzhou.WZWeight.widget.PullToRefreshListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
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

public class ShowActivity extends TabActivity {
	private static final String TAG = "Acitivity_show";

	private static final int REFRESH_ID = Menu.FIRST;
	private static final int EXIT_ID = Menu.FIRST + 1;

	private int refreshIndexFromme = 0;
	private int refreshIndexTome = 0;

	DataBaseAdapter mDataBaseAdapter;

	private Boolean type_mine = false;
	private Boolean type_tome = false;

	Spinner spinner_mine;
	Spinner spinner_tome;
	EditText edit_starttime_me;
	EditText edit_starttime_other;
	EditText edit_overtime_me;
	EditText edit_overtime_other;
	Button button_starttime_me;
	Button button_starttime_other;
	Button button_overtime_me;
	Button button_overtime_other;
	Button buttonSearchMine;
	Button buttonSearchTome;
	TabHost mTab;
	Calendar c;

	View waitDialog;
	ProgressBar waitProgressBar;
	TextView waitText;
	
	ProgressDialog dialogMine;
	ProgressDialog dialogTome;
	
	Builder builderMine;
	Builder builderTome;

	private static Handler mMainHandler;
	private static final int taskMineOk = 1;
	private static final int taskMineFail = 0;
	private static final int taskTomeFail = 2;
	private static final int taskTomeOk = 3;

	private static Date dateMineStart;
	private static Date dateMineStop;
	private static Date dateTomeStart;
	private static Date dateTomeStop;
	public static final String ITEMID = "itemId";
	public static final String INDEX = "index";

	DateFormat df;




	private List<Map<String, Object>> groupArrayFromme = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> tempArrayFromme = new ArrayList<Map<String, Object>>();
	private List<List<Map<String, Object>>> childArrayFromme = new ArrayList<List<Map<String, Object>>>();

	private List<Map<String, Object>> groupArrayTome = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> tempArrayTome = new ArrayList<Map<String, Object>>();
	private List<List<Map<String, Object>>> childArrayTome = new ArrayList<List<Map<String, Object>>>();

	private String[] isParentFromme;
	private String[] isParentTome;
	Cursor mCursorGroupFromme;
	Cursor mCursorChildFromme;
	Cursor mCursorGroupTome;
	Cursor mCursorChildTome;

	private PullToRefreshListView list_mine;
	private PullToRefreshListView list_tome;

	@Override
	protected void onPause() {

		super.onPause();
		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;
			MyLog.d(TAG, "mDataBaseAdapter IS CLOSED");
		}
		if (mCursorGroupFromme != null) {
			mCursorGroupFromme.close();
			mCursorGroupFromme = null;
		}
		if (mCursorChildFromme != null) {
			mCursorChildFromme.close();
			mCursorChildFromme = null;
		}

		if (mCursorGroupTome != null) {
			mCursorGroupTome.close();
			mCursorGroupTome = null;
		}
		if (mCursorChildTome != null) {
			mCursorChildTome.close();
			mCursorChildTome = null;
		}
	}

	@Override
	protected void onResume() {

		super.onResume();

		Calendar cNew = Calendar.getInstance();
		Calendar cNew1 = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cNew.add(Calendar.MONTH, -1);
		cNew.set(Calendar.HOUR_OF_DAY, 0);
		cNew.set(Calendar.MINUTE, 0);
		cNew.set(Calendar.SECOND, 0);
		cNew.set(Calendar.MILLISECOND, 0);
		Date date = cNew.getTime();
		String str = df.format(date);
		cNew1.add(Calendar.MONTH, 1);
		cNew1.set(Calendar.HOUR_OF_DAY, 0);
		cNew1.set(Calendar.MINUTE, 0);
		cNew1.set(Calendar.SECOND, 0);
		cNew1.set(Calendar.MILLISECOND, 0);
		cNew1.add(Calendar.SECOND, -1);
		Date date1 = cNew1.getTime();
		String str1 = df.format(date1);

		edit_starttime_me.setText(str.split(" ")[0].replace("-", "/"));
		edit_starttime_other.setText(str.split(" ")[0].replace("-", "/"));
		edit_overtime_me.setText(str1.split(" ")[0].replace("-", "/"));
		edit_overtime_other.setText(str1.split(" ")[0].replace("-", "/"));

		refleshListFromme("0");

		list_mine.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {



				if (childArrayFromme.get(groupPosition).size() == 0) {
					Toast.makeText(ShowActivity.this, "û��������,�����鿴��ϸ��Ϣ",
							Toast.LENGTH_SHORT).show();
				}


				return false;
			}
		});

		list_mine.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {


				TextView idText = (TextView) arg1
						.findViewById(R.id.linear_item_id);
				String id = idText.getText().toString();
				MyLog.d(TAG, id);
				Intent intent = new Intent(ShowActivity.this,
						CheckSingleActivity.class);
				intent.putExtra(ITEMID, id);
				intent.putExtra(INDEX, 0);
				startActivity(intent);
				return true;
			}
		});

		list_mine.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				parent.collapseGroup(groupPosition);

				return false;
			}
		});


		list_mine
				.setOnRefreshListener(new com.wenzhou.WZWeight.widget.PullToRefreshListView.OnRefreshListener() {
					public void onRefresh() {

						new mineThread().start();
						refreshIndexFromme = 1;
					}
				});

		refleshListTome("0");

		list_tome.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {

				MyLog.d("childArrayTome", "childArrayTome count is  "
						+ childArrayTome.get(groupPosition).size());

				if (childArrayTome.get(groupPosition).size() == 0) {
					Toast.makeText(ShowActivity.this, "û��������,�����鿴��ϸ��Ϣ",
							Toast.LENGTH_SHORT).show();
				}


				return false;
			}
		});

		list_tome.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {


				TextView idText = (TextView) arg1
						.findViewById(R.id.linear_item_id);
				String taskId = idText.getText().toString();
				MyLog.d(TAG, taskId);
				Intent intent = new Intent(ShowActivity.this,
						ShowSingleActivity.class);
				intent.putExtra(CheckActivity.ITEMID, taskId);
				startActivity(intent);
				return true;
			}
		});

		list_tome.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				parent.collapseGroup(groupPosition);
				return false;
			}
		});


		list_tome
				.setOnRefreshListener(new com.wenzhou.WZWeight.widget.PullToRefreshListView.OnRefreshListener() {
					public void onRefresh() {

						new tomeThread().start();
						refreshIndexTome = 1;
					}
				});

		MyLog.d(TAG, "test 5");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Log.d("ontouch ", "touch event");

		for (int i = 0; i < groupArrayFromme.size(); i++) {
			if (list_mine.isGroupExpanded(i)) {
				list_mine.collapseGroup(i);

			}
		}

		for (int i = 0; i < groupArrayTome.size(); i++) {
			if (list_tome.isGroupExpanded(i)) {
				list_tome.collapseGroup(i);

			}
		}

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.show);


		Intent intent = getIntent();
		String index = intent.getStringExtra("INDEX");
		MyLog.d(TAG, "index is " + index);

		if (mDataBaseAdapter == null) {
			MyLog.d(TAG, "mDataBaseAdapter IS NULL IN CREAT");
			mDataBaseAdapter = new DataBaseAdapter(this);
			mDataBaseAdapter.open();
		}
		MyLog.d(TAG, "mDataBaseAdapter NOT NULL IN CREAT");

		mTab = getTabHost();
		mTab.addTab(mTab
				.newTabSpec("tab_me")
				.setIndicator("�ҷ��������",
						getResources().getDrawable(R.drawable.fromme))
				.setContent(R.id.relativelayout_mine));

		mTab.addTab(mTab
				.newTabSpec("tab_other")
				.setIndicator("������ҵ�����",
						getResources().getDrawable(R.drawable.tome))
				.setContent(R.id.relativelayout_tome));


		if (index.equals("system")) {
			mTab.setCurrentTab(0);
			mTab.getCurrentTab();
		} else {
			mTab.setCurrentTab(1);
			dialogTome = ProgressDialog.show(ShowActivity.this, "��ȴ�...",
					"�������������б?���Ժ�...", true, true);
			new tomeThread().start();

		}

		button_starttime_me = (Button) findViewById(R.id.button_show1);
		button_overtime_me = (Button) findViewById(R.id.button_show2);
		button_starttime_other = (Button) findViewById(R.id.button_tome1);
		button_overtime_other = (Button) findViewById(R.id.button_tome2);
		buttonSearchMine = (Button) findViewById(R.id.button_show3);
		buttonSearchTome = (Button) findViewById(R.id.button_tome3);
		edit_starttime_me = (EditText) findViewById(R.id.editText_show1);
		edit_overtime_me = (EditText) findViewById(R.id.editText_show2);
		edit_starttime_other = (EditText) findViewById(R.id.editText_tome1);
		edit_overtime_other = (EditText) findViewById(R.id.editText_tome2);
		spinner_mine = (Spinner) findViewById(R.id.spinner_show1);
		spinner_tome = (Spinner) findViewById(R.id.spinner_tome1);
		list_mine = (PullToRefreshListView) findViewById(R.id.listView_show1);
		list_tome = (PullToRefreshListView) findViewById(R.id.listView_tome1);

		list_mine.setGroupIndicator(this.getResources().getDrawable(
				R.drawable.expandablelist_icon_selector));
		list_tome.setGroupIndicator(this.getResources().getDrawable(
				R.drawable.expandablelist_icon_selector));

		c = Calendar.getInstance();
		setButton();
		setSpinner();

		df = new SimpleDateFormat("yyyy/MM/dd");

		buttonSearchMine.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				MyLog.d(TAG, "is MINE");

				if (type_mine == false) {

					MyLog.d(TAG, "δ���");

					refleshListFromme("0");

				} else {

					MyLog.d(TAG, "�����");
					refleshListFromme("1");
				}

			}

		});

		buttonSearchTome.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				MyLog.d(TAG, "is TOme");

				if (type_tome == false) {

					refleshListTome("0");
				} else {

					refleshListTome("1");
				}

			}

		});

		ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Constant.task_tyoe);
		adapterSpinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner_mine.setAdapter(adapterSpinner);
		spinner_tome.setAdapter(adapterSpinner);

		spinner_mine
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						arg0.setVisibility(View.VISIBLE);
						if (arg2 == 1) {
							type_mine = true;
							refleshListFromme("1");
						} else {
							type_mine = false;
							MyLog.d(TAG, "" + type_mine);
							refleshListFromme("0");
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}

				});
		spinner_tome
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						arg0.setVisibility(View.VISIBLE);
						if (arg2 == 1) {
							type_tome = true;
							refleshListTome("1");
						} else {
							type_tome = false;
							refleshListTome("0");
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		mMainHandler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {

				super.handleMessage(msg);
				String isOver;
				switch (msg.what) {
				case taskMineOk:

					if (refreshIndexFromme == 1) {
						list_mine.onRefreshComplete("����ˢ�£�"
								+ new Date().toLocaleString());
						NewDataToastTop.makeText(ShowActivity.this,
								"�ҷ��������ˢ�³ɹ�", true).show();
					} else {
						dialogMine.dismiss();
						NewDataToastButtom.makeText(ShowActivity.this,
								"�ҷ��������ˢ�³ɹ�", true).show();
					}

					if (type_mine == false) {
						isOver = "0";
						refleshListFromme("0");
					} else {
						isOver = "1";
						refleshListFromme("1");
					}

					break;
				case taskMineFail:

					if (refreshIndexFromme == 1) {
						list_mine.onRefreshComplete("");
					} else {
						dialogMine.dismiss();
					}
					Toast.makeText(ShowActivity.this, "�ҷ��������ˢ��ʧ��",
							Toast.LENGTH_LONG).show();

					break;
				case taskTomeOk:

					if (refreshIndexTome == 1) {
						list_tome.onRefreshComplete("����ˢ�£�"
								+ new Date().toLocaleString());

						NewDataToastTop.makeText(ShowActivity.this,
								"������ҵ�����ˢ�³ɹ�", true).show();
					} else {
						dialogTome.dismiss();
						NewDataToastButtom.makeText(ShowActivity.this,
								"������ҵ�����ˢ�³ɹ�", true).show();
					}

					if (type_tome == false) {
						isOver = "0";
						refleshListTome("0");
					} else {
						isOver = "1";
						refleshListTome("1");
					}

					break;
				case taskTomeFail:

					if (refreshIndexTome == 1) {
						list_tome.onRefreshComplete("");
					} else {
						dialogTome.dismiss();
					}
					Toast.makeText(ShowActivity.this, "������ҵ�����ˢ�³ɹ�",
							Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}

		};

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {


		menu.add(0, REFRESH_ID, 0, R.string.refresh).setIcon(R.drawable.edit);
		menu.add(0, EXIT_ID, 0, R.string.exit).setIcon(R.drawable.exit);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		switch (item.getItemId()) {
		case REFRESH_ID:
			if (mTab.getCurrentTab() == 0) {
				dialogMine = new ProgressDialog(ShowActivity.this);
				dialogMine.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				dialogMine.setTitle("��ȴ�...");
				dialogMine.setMessage("����ˢ���������Ժ�...");
				dialogMine.setIcon(R.drawable.ic);
				dialogMine.setCancelable(true);
				dialogMine.setIndeterminate(false);
				dialogMine.show();
				
				new mineThread().start();
				refreshIndexFromme = 2;

			} else {
				
				dialogTome = new ProgressDialog(ShowActivity.this);
				dialogTome.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				dialogTome.setTitle("��ȴ�...");
				dialogTome.setMessage("�������������б?���Ժ�...");
				dialogTome.setIcon(R.drawable.ic);
				dialogTome.setCancelable(true);
				dialogTome.setIndeterminate(false);
				dialogTome.show();
				new tomeThread().start();
				refreshIndexTome = 2;
			}
			break;
		case EXIT_ID:
			ShowActivity.this.finish();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	private void setButton() {

		button_starttime_me.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				new DatePickerDialog(ShowActivity.this,
						new DatePickerDialog.OnDateSetListener() {

							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {

								monthOfYear = monthOfYear + 1;
								edit_starttime_me.setText(year + "/"
										+ monthOfYear + "/" + dayOfMonth);
								MyLog.d(TAG, year + "/" + monthOfYear + "/"
										+ dayOfMonth);

							}

						}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
								.get(Calendar.DAY_OF_MONTH)).show();

			}

		});

		button_overtime_me.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				new DatePickerDialog(ShowActivity.this,
						new DatePickerDialog.OnDateSetListener() {

							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {

								monthOfYear = monthOfYear + 1;
								edit_overtime_me.setText(year + "/"
										+ monthOfYear + "/" + dayOfMonth);
								MyLog.d(TAG, year + "/" + monthOfYear + "/"
										+ dayOfMonth);

							}

						}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
								.get(Calendar.DAY_OF_MONTH)).show();

			}

		});

		button_starttime_other.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				new DatePickerDialog(ShowActivity.this,
						new DatePickerDialog.OnDateSetListener() {

							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {

								monthOfYear = monthOfYear + 1;
								edit_starttime_other.setText(year + "/"
										+ monthOfYear + "/" + dayOfMonth);
								MyLog.d(TAG, year + "/" + monthOfYear + "/"
										+ dayOfMonth);

							}

						}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
								.get(Calendar.DAY_OF_MONTH)).show();

			}

		});

		button_overtime_other.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				new DatePickerDialog(ShowActivity.this,
						new DatePickerDialog.OnDateSetListener() {

							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {

								monthOfYear = monthOfYear + 1;
								edit_overtime_other.setText(year + "/"
										+ monthOfYear + "/" + dayOfMonth);
								MyLog.d(TAG, year + "/" + monthOfYear + "/"
										+ dayOfMonth);

							}

						}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
								.get(Calendar.DAY_OF_MONTH)).show();

			}

		});

	}

	private void setSpinner() {

		ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Constant.task_tyoe);
		adapterSpinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_mine.setAdapter(adapterSpinner);
		spinner_tome.setAdapter(adapterSpinner);

		spinner_mine
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						arg0.setVisibility(View.VISIBLE);
						if (arg2 == 1) {
							type_mine = true;
							MyLog.d(TAG, "" + type_mine);
						} else {
							type_mine = false;
							MyLog.d(TAG, "" + type_mine);
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}

				});
		spinner_tome
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						arg0.setVisibility(View.VISIBLE);
						if (arg2 == 1) {
							type_tome = true;
							MyLog.d(TAG, "" + type_tome);
						} else {
							type_tome = false;
							MyLog.d(TAG, "" + type_tome);
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});
	}

	class mineThread extends Thread {
		@Override
		public void run() {

			super.run();
			JSONObject obj = new JSONObject();
			String recent_time = getSharedPreferences("recent_time",
					Context.MODE_PRIVATE).getString("recent_time", "");
			MyLog.d(TAG, "recent_time is " + recent_time);
			try {
				obj.put("tag", Constant.TASK_GET);
				obj.put("alloc_type", 0);
				obj.put("page_num", 1);
				obj.put("recent_time", recent_time);
				obj.put("current_page", 1);
			} catch (JSONException e1) {

				e1.printStackTrace();
			}
			try {
				HttpPost httpPostRequest = new HttpPost(Constant.serverUrl);


				StringEntity se = new StringEntity(obj.toString(), "GB2312");



				httpPostRequest.setEntity(se);
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

				defaultHttpClient.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);

				defaultHttpClient.getParams().setParameter(
						CoreConnectionPNames.SO_TIMEOUT, 10000);
				httpPostRequest.setHeader(
						"Cookie",
						"ASP.NET_SessionId="
								+ getSharedPreferences("constant",
										Context.MODE_PRIVATE).getString(
										"session", ""));
				MyLog.d(TAG,
						"Session is "
								+ getSharedPreferences("constant",
										Context.MODE_PRIVATE).getString(
										"session", ""));

				HttpResponse httpResponse = new DefaultHttpClient()
						.execute(httpPostRequest);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					String result = EntityUtils.toString(httpResponse
							.getEntity());
					result = result.replaceAll("\r\n|\n\r|\r|\n", "");
					MyLog.d(TAG, "result is" + result);







					JSONObject jsonTotal = new JSONObject(result);
					MyLog.d(TAG, jsonTotal.toString());
					int tag = jsonTotal.getInt("tag");

					if (tag == Constant.TASK_GET_RESPONSE) {



						mDataBaseAdapter.deleteTable(Constant.index_table2);
						;


						JSONArray jsonArrayMine = jsonTotal
								.getJSONArray("task");
						for (int i = 0; i < jsonArrayMine.length(); i++) {

							JSONObject item = jsonArrayMine.getJSONObject(i);
							String calendar_detail_id = item
									.getString("calendar_detail_id");
							String author = item.getString("author");
							String author_name = item.getString("author_name");
							String creat_time = item.getString("creat_time");
							String over_time = item.getString("over_time");
							String user_id = item.getString("user_id");
							String user_name = item.getString("user_name");
							String state = item.getString("state");
							String accept_time = item.getString("accept_time");
							String title = item.getString("title");
							String days = item.getString("days");
							String IsSplit = item.getString("IsSplit");
							MyLog.d(TAG, "IsSplit IS " + IsSplit.toString());
							String ParentID = item.getString("ParentID");
							MyLog.d(TAG, "ParentID IS " + ParentID.toString());

							String cycle = item.getString("task_type_detail");

							ContentValues values = new ContentValues();
							values.put(InfoColumn.CALENDAR_DETAIL_ID,
									calendar_detail_id);
							values.put(InfoColumn.CREAT_TIME, creat_time);
							values.put(InfoColumn.OVER_TIME, over_time);
							values.put(InfoColumn.USER_ID, user_id);
							values.put(InfoColumn.USER_NAME, user_name);
							values.put(InfoColumn.TITLE, title);
							values.put(InfoColumn.AUTHOR, author);
							values.put(InfoColumn.AUTHOR_NAME, author_name);
							values.put(InfoColumn.STATE, state);
							values.put(InfoColumn.ACCEPT_TIME, accept_time);
							values.put(InfoColumn.DAYS, days);
							values.put(InfoColumn.TASK_TYPE_DETAIL, cycle);
							values.put(InfoColumn.IS_SPLIT, IsSplit);
							values.put(InfoColumn.PARENT_ID_NEW, ParentID);
							mDataBaseAdapter.insertData(Constant.index_table2,
									values);

						}

						recent_time = "" + c.get(Calendar.YEAR) + "-"
								+ c.get(Calendar.MONTH) + "-"
								+ c.get(Calendar.DAY_OF_MONTH) + " "
								+ c.get(Calendar.HOUR_OF_DAY) + "-"
								+ c.get(Calendar.MINUTE) + "-"
								+ c.get(Calendar.SECOND);
						MyLog.d(TAG, recent_time);
						getSharedPreferences("recent_time",
								Context.MODE_PRIVATE).edit()
								.putString("recent_time", recent_time).commit();

						Message msg = mMainHandler.obtainMessage(taskMineOk,
								null);
						mMainHandler.sendMessage(msg);
					} else {

						Message msg = mMainHandler.obtainMessage(taskMineFail,
								null);
						mMainHandler.sendMessage(msg);
					}

				} else {
					MyLog.d(TAG, "no response");
					Message msg = mMainHandler
							.obtainMessage(taskMineFail, null);
					mMainHandler.sendMessage(msg);
				}
			} catch (Exception e) {

				e.printStackTrace();
				MyLog.d(TAG, e.toString());

			}
		}

	}

	class tomeThread extends Thread {
		@Override
		public void run() {

			super.run();
			JSONObject obj = new JSONObject();

			String recent_time = getSharedPreferences("recent_time",
					Context.MODE_PRIVATE).getString("recent_time", "");
			MyLog.d(TAG, "recent_time is " + recent_time);

			try {
				obj.put("tag", Constant.TASK_GET);
				obj.put("alloc_type", 0);
				obj.put("page_num", 1);
				obj.put("recent_time", recent_time);
				obj.put("current_page", 1);
				MyLog.d(TAG, "send obj is " + obj.toString());

			} catch (JSONException e1) {

				e1.printStackTrace();
			}
			try {
				HttpPost httpPostRequest = new HttpPost(Constant.serverUrl);


				StringEntity se = new StringEntity(obj.toString(), "GB2312");



				httpPostRequest.setEntity(se);
				DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

				defaultHttpClient.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);

				defaultHttpClient.getParams().setParameter(
						CoreConnectionPNames.SO_TIMEOUT, 10000);
				httpPostRequest.setHeader(
						"Cookie",
						"ASP.NET_SessionId="
								+ getSharedPreferences("constant",
										Context.MODE_PRIVATE).getString(
										"session", ""));
				MyLog.d(TAG,
						"Session is "
								+ getSharedPreferences("constant",
										Context.MODE_PRIVATE).getString(
										"session", ""));

				HttpResponse httpResponse = new DefaultHttpClient()
						.execute(httpPostRequest);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					String result = EntityUtils.toString(httpResponse
							.getEntity());
					result = result.replaceAll("\r\n|\n\r|\r|\n", "");
					MyLog.d(TAG, "result is" + result);







					JSONObject jsonTotal = new JSONObject(result);
					MyLog.d(TAG, jsonTotal.toString());
					int tag = jsonTotal.getInt("tag");

					if (tag == Constant.TASK_GET_RESPONSE) {



						mDataBaseAdapter.deleteTable(Constant.index_table2);
						;


						JSONArray jsonArrayMine = jsonTotal
								.getJSONArray("task");
						for (int i = 0; i < jsonArrayMine.length(); i++) {

							JSONObject item = jsonArrayMine.getJSONObject(i);
							String calendar_detail_id = item
									.getString("calendar_detail_id");
							String author = item.getString("author");
							String author_name = item.getString("author_name");
							String creat_time = item.getString("creat_time");
							String over_time = item.getString("over_time");
							String user_id = item.getString("user_id");
							String user_name = item.getString("user_name");
							String state = item.getString("state");
							String title = item.getString("title");
							String days = item.getString("days");
							String cycle = item.getString("task_type_detail");
							String accept_time = item.getString("accept_time");
							String IsSplit = item.getString("IsSplit");
							MyLog.d(TAG, "IsSplit IS " + IsSplit.toString());
							String ParentID = item.getString("ParentID");
							MyLog.d(TAG, "ParentID IS " + ParentID.toString());

							ContentValues values = new ContentValues();
							values.put(InfoColumn.CALENDAR_DETAIL_ID,
									calendar_detail_id);
							values.put(InfoColumn.CREAT_TIME, creat_time);
							values.put(InfoColumn.OVER_TIME, over_time);
							values.put(InfoColumn.USER_NAME, user_name);
							values.put(InfoColumn.USER_ID, user_id);
							values.put(InfoColumn.TITLE, title);
							values.put(InfoColumn.AUTHOR, author);
							values.put(InfoColumn.AUTHOR_NAME, author_name);
							values.put(InfoColumn.STATE, state);
							values.put(InfoColumn.DAYS, days);
							values.put(InfoColumn.TASK_TYPE_DETAIL, cycle);
							values.put(InfoColumn.ACCEPT_TIME, accept_time);
							values.put(InfoColumn.IS_SPLIT, IsSplit);
							values.put(InfoColumn.PARENT_ID_NEW, ParentID);
							mDataBaseAdapter.insertData(Constant.index_table2,
									values);

						}

						recent_time = "" + c.get(Calendar.YEAR) + "-"
								+ c.get(Calendar.MONTH) + "-"
								+ c.get(Calendar.DAY_OF_MONTH) + " "
								+ c.get(Calendar.HOUR_OF_DAY) + "-"
								+ c.get(Calendar.MINUTE) + "-"
								+ c.get(Calendar.SECOND);
						MyLog.d(TAG, recent_time);
						getSharedPreferences("recent_time",
								Context.MODE_PRIVATE).edit()
								.putString("recent_time", recent_time).commit();

						Message msg = mMainHandler.obtainMessage(taskTomeOk,
								null);
						mMainHandler.sendMessage(msg);
					} else {

						Message msg = mMainHandler.obtainMessage(taskTomeFail,
								null);
						mMainHandler.sendMessage(msg);
					}

				} else {
					MyLog.d(TAG, "no response");
					Message msg = mMainHandler
							.obtainMessage(taskTomeFail, null);
					mMainHandler.sendMessage(msg);
				}
			} catch (Exception e) {

				e.printStackTrace();
				MyLog.d(TAG, e.toString());

			}
		}

	}

	private String setSelectStrMine() {

		String selectString = "state=?";

		try {

			dateMineStart = df.parse(edit_starttime_me.getText().toString());
			Calendar calendarMineStart = Calendar.getInstance();
			calendarMineStart.setTime(dateMineStart);
			calendarMineStart.add(Calendar.SECOND, -1);

			dateMineStart = calendarMineStart.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateMineStartString = sdf.format(dateMineStart);
			selectString += " AND date(over_time) >= '" + dateMineStartString
					+ "'";

			dateMineStop = df.parse(edit_overtime_me.getText().toString());
			Calendar calendarMineStop = Calendar.getInstance();
			calendarMineStop.setTime(dateMineStop);
			calendarMineStop.add(Calendar.DAY_OF_MONTH, 1);
			calendarMineStop.add(Calendar.SECOND, -1);

			dateMineStop = calendarMineStop.getTime();
			String dateMineStopString = sdf.format(dateMineStop);

			selectString += " AND date(over_time) <= '" + dateMineStopString
					+ "'";

		} catch (ParseException e) {

			e.printStackTrace();
		}

		MyLog.d(TAG, selectString);
		return selectString;

	}

	private String setSelectStrTome() {
		String selectString = "state=?";
		try {

			dateTomeStart = df.parse(edit_starttime_other.getText().toString());
			Calendar calendarMineStart = Calendar.getInstance();
			calendarMineStart.setTime(dateTomeStart);
			calendarMineStart.add(Calendar.SECOND, -1);

			dateTomeStart = calendarMineStart.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateMineStartString = sdf.format(dateTomeStart);
			selectString += " AND date(over_time) >= '" + dateMineStartString
					+ "'";

			dateTomeStop = df.parse(edit_overtime_other.getText().toString());
			Calendar calendarMineStop = Calendar.getInstance();
			calendarMineStop.setTime(dateTomeStop);
			calendarMineStop.add(Calendar.DAY_OF_MONTH, 1);
			calendarMineStop.add(Calendar.SECOND, -1);

			dateTomeStop = calendarMineStop.getTime();
			String dateMineStopString = sdf.format(dateTomeStop);

			selectString += " AND date(over_time) <= '" + dateMineStopString
					+ "'";

		} catch (ParseException e) {

			e.printStackTrace();
		}

		MyLog.d(TAG, selectString);
		return selectString;
	}

	private void refleshListFromme(String isOver) {

		if (mCursorGroupFromme != null) {
			mCursorGroupFromme.close();
			MyLog.d(TAG, "is come here closed");
			mCursorGroupFromme = null;
		}

		if (mCursorChildFromme != null) {
			mCursorChildFromme.close();
			mCursorChildFromme = null;
		}

		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;

		}

		mDataBaseAdapter = new DataBaseAdapter(ShowActivity.this);
		mDataBaseAdapter.open();

		String authorId = Constant.getAuthorId();
		MyLog.d(TAG, "authorId is " + authorId);








		mCursorGroupFromme = mDataBaseAdapter.fetchAllData(
				Constant.index_table2, InfoColumn.PROJECTION,
				setSelectStrMine() + "AND author_name=?", new String[] {
						isOver, Constant.getAuthor() }, InfoColumn.OVER_TIME
						+ " ASC");

		setDataForGroupFromme();
		setDataForChildFromme();

		for (int k = 0; k < childArrayFromme.size(); k++) {
			MyLog.d("childArrayFromme", " get data childArrayFromme count is "
					+ childArrayFromme.get(k).size());
		}

		list_mine.setAdapter(new ExpandableAdapterFromme(ShowActivity.this,
				groupArrayFromme, childArrayFromme));

	}

	private void setDataForGroupFromme() {

		isParentFromme = new String[mCursorGroupFromme.getCount()];

		Map<String, Object> map;
		if (!groupArrayFromme.isEmpty()) {
			groupArrayFromme.clear();
		}

		mCursorGroupFromme.moveToFirst();

		if (mCursorGroupFromme.getCount() > 0) {
			int i = 0;

			do {
				map = new HashMap<String, Object>();
				map.put("accept", mCursorGroupFromme
						.getString(InfoColumn.ACCEPT_TIME_COLUMN));
				String id = mCursorGroupFromme
						.getString(InfoColumn.CALENDAR_DETAIL_ID_COLUMN);
				map.put("id", id);
				map.put("date", mCursorGroupFromme
						.getString(InfoColumn.OVER_TIME_COLUMN));
				map.put("type", mCursorGroupFromme
						.getString(InfoColumn.TASK_TYPE_COLUMN));
				map.put("name", mCursorGroupFromme
						.getString(InfoColumn.USER_NAME_COLUMN));
				map.put("day",
						mCursorGroupFromme.getString(InfoColumn.DAYS_COLUMN));
				map.put("state",
						mCursorGroupFromme.getString(InfoColumn.STATE_COLUMN));

				if (mCursorGroupFromme.getString(InfoColumn.IS_SPLIT_COLUMN)
						.equals("true")) {
					isParentFromme[i] = id;
					map.put("title",
							mCursorGroupFromme
									.getString(InfoColumn.TITLE_COLUMN)
									+ "(���Ҫ��)");
					i++;
				} else {
					isParentFromme[i] = "null";
					map.put("title", mCursorGroupFromme
							.getString(InfoColumn.TITLE_COLUMN));
					i++;
				}

				groupArrayFromme.add(map);
			} while (mCursorGroupFromme.moveToNext());
		}

	}

	private void setDataForChildFromme() {
		Map<String, Object> map;

		if (!childArrayFromme.isEmpty()) {
			childArrayFromme.clear();

		}

		for (int j = 0; j < isParentFromme.length; j++) {
			MyLog.d("isParentFromme", "isParentFromme is " + isParentFromme[j]);
		}

		for (int i = 0; i < isParentFromme.length; i++) {

			if (isParentFromme[i].equals("null")) {

				MyLog.d("null", "isParentFromme" + i + "is null");

				tempArrayFromme = new ArrayList<Map<String, Object>>();
				childArrayFromme.add(tempArrayFromme);
			} else {

				tempArrayFromme = new ArrayList<Map<String, Object>>();

				String parentId = isParentFromme[i];

				MyLog.d("parentId", "parentId" + i + " is " + parentId);

				mCursorChildFromme = mDataBaseAdapter.fetchAllData(
						Constant.index_table2, InfoColumn.PROJECTION,
						" ParentID = ?", new String[] { parentId },
						InfoColumn.DAYS + " ASC");

				MyLog.d("mCursorChildFromme", "mCursorChildFromme count " + i
						+ " is " + mCursorChildFromme.getCount());

				mCursorChildFromme.moveToFirst();
				if (mCursorChildFromme.getCount() > 0) {
					do {
						map = new HashMap<String, Object>();

						map.put("title", mCursorChildFromme
								.getString(InfoColumn.TITLE_COLUMN));
						map.put("accept", mCursorChildFromme
								.getString(InfoColumn.ACCEPT_TIME_COLUMN));
						String id = mCursorChildFromme
								.getString(InfoColumn.CALENDAR_DETAIL_ID_COLUMN);
						map.put("id", id);
						map.put("date", mCursorChildFromme
								.getString(InfoColumn.OVER_TIME_COLUMN));
						map.put("type", mCursorChildFromme
								.getString(InfoColumn.TASK_TYPE_COLUMN));
						map.put("name", mCursorChildFromme
								.getString(InfoColumn.USER_NAME_COLUMN));
						map.put("day", mCursorChildFromme
								.getString(InfoColumn.DAYS_COLUMN));
						map.put("state", mCursorChildFromme
								.getString(InfoColumn.STATE_COLUMN));

						tempArrayFromme.add(map);

					} while (mCursorChildFromme.moveToNext());
				}

				MyLog.d("tempArrayFromme", "tempArrayFromme count is " + i
						+ " " + tempArrayFromme.size());

				childArrayFromme.add(tempArrayFromme);
				mCursorChildFromme.close();

			}

		}

		for (int k = 0; k < childArrayFromme.size(); k++) {
			MyLog.d("childArrayFromme", " set data childArrayFromme count is "
					+ childArrayFromme.get(k).size());
		}

	}

	private void refleshListTome(String isOver) {

		if (mCursorGroupTome != null) {
			mCursorGroupTome.close();
			mCursorGroupTome = null;
		}

		if (mCursorChildTome != null) {
			mCursorChildTome.close();
			mCursorChildTome = null;
		}

		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;

		}

		mDataBaseAdapter = new DataBaseAdapter(ShowActivity.this);
		mDataBaseAdapter.open();

		String authorId = Constant.getAuthorId();
		MyLog.d(TAG, "authorId is " + authorId);








		mCursorGroupTome = mDataBaseAdapter.fetchAllData(
				Constant.index_table2,
				InfoColumn.PROJECTION,
				setSelectStrTome() + " AND author_name <> ? "
						+ " AND user_name=?",
				new String[] { isOver, Constant.getAuthor(),
						Constant.getAuthor() }, InfoColumn.OVER_TIME + " ASC");







		setDataForGroupTome();
		setDataForChildTome();

		MyLog.d("childArrayTome",
				"childArrayTome count is " + childArrayTome.size());





		list_tome.setAdapter(new ExpandableAdapter(ShowActivity.this,
				groupArrayTome, childArrayTome));

	}

	private void setDataForGroupTome() {

		isParentTome = new String[mCursorGroupTome.getCount()];

		Map<String, Object> map;
		if (!groupArrayTome.isEmpty()) {
			groupArrayTome.clear();
		}

		if (mCursorGroupTome.moveToFirst()) {

			if (mCursorGroupTome.getCount() > 0) {
				int i = 0;

				do {
					map = new HashMap<String, Object>();
					map.put("accept", mCursorGroupTome
							.getString(InfoColumn.ACCEPT_TIME_COLUMN));
					String id = mCursorGroupTome
							.getString(InfoColumn.CALENDAR_DETAIL_ID_COLUMN);
					map.put("id", id);
					map.put("date", mCursorGroupTome
							.getString(InfoColumn.OVER_TIME_COLUMN));
					map.put("type", mCursorGroupTome
							.getString(InfoColumn.TASK_TYPE_COLUMN));
					map.put("name", mCursorGroupTome
							.getString(InfoColumn.AUTHOR_NAME_COLUMN));
					map.put("day",
							mCursorGroupTome.getString(InfoColumn.DAYS_COLUMN));
					map.put("state",
							mCursorGroupTome.getString(InfoColumn.STATE_COLUMN));

					if (mCursorGroupTome.getString(InfoColumn.IS_SPLIT_COLUMN)
							.equals("true")) {
						isParentTome[i] = id;
						map.put("title",
								mCursorGroupTome
										.getString(InfoColumn.TITLE_COLUMN)
										+ "(���Ҫ��)");
						i++;
					} else {
						isParentTome[i] = "null";
						map.put("title", mCursorGroupTome
								.getString(InfoColumn.TITLE_COLUMN));
						i++;
					}
					groupArrayTome.add(map);
				} while (mCursorGroupTome.moveToNext());

			}

		}

	}

	private void setDataForChildTome() {
		Map<String, Object> map;

		if (!childArrayTome.isEmpty()) {
			childArrayTome.clear();
		}

		for (int i = 0; i < mCursorGroupTome.getCount(); i++) {
			MyLog.d("isParentTome", "isParentTome is " + isParentTome[i]);
		}

		for (int i = 0; i < isParentTome.length; i++) {

			if (isParentTome[i].equals("null")) {
				MyLog.d("null", "isParentTome" + i + " id null");
				tempArrayTome = new ArrayList<Map<String, Object>>();
				childArrayTome.add(tempArrayTome);

			} else {
				tempArrayTome = new ArrayList<Map<String, Object>>();

				String parentId = isParentTome[i];

				mCursorChildTome = mDataBaseAdapter.fetchAllData(
						Constant.index_table2, InfoColumn.PROJECTION,
						"ParentID = ?", new String[] { parentId },
						InfoColumn.DAYS + " ASC");

				MyLog.d("mCursorChildTome", "mCursorChildTome count is "
						+ mCursorChildTome.getCount());
				mCursorChildTome.moveToFirst();
				if (mCursorChildTome.getCount() > 0) {

					do {
						map = new HashMap<String, Object>();
						map.put("title", mCursorChildTome
								.getString(InfoColumn.TITLE_COLUMN));
						map.put("accept", mCursorChildTome
								.getString(InfoColumn.ACCEPT_TIME_COLUMN));
						String id = mCursorChildTome
								.getString(InfoColumn.CALENDAR_DETAIL_ID_COLUMN);
						map.put("id", id);
						map.put("date", mCursorChildTome
								.getString(InfoColumn.OVER_TIME_COLUMN));
						map.put("type", mCursorChildTome
								.getString(InfoColumn.TASK_TYPE_COLUMN));
						map.put("name", mCursorChildTome
								.getString(InfoColumn.AUTHOR_NAME_COLUMN));
						map.put("day", mCursorChildTome
								.getString(InfoColumn.DAYS_COLUMN));
						map.put("state", mCursorChildTome
								.getString(InfoColumn.STATE_COLUMN));
						tempArrayTome.add(map);

					} while (mCursorChildTome.moveToNext());
				}

				childArrayTome.add(tempArrayTome);

				MyLog.d("tempArrayTome", "tempArrayTome count is "
						+ tempArrayTome.size());
				mCursorChildTome.close();
			}

		}

		MyLog.d("childArrayTome", "mCursorGroupTome count is "
				+ mCursorGroupTome.getCount());
		MyLog.d("childArrayTome",
				"childArrayTome count is " + childArrayTome.size());

	}
}
