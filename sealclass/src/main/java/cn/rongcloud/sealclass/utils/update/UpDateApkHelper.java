package cn.rongcloud.sealclass.utils.update;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.rongcloud.rtc.media.http.HttpClient;
import cn.rongcloud.rtc.media.http.Request;
import cn.rongcloud.rtc.media.http.RequestMethod;
import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.api.SealClassUrls;


public class UpDateApkHelper {
    private static final String TAG = "UpDateApkHelper";
    private static final String GET_CLIENT_NEW_VERSION = SealClassUrls.DOMAIN + SealClassUrls.APPVERSION_LATEST;
    private Activity activity;

    public UpDateApkHelper(Activity activity) {
        this.activity = activity;
    }

    public void diffVersionFromServer() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("platform", 1);//1:android
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final Request request = new Request.Builder().url(GET_CLIENT_NEW_VERSION).method(RequestMethod.GET)
                .body(jsonObject.toString())
                .build();
        HttpClient.getDefault().request(request, new HttpClient.ResultCallback() {
            @Override
            public void onResponse(String result) {
                if (TextUtils.isEmpty(result)) {
                    return;
                }
                Log.i(TAG, "result :" + result);
                try {
                    JSONObject root = new JSONObject(result);
                    if (root.getInt("errCode") == 0) {
                        JSONObject data = root.getJSONObject("data");
                        JSONArray jsonArray_result = data.getJSONArray("result");
                        JSONObject jsonObject_Platform = null;
                        for (int i = 0; i < jsonArray_result.length(); i++) {
                            jsonObject_Platform = (JSONObject) jsonArray_result.get(i);
                            if (jsonObject_Platform != null) {
                                int platform = jsonObject_Platform.getInt("platform");
                                if (platform == 1) {
                                    break;
                                }
                            }
                        }
                        if (jsonObject_Platform != null) {
                            final String remoteVersion = jsonObject_Platform.getString("version");
                            final String downLoadUrl = jsonObject_Platform.getString("url");
                            String localVersion = getVersionLocal();
                            Log.i(TAG, "onResponse() remote version: " + remoteVersion + " local version: " + localVersion + " downLoadUrl " + downLoadUrl);
                            if (needUpDate(remoteVersion, localVersion)) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showUpdateDialog(remoteVersion, downLoadUrl);
                                    }
                                });
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int errorCode) {
                Log.i(TAG, "onFailure() errorCode = " + errorCode);
            }
        });
    }

    private String getVersionLocal() {
        PackageManager packageManager = activity.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(activity.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("StringFormatInvalid")
    private void showUpdateDialog(String targetVersion, final String downLoadUrl) {
        final AlertDialog dlg = new AlertDialog.Builder(activity).create();
        dlg.setTitle(String.format(activity.getString(R.string.apk_update_title), targetVersion));
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, String.format(activity.getString(R.string.apk_update_dialog_ok), targetVersion), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(activity, R.string.downloading_apk, Toast.LENGTH_SHORT).show();
                UpdateService.Builder.create(downLoadUrl)
                        .setStoreDir("update/flag")
                        .setDownloadSuccessNotificationFlag(Notification.DEFAULT_ALL)
                        .setDownloadErrorNotificationFlag(Notification.DEFAULT_ALL)
                        .build(activity);
                dlg.cancel();
            }
        });

        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, String.format(activity.getString(R.string.apk_update_dialog_cancel), targetVersion), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dlg.cancel();
            }
        });
        dlg.show();
    }

    /**
     * @param localVersion
     * @param remoteVersion
     * @return
     */
    private boolean needUpDate(String remoteVersion, String localVersion) {
        try {
            String[] remoteValues = remoteVersion.split("\\.");
            String[] localValues = localVersion.split("\\.");
            int length = remoteValues.length > localValues.length ? remoteValues.length : localValues.length;
            for (int i = 0; i < length; i++) {
               int remoteValue = remoteValues.length > i ? Integer.valueOf(remoteValues[i]) : 0;
               int localValue = localValues.length > i ? Integer.valueOf(localValues[i]) : 0;
               if (remoteValue > localValue){
                   return true;
               }else if (localValue > remoteValue){
                   return false;
               }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  false;
    }
}
