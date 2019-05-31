package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.model.DeviceType;
import cn.rongcloud.sealclass.model.InviteAction;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 邀请开启设备消息
 * 包括开启摄像头和开启麦克风
 */
@MessageTag(value = "SC:CDNMsg", flag = MessageTag.NONE)
public class ControlDeviceNotifyMessage extends MessageContent {
    private final static String TAG = ControlDeviceNotifyMessage.class.getSimpleName();

    private int action;  // 1.邀请；2.拒绝；3.接受
    private String ticket;
    private int type;       //邀请类型：0.麦克风；1.摄像头
    private String opUserId; // 操作人用户id
    private String opUserName; // 操作人用户名

    public ControlDeviceNotifyMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            action = jsonObject.optInt("action");
            ticket = jsonObject.optString("ticket");
            type = jsonObject.optInt("type");
            opUserId = jsonObject.optString("opUserId");
            opUserName = jsonObject.optString("opUserName");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public ControlDeviceNotifyMessage(Parcel parcel) {
        action = parcel.readInt();
        ticket = parcel.readString();
        type = parcel.readInt();
        opUserId = parcel.readString();
        opUserName = parcel.readString();
    }

    public InviteAction getAction() {
        return InviteAction.getAction(action);
    }

    public String getTicket() {
        return ticket;
    }

    public DeviceType getType() {
        return DeviceType.getDeviceType(type);
    }

    public String getOpUserId() {
        return opUserId;
    }

    public String getOpUserName() {
        return opUserName;
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
        dest.writeInt(action);
        dest.writeString(ticket);
        dest.writeInt(type);
        dest.writeString(opUserId);
        dest.writeString(opUserName);
    }

    public static final Creator<ControlDeviceNotifyMessage> CREATOR = new Creator<ControlDeviceNotifyMessage>() {
        @Override
        public ControlDeviceNotifyMessage createFromParcel(Parcel source) {
            return new ControlDeviceNotifyMessage(source);
        }

        @Override
        public ControlDeviceNotifyMessage[] newArray(int size) {
            return new ControlDeviceNotifyMessage[size];
        }
    };
}
