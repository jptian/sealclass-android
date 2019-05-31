package cn.rongcloud.sealclass.model;

/**
 * 邀请设备开启控制
 */
public class DeviceControlInvite {
    private String ticket;  // 请求凭证 Id
    private String opUserId; // 操作人 用户id
    private String opUserName; // 操作人用户名称
    private InviteAction action; // 操作行为
    private DeviceType deviceType; // 操作设备

    public String getOpUserId() {
        return opUserId;
    }

    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId;
    }

    public String getOpUserName() {
        return opUserName;
    }

    public void setOpUserName(String opUserName) {
        this.opUserName = opUserName;
    }

    public InviteAction getAction() {
        return action;
    }

    public void setAction(InviteAction action) {
        this.action = action;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
