package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 切换白板页数消息
 */
@MessageTag(value = "SC:WBTPMsg", flag = MessageTag.NONE)
public class TurnPageMessage extends MessageContent {
    private final static String TAG = TurnPageMessage.class.getSimpleName();

    private String whiteboardId;  //白板 ID
    private String userId; //操作人 ID
    private int curPg; //当前页

    public TurnPageMessage(byte[] data){
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            whiteboardId = jsonObject.optString("whiteboardId");
            userId = jsonObject.optString("userId");
            curPg = jsonObject.optInt("curPg");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public TurnPageMessage(Parcel parcel){
        whiteboardId = parcel.readString();
        userId = parcel.readString();
        curPg = parcel.readInt();
    }

    public String getWhiteboardId() {
        return whiteboardId;
    }

    public String getUserId() {
        return userId;
    }

    public int getCurPg() {
        return curPg;
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

    }

    public static final Creator<TurnPageMessage> CREATOR = new Creator<TurnPageMessage>() {
        @Override
        public TurnPageMessage createFromParcel(Parcel source) {
            return new TurnPageMessage(source);
        }

        @Override
        public TurnPageMessage[] newArray(int size) {
            return new TurnPageMessage[size];
        }
    };
}
