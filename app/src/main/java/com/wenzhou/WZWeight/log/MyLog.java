package com.wenzhou.WZWeight.log;

import android.util.Log;

public class MyLog {
    static boolean isOpenDebugLog = true;

    public static void d(String tag, String log) {
        if (isOpenDebugLog) {
            Log.d(tag, log);
        }
    }

    public static void w(String tag, String log) {
        if (isOpenDebugLog) {
            Log.w(tag, log);
        }
    }

    public static void e(String tag, String log, Throwable t) {
        if (isOpenDebugLog) {
            Log.e(tag, log, t);
        }
    }

    public static void e(String tag, String log) {
        if (isOpenDebugLog) {
            Log.e(tag, log);
        }
    }

    public static void i(String tag, String log) {
        if (isOpenDebugLog) {
            Log.i(tag, log);
        }

    }
}