package com.wenzhou.WZWeight;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.log.MyLog;
import com.wenzhou.WZWeight.sqlite.DataBaseAdapter;
import com.wenzhou.WZWeight.sqlite.InfoColumn;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckSingleActivity extends Activity {
	private static final String TAG = "Activity_check_single";

	private static final int CHANGE_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int CHECK_ID = Menu.FIRST + 2;
	private static final int CHECKONTIME_ID = Menu.FIRST + 3;
	private static final int BACK_ID = Menu.FIRST + 4;

	TextView senderText;
	TextView dateText;
	TextView userText;
	TextView deadlineText;
	TextView daysText;
	TextView titleText;
	TextView memoText;
	ProgressBar mProgressBar;
	//
	DataBaseAdapter mDataBaseAdapter;
	String itemId;
	Cursor mCursor;
	JSONObject objTarget;
	private String command;
	private String memo;
	// private String accept_time;
	private String done_time;
	private String over_time;
	private long days;


	private String detail;

	private static Handler mMainHandler;
	private static final int deleteOk = 1;
	private static final int finishOk = 0;
	private static final int checkFail = 2;
	private static final int finishOnTimeOk = 3;

	public static final String ITEMID = "itemId";
	public static final String INDEX = "index";

	private static int index;
	private static boolean isRunning;
	private taskDetailTask mtaskDetailTask;

	@Override
	protected void onPause() {

		super.onPause();
		isRunning = false;
		MyLog.d(TAG, "isRunning is" + isRunning);
		mtaskDetailTask.cancel(true);
		if (mtaskDetailTask.isCancelled()) {
			MyLog.d(TAG, "mtaskDetailTask is cancelled!");
		}

		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;
		}
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
	}

	@Override
	protected void onResume() {

		super.onResume();

		isRunning = true;
		MyLog.d(TAG, "isRunning is" + isRunning);

		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;
		}
		mDataBaseAdapter = new DataBaseAdapter(CheckSingleActivity.this);
		mDataBaseAdapter.open();

		MyLog.d(TAG, "resume itemId is " + itemId);
		mCursor = mDataBaseAdapter.fetchAllData(Constant.index_table2,
				InfoColumn.PROJECTION, "calendar_detail_id=?",
				new String[] { itemId }, null);
		MyLog.d(TAG, "" + mCursor.getCount());

		if (mCursor != null) {
			senderText.setText(Constant.getAuthor());
			dateText.setText(mCursor.getString(InfoColumn.CREAT_TIME_COLUMN));
			userText.setText(mCursor.getString(InfoColumn.USER_NAME_COLUMN));
			deadlineText
					.setText(mCursor.getString(InfoColumn.OVER_TIME_COLUMN));
			daysText.setText(mCursor.getString(InfoColumn.DAYS_COLUMN));
			titleText.setText(mCursor.getString(InfoColumn.TITLE_COLUMN));
			memoText.setText(mCursor.getString(InfoColumn.MEMO_COLUMN));
			over_time = mCursor.getString(InfoColumn.OVER_TIME_COLUMN)
					.toString();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.check_single_new);

		Intent intent = getIntent();
		itemId = intent.getStringExtra(ITEMID);
		index = intent.getIntExtra(INDEX, 1);
		MyLog.d(TAG, "itemId is " + itemId);
		isRunning = true;
		MyLog.d(TAG, "isRunning is" + isRunning);

		senderText = (TextView) findViewById(R.id.textView_check_single_sender);
		dateText = (TextView) findViewById(R.id.textView_check_single_date);
		userText = (TextView) findViewById(R.id.textView_check_single_user);
		deadlineText = (TextView) findViewById(R.id.textView_check_single_deadline);
		daysText = (TextView) findViewById(R.id.textView_check_single_cycle);
		titleText = (TextView) findViewById(R.id.textView_check_single_title);
		memoText = (TextView) findViewById(R.id.textView_check_single_memo);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;
		}
		mDataBaseAdapter = new DataBaseAdapter(CheckSingleActivity.this);
		mDataBaseAdapter.open();

		mtaskDetailTask = new taskDetailTask();
		mtaskDetailTask.execute(itemId);

		mMainHandler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {

				super.handleMessage(msg);
				switch (msg.what) {
				case deleteOk:
					if (mDataBaseAdapter != null) {
						mDataBaseAdapter.close();
						mDataBaseAdapter = null;
					}
					mDataBaseAdapter = new DataBaseAdapter(
							CheckSingleActivity.this);
					mDataBaseAdapter.open();
					mDataBaseAdapter.deleteData(Constant.index_table2, itemId);

					Toast.makeText(CheckSingleActivity.this, "ɾ������ɹ�",
							Toast.LENGTH_SHORT).show();
					CheckSingleActivity.this.finish();
					break;
				case finishOk:
					ContentValues values = new ContentValues();
					values.put(InfoColumn.STATE, 1);
					values.put(InfoColumn.DONE_TIME, done_time);
					values.put(InfoColumn.DAYS, days);

					if (mDataBaseAdapter != null) {
						mDataBaseAdapter.close();
						mDataBaseAdapter = null;
					}
					mDataBaseAdapter = new DataBaseAdapter(
							CheckSingleActivity.this);
					mDataBaseAdapter.open();

					mDataBaseAdapter.updateData(Constant.index_table2, itemId,
							values);
					mProgressBar.setVisibility(View.INVISIBLE);
					Toast.makeText(CheckSingleActivity.this, "��������ɹ�",
							Toast.LENGTH_SHORT).show();
					CheckSingleActivity.this.finish();
					break;
				case finishOnTimeOk:

					ContentValues values1 = new ContentValues();
					values1.put(InfoColumn.STATE, 1);
					values1.put(InfoColumn.DONE_TIME, done_time);
					values1.put(InfoColumn.DAYS, days);

					if (mDataBaseAdapter != null) {
						mDataBaseAdapter.close();
						mDataBaseAdapter = null;
					}
					mDataBaseAdapter = new DataBaseAdapter(
							CheckSingleActivity.this);
					mDataBaseAdapter.open();
					mDataBaseAdapter.updateData(Constant.index_table2, itemId,
							values1);
					mProgressBar.setVisibility(View.INVISIBLE);
					Toast.makeText(CheckSingleActivity.this, "��ʱ��������ɹ�",
							Toast.LENGTH_SHORT).show();
					CheckSingleActivity.this.finish();
					break;
				case checkFail:
					Toast.makeText(CheckSingleActivity.this,
							"���ջ�ɾ������ʧ��" + detail, Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}

		};

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {


		if (index == 1) {
			menu.add(0, CHANGE_ID, 0, R.string.change).setIcon(R.drawable.edit);
			menu.add(0, DELETE_ID, 0, R.string.delete).setIcon(
					R.drawable.delete);
			menu.add(0, CHECK_ID, 0, R.string.check).setIcon(R.drawable.relese);
			menu.add(0, CHECKONTIME_ID, 0, R.string.check_ontime).setIcon(
					R.drawable.edit);
			menu.add(0, BACK_ID, 0, R.string.back).setIcon(R.drawable.relese);
		} else {
			menu.add(0, CHANGE_ID, 0, R.string.change).setIcon(R.drawable.edit);
			menu.add(0, DELETE_ID, 0, R.string.delete).setIcon(
					R.drawable.delete);
			menu.add(0, BACK_ID, 0, R.string.back).setIcon(R.drawable.relese);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case CHANGE_ID:
			Intent intent = new Intent(Constant.EDIT, null);
			intent.putExtra(CheckActivity.ITEMID, itemId);
			startActivity(intent);
			break;
		case DELETE_ID:
			command = "delete";

			new checkThread().start();
			break;
		case CHECK_ID:
			command = "finish";
			mProgressBar.setVisibility(View.VISIBLE);
			new checkThread().start();
			break;
		case CHECKONTIME_ID:
			command = "finishOnTime";
			mProgressBar.setVisibility(View.VISIBLE);
			new checkThread().start();
			break;
		case BACK_ID:
			CheckSingleActivity.this.finish();
			break;

		}
		return super.onOptionsItemSelected(item);

	}

	class taskDetailTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			mProgressBar.setVisibility(View.VISIBLE);
			mProgressBar.setIndeterminate(false);
		}

		@Override
		protected Boolean doInBackground(String... params) {

			MyLog.d(TAG, "isRunning is" + isRunning);

			if (isRunning) {
				JSONObject obj = new JSONObject();
				try {
					obj.put("tag", Constant.TASK_GET_DETAIL);
					obj.put("calendar_detail_id", itemId);
				} catch (JSONException e1) {

					e1.printStackTrace();
				}

				try {

					HttpPost httpPostRequest = new HttpPost(Constant.serverUrl);



					StringEntity se = new StringEntity(obj.toString(),
							HTTP.UTF_8);



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
					httpPostRequest.addHeader("charset", HTTP.UTF_8);

					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpPostRequest);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {


						String result = EntityUtils.toString(httpResponse
								.getEntity());
						result = result.replaceAll("\r\n|\n\r|\r|\n", "");



						MyLog.d(TAG, "result is  " + result);

						JSONObject item = new JSONObject(result);
						MyLog.d(TAG, "item is  " + item.toString());

						int tag = item.getInt("tag");
						MyLog.d(TAG, "tag is  " + tag);

						if (tag == Constant.TASK_GET_DETAIL_RESPONSE) {

							memo = item.getString("memo");


							return true;
						} else {
							MyLog.d(TAG, "response tag is not 8 ,is " + tag);
							return false;
						}
					} else {
						MyLog.d(TAG, "response code is not 200, is"
								+ httpResponse.getStatusLine().getStatusCode());
						return false;
					}

				} catch (Exception e) {

					MyLog.d(TAG, "test 1");
					e.printStackTrace();
					MyLog.d(TAG, e.toString());
					return false;
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			super.onPostExecute(result);


			MyLog.d(TAG, "isRunning is" + isRunning);

			if (isRunning) {
				mProgressBar.setVisibility(View.INVISIBLE);

				if (result == true) {
					Spanned text = Html.fromHtml(memo);
					memoText.setText(text);
					
					ContentValues values = new ContentValues();
					values.put(InfoColumn.MEMO, memo);
					mDataBaseAdapter.updateData(Constant.index_table2, itemId,
							values);
					mDataBaseAdapter.close();

				} else {
					Toast.makeText(CheckSingleActivity.this,
							"��ȡ��ϸ����ʧ�ܣ���������", Toast.LENGTH_LONG).show();

				}
			} else {
				MyLog.d(TAG, "activity is not running");
			}
		}

	}

	class checkThread extends Thread {

		@Override
		public void run() {

			super.run();
			JSONObject objCheck = new JSONObject();
			try {
				objCheck.put("tag", Constant.TASK_CHECK);
				objCheck.put("command", command);
				objCheck.put("calendar_detail_id", itemId);

			} catch (JSONException e) {

				e.printStackTrace();
			}

			try {

				HttpPost httpPostRequest = new HttpPost(Constant.serverUrl);


				StringEntity se = new StringEntity(objCheck.toString());



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
				httpPostRequest.addHeader("charset", HTTP.UTF_8);

				HttpResponse httpResponse = new DefaultHttpClient()
						.execute(httpPostRequest);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {


					String result = EntityUtils.toString(httpResponse
							.getEntity());
					result = result.replaceAll("\r\n|\n\r|\r|\n", "");
					MyLog.d(TAG, result);
					JSONObject item = new JSONObject(result);
					int tag = item.getInt("tag");
					String status = item.getString("status");
					detail = item.getString("detail");
					if (tag == Constant.TASK_CHECK_RESPONSE) {

						if (status.equals("true")) {
							if (command.equals("delete")) {

								Message msg = mMainHandler.obtainMessage(
										deleteOk, null);
								mMainHandler.sendMessage(msg);

							} else if (command.equals("finish")) {

								done_time = item.getString("done_time");
								String[] done_time_array = done_time.split("T");
								String[] over_time_array = over_time.split("T");
								SimpleDateFormat sdf = new SimpleDateFormat(
										"yyyy-MM-dd");
								Date doneDate = (Date) sdf
										.parse(done_time_array[0]);
								Date overDater = (Date) sdf
										.parse(over_time_array[0]);

								days = (overDater.getTime() - doneDate
										.getTime()) / (24 * 60 * 60 * 1000);
								MyLog.d(TAG, "DAYS IS " + days);
								Message msg = mMainHandler.obtainMessage(
										finishOk, null);
								mMainHandler.sendMessage(msg);
							} else if (command.equals("finishOnTime")) {

								done_time = item.getString("done_time");
								done_time = done_time.replace("/", "-");

								String[] done_time_array = done_time.split("T");
								String[] over_time_array = over_time.split("T");
								SimpleDateFormat sdf = new SimpleDateFormat(
										"yyyy-MM-dd");
								Date doneDate = (Date) sdf
										.parse(done_time_array[0]);
								Date overDater = (Date) sdf
										.parse(over_time_array[0]);

								days = (overDater.getTime() - doneDate
										.getTime()) / (24 * 60 * 60 * 1000);
								MyLog.d(TAG, "DAYS IS " + days);
								Message msg = mMainHandler.obtainMessage(
										finishOnTimeOk, null);
								mMainHandler.sendMessage(msg);
							} else {
								MyLog.d(TAG, "wrong command");
								Message msg = mMainHandler.obtainMessage(
										checkFail, null);
								mMainHandler.sendMessage(msg);
							}
						} else {
							Message msg = mMainHandler.obtainMessage(checkFail,
									null);
							mMainHandler.sendMessage(msg);

						}
					} else {
						MyLog.d(TAG, "response tag is not 12 ,is " + tag);
						Message msg = mMainHandler.obtainMessage(checkFail,
								null);
						mMainHandler.sendMessage(msg);
					}
				} else {
					MyLog.d(TAG, "response code is not 200, is"
							+ httpResponse.getStatusLine().getStatusCode());
					Message msg = mMainHandler.obtainMessage(checkFail, null);
					mMainHandler.sendMessage(msg);

				}

			} catch (Exception e) {

				e.printStackTrace();
				MyLog.d(TAG, e.toString());
				Message msg = mMainHandler.obtainMessage(checkFail, null);
				mMainHandler.sendMessage(msg);

			}
		}

	}

}
