package cn.rongcloud.sealclass.model;


import cn.rongcloud.sealclass.permission.ClassPermission;
import cn.rongcloud.sealclass.permission.ClassExecutedPermission;
import cn.rongcloud.sealclass.permission.PermissionGroup;
import cn.rongcloud.sealclass.permission.PermissionGroupFactory;
import cn.rongcloud.sealclass.permission.PermissionGroupLevel;

/**
 * 权限组类型等级
 */
public enum Role {
    /**
     * 助教权限组
     */
    ASSISTANT(1,PermissionGroupLevel.GROUP_ASSISTANT),
    /**
     * 讲师权限组
     */
    LECTURER(2, PermissionGroupLevel.GROUP_LECTURER),

    /**
     * 学员权限组
     */
    STUDENT(3, PermissionGroupLevel.GROUP_STUDENT),
    /**
     * 旁听者权限组
     */
    LISTENER(4, PermissionGroupLevel.GROUP_LISTENER);



    private PermissionGroup permGroup;

    private PermissionGroupLevel level;

    private int value;



    Role(int value, PermissionGroupLevel level) {
         this.value = value;
         this.level = level;
         permGroup = PermissionGroupFactory.getPermissionGroup(level);
    }

    public int getValue() {
        return value;
    }

    /**
     * 是否存在可执行的权限
     * @param permission 可执行权限， 具体可查看{@link ClassPermission}
     * @return true 存在； false 不存在
     */
    public boolean hasPermission(ClassPermission permission) {
        return permGroup.hasPermission(permission);
    }

    /**
     * 是否存在可被执行的权限
     * @param permission 被执行权限， 具体可查看{@link ClassExecutedPermission}
     * @return true 存在； false 不存在
     */
    public boolean hasExecutedPermission(ClassExecutedPermission permission) {
        return permGroup.hasExecutedPermission(permission);
    }

    public static Role createRole(int value) {
        Role[] roles = Role.values();
        for (Role role : roles) {
            if  (role.getValue() == value) {
                return role;
            }
        }
        return Role.LISTENER;
    }

}
