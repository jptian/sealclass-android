package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.model.DeviceType;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 设备状态同步消息
 */
@MessageTag(value = "SC:DRMsg", flag = MessageTag.NONE)
public class DeviceStateChangedMessage extends MessageContent {
    private final static String TAG = DeviceStateChangedMessage.class.getSimpleName();

    private Boolean enable; // 开启状态
    private int type; // 设备类型:0.麦克风；1.摄像头
    private String userId;

    public DeviceStateChangedMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            enable = jsonObject.optBoolean("enable");
            type = jsonObject.optInt("type");
            userId = jsonObject.optString("userId");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public DeviceStateChangedMessage(Parcel parcel) {
        enable = parcel.readInt() != 0;
        type = parcel.readInt();
        userId = parcel.readString();
    }

    public Boolean getEnable() {
        return enable;
    }

    public DeviceType getType() {
        return DeviceType.getDeviceType(type);
    }

    public String getUserId() {
        return userId;
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
        dest.writeInt(enable ? 1 : 0);
        dest.writeInt(type);
        dest.writeString(userId);
    }

    public static final Creator<DeviceStateChangedMessage> CREATOR = new Creator<DeviceStateChangedMessage>() {
        @Override
        public DeviceStateChangedMessage createFromParcel(Parcel source) {
            return new DeviceStateChangedMessage(source);
        }

        @Override
        public DeviceStateChangedMessage[] newArray(int size) {
            return new DeviceStateChangedMessage[size];
        }
    };
}
