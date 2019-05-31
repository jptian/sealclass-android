package cn.rongcloud.sealclass.permission;


/**
 * 权限集操作接口。可以通过此接口查询权限集的等级，可执行的权限是否存在 和 被执行的权限是否存在
 */
public interface PermissionGroup {
    /**
     * 权限集的角色
     * @return 权限等级 {@link PermissionGroupLevel}
     */
    PermissionGroupLevel permissionGroupRole();

    /**
     * 是否存在可执行的权限
     * @param permission 可执行权限， 具体可查看{@link ClassPermission}
     * @return true 存在； false 不存在
     */
    boolean hasPermission(ClassPermission permission);

    /**
     * 是否存在可被执行的权限
     * @param permission 被执行权限， 具体可查看{@link ClassExecutedPermission}
     * @return true 存在； false 不存在
     */
    boolean hasExecutedPermission(ClassExecutedPermission permission);
}
