package cn.rongcloud.sealclass.model;

/**
 * 描述用户进出课堂的行为
 */
public enum ClassMemberChangedAction {
    JOIN(1),
    LEAVE(2),
    KICK(3),
    DESTROY(4),
    UNKNOWN(-999);

    private int value;

    ClassMemberChangedAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static ClassMemberChangedAction getAction(int value) {
        ClassMemberChangedAction[] values = ClassMemberChangedAction.values();
        for (ClassMemberChangedAction action : values) {
            if (action.value == value) {
                return action;
            }
        }

        return UNKNOWN;
    }
}
