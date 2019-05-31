package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 申请发言返回结果消息
 */
@MessageTag(value = "SC:SRMsg", flag = MessageTag.NONE)
public class SpeechResultMessage extends MessageContent {
    private final static String TAG = SpeechResultMessage.class.getSimpleName();

    private String opUserId;    //操作用户id
    private String opUserName;
    private String reqUserId;   //请求用户id
    private String reqUserName;
    private int action;         // 1.同意；2.拒绝

    public SpeechResultMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            opUserId = jsonObject.optString("opUserId");
            opUserName = jsonObject.optString("opUserName");
            reqUserId = jsonObject.optString("reqUserId");
            reqUserName = jsonObject.optString("reqUserName");
            action = jsonObject.optInt("action");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public String getOpUserId() {
        return opUserId;
    }

    public String getOpUserName() {
        return opUserName;
    }

    public String getReqUserId() {
        return reqUserId;
    }

    public String getReqUserName() {
        return reqUserName;
    }

    public boolean isAccept() {
        // 1.同意；2.拒绝
        return action == 1;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    public SpeechResultMessage(Parcel parcel) {
        opUserId = parcel.readString();
        opUserName = parcel.readString();
        reqUserId = parcel.readString();
        reqUserName = parcel.readString();
        action = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(opUserId);
        dest.writeString(opUserName);
        dest.writeString(reqUserId);
        dest.writeString(reqUserName);
        dest.writeInt(action);
    }

    public static final Creator<SpeechResultMessage> CREATOR = new Creator<SpeechResultMessage>() {
        @Override
        public SpeechResultMessage createFromParcel(Parcel source) {
            return new SpeechResultMessage(source);
        }

        @Override
        public SpeechResultMessage[] newArray(int size) {
            return new SpeechResultMessage[size];
        }
    };
}
