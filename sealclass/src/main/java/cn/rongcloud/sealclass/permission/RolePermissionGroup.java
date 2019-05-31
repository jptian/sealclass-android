package cn.rongcloud.sealclass.permission;

import java.util.List;

abstract class RolePermissionGroup implements PermissionGroup{

    List<ClassExecutedPermission> executedPermissionList;
    List<ClassPermission> hasPermissionsList;

    public RolePermissionGroup () {
        hasPermissionsList = onInitPermissions();
        executedPermissionList = onInitExecutedPermissions();
    }


    @Override
    public boolean hasExecutedPermission(ClassExecutedPermission permission) {
        if (executedPermissionList == null || executedPermissionList.size() <= 0) {
            return false;
        }
        return executedPermissionList.contains(permission);
    }


    @Override
    public boolean hasPermission(ClassPermission permission) {
        if (hasPermissionsList == null || hasPermissionsList.size() <= 0) {
            return false;
        }
        return hasPermissionsList.contains(permission);
    }

    protected abstract List<ClassPermission> onInitPermissions();

    protected abstract List<ClassExecutedPermission> onInitExecutedPermissions();
}
