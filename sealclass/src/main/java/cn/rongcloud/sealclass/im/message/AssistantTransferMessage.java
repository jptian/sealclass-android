package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 助教转移消息
 */
@MessageTag(value = "SC:ATMsg", flag = MessageTag.NONE)
public class AssistantTransferMessage extends MessageContent {
    private final static String TAG = AssistantTransferMessage.class.getSimpleName();

    private String opUserId;//操作者 ID
    private String toUserId;//接受者 ID

    public AssistantTransferMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            opUserId = jsonObject.optString("opUserId");
            toUserId = jsonObject.optString("toUserId");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public AssistantTransferMessage(Parcel parcel) {
        opUserId = parcel.readString();
        toUserId = parcel.readString();
    }

    public String getOpUserId() {
        return opUserId;
    }

    public String getToUserId() {
        return toUserId;
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
        dest.writeString(opUserId);
        dest.writeString(toUserId);
    }

    public static final Creator<AssistantTransferMessage> CREATOR = new Creator<AssistantTransferMessage>() {
        @Override
        public AssistantTransferMessage createFromParcel(Parcel source) {
            return new AssistantTransferMessage(source);
        }

        @Override
        public AssistantTransferMessage[] newArray(int size) {
            return new AssistantTransferMessage[size];
        }
    };
}
