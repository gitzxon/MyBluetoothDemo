package com.zxon.mybluetoothdemo.util;

import android.util.Log;

import com.socks.library.KLog;

/**
 * Created by leon on 16/2/1.
 */
public class LogUtil {

    public static final String TAG = "debug_ang";
    public static final boolean DEBUG = true;

    public static void d(String msg) {
        if (DEBUG == false) return;
        KLog.d(TAG, msg);
    }

    public static void d(Object msg) {
        if (DEBUG == false) return;
        KLog.d(TAG, msg);
    }


    public static void i(String msg) {
        if (DEBUG == false) return;
        KLog.i(TAG, msg);
    }

}