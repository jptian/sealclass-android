package cn.rongcloud.sealclass.permission;

import java.util.List;

class AssistantPermissionGroup extends LecturerPermissionGroup {
    @Override
    public PermissionGroupLevel permissionGroupRole() {
        return PermissionGroupLevel.GROUP_ASSISTANT;
    }

    @Override
    protected List<ClassExecutedPermission> onInitExecutedPermissions() {
        return null;
    }

    @Override
    protected List<ClassPermission> onInitPermissions() {
        List<ClassPermission> classPermissions = super.onInitPermissions();
        classPermissions.add(ClassPermission.TRANSFER_ROLE);
        classPermissions.add(ClassPermission.CONTROL_MEMBER_MIC);
        classPermissions.add(ClassPermission.CONTROL_MEMBER_CAMERA);
        classPermissions.add(ClassPermission.UPGRADE_MEMBER);
        classPermissions.add(ClassPermission.DOWNGRADE_MEMBER);
        classPermissions.add(ClassPermission.KICK_OFF_MEMBER);
        classPermissions.remove(ClassPermission.LECTURE);
        return classPermissions;
    }
}
