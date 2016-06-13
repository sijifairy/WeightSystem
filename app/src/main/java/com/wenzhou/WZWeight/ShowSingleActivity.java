package com.wenzhou.WZWeight;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wenzhou.WZWeight.R;
import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.log.MyLog;
import com.wenzhou.WZWeight.sqlite.DataBaseAdapter;
import com.wenzhou.WZWeight.sqlite.InfoColumn;

public class ShowSingleActivity extends Activity {

	private static final String TAG = "Activity_show_single";
	private static final int BACK_ID = Menu.FIRST;
	private static final int PLAN_ID = Menu.FIRST + 1;

	TextView senderText;
	TextView dateText;
	TextView userText;
	TextView deadlineText;
	TextView daysText;
	TextView titleText;
	TextView memoText;

	DataBaseAdapter mDataBaseAdapter;
	private String itemId;
	private boolean isParentTask;
	Cursor mCursor;
	ProgressBar mProgressBar;

	JSONObject objTarget;
	private String memo;
	private String accept_time;
	public static final String ITEMID = "itemId";
	private static boolean isRunning;
	private static taskDetailTask mtaskDetailTask;

	@Override
	protected void onPause() {

		super.onPause();
		isRunning = false;

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

		MyLog.d(TAG, "countNew is " + Constant.countNew);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {


		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.show_single_new);

		Intent intent = getIntent();
		itemId = intent.getStringExtra(ITEMID);
		MyLog.d(TAG, "itemId is " + itemId);

		isRunning = true;

		senderText = (TextView) findViewById(R.id.textView_show_single_sender);
		dateText = (TextView) findViewById(R.id.textView_show_single_date);
		userText = (TextView) findViewById(R.id.textView_show_single_user);
		deadlineText = (TextView) findViewById(R.id.textView_show_single_deadline);
		daysText = (TextView) findViewById(R.id.textView_show_single_cycle);
		titleText = (TextView) findViewById(R.id.textView_show_single_title);
		memoText = (TextView) findViewById(R.id.textView_show_single_memo);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);

		if (mDataBaseAdapter != null) {
			mDataBaseAdapter.close();
			mDataBaseAdapter = null;
		}
		mDataBaseAdapter = new DataBaseAdapter(ShowSingleActivity.this);
		mDataBaseAdapter.open();

		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		mCursor = mDataBaseAdapter.fetchAllData(Constant.index_table2,
				InfoColumn.PROJECTION, "calendar_detail_id=?",
				new String[] { itemId }, null);
		mCursor.moveToFirst();
		isParentTask = (mCursor.getString(InfoColumn.IS_SPLIT_COLUMN).equals(
				"true") ? true : false);

		mtaskDetailTask = new taskDetailTask();
		mtaskDetailTask.execute(itemId);

	}

	protected void onResume() {

		super.onResume();
		isRunning = true;

	}

	public boolean onCreateOptionsMenu(Menu menu) {


		menu.add(0, BACK_ID, 0, R.string.back).setIcon(R.drawable.relese);
		if (isParentTask) {
			menu.add(0, PLAN_ID, 0, "���Ž��").setIcon(R.drawable.edit);
		}

		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case BACK_ID:
			ShowSingleActivity.this.finish();
			break;
		case PLAN_ID:

			Intent intent = new Intent(Constant.PLAN, null);
			intent.putExtra(CheckActivity.ITEMID, itemId);
			startActivity(intent);
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
							accept_time = item.getString("accept_time");

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


			if (isRunning) {
				mProgressBar.setVisibility(View.INVISIBLE);

				if (result == true) {

					if (mCursor != null) {
						senderText.setText(mCursor
								.getString(InfoColumn.AUTHOR_NAME_COLUMN));
						dateText.setText(mCursor
								.getString(InfoColumn.CREAT_TIME_COLUMN));
						userText.setText(mCursor
								.getString(InfoColumn.USER_NAME_COLUMN));
						deadlineText.setText(mCursor
								.getString(InfoColumn.OVER_TIME_COLUMN));
						daysText.setText(mCursor
								.getString(InfoColumn.DAYS_COLUMN));
						titleText.setText(mCursor
								.getString(InfoColumn.TITLE_COLUMN));
						memoText.setText(mCursor
								.getString(InfoColumn.MEMO_COLUMN));

						MyLog.d(TAG,
								mCursor.getString(InfoColumn.ACCEPT_TIME_COLUMN)
										.toString());
						MyLog.d(TAG, "countNew is " + Constant.countNew);
						if (mCursor.getString(InfoColumn.ACCEPT_TIME_COLUMN)
								.toString().equals("null")) {
							Constant.countNew--;
						}

					}

					Spanned text = Html.fromHtml(memo);
					memoText.setText(text);
					ContentValues values = new ContentValues();
					values.put(InfoColumn.MEMO, memo);
					values.put(InfoColumn.ACCEPT_TIME, accept_time);
					mDataBaseAdapter.updateData(Constant.index_table2, itemId,
							values);
					mDataBaseAdapter.close();

				} else {
					Toast.makeText(ShowSingleActivity.this, "��ȡ��ϸ����ʧ�ܣ���������",
							Toast.LENGTH_LONG).show();
					if (mCursor != null) {
						mCursor.close();
						mCursor = null;
					}
					mCursor = mDataBaseAdapter.fetchAllData(
							Constant.index_table1, InfoColumn.PROJECTION,
							"calendar_detail_id=?", new String[] { itemId },
							null);
					mCursor.moveToFirst();

					if (mCursor != null) {
						senderText.setText(mCursor
								.getString(InfoColumn.AUTHOR_NAME_COLUMN));
						dateText.setText(mCursor
								.getString(InfoColumn.CREAT_TIME_COLUMN));
						userText.setText(mCursor
								.getString(InfoColumn.USER_NAME_COLUMN));
						deadlineText.setText(mCursor
								.getString(InfoColumn.OVER_TIME_COLUMN));
						daysText.setText(mCursor
								.getString(InfoColumn.DAYS_COLUMN));
						titleText.setText(mCursor
								.getString(InfoColumn.TITLE_COLUMN));
						memoText.setText(mCursor
								.getString(InfoColumn.MEMO_COLUMN));

					}
				}

			} else {
				MyLog.d(TAG, "activity is not running");
			}
		}

	}

}
