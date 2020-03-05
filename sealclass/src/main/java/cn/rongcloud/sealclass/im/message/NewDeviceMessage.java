package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

@MessageTag(value = "SC:NewDeviceMsg", flag = MessageTag.NONE)
public class NewDeviceMessage extends MessageContent {

    private final static String TAG = NewDeviceMessage.class.getSimpleName();

    private String deviceId;
    private String deviceType;
    private int platform; //Android 1, iOS 2
    private long updateDt; // 操作时间

    public NewDeviceMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            deviceId = jsonObject.optString("deviceId");
            deviceType = jsonObject.optString("deviceType");
            platform = jsonObject.optInt("platform");
            updateDt = jsonObject.optLong("updateDt");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public NewDeviceMessage(Parcel parcel) {
        deviceId = parcel.readString();
        deviceType = parcel.readString();
        platform = parcel.readInt();
        updateDt = parcel.readLong();
    }

    public static String getTAG() {
        return TAG;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public int getPlatform() {
        return platform;
    }

    public long getUpdateDt() {
        return updateDt;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceId);
        dest.writeString(deviceType);
        dest.writeInt(platform);
        dest.writeLong(updateDt);
    }

    public static final Creator<NewDeviceMessage> CREATOR = new Creator<NewDeviceMessage>() {
        @Override
        public NewDeviceMessage createFromParcel(Parcel source) {
            return new NewDeviceMessage(source);
        }

        @Override
        public NewDeviceMessage[] newArray(int size) {
            return new NewDeviceMessage[size];
        }
    };
}
