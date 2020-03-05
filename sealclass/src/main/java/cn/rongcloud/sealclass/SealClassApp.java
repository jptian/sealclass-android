package cn.rongcloud.sealclass;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;

import androidx.multidex.MultiDexApplication;

import java.util.Iterator;
import java.util.List;

import cn.rongcloud.rtc.CenterManager;
import cn.rongcloud.sealclass.im.IMManager;
import cn.rongcloud.sealclass.utils.ClassNotificationService;
import cn.rongcloud.sealclass.utils.SessionManager;
import cn.rongcloud.sealclass.utils.Utils;
import cn.rongcloud.sealclass.utils.log.SLog;

public class SealClassApp extends MultiDexApplication {
    private static SealClassApp instance;
    private int mActiveCount = 0;
    private int mAliveCount=0;
    private boolean isActive;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // 初始化日志打印
        SLog.init(this);

        /*
         * 以上部分在所有进程中会执行
         */
        if (!getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {
            return;
        }
        /*
         * 以下部分仅在主进程中进行执行
         */

        Utils.init(this);
        SessionManager.getInstance().initContext(this);
        registerLifecycleCallbacks();
    }


    /**
     * 获取当前进程的名称
     *
     * @param context
     * @return
     */
    public String getCurProcessName(Context context) {
        int pid = Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = mActivityManager.getRunningAppProcesses();
        if (runningAppProcessInfoList == null) {
            return null;
        } else {
            Iterator processInfoIterator = runningAppProcessInfoList.iterator();

            ActivityManager.RunningAppProcessInfo appProcess;
            do {
                if (!processInfoIterator.hasNext()) {
                    return null;
                }

                appProcess = (ActivityManager.RunningAppProcessInfo) processInfoIterator.next();
            } while (appProcess.pid != pid);

            return appProcess.processName;
        }
    }

    /**
     * 获取当前 Application 实例
     *
     * @return
     */
    public static SealClassApp getApplication() {
        return instance;
    }


    private void registerLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                mAliveCount++;
            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActiveCount++;
                notifyChange();
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActiveCount--;
                notifyChange();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                mAliveCount--;
                if (mAliveCount == 0) {
                    stopNotificationService();
                }
            }
        });
    }

    private void notifyChange() {
        if (mActiveCount > 0) {
            if (!isActive) {
                isActive = true;
                //AppForeground
                stopNotificationService();
            }
        } else {
            if (isActive) {
                isActive = false;
                //AppBackground
                if (CenterManager.getInstance().isInRoom()) {
                    startService(new Intent(this, ClassNotificationService.class));
                }
            }
        }
    }

    private void stopNotificationService() {
        if (CenterManager.getInstance().isInRoom()) {
            stopService(new Intent(SealClassApp.this, ClassNotificationService.class));
        }
    }
}
