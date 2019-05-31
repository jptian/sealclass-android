package cn.rongcloud.sealclass.repository;

import java.util.List;

import cn.rongcloud.sealclass.model.ApplyForSpeechRequest;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.ClassMemberChangedAction;
import cn.rongcloud.sealclass.model.DeviceControlInvite;
import cn.rongcloud.sealclass.model.DeviceType;
import cn.rongcloud.sealclass.model.RoleChangedUser;
import cn.rongcloud.sealclass.model.ScreenDisplay;
import cn.rongcloud.sealclass.model.SpeechResult;
import cn.rongcloud.sealclass.model.TicketExpired;
import cn.rongcloud.sealclass.model.UpgradeRoleInvite;
import cn.rongcloud.sealclass.model.WhiteBoard;
import cn.rongcloud.sealclass.model.WhiteBoardAction;

public interface OnClassEventListener {
    /**
     * 当有用户进出课堂时回调
     *
     * @param action      当前用户的行为：1.进入课堂；2.退出课堂；3.踢出课堂
     * @param classMember
     */
    void onMemberChanged(ClassMemberChangedAction action, ClassMember classMember);

    /**
     * 当白板发送变化时回调
     *
     * @param action     发生变化的白板状态：1.创建白板；2.删除白板
     * @param whiteBoard
     */
    void onWhiteBoardChanged(WhiteBoardAction action, WhiteBoard whiteBoard);

    /**
     * 当有邀请开启设备（摄像头、麦克风）请求时和邀请对方开启设备后对方有回应时回调
     *
     * @param deviceControlInvite
     */
    void onOpenDeviceInvite(DeviceControlInvite deviceControlInvite);

    /**
     * 当用户的摄像头，麦克风状态改变时回调
     *
     * @param userId     设备状态发生改变的用户id
     * @param deviceType 设备类型
     * @param isEnable   设备是否启用
     */
    void onDeviceStateChanged(String userId, DeviceType deviceType, boolean isEnable);

    /**
     * 当共享画布区内容发送改变时回调
     */
    void onDisplayChanged(ScreenDisplay screenDisplay);

    /**
     * 当白板翻页时回调
     *
     * @param whiteBoardId 白板 id
     * @param curPage      当前页数
     * @param optUserId    操作用户 id
     */
    void onWhiteBoardPageChanged(String whiteBoardId, int curPage, String optUserId);

    /**
     * 用户角色改变回调
     *
     * @param roleChangedUserList
     * @param optUserId           操作用户 id
     */
    void onRoleChanged(List<RoleChangedUser> roleChangedUserList, String optUserId);

    /**
     * 接受到旁听人申请发言回调
     *
     * @param applyForSpeechRequest
     */
    void onApplyForSpeechRequest(ApplyForSpeechRequest applyForSpeechRequest);

    /**
     * 当请求过期时回调
     *
     * @param ticketExpired
     */
    void onTicketExpired(TicketExpired ticketExpired);

    /**
     * 在申请发言有应答时回调
     *
     * @param speechResult
     */
    void onRequestSpeechResult(SpeechResult speechResult);

    /**
     * 在被邀请升级角色时回调
     */
    void onInviteUpgradeRole(UpgradeRoleInvite upgradeRoleInvite);

    /**
     * 当有未读消息时回调
     * @param count
     */
    void onExistUnReadMessage(int count);

}
