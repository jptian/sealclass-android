package cn.rongcloud.sealclass.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RoleChangedUser implements Parcelable {
    private String userId;
    private String userName;
    private int role;

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Role getRole() {
        return Role.createRole(role);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRole(int role) {
        this.role = role;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeInt(role);
    }

    public static final Creator<RoleChangedUser> CREATOR = new Parcelable.Creator<RoleChangedUser>() {
        @Override
        public RoleChangedUser createFromParcel(Parcel source) {
            RoleChangedUser roleChangedUser = new RoleChangedUser();
            roleChangedUser.userId = source.readString();
            roleChangedUser.userName = source.readString();
            roleChangedUser.role = source.readInt();
            return roleChangedUser;
        }

        @Override
        public RoleChangedUser[] newArray(int size) {
            return new RoleChangedUser[size];
        }
    };
}
