package cn.rongcloud.sealclass.im;

import android.content.Context;

import io.rong.imlib.RongIMClient.ConnectCallback;
import io.rong.imlib.RongIMClient.ConnectionErrorCode;
import io.rong.imlib.RongIMClient.DatabaseOpenStatus;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.common.ResultCallback;
import cn.rongcloud.sealclass.im.message.ApplyForSpeechMessage;
import cn.rongcloud.sealclass.im.message.AssistantTransferMessage;
import cn.rongcloud.sealclass.im.message.ControlDeviceNotifyMessage;
import cn.rongcloud.sealclass.im.message.DeviceStateChangedMessage;
import cn.rongcloud.sealclass.im.message.DisplayMessage;
import cn.rongcloud.sealclass.im.message.MemberChangedMessage;
import cn.rongcloud.sealclass.im.message.NewDeviceMessage;
import cn.rongcloud.sealclass.im.message.RoleChangedMessage;
import cn.rongcloud.sealclass.im.message.RoleSingleChangedMessage;
import cn.rongcloud.sealclass.im.message.SpeechResultMessage;
import cn.rongcloud.sealclass.im.message.TicketExpiredMessage;
import cn.rongcloud.sealclass.im.message.TurnPageMessage;
import cn.rongcloud.sealclass.im.message.UpgradeRoleMessage;
import cn.rongcloud.sealclass.im.message.WhiteBoardMessage;
import cn.rongcloud.sealclass.im.provider.ClassFileMessageItemProvider;
import cn.rongcloud.sealclass.im.provider.ClassImageMessageItemProvider;
import cn.rongcloud.sealclass.im.provider.ClassMemberChangedNotificationProvider;
import cn.rongcloud.sealclass.im.provider.ClassTextMessageItemProvider;
import cn.rongcloud.sealclass.im.provider.RoleChangedMessageItemProvider;
import cn.rongcloud.sealclass.model.RoleChangedUser;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

/**
 * Rong IM 业务相关封装
 */
public class IMManager {
    private static final String TAG = IMManager.class.getSimpleName();
    private static IMManager sInstance;
    private final int DEFAULT_MESSAGE_COUNT = -1;//-1代表不拉取历史消息
    private List<RongIMClient.OnReceiveMessageListener> listenerList = new ArrayList<>();

    public static IMManager getInstance() {
        if (sInstance == null) {
            synchronized (IMManager.class) {
                if (sInstance == null) {
                    sInstance = new IMManager();
                }
            }
        }
        return sInstance;
    }

    private IMManager() {
    }

