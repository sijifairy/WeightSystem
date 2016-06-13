package com.wenzhou.WZWeight;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wenzhou.WZWeight.R;
import com.wenzhou.WZWeight.widget.NewDataToastButtom;
import com.wenzhou.WZWeight.widget.NewDataToastTop;
import com.wenzhou.WZWeight.widget.PullToRefreshListView;
import com.wenzhou.WZWeight.adapter.ExpandableAdapterFromme;
import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.log.MyLog;
import com.wenzhou.WZWeight.sqlite.DataBaseAdapter;
import com.wenzhou.WZWeight.sqlite.InfoColumn;

public class CheckActivity extends Activity {
	private static final String TAG = "Activity_check";

	private static final int REFRESH_ID = Menu.FIRST;
	private static final int EXIT_ID = Menu.FIRST + 1;

	public static final String ITEMID = "itemId";
	public static final String INDEX = "index";
	private ProgressBar mHeadProgress;

	// private ExpandableListView expandableListView;
	private PullToRefreshListView expandableListView;
	DataBaseAdapter mDataBaseAdapter;

	Calendar c;
	ProgressDialog dialog;

	private int refreshIndex = 0;

	private static Handler mMainHandler;
	private static final int taskMineOk = 1;
	private static final int taskMineFail = 0;

	private String[] isParent;
	Cursor mCursorGroup;
	Cursor mCursorChild;

