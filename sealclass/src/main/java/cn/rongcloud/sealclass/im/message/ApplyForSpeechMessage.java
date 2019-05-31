package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 旁听申请发言消息
 */
@MessageTag(value = "SC:RSMsg", flag = MessageTag.NONE)
public class ApplyForSpeechMessage extends MessageContent {
    private final static String TAG = ApplyForSpeechMessage.class.getSimpleName();

    private String reqUserId;
    private String reqUserName;
    private String ticket;

    public ApplyForSpeechMessage(byte[] data){
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            reqUserId = jsonObject.optString("reqUserId");
            reqUserName = jsonObject.optString("reqUserName");
            ticket = jsonObject.optString("ticket");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public ApplyForSpeechMessage(Parcel parcel){
        reqUserId = parcel.readString();
        reqUserName = parcel.readString();
        ticket = parcel.readString();
    }

    public String getReqUserId() {
        return reqUserId;
    }

    public String getReqUserName() {
        return reqUserName;
    }

    public String getTicket() {
        return ticket;
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
        dest.writeString(reqUserId);
        dest.writeString(reqUserName);
        dest.writeString(ticket);
    }

    public static final Creator<ApplyForSpeechMessage> CREATOR = new Creator<ApplyForSpeechMessage>() {
        @Override
        public ApplyForSpeechMessage createFromParcel(Parcel source) {
            return new ApplyForSpeechMessage(source);
        }

        @Override
        public ApplyForSpeechMessage[] newArray(int size) {
            return new ApplyForSpeechMessage[size];
        }
    };
}
