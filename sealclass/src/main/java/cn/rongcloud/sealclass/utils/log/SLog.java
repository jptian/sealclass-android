package cn.rongcloud.sealclass.utils.log;

import android.content.Context;

public class SLog {


    private  static final String TAG = "SealClass";
    public static final String TAG_NET = "SealClassNet";

    public static final String TAG_DATA = "SealClassData";

    public static final String TAG_IM = "SealClassIM";

    public static final String TAG_RTC = "SealClassRTC";

    public static void init(Context context) {
        SLogCreator.sInstance.init(context);
    }

    public static void i(String tag, String msg) {
        SLogCreator.sInstance.i(TAG, "[" + tag + "] " + msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        SLogCreator.sInstance.i(TAG, "[" + tag + "] " + msg, tr);
    }

    public static void v(String tag, String msg) {
        SLogCreator.sInstance.v(TAG,"[" + tag + "] " + msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        SLogCreator.sInstance.v(TAG, "[" + tag + "] " + msg, tr);
    }

    public static void d(String tag, String msg) {
        SLogCreator.sInstance.d(TAG, "[" + tag + "] " + msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        SLogCreator.sInstance.d(TAG, "[" + tag + "] " + msg, tr);
    }

    public static void w(String tag, String msg) {
        SLogCreator.sInstance.w(TAG, "[" + tag + "] " + msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        SLogCreator.sInstance.w(TAG, "[" + tag + "] " + msg, tr);
    }

    public static void e(String tag, String msg) {
        SLogCreator.sInstance.e(TAG, "[" + tag + "] " + msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        SLogCreator.sInstance.e(TAG, "[" + tag + "] " + msg, tr);
    }

    private static class SLogCreator {
        // 使用其他Log请替换此实现
        public final static ISLog sInstance = new SimpleDebugSLog();
    }
}
