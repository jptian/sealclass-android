package cn.rongcloud.sealclass.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import io.rong.imlib.common.RongLibConst;

public class Utils {
    public static final String SCREEN_SHARING = "ScreenSharing";
    private static Context mContext = null;
    private static Map<String, Long> mapLastClickTime = new HashMap<>();

    public static final String KEY_screeHeight = "screeHeight";
    public static final String KEY_screeWidth = "screeWidth";

    private Utils() {
        throw new UnsupportedOperationException("RongRTCUtils Error!");
    }

    public static void init(Context context) {
        Utils.mContext = context.getApplicationContext();
    }

    public static Context getContext() {
        if (null != mContext) {
            return mContext;
        }
        throw new NullPointerException("u should context init first");
    }

    /**
     * 尽量保证ANDROID_ID的稳定
     *
     * @return
     */
    public static String getDeviceId() {
        SharedPreferences sp = mContext.getSharedPreferences(RongLibConst.SP_STATISTICS, 0);
        String deviceId = sp.getString("ANDROID_ID", "");
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            if (TextUtils.isEmpty(deviceId)) {
                SecureRandom random = new SecureRandom();
                deviceId = new BigInteger(64, random).toString(16);
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("ANDROID_ID", deviceId);
            editor.commit();
        }
        return deviceId;
    }
}
