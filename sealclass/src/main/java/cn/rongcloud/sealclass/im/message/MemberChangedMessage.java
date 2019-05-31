package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.model.ClassMemberChangedAction;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 课堂内用户进出消息
 */
@MessageTag(value = "SC:RMCMsg", flag = MessageTag.ISPERSISTED)
public class MemberChangedMessage extends MessageContent {
    private final static String TAG = MemberChangedMessage.class.getSimpleName();
    private int action;     // 用户行为：1.加入；2.离开；3.踢出
    private String userId;
    private String userName;
    private int role;       // 用户角色
    private long timestamp;

    public MemberChangedMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            action = jsonObject.optInt("action");
            userId = jsonObject.optString("userId");
            userName = jsonObject.optString("userName");
            role = jsonObject.optInt("role");
            timestamp = jsonObject.optLong("timestamp");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public MemberChangedMessage(Parcel in) {
        action = in.readInt();
        userId = in.readString();
        userName = in.readString();
        role = in.readInt();
        timestamp = in.readLong();
    }

    public ClassMemberChangedAction getAction() {
        return ClassMemberChangedAction.getAction(action);
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Role getRole() {
        return Role.createRole(role);
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", action);
            jsonObject.put("userId", userId);
            jsonObject.put("userName", userName);
            jsonObject.put("role", role);
            jsonObject.put("timestamp", timestamp);
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }

        try {
            return jsonObject.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        return new byte[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(action);
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeInt(role);
        dest.writeLong(timestamp);
    }

    public static final Creator<MemberChangedMessage> CREATOR = new Creator<MemberChangedMessage>() {
        @Override
        public MemberChangedMessage createFromParcel(Parcel source) {
            return new MemberChangedMessage(source);
        }

        @Override
        public MemberChangedMessage[] newArray(int size) {
            return new MemberChangedMessage[size];
        }
    };
}