	private List<Map<String, Object>> groupArray = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> tempArray = new ArrayList<Map<String, Object>>();
	private List<List<Map<String, Object>>> childArray = new ArrayList<List<Map<String, Object>>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_expandlist_reflesh_demo);

		expandableListView = (PullToRefreshListView) findViewById(R.id.expandableListView);
		mHeadProgress = (ProgressBar) findViewById(R.id.head_progressBar);
		expandableListView.setGroupIndicator(this.getResources().getDrawable(
				R.drawable.expandablelist_icon_selector));

		MyLog.d(TAG, "is come here create");

		mMainHandler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {

				super.handleMessage(msg);
				switch (msg.what) {

				case taskMineOk:
					MyLog.d(TAG, "taskMineOk " + refreshIndex);

					if (refreshIndex == 1) {
						expandableListView.onRefreshComplete("����ˢ�£�"
								+ new Date().toLocaleString());

						NewDataToastTop.makeText(CheckActivity.this, "����ˢ�³ɹ�",
								true).show();
					} else {
						dialog.dismiss();
						NewDataToastButtom.makeText(CheckActivity.this,
								"����ˢ�³ɹ�", true).show();
					}

					refleshList();

					break;
				case taskMineFail:
					MyLog.d(TAG, "taskMineFail " + refreshIndex);

					if (refreshIndex == 1) {
						expandableListView.onRefreshComplete("");
					} else {
						dialog.dismiss();
					}

					Toast.makeText(CheckActivity.this, "�ҷ��������ˢ��ʧ��",
							Toast.LENGTH_LONG).show();

					break;
				default:
					break;
				}
			}

		};
	}

	@Override
	protected void onPause() {

		super.onPause();
		MyLog.d(TAG, "is come here pause");
		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;

		}
		if (mCursorGroup != null) {
			mCursorGroup.close();
			mCursorGroup = null;
			MyLog.d(TAG, "is come here mCursorGroup");
		}

		if (mCursorChild != null) {
			mCursorChild.close();
			mCursorChild = null;
			MyLog.d(TAG, "is come here mCursorChild");
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Log.d("ontouch ", "touch event");

		for (int i = 0; i < groupArray.size(); i++) {
			if (expandableListView.isGroupExpanded(i)) {
				expandableListView.collapseGroup(i);

			}
		}

		return true;
	}

	@Override
	protected void onResume() {

		super.onResume();
		MyLog.d(TAG, "is come here resume");

		refleshList();

		expandableListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {

						TextView idText = (TextView) arg1
								.findViewById(R.id.linear_item_id);
						String id = idText.getText().toString();
						MyLog.d(TAG, id);

						Intent intent = new Intent(CheckActivity.this,
								CheckSingleActivity.class);
						intent.putExtra(ITEMID, id);
						intent.putExtra(INDEX, 1);
						startActivity(intent);

						return true;
					}
				});

		expandableListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				parent.collapseGroup(groupPosition);
				return false;
			}
		});


		expandableListView
				.setOnRefreshListener(new com.wenzhou.WZWeight.widget.PullToRefreshListView.OnRefreshListener() {
					public void onRefresh() {



						c = Calendar.getInstance();
						new refreshThread().start();
						refreshIndex = 1;
					}
				});

		expandableListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {

				MyLog.d("childArrayTome", "childArray count is  "
						+ childArray.get(groupPosition).size());

				if (childArray.get(groupPosition).size() == 0) {
					Toast.makeText(CheckActivity.this, "û��������,�����鿴��ϸ��Ϣ",
							Toast.LENGTH_SHORT).show();
				}


				return false;
			}
		});

	}

	private void refleshList() {

		if (mCursorGroup != null) {
			mCursorGroup.close();
			MyLog.d(TAG, "is come here closed");
			mCursorGroup = null;
		}

		if (mCursorChild != null) {
			mCursorChild.close();
			mCursorChild = null;
		}

		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;

		}

		mDataBaseAdapter = new DataBaseAdapter(CheckActivity.this);
		mDataBaseAdapter.open();

		String authorId = Constant.getAuthorId();
		MyLog.d(TAG, "authorId is " + authorId);

		mCursorGroup = mDataBaseAdapter.fetchAllData(Constant.index_table2,
				InfoColumn.PROJECTION, "author_name=?" + "AND state=?"
						+ " AND accept_time <> ?",
				new String[] { Constant.getAuthor(), "0", "null" },
				InfoColumn.OVER_TIME + " ASC");





		setDataForGroup();
		setDataForChild();

		expandableListView.setAdapter(new ExpandableAdapterFromme(
				CheckActivity.this, groupArray, childArray));

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

			dialog = new ProgressDialog(CheckActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setTitle("��ȴ�...");
			dialog.setMessage("�������������б?���Ժ�...");
			dialog.setIcon(R.drawable.ic);
			dialog.setCancelable(true);
			dialog.setIndeterminate(false);
			dialog.show();
			
			c = Calendar.getInstance();
			new refreshThread().start();
			refreshIndex = 2;
			break;
		case EXIT_ID:
			CheckActivity.this.finish();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	class refreshThread extends Thread {
		@Override
		public void run() {

			super.run();
			JSONObject obj = new JSONObject();
			String recent_time = getSharedPreferences("recent_time",
					Context.MODE_PRIVATE).getString("recent_time", "");
			MyLog.d(TAG, "recent_time is " + recent_time);
			try {
				obj.put("tag", Constant.TASK_GET);
				obj.put("alloc_type", 2);
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
				httpPostRequest.setHeader("Cookie", "ASP.NET_SessionId="
						+ Constant.getSession());
				MyLog.d(TAG, "Session is " + Constant.getSession());

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

						mDataBaseAdapter.deleteTable(Constant.index_table1);

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
							mDataBaseAdapter.insertData(Constant.index_table1,
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

	private void setDataForGroup() {

		isParent = new String[mCursorGroup.getCount()];

		Map<String, Object> map;
		if (!groupArray.isEmpty()) {
			groupArray.clear();
		}

		if (mCursorGroup.moveToFirst()) {

			if (mCursorGroup.getCount() > 0) {
				int i = 0;

				do {
					map = new HashMap<String, Object>();
					map.put("accept", mCursorGroup
							.getString(InfoColumn.ACCEPT_TIME_COLUMN));
					String id = mCursorGroup
							.getString(InfoColumn.CALENDAR_DETAIL_ID_COLUMN);
					map.put("id", id);
					map.put("date",
							mCursorGroup.getString(InfoColumn.OVER_TIME_COLUMN));
					map.put("type",
							mCursorGroup.getString(InfoColumn.TASK_TYPE_COLUMN));
					map.put("name",
							mCursorGroup.getString(InfoColumn.USER_NAME_COLUMN));
					map.put("day",
							mCursorGroup.getString(InfoColumn.DAYS_COLUMN));
					map.put("state",
							mCursorGroup.getString(InfoColumn.STATE_COLUMN));

					if (mCursorGroup.getString(InfoColumn.IS_SPLIT_COLUMN)
							.equals("true")) {
						isParent[i] = id;
						map.put("title",
								mCursorGroup.getString(InfoColumn.TITLE_COLUMN)
										+ "(���Ҫ��)");
						i++;
					} else {
						isParent[i] = "null";
						map.put("title",
								mCursorGroup.getString(InfoColumn.TITLE_COLUMN));
						i++;
					}
					groupArray.add(map);
				} while (mCursorGroup.moveToNext());
			}

		}

	}

	private void setDataForChild() {
		Map<String, Object> map;

		if (!childArray.isEmpty()) {
			childArray.clear();
		}

		for (int j = 0; j < isParent.length; j++) {
			MyLog.d("isParent", "isParent is " + isParent[j]);
		}

		for (int i = 0; i < isParent.length; i++) {

			if (isParent[i].equals("null")) {
				MyLog.d("null", "" + i);
				tempArray = new ArrayList<Map<String, Object>>();
				childArray.add(tempArray);

			} else {
				tempArray = new ArrayList<Map<String, Object>>();

				String parentId = isParent[i];







				mCursorChild = mDataBaseAdapter.fetchAllData(
						Constant.index_table2, InfoColumn.PROJECTION,
						"ParentID = ?" + "AND state=?"
								+ " AND accept_time <> ?", new String[] {
								parentId, "0", "null" }, InfoColumn.DAYS
								+ " ASC");

				mCursorChild.moveToFirst();

				if (mCursorChild.getCount() > 0) {

					do {
						map = new HashMap<String, Object>();

						map.put("title",
								mCursorChild.getString(InfoColumn.TITLE_COLUMN));
						map.put("accept", mCursorChild
								.getString(InfoColumn.ACCEPT_TIME_COLUMN));
						String id = mCursorChild
								.getString(InfoColumn.CALENDAR_DETAIL_ID_COLUMN);
						map.put("id", id);
						map.put("date", mCursorChild
								.getString(InfoColumn.OVER_TIME_COLUMN));
						map.put("type", mCursorChild
								.getString(InfoColumn.TASK_TYPE_COLUMN));
						map.put("name", mCursorChild
								.getString(InfoColumn.USER_NAME_COLUMN));
						map.put("day",
								mCursorChild.getString(InfoColumn.DAYS_COLUMN));
						map.put("state",
								mCursorChild.getString(InfoColumn.STATE_COLUMN));

						tempArray.add(map);

					} while (mCursorChild.moveToNext());
				}

				childArray.add(tempArray);
				mCursorChild.close();

			}

			MyLog.d("tempArray",
					" set data tempArray count is " + tempArray.size());
			MyLog.d("childArray", " set data childArray count is "
					+ childArray.get(i).size());
		}

		for (int k = 0; k < childArray.size(); k++) {
			MyLog.d("childArray", " get data childArray count is "
					+ childArray.get(k).size());
		}

	}

}
