package cn.rongcloud.sealclass.utils;

import android.widget.Toast;

import cn.rongcloud.sealclass.SealClassApp;
import cn.rongcloud.sealclass.common.ErrorCode;

/**
 * Toast 工具类
 */
public class ToastUtils {
    private static Toast lastToast;

    public static void showErrorToast(ErrorCode errorCode) {
        //根据错误码进行对应错误提示
        int errorMsgResourceId = errorCode.getMessageResId();

        // 特殊错误码不做提示
        if(errorMsgResourceId == 0) return;

        String message = SealClassApp.getApplication().getResources().getString(errorMsgResourceId);
        if (lastToast != null) {
            lastToast.setText(message);
        } else {
            lastToast = Toast.makeText(SealClassApp.getApplication(), message, Toast.LENGTH_SHORT);
        }
        lastToast.show();
    }

    public static void showErrorToast(int errorCode) {
        showErrorToast(ErrorCode.fromCode(errorCode));
    }

    public static void showToast(int resourceId) {
        showToast(resourceId, Toast.LENGTH_SHORT);
    }

    public static void showToast(int resourceId, int duration) {
        showToast(SealClassApp.getApplication().getResources().getString(resourceId), duration);
    }

    public static void showToast(String message) {
        showToast(message, Toast.LENGTH_SHORT);
    }

    public static void showToast(String message, int duration) {
        if (lastToast != null) {
            lastToast.setText(message);
        } else {
            lastToast = Toast.makeText(SealClassApp.getApplication(), message, duration);
        }
        lastToast.show();
    }
}
