package cn.rongcloud.sealclass.model;

/**
 * 请求行为
 */
public enum InviteAction {
    INVITE(1), // 邀请

    REJECT(2), // 拒绝

    APPROVE(3), // 同意

    UNKNOWN(-999);

    private int type;

    InviteAction(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static InviteAction getAction(int type) {
        InviteAction[] values = InviteAction.values();
        for (InviteAction action : values) {
            if (action.type == type) {
                return action;
            }
        }

        return UNKNOWN;
    }
}
