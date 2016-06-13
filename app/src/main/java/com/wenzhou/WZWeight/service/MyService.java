package com.wenzhou.WZWeight.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.IBinder;
import android.provider.Settings;

import com.wenzhou.WZWeight.LoginActivity;
import com.wenzhou.WZWeight.R;
import com.wenzhou.WZWeight.ShowActivity;
import com.wenzhou.WZWeight.application.Constant;
import com.wenzhou.WZWeight.log.MyLog;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
	Timer timer;
	private int num=1;
	private int oldNewsNum=0;
	private static final String TAG = "MyService";

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		timer.cancel();
	}

	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);

		timer = new Timer();
		TimerTask pullTask = new PullTimerTask();
		timer.scheduleAtFixedRate(pullTask, 1000 * 60, Constant.PULLPARIOD);

	}

	class PullTimerTask extends TimerTask {

		@Override
		public void run() {

			


			if (!checkNetWorkStatus()) {

				MyLog.d(TAG, "net is not ok!");
				return;
			}

			JSONObject obj = new JSONObject();




			String userName = getSharedPreferences("constant",
					Context.MODE_PRIVATE).getString("userName", "");
			String passWord = getSharedPreferences("constant",
					Context.MODE_PRIVATE).getString("passWord", "");
			String session = getSharedPreferences("constant",
					Context.MODE_PRIVATE).getString("session", "");

			if (userName.equals("")) {
				MyLog.d(TAG, "username is null");

				return;
			} else if (passWord.equals("")) {
				MyLog.d(TAG, "passWord is null");

				return;
			} else if (session.equals("")) {
				MyLog.d(TAG, "session is null");

				return;

			}

			else {
				MyLog.d(TAG, "has user information");

				try {
					obj.put("tag", Constant.BACKGROUND_TASK_LIST_GET);
					MyLog.d(TAG, "obj is " + obj.toString());
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
							+ session);
					MyLog.d(TAG, "session is " + session);

					HttpResponse httpResponse = new DefaultHttpClient()
							.execute(httpPostRequest);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(httpResponse
								.getEntity());
						result = result.replaceAll("\r\n|\n\r|\r|\n", "");

						MyLog.d(TAG, "result is  " + result);

						JSONObject item = new JSONObject(result);
						MyLog.d(TAG, "receive json is " + item.toString());
						int tag = item.getInt("tag");
						int new_task_num = item.getInt("new_task_num");
						

						if (tag == Constant.BACKGROUND_TASK_LIST_GET_REESPONSE) {

							if (new_task_num > 0) {
																
								NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
								Notification notification = new Notification(
										R.drawable.ic, "OA���񿴰��и���",
										System.currentTimeMillis());
								notification.flags = Notification.FLAG_AUTO_CANCEL;





								notification.defaults = Notification.DEFAULT_ALL;

								Context context = getApplicationContext();
								CharSequence contentTitle = "OA���񿴰���������";
								CharSequence contentText = "����" + new_task_num
										+ "��������" + "�����鿴";
								Intent notificationIntent;

								if (Constant.getSession() == null) {
									notificationIntent = new Intent(
											MyService.this, LoginActivity.class);
								} else {
									notificationIntent = new Intent(
											MyService.this,
											ShowActivity.class);
								}

								notificationIntent.putExtra("INDEX", "service");
								PendingIntent contentIntent = PendingIntent
										.getActivity(context, 0,
												notificationIntent, 0);
								notification.setLatestEventInfo(context,
										contentTitle, contentText,
										contentIntent);
								nm.notify(1, notification);

							} else {
								MyLog.d(TAG, "no new task");
							}

						} else {
							MyLog.d(TAG, "tag is not 14 " + tag);

						}

					} else {
						MyLog.d(TAG, "status code is not 200 "
								+ httpResponse.getStatusLine().getStatusCode());

					}
				} catch (Exception e) {

					e.printStackTrace();
					MyLog.d(TAG, e.toString());

				}
			}

		}

	}


	private void setNoticeForLoad() {

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic,
				"OA���񿴰���Ҫ����", System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;




		notification.defaults = Notification.DEFAULT_ALL;

		Context context = getApplicationContext();
		CharSequence contentTitle = "OA���񿴰���Ҫ����";
		CharSequence contentText = "���OA���񿴰���Ҫ����,�����ת�Ƶ���¼���棬�����¼�Լ������";
		Intent notificationIntent = new Intent(MyService.this,
				LoginActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		nm.notify(1, notification);

	}


	private void setNoticeForNetwork() {

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic,
				"û�����磬OA���񿴰��޷�����", System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;




		notification.defaults = Notification.DEFAULT_ALL;

		Context context = getApplicationContext();
		CharSequence contentTitle = "����������";
		CharSequence contentText = "�������������⣬������������";
		Intent notificationIntent = new Intent(
				Settings.ACTION_WIRELESS_SETTINGS);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		nm.notify(1, notification);

	}


	private boolean checkNetWorkStatus() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobileState = mConnectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState();
		State wifiState = mConnectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();
		if (mobileState == State.CONNECTED || mobileState == State.CONNECTING)
			return true;
		if (wifiState == State.CONNECTED || wifiState == State.CONNECTING)
			return true;
		return false;
	}

}
