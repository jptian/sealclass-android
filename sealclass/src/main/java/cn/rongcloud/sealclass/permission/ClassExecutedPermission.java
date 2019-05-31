package cn.rongcloud.sealclass.permission;

/**
 * 所有可被操作权限。
 * 当拥有此权限则代码允许自己被他人执行某种操作。
 */
public enum ClassExecutedPermission {
    /**
     * 可被升级
     */
    UPGRADE,
    /**
     * 可被降级
     */
    DOWNGRADE,
    /**
     * 可被控制摄像头
     */
    CONTROL_VIDEO,
    /**
     * 可被控制语音
     */
    CONTROL_MIC,
    /**
     * 可被踢
     */
    KICK_OFF,
    /**
     * 可接收转移身份
     */
    ACCEPT_TRANSFER_ROLE
}
