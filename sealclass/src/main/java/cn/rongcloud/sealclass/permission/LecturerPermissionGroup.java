package cn.rongcloud.sealclass.permission;

import java.util.List;

public class LecturerPermissionGroup extends StudentPermissionGroup {
    @Override
    public PermissionGroupLevel permissionGroupRole() {
        return PermissionGroupLevel.GROUP_LECTURER;
    }

    @Override
    protected List<ClassExecutedPermission> onInitExecutedPermissions() {
        List<ClassExecutedPermission> classExecutedPermissions = super.onInitExecutedPermissions();
        classExecutedPermissions.remove(ClassExecutedPermission.UPGRADE);
        return classExecutedPermissions;
    }

    @Override
    protected List<ClassPermission> onInitPermissions() {
        List<ClassPermission> classPermissions = super.onInitPermissions();
        classPermissions.add(ClassPermission.LECTURE);
        classPermissions.add(ClassPermission.CREATE_WHITE_BOARD);
        classPermissions.add(ClassPermission.RESOURCE_LIBARAY);

        return classPermissions;
    }
}
