package cn.rongcloud.sealclass.model;

import java.io.Serializable;
import java.util.List;

public class LoginResult implements Serializable {
    private String authorization;
    private String display;
    private String imToken;
    private String roomId;
    private UserInfo userInfo;
    private List<ClassMember> members;
    private List<WhiteBoard> whiteboards;
    private String appkey;

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getImToken() {
        return imToken;
    }

    public void setImToken(String imToken) {
        this.imToken = imToken;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<ClassMember> getMembers() {
        return members;
    }

    public void setMembers(List<ClassMember> members) {
        this.members = members;
    }

    public List<WhiteBoard> getWhiteboards() {
        return whiteboards;
    }

    public void setWhiteboards(List<WhiteBoard> whiteboards) {
        this.whiteboards = whiteboards;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }
}
