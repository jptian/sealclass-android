package cn.rongcloud.sealclass.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.sealclass.model.InviteAction;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 升级角色权限消息，被邀请人收到此消息
 */
@MessageTag(value = "SC:IURMsg", flag = MessageTag.NONE)
public class UpgradeRoleMessage extends MessageContent {
    private final static String TAG = RoleChangedMessage.class.getSimpleName();

    private int action;     // 行为类型：1.邀请;2.拒绝；3.同意
    private String opUserId;
    private String opUserName;
    private int role;       // 被提升到的权限
    private String ticket;  // 凭证 id

    public UpgradeRoleMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            SLog.e(TAG, e.toString());
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            action = jsonObject.optInt("action");
            opUserId = jsonObject.optString("opUserId");
            opUserName = jsonObject.optString("opUserName");
            role = jsonObject.optInt("role");
            ticket = jsonObject.optString("ticket");
        } catch (JSONException e) {
            SLog.e(TAG, e.toString());
        }
    }

    public UpgradeRoleMessage(Parcel parcel) {
        action = parcel.readInt();
        opUserId = parcel.readString();
        opUserName = parcel.readString();
        role = parcel.readInt();
        ticket = parcel.readString();
    }

    public InviteAction getAction() {
        return InviteAction.getAction(action);
    }

    public String getOpUserId() {
        return opUserId;
    }

    public String getOpUserName() {
        return opUserName;
    }

    public Role getRole() {
        return Role.createRole(role);
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
        dest.writeInt(action);
        dest.writeString(opUserId);
        dest.writeString(opUserName);
        dest.writeInt(role);
        dest.writeString(ticket);
    }

    public static final Creator<UpgradeRoleMessage> CREATOR = new Creator<UpgradeRoleMessage>() {
        @Override
        public UpgradeRoleMessage createFromParcel(Parcel source) {
            return new UpgradeRoleMessage(source);
        }

        @Override
        public UpgradeRoleMessage[] newArray(int size) {
            return new UpgradeRoleMessage[size];
        }
    };

}
