package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.model.WhiteBoardAction;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 课堂白板变动消息
 */
@MessageTag(value = "SC:WBMsg", flag = MessageTag.NONE)
public class WhiteBoardMessage extends MessageContent {
    private final static String TAG = WhiteBoardMessage.class.getSimpleName();
    private String whiteboardId;
    private String whiteboardName;
    private int action; // 白板变动行为：1.创建；2.删除

    public WhiteBoardMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            action = jsonObject.optInt("action");
            whiteboardId = jsonObject.optString("whiteboardId");
            whiteboardName = jsonObject.optString("whiteboardName");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public WhiteBoardMessage(Parcel in) {
        whiteboardId = in.readString();
        whiteboardName = in.readString();
        action = in.readInt();
    }

    public String getWhiteboardId() {
        return whiteboardId;
    }

    public String getWhiteboardName() {
        return whiteboardName;
    }

    public WhiteBoardAction getAction() {
        return WhiteBoardAction.getAction(action);
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
        dest.writeString(whiteboardId);
        dest.writeString(whiteboardName);
        dest.writeInt(action);
    }

    public static final Creator<WhiteBoardMessage> CREATOR = new Creator<WhiteBoardMessage>() {
        @Override
        public WhiteBoardMessage createFromParcel(Parcel source) {
            return new WhiteBoardMessage(source);
        }

        @Override
        public WhiteBoardMessage[] newArray(int size) {
            return new WhiteBoardMessage[size];
        }
    };
}
