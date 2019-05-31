package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.model.RoleChangedUser;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 多用户角色改变消息
 */
@MessageTag(value = "SC:RCMsg", flag = MessageTag.NONE)
public class RoleChangedMessage extends MessageContent {
    private final static String TAG = RoleChangedMessage.class.getSimpleName();

    private String opUserId;
    private List<RoleChangedUser> users;


    public RoleChangedMessage(){
    }
    public RoleChangedMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            opUserId = jsonObject.optString("opUserId");
            JSONArray usersJson = jsonObject.optJSONArray("users");
            users = new ArrayList<>();
            if (usersJson != null && usersJson.length() > 0) {
                int length = usersJson.length();
                for (int i = 0; i < length; i++) {
                    JSONObject usersJSONObject = usersJson.getJSONObject(i);
                    RoleChangedUser user = new RoleChangedUser();
                    user.setUserId(usersJSONObject.optString("userId"));
                    user.setUserName(usersJSONObject.optString("userName"));
                    user.setRole(usersJSONObject.optInt("role"));
                    users.add(user);
                }
            }
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public RoleChangedMessage(Parcel parcel) {
        opUserId = parcel.readString();
        users = parcel.createTypedArrayList(RoleChangedUser.CREATOR);
    }

    public String getOpUserId() {
        return opUserId;
    }

    public List<RoleChangedUser> getUsers() {
        return users;
    }

    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId;
    }

    public void setUsers(List<RoleChangedUser> users) {
        this.users = users;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("opUserId", opUserId);

            if(users != null && users.size() > 0){
                JSONArray jsonArray = new JSONArray();
                for(RoleChangedUser user : users) {
                    JSONObject userJson = new JSONObject();
                    userJson.put("userId", user.getUserId());
                    userJson.put("userName", user.getUserName());
                    userJson.put("role", user.getRole().getValue());
                    jsonArray.put(userJson);
                }
                jsonObject.put("users", jsonArray);
            }
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
        dest.writeTypedList(users);
    }

    public static final Creator<RoleChangedMessage> CREATOR = new Creator<RoleChangedMessage>() {
        @Override
        public RoleChangedMessage createFromParcel(Parcel source) {
            return new RoleChangedMessage(source);
        }

        @Override
        public RoleChangedMessage[] newArray(int size) {
            return new RoleChangedMessage[size];
        }
    };
}
