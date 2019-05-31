package cn.rongcloud.sealclass.permission;


/**
 * 权限组工厂类。可更具不同的权限等级生产对应的权限对象。
 */
public class PermissionGroupFactory {
    public static PermissionGroup getPermissionGroup(PermissionGroupLevel role) {
        PermissionGroup permissionGroup =  null;
        switch (role) {
            case GROUP_LISTENER:
                permissionGroup = new ListenerPermissionGroup();
                break;
            case GROUP_STUDENT:
                permissionGroup = new StudentPermissionGroup();
                break;
            case GROUP_LECTURER:
                permissionGroup = new LecturerPermissionGroup();
                break;
            case GROUP_ASSISTANT:
                permissionGroup = new AssistantPermissionGroup();
                break;
            default:
                permissionGroup = new ListenerPermissionGroup();
                break;
        }
        return permissionGroup;
    }
}
