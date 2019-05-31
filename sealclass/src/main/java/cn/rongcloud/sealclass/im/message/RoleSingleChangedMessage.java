package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.model.RoleChangedUser;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 单用户角色改变消息
 * 此消息仅用于本地显示单条角色变化显示用
 */
@MessageTag(value = "SCX:RCMsg", flag = MessageTag.ISPERSISTED)
public class RoleSingleChangedMessage extends MessageContent {
    private final static String TAG = RoleSingleChangedMessage.class.getSimpleName();

    private String opUserId;
    private RoleChangedUser user;

    public RoleSingleChangedMessage(){
    }

    public RoleSingleChangedMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            opUserId = jsonObject.optString("opUserId");
            JSONObject usersJson = jsonObject.optJSONObject("user");
            RoleChangedUser user = new RoleChangedUser();
            user.setUserId(usersJson.optString("userId"));
            user.setUserName(usersJson.optString("userName"));
            user.setRole(usersJson.optInt("role"));
            this.user = user;
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public RoleSingleChangedMessage(Parcel parcel) {
        opUserId = parcel.readString();
        user = parcel.readParcelable(RoleChangedUser.class.getClassLoader());
    }

    public String getOpUserId() {
        return opUserId;
    }

    public RoleChangedUser getUser() {
        return user;
    }

    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId;
    }

    public void setUser(RoleChangedUser user) {
        this.user = user;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("opUserId", opUserId);

            JSONObject userJson = new JSONObject();
            userJson.put("userId", user.getUserId());
            userJson.put("userName", user.getUserName());
            userJson.put("role", user.getRole().getValue());
            jsonObject.put("user", userJson);
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
        dest.writeString(opUserId);
        dest.writeParcelable(user, 0);
    }

    public static final Creator<RoleSingleChangedMessage> CREATOR = new Creator<RoleSingleChangedMessage>() {
        @Override
        public RoleSingleChangedMessage createFromParcel(Parcel source) {
            return new RoleSingleChangedMessage(source);
        }

        @Override
        public RoleSingleChangedMessage[] newArray(int size) {
            return new RoleSingleChangedMessage[size];
        }
    };
}
