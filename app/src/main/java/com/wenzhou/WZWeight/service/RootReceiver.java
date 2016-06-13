package com.wenzhou.WZWeight.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wenzhou.WZWeight.log.MyLog;

import java.util.List;

public class RootReceiver extends BroadcastReceiver {
	private static final String TAG = "RootReceiver";

	@Override
	public void onReceive(Context arg0, Intent arg1) {


		if (isServiceRunning(arg0, "com.ningbo.OASystem.MyService")) {
			MyLog.d(TAG, "service is running");
			arg0.stopService(new Intent("com.ningbo.OATask.myservice"));
			MyLog.d(TAG, "service is stopped");
			arg0.startService(new Intent("com.ningbo.OATask.myservice"));
			MyLog.d(TAG, "service is restarted");

		} else {
			MyLog.d(TAG, "service is not  running");
			arg0.startService(new Intent("com.ningbo.OATask.myservice"));
		}

	}


	public static boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(30);

		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			MyLog.d(TAG, serviceList.get(i).service.getClassName());
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

}