    /**
     * 初始化，需要在使用前初始化一次
     *
     * @param context
     */
    public static void init(Context context,String appkey) {
        final IMManager imManager = getInstance();

        // 可在初始 SDK 时直接带入融云 IM 申请的APP KEY
        RongIM.init(context, appkey, false);

        // 注册自定义消息
        RongIM.registerMessageType(ApplyForSpeechMessage.class);
        RongIM.registerMessageType(AssistantTransferMessage.class);
        RongIM.registerMessageType(ControlDeviceNotifyMessage.class);
        RongIM.registerMessageType(DeviceStateChangedMessage.class);
        RongIM.registerMessageType(DisplayMessage.class);
        RongIM.registerMessageType(MemberChangedMessage.class);
        RongIM.registerMessageType(RoleChangedMessage.class);
        RongIM.registerMessageType(SpeechResultMessage.class);
        RongIM.registerMessageType(TicketExpiredMessage.class);
        RongIM.registerMessageType(TurnPageMessage.class);
        RongIM.registerMessageType(UpgradeRoleMessage.class);
        RongIM.registerMessageType(WhiteBoardMessage.class);
        RongIM.registerMessageType(RoleSingleChangedMessage.class);
        RongIM.registerMessageType(NewDeviceMessage.class);

        // 设置在发送消息时添加用户信息
        RongIM.getInstance().setMessageAttachedUserInfo(true);

        // 设置自定义文本显示
        RongIM.registerMessageTemplate(new ClassTextMessageItemProvider());
        RongIM.registerMessageTemplate(new ClassImageMessageItemProvider());
        RongIM.registerMessageTemplate(new ClassFileMessageItemProvider());
        RongIM.registerMessageTemplate(new ClassMemberChangedNotificationProvider());
        RongIM.registerMessageTemplate(new RoleChangedMessageItemProvider());
        /*
         * 管理消息监听，由于同一时间只能有一个消息监听加入 融云 的消息监听，所以做一个消息管理来做消息路由
         */
        RongIM.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(Message message, int left) {
                SLog.d(TAG, "onReceived message. tag:" + message.getObjectName());
                boolean result = false;
                synchronized (imManager.listenerList) {
                    if (imManager.listenerList.size() > 0) {
                        for (RongIMClient.OnReceiveMessageListener listener : imManager.listenerList) {
                            result = listener.onReceived(message, left);
                            if (result) {
                                break;
                            }
                        }
                    }
                }
                return result;
            }
        });
    }

    /**
     * 设置用户信息
     *
     * @param userInfo
     */
    public void setCurrentUserInfo(UserInfo userInfo) {
        RongIM.getInstance().setCurrentUserInfo(userInfo);
    }

    /**
     * 登录 IM 服务器
     *
     * @param token
     */
    public void login(final String token, final ResultCallback<String> callBack) {
        RongIMClient.connect(token, new ConnectCallback() {
            @Override
            public void onSuccess(String userId) {
                SLog.d(SLog.TAG_IM, "IM connect success:" + userId);
                callBack.onSuccess(userId);
            }

            @Override
            public void onError(ConnectionErrorCode errorCode) {
                SLog.e(SLog.TAG_IM, "IM connect error - code:" + errorCode.getValue());
                callBack.onFail(errorCode.getValue());
            }

            @Override
            public void onDatabaseOpened(DatabaseOpenStatus databaseOpenStatus) {
                SLog.d(SLog.TAG_IM, "IM connect onDatabaseOpened");
            }
        });
    }


    /**
     * 加入消息监听
     *
     * @param listener
     */
    public void addMessageReceiveListener(RongIMClient.OnReceiveMessageListener listener) {
        synchronized (listenerList) {
            listenerList.add(listener);
        }
    }

    /**
     * 移除消息监听
     *
     * @param listener
     */
    public void removeMessageReceiveListener(RongIMClient.OnReceiveMessageListener listener) {
        synchronized (listenerList) {
            listenerList.remove(listener);
        }
    }

    /**
     * 设置未读消息数监听
     * @param listener
     */
    public void addOnUnReadMessageListener(IUnReadMessageObserver listener){
        RongIM.getInstance().addUnReadMessageCountChangedObserver(listener, Conversation.ConversationType.GROUP);
    }

    /**
     * 移除未读消息数监听
     * @param listener
     */
    public void removeOnUnReadMessageListener(IUnReadMessageObserver listener){
        RongIM.getInstance().removeUnReadMessageCountChangedObserver(listener);
    }

    /**
     * 将私聊消息存为房间消息
     * @param message
     * @param roomId
     */
    public void savePrivateMessageToRoom(Message message, String roomId){
        RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.GROUP, roomId, message.getSenderUserId(),message.getReceivedStatus(), message.getContent(), null);
    }

    /**
     * 将多用户角色改变消息拆分保存成单一角色改变
     */
    public void saveRoleChangedMessageToSingle(List<RoleChangedUser> roleChangedUserList, String optUserId, String roomId){
        if(roleChangedUserList != null && roleChangedUserList.size() > 0) {
            for(RoleChangedUser user : roleChangedUserList){
                RoleSingleChangedMessage roleSingleChangedMessage = new RoleSingleChangedMessage();
                roleSingleChangedMessage.setUser(user);
                roleSingleChangedMessage.setOpUserId(optUserId);
                Message.ReceivedStatus receivedStatus = new Message.ReceivedStatus(0);
                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.GROUP, roomId, optUserId, receivedStatus, roleSingleChangedMessage, null);
            }
        }
    }

    /**
     * 设置连接状态变化的监听器。
     * @param listener
     */
    public void setConnectionStatusListener(RongIMClient.ConnectionStatusListener listener){
        RongIMClient.setConnectionStatusListener(listener);
    }
}
