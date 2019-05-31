package cn.rongcloud.sealclass.utils;

import android.content.Context;
import android.os.Build;

import java.lang.reflect.Method;

import cn.rongcloud.sealclass.utils.log.SLog;

// 非 Android P 刘海适配
public class NotchScreenUtils {

    private static final int VIVO_NOTCH = 0x00000020;//是否有刘海


    //huawei
    private static boolean hasNotchInHuawei(Context context) {
        boolean hasNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method hasNotchInScreen = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            if(hasNotchInScreen != null) {
                hasNotch = (boolean) hasNotchInScreen.invoke(HwNotchSizeUtil);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNotch;
    }

    // Oppo
    private static boolean hasNotchInOppo(Context context) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    // vivo
    private static boolean hasNotchAtVivo(Context context) {
        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (boolean) method.invoke(FtFeature, VIVO_NOTCH);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return ret;
        }
    }


    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight ;
    }



    private static int[] getHuaweiNotchSize(Context context) {
        int[] ret = new int[]{0, 0};
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("getNotchSize");
            ret = (int[]) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return ret;
        }
    }

    private static boolean hasNotch(Context context) {
        String manufacturer = Build.MANUFACTURER;
        if ( manufacturer.equalsIgnoreCase("oppo")) {
            return hasNotchInOppo(context);
        } else if (manufacturer.equalsIgnoreCase("vivo"))  {
            return hasNotchAtVivo(context);
        } else if (manufacturer.equalsIgnoreCase("huawei"))  {
           return hasNotchInHuawei(context);
        } else if (manufacturer.equalsIgnoreCase("xiaomi"))  {

        }
        return false;
    }

    private static int getOffSetByManufacturer(Context context) {
        String manufacturer = Build.MANUFACTURER;
        if ( manufacturer.equalsIgnoreCase("oppo") || manufacturer.equalsIgnoreCase("vivo")
            || manufacturer.equalsIgnoreCase("huawei"))  {
            return getStatusBarHeight(context);
        } else if (manufacturer.equalsIgnoreCase("xiaomi"))  {

        }
        return 0;
    }

    public static int getOffset(Context context) {
        boolean hasNotch = hasNotch(context);
        SLog.d("NotchScreen", "hasNotch = " + hasNotch);
        if (hasNotch) {
            return getOffSetByManufacturer(context);
        }
        return 0;
    }
}
