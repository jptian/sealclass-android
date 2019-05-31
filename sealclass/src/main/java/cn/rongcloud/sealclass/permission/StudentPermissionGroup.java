package cn.rongcloud.sealclass.permission;

import java.util.List;

class StudentPermissionGroup extends ListenerPermissionGroup{

    @Override
    public PermissionGroupLevel permissionGroupRole() {
        return PermissionGroupLevel.GROUP_STUDENT;
    }


    @Override
    protected List<ClassPermission> onInitPermissions() {
        List<ClassPermission> classPermissions = super.onInitPermissions();
        classPermissions.remove(ClassPermission.APPLY_SPEECH);
        classPermissions.add(ClassPermission.VIDEO_CHAT);
        classPermissions.add(ClassPermission.AUDIO_CHAT);
        classPermissions.add(ClassPermission.USE_WHITE_BOARD);
        return classPermissions;
    }

    @Override
    protected List<ClassExecutedPermission> onInitExecutedPermissions() {
        List<ClassExecutedPermission> classExecutedPermissions = super.onInitExecutedPermissions();
        classExecutedPermissions.add(ClassExecutedPermission.DOWNGRADE);
        classExecutedPermissions.add(ClassExecutedPermission.CONTROL_VIDEO);
        classExecutedPermissions.add(ClassExecutedPermission.CONTROL_MIC);
        classExecutedPermissions.add(ClassExecutedPermission.ACCEPT_TRANSFER_ROLE);
        return classExecutedPermissions;
    }
}
