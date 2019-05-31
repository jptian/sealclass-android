package cn.rongcloud.sealclass.model;

/**
 * 用户升级角色
 */
public class UpgradeRoleInvite {
    private String ticket;
    private String opUserId;
    private String opUserName;
    private Role role;
    private InviteAction action;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public InviteAction getAction() {
        return action;
    }

    public void setAction(InviteAction action) {
        this.action = action;
    }
}
