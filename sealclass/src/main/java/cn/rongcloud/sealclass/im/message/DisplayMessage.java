package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.model.ScreenDisplay;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 切换显示消息
 */
@MessageTag(value = "SC:DisplayMsg", flag = MessageTag.NONE)
public class DisplayMessage extends MessageContent {
    private final static String TAG = DisplayMessage.class.getSimpleName();
    private String display;

    public DisplayMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            display = jsonObject.optString("display");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public DisplayMessage(Parcel parcel) {
        display = parcel.readString();
    }

    public ScreenDisplay getDisplay() {
        return ScreenDisplay.createScreenDisplay(display);
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
        dest.writeString(display);
    }

    public static final Creator<DisplayMessage> CREATOR = new Creator<DisplayMessage>() {
        @Override
        public DisplayMessage createFromParcel(Parcel source) {
            return new DisplayMessage(source);
        }

        @Override
        public DisplayMessage[] newArray(int size) {
            return new DisplayMessage[size];
        }
    };
}
