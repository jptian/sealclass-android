package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 请求超时消息
 */
@MessageTag(value = "SC:TEMsg", flag = MessageTag.NONE)
public class TicketExpiredMessage extends MessageContent {
    private final static String TAG = ControlDeviceNotifyMessage.class.getSimpleName();
    private String ticket;
    private String fromUserId;//请求发言者
    private String toUserId;//处理请求者

    public TicketExpiredMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            ticket = jsonObject.optString("ticket");
            fromUserId = jsonObject.optString("fromUserId");
            toUserId = jsonObject.optString("toUserId");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public TicketExpiredMessage(Parcel parcel) {
        ticket = parcel.readString();
        fromUserId = parcel.readString();
        toUserId = parcel.readString();
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getTicket() {
        return ticket;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ticket);
        dest.writeString(fromUserId);
        dest.writeString(toUserId);
    }

    public static final Creator<TicketExpiredMessage> CREATOR = new Creator<TicketExpiredMessage>() {
        @Override
        public TicketExpiredMessage createFromParcel(Parcel source) {
            return new TicketExpiredMessage(source);
        }

        @Override
        public TicketExpiredMessage[] newArray(int size) {
            return new TicketExpiredMessage[size];
        }
    };
}
