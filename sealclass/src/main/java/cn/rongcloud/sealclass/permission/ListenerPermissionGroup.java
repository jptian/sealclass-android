package cn.rongcloud.sealclass.permission;

import java.util.ArrayList;
import java.util.List;

class ListenerPermissionGroup extends RolePermissionGroup {

    @Override
    public PermissionGroupLevel permissionGroupRole() {
        return PermissionGroupLevel.GROUP_LISTENER;
    }

    @Override
    protected List<ClassPermission> onInitPermissions() {
        List<ClassPermission> permissions = new ArrayList<>();
        permissions.add(ClassPermission.LOOK_MEMBER_LIST);
        permissions.add(ClassPermission.LOOK_MEMBER_VIDEO_LIST);
        permissions.add(ClassPermission.IM_CHAT);
        permissions.add(ClassPermission.CONTROL_SOUND);
        permissions.add(ClassPermission.APPLY_SPEECH);
        return permissions;
    }

    @Override
    protected List<ClassExecutedPermission> onInitExecutedPermissions() {
        List<ClassExecutedPermission> permissions = new ArrayList<>();
        permissions.add(ClassExecutedPermission.UPGRADE);
        permissions.add(ClassExecutedPermission.KICK_OFF);
        return permissions;
    }


}
