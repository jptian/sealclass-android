package cn.rongcloud.sealclass.permission;

/**
 * 所有可操作权限。
 * 当拥有对应的权限即可去进行操作权限所对应的处理操作。
 */
public enum ClassPermission {

    //本地个人功能权限

    /**
     * 创建白板
     */
    CREATE_WHITE_BOARD,
    /**
     * 使用白板
     */
    USE_WHITE_BOARD,
    /**
     * 使用资源库
     */
    RESOURCE_LIBARAY,
    /**
     * 查看用户列表
     */
    LOOK_MEMBER_LIST,
    /**
     * 查看成员视频列表
     */
    LOOK_MEMBER_VIDEO_LIST,
    /**
     * IM 聊天
     */
    IM_CHAT,

    /**
     * 视频聊天
     */
    VIDEO_CHAT,
    /**
     * 语音聊天
     */
    AUDIO_CHAT,
    /**
     * 控制声音外放
     */
    CONTROL_SOUND,


    /**
     * 申请发言
     */
    APPLY_SPEECH,

    /**
     * 讲课权限
     */
    LECTURE,

    /**
     * 转移身份
     */
    TRANSFER_ROLE,
    /**
     * 控制成员的语音
     */
    CONTROL_MEMBER_MIC,
    /**
     * 控制成员的摄像头
     */
    CONTROL_MEMBER_CAMERA,

    /**
     * 升级成员等级
     */
    UPGRADE_MEMBER,
    /**
     * 降低成员等级
     */
    DOWNGRADE_MEMBER,
    /**
     * 踢成员
     */
    KICK_OFF_MEMBER;

}
