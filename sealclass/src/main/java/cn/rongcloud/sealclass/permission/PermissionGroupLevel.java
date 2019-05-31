package cn.rongcloud.sealclass.permission;


/**
 * 权限组类型等级
 */
public enum PermissionGroupLevel {

    /**
     * 旁听者权限组
     */
    GROUP_LISTENER(0),
    /**
     * 学员权限组
     */
    GROUP_STUDENT(1),
    /**
     * 讲师权限组
     */
    GROUP_LECTURER(2),
    /**
     * 助教权限组
     */
    GROUP_ASSISTANT(3);


    private int level;
    PermissionGroupLevel(int level) {
         this.level = level;
    }

    public int getLevel() {
        return level;
    }


}
