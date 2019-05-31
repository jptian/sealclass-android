package cn.rongcloud.sealclass.repository;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.rtc.RTCErrorCode;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.stream.MediaType;
import cn.rongcloud.rtc.stream.remote.RongRTCAVInputStream;
import cn.rongcloud.sealclass.api.SealClassApi;
import cn.rongcloud.sealclass.api.retrofit.CallBackWrapper;
import cn.rongcloud.sealclass.api.retrofit.RetrofitUtil;
import cn.rongcloud.sealclass.common.ResultCallback;
import cn.rongcloud.sealclass.im.IMManager;
import cn.rongcloud.sealclass.im.message.ApplyForSpeechMessage;
import cn.rongcloud.sealclass.im.message.AssistantTransferMessage;
import cn.rongcloud.sealclass.im.message.ControlDeviceNotifyMessage;
import cn.rongcloud.sealclass.im.message.DeviceStateChangedMessage;
import cn.rongcloud.sealclass.im.message.DisplayMessage;
import cn.rongcloud.sealclass.im.message.MemberChangedMessage;
import cn.rongcloud.sealclass.im.message.RoleChangedMessage;
import cn.rongcloud.sealclass.im.message.SpeechResultMessage;
import cn.rongcloud.sealclass.im.message.TicketExpiredMessage;
import cn.rongcloud.sealclass.im.message.TurnPageMessage;
import cn.rongcloud.sealclass.im.message.UpgradeRoleMessage;
import cn.rongcloud.sealclass.im.message.WhiteBoardMessage;
import cn.rongcloud.sealclass.model.ApplyForSpeechRequest;
import cn.rongcloud.sealclass.model.ChangedUser;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.DeviceControlInvite;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.model.RoleChangedUser;
import cn.rongcloud.sealclass.model.SpeechResult;
import cn.rongcloud.sealclass.model.StreamResource;
import cn.rongcloud.sealclass.model.TicketExpired;
import cn.rongcloud.sealclass.model.UpgradeRoleInvite;
import cn.rongcloud.sealclass.model.WhiteBoard;
import cn.rongcloud.sealclass.rtc.RtcManager;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * 课堂数据仓库
 */
public class ClassRepository extends BaseRepository {
    private static final String PARAM_ROOM_ID = "roomId";
    private static final String PARAM_USER_ID = "userId";
    private static final String PARAM_CAMERA_ON = "cameraOn";
    private static final String PARAM_MIC_ON = "microphoneOn";
    private static final String PARAM_USERS = "users";
    private static final String PARAM_TICKET = "ticket";
    private static final String PARAM_ROLE = "role";
    private static final String PARAM_WHITEBOARD_ID = "whiteboardId";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_URI = "uri";
    private SealClassApi sealClassService;
    private IMManager imManager;
    private HashMap<OnClassEventListener, ClassMessageReceiveListener> classEventListenerMap = new HashMap<>();


    public ClassRepository(Context context) {
        super(context);
        sealClassService = getService(SealClassApi.class);
        imManager = IMManager.getInstance();
    }

    public void leave(String roomId, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        sealClassService.leave(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }


    public void kickOff(String roomId, String userId, final ResultCallback<Boolean> callBack) {

        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_USER_ID, userId);
        sealClassService.kickOff(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }


    public void controlCamera(String roomId, String userId, boolean cameraOn, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_USER_ID, userId);
        bodyMap.put(PARAM_CAMERA_ON, cameraOn);
        sealClassService.deviceControl(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));

    }

    public void controlMicrophone(String roomId, String userId, boolean microphone, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_USER_ID, userId);
        bodyMap.put(PARAM_MIC_ON, microphone);
        sealClassService.deviceControl(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }


    public void deviceApprove(String roomId, String ticket, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_TICKET, ticket);
        sealClassService.deviceApprove(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void deviceReject(String roomId, String ticket, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_TICKET, ticket);
        sealClassService.deviceReject(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void deviceSyncCamera(String roomId, boolean cameraOn, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_CAMERA_ON, cameraOn);
        sealClassService.deviceSync(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void deviceSyncMic(String roomId, boolean microphoneOn, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_MIC_ON, microphoneOn);
        sealClassService.deviceSync(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void downgrade(String roomId, List<ChangedUser> users, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_USERS, users);
        sealClassService.downgrade(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));

    }

    public void applySpeech(String roomId, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        sealClassService.applySpeech(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }


    public void approveSpeech(String roomId, String ticket, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_TICKET, ticket);
        sealClassService.applyApprove(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }


    public void rejectSpeech(String roomId, String ticket, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_TICKET, ticket);
        sealClassService.applyReject(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void transferRole(String roomId, String userId, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_USER_ID, userId);
        sealClassService.transferRole(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void upgradeIntive(String roomId, String userId, int role, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_USER_ID, userId);
        bodyMap.put(PARAM_ROLE, role);
        sealClassService.upgradeInvite(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void upgradeApprove(String roomId, String ticket, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_TICKET, ticket);
        sealClassService.upgradeApprove(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void upgradeReject(String roomId, String ticket, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_TICKET, ticket);
        sealClassService.upgradeReject(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void changeRole(String roomId, String userId, int role, ResultCallback<Boolean> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_USER_ID, userId);
        bodyMap.put(PARAM_ROLE, role);
        sealClassService.changeRole(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<Boolean>(callBack));
    }

    public void createWhiteBoard(String roomId, ResultCallback<String> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        sealClassService.createWhiteBoard(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<String>(callBack));
    }

    /**
     * 删除白板
     *
     * @param roomId
     * @param whiteBoardId
     * @param callback
     */
    public void deleteWhiteBoard(String roomId, String whiteBoardId, ResultCallback<Boolean> callback) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_WHITEBOARD_ID, whiteBoardId);
        sealClassService.deleteWhiteBoard(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<>(callback));
    }

    /**
     * 获取白板列表
     *
     * @param roomId
     * @param callback
     */
    public void getWhiteBoardList(String roomId, ResultCallback<List<WhiteBoard>> callback) {
        sealClassService.getWhiteBoardList(roomId).enqueue(new CallBackWrapper<List<WhiteBoard>>(callback));
    }

    /**
     * 切换共享画布区显示内容
     *
     * @param roomId   房间Id
     * @param type     显示类型，参考{@link cn.rongcloud.sealclass.model.ScreenDisplay.Display}
     * @param userId   当显示类型为用户时，传入该用户的id
     * @param uri      当显示类型为白板时，传入白板id
     * @param callback
     */
    public void switchDisplay(String roomId, int type, String userId, String uri, ResultCallback<Boolean> callback) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put(PARAM_ROOM_ID, roomId);
        bodyMap.put(PARAM_TYPE, type);
        if (userId != null) {
            bodyMap.put(PARAM_USER_ID, userId);
        }
        if (uri != null) {
            bodyMap.put(PARAM_URI, uri);
        }
        sealClassService.switchDisplay(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<>(callback));
    }


    public void addOnClassEventListener(String roomId, OnClassEventListener onClassEventListener) {
        if (roomId == null) {
            SLog.e(SLog.TAG_IM, "addOnClassEventListener roomId is null");
            return;
        }

        ClassMessageReceiveListener classMessageReceiveListener = new ClassMessageReceiveListener(roomId, onClassEventListener);
        classEventListenerMap.put(onClassEventListener, classMessageReceiveListener);
        imManager.addMessageReceiveListener(classMessageReceiveListener);
        imManager.addOnUnReadMessageListener(classMessageReceiveListener);
    }

    public void removeOnClassEventListener(OnClassEventListener onClassEventListener) {
        ClassMessageReceiveListener classMessageReceiveListener = classEventListenerMap.get(onClassEventListener);
        imManager.removeMessageReceiveListener(classMessageReceiveListener);
        imManager.removeOnUnReadMessageListener(classMessageReceiveListener);
    }


    /**
     * 处理 IM 自定义消息，并回调对应的{@link OnClassEventListener}事件监听
     */
    private class ClassMessageReceiveListener implements RongIMClient.OnReceiveMessageListener, IUnReadMessageObserver {
        private OnClassEventListener eventListener;
        private String roomId;

        ClassMessageReceiveListener(String roomId, OnClassEventListener listener) {
            this.roomId = roomId;
            eventListener = listener;
        }

        @Override
        public boolean onReceived(Message message, int left) {
            String targetId = message.getTargetId();
            if (message.getConversationType() == Conversation.ConversationType.GROUP && (roomId == null || !roomId.equals(targetId))) return false;

            MessageContent content = message.getContent();
            if (content instanceof MemberChangedMessage) {
                MemberChangedMessage memberChangedMessage = (MemberChangedMessage) content;
                ClassMember classMember = new ClassMember();
                // 默认设置为 true， 如果用户的摄像头和 mic 的状态改变的话， 则会通过设备变化通知消息通知
                classMember.setMicrophone(true);
                classMember.setCamera(true);
                classMember.setRole(memberChangedMessage.getRole().getValue());
                classMember.setUserId(memberChangedMessage.getUserId());
                classMember.setUserName(memberChangedMessage.getUserName());
                classMember.setJoinTime(memberChangedMessage.getTimestamp());
                eventListener.onMemberChanged(memberChangedMessage.getAction(), classMember);
                return true;
            } else if (content instanceof WhiteBoardMessage) {
                WhiteBoardMessage whiteboardMessage = (WhiteBoardMessage) content;
                WhiteBoard whiteBoard = new WhiteBoard();
                whiteBoard.setName(whiteboardMessage.getWhiteboardName());
                whiteBoard.setWhiteboardId(whiteboardMessage.getWhiteboardId());
                eventListener.onWhiteBoardChanged(whiteboardMessage.getAction(), whiteBoard);
                return true;

            } else if (content instanceof ControlDeviceNotifyMessage) {     // 邀请开启设备消息
                ControlDeviceNotifyMessage controlDeviceNotifyMessage = (ControlDeviceNotifyMessage) content;
                DeviceControlInvite deviceControlInvite = new DeviceControlInvite();
                deviceControlInvite.setTicket(controlDeviceNotifyMessage.getTicket());
                deviceControlInvite.setAction(controlDeviceNotifyMessage.getAction());
                deviceControlInvite.setDeviceType(controlDeviceNotifyMessage.getType());
                deviceControlInvite.setOpUserId(controlDeviceNotifyMessage.getOpUserId());
                deviceControlInvite.setOpUserName(controlDeviceNotifyMessage.getOpUserName());
                eventListener.onOpenDeviceInvite(deviceControlInvite);
                return true;

            } else if (content instanceof DeviceStateChangedMessage) {    // 设备状态改变消息
                DeviceStateChangedMessage deviceStateChangedMessage = (DeviceStateChangedMessage) content;
                eventListener.onDeviceStateChanged(
                        deviceStateChangedMessage.getUserId()
                        , deviceStateChangedMessage.getType()
                        , deviceStateChangedMessage.getEnable());
                return true;

            } else if (content instanceof DisplayMessage) {      // 共享画布区内容发生改变
                DisplayMessage displayMessage = (DisplayMessage) content;
                eventListener.onDisplayChanged(displayMessage.getDisplay());
                return true;

            } else if (content instanceof TurnPageMessage) {      // 白板翻页消息
                TurnPageMessage turnPageMessage = (TurnPageMessage) content;
                eventListener.onWhiteBoardPageChanged(turnPageMessage.getWhiteboardId(), turnPageMessage.getCurPg(), turnPageMessage.getUserId());
                return true;

            } else if (content instanceof RoleChangedMessage) {   // 用户角色改变
                RoleChangedMessage roleChangedMessage = (RoleChangedMessage) content;
                eventListener.onRoleChanged(roleChangedMessage.getUsers(), roleChangedMessage.getOpUserId());
                return true;

            } else if (content instanceof ApplyForSpeechMessage) {    // 申请发言
                ApplyForSpeechMessage applyForSpeechMessage = (ApplyForSpeechMessage) content;
                ApplyForSpeechRequest applyForSpeechRequest = new ApplyForSpeechRequest();
                applyForSpeechRequest.setReqUserId(applyForSpeechMessage.getReqUserId());
                applyForSpeechRequest.setReqUserName(applyForSpeechMessage.getReqUserName());
                applyForSpeechRequest.setTicket(applyForSpeechMessage.getTicket());
                eventListener.onApplyForSpeechRequest(applyForSpeechRequest);
                return true;
            } else if (content instanceof TicketExpiredMessage) {     // 申请过期消息
                TicketExpiredMessage ticketExpiredMessage = (TicketExpiredMessage) content;
                TicketExpired ticketExpired = new TicketExpired();
                ticketExpired.setTicket(ticketExpiredMessage.getTicket());
                ticketExpired.setFromUserId(ticketExpiredMessage.getFromUserId());
                ticketExpired.setToUserId(ticketExpiredMessage.getToUserId());
                eventListener.onTicketExpired(ticketExpired);
                return true;
            } else if (content instanceof SpeechResultMessage) {    // 申请发言结果消息
                SpeechResultMessage speechResultMessage = (SpeechResultMessage) content;
                SpeechResult result = new SpeechResult();
                result.setOpUserId(speechResultMessage.getOpUserId());
                result.setOpUserName(speechResultMessage.getOpUserName());
                result.setReqUserId(speechResultMessage.getReqUserId());
                result.setReqUserName(speechResultMessage.getReqUserName());
                result.setAccept(speechResultMessage.isAccept());
                eventListener.onRequestSpeechResult(result);
                return true;
            } else if (content instanceof AssistantTransferMessage) {    // 转让助教
                AssistantTransferMessage assistantTransferMessage = (AssistantTransferMessage) content;

                List<RoleChangedUser> roleChangedUserList = new ArrayList<>();
                RoleChangedUser assistant = new RoleChangedUser();
                assistant.setRole(Role.ASSISTANT.getValue());
                assistant.setUserId(assistantTransferMessage.getToUserId());
                roleChangedUserList.add(assistant);

                // 操作者
                RoleChangedUser oldAssistant = new RoleChangedUser();
                oldAssistant.setRole(Role.STUDENT.getValue());
                oldAssistant.setUserId(assistantTransferMessage.getOpUserId());
                roleChangedUserList.add(oldAssistant);

                eventListener.onRoleChanged(roleChangedUserList, assistantTransferMessage.getOpUserId());
                return true;

            } else if (content instanceof UpgradeRoleMessage) {      // 邀请升级
                UpgradeRoleMessage upgradeRoleMessage = (UpgradeRoleMessage) content;
                UpgradeRoleInvite upgradeRoleInvite = new UpgradeRoleInvite();
                upgradeRoleInvite.setOpUserId(upgradeRoleMessage.getOpUserId());
                upgradeRoleInvite.setOpUserName(upgradeRoleMessage.getOpUserName());
                upgradeRoleInvite.setAction(upgradeRoleMessage.getAction());
                upgradeRoleInvite.setRole(upgradeRoleMessage.getRole());
                upgradeRoleInvite.setTicket(upgradeRoleMessage.getTicket());
                eventListener.onInviteUpgradeRole(upgradeRoleInvite);

                return true;
            }

            return false;
        }

        @Override
        public void onCountChanged(int unread) {
            if (eventListener != null) {
                eventListener.onExistUnReadMessage(unread);
            }
        }
    }


    public void setOnClassVideoEventListener(OnClassVideoEventListener listener) {
        RtcManager.getInstance().setRtcCallback(new VideoCallback(listener));
    }

    public void joinRtcRoom(String roomId, ResultCallback<String> callback) {
        RtcManager.getInstance().joinRtcRoom(roomId, callback);
    }

    public void quitRtcRoom(String roomId, ResultCallback<String> callback) {
        RtcManager.getInstance().quitRtcRoom(roomId, callback);
    }

    public void startRtcChat(RongRTCVideoView view, ResultCallback<Boolean> callback) {
        RtcManager.getInstance().startRtcChat(view, callback);
    }


    public void stopRtcChat(ResultCallback<Boolean> callback) {
        RtcManager.getInstance().stopRTCChat(callback);
    }

    public void setLocalVideoEnable(boolean enable) {
        RtcManager.getInstance().setLocalVideoEnable(enable);
    }

    public void setLocalMicEnable(boolean enable) {
        RtcManager.getInstance().setLocalMicEnable(enable);
    }

    public void muteRoomVoice(boolean mute, ResultCallback<Boolean> callback) {
        RtcManager.getInstance().muteRoomVoice(mute, callback);
    }

    public void subscribeAllResource(HashMap<String, RongRTCVideoView> videoViews, ResultCallback<Boolean> callback) {
        RtcManager.getInstance().subscribeAll(videoViews, callback);
    }

    public void subscribeResource(String userId, RongRTCVideoView videoView, ResultCallback<String> callback) {
        RtcManager.getInstance().subscribe(userId, videoView, callback);
    }

   public void subscribeResource(String userId, RongRTCVideoView videoView, RongRTCVideoView screenShareView, ResultCallback<String> callback) {
        RtcManager.getInstance().subscribe(userId, videoView, screenShareView, callback);
    }

    public void unSubscribeResource(String userId,  ResultCallback<String> callback) {
        RtcManager.getInstance().unSubscribe(userId, callback);
    }

    public void subscribeVideo(String userId, RongRTCVideoView videoView, ResultCallback<String> callback) {
        RtcManager.getInstance().subscribeVideo(userId, videoView, callback);
    }

    public void unSubscribeVideo(String userId,  ResultCallback<String> callback) {
        RtcManager.getInstance().unSubscribeVideo(userId, callback);
    }

    public void subscribeAudio(String userId, ResultCallback<String> callback) {
        RtcManager.getInstance().subscribeAudio(userId, callback);
    }

    public void unSubscribeAudio(String userId,  ResultCallback<String> callback) {
        RtcManager.getInstance().unSubscribeAudio(userId, callback);
    }


    public void subscribeScreen(String userId, RongRTCVideoView videoView, ResultCallback<String> callback) {
        RtcManager.getInstance().subscribeScreen(userId, videoView, callback);
    }

    public void unSubscribeScreen(String userId, ResultCallback<String> callback) {
        RtcManager.getInstance().unSubscribeScreen(userId, callback);
    }


    private class VideoCallback implements RtcManager.RtcCallback {

        private OnClassVideoEventListener listener;

        public VideoCallback(OnClassVideoEventListener listener) {
            this.listener = listener;
        }


        @Override
        public void onInitialRemoteUserList(Map<String, List<RongRTCAVInputStream>> userInfos) {

            if (listener != null) {
                listener.onInitVideoList(getStreamResourceList(userInfos));
            }
        }

        @Override
        public void onRemoteUserPublishResource(String userId, List<RongRTCAVInputStream> list) {
            if (listener != null) {
                StreamResource streamResource = getPublicStreamResource(userId, list);
                listener.onAddVideoUser(streamResource);
            }
        }

        @Override
        public void onRemoteUserUnPublishResource(String userId, List<RongRTCAVInputStream> list) {
            if (listener != null) {
                StreamResource streamResource = getUnPublicStreamResource(userId, list);
                listener.onRemoveVideoUser(streamResource);
            }
        }

        @Override
        public void onRemoteUserAudioStreamMute(String userId, boolean mute) {

        }

        @Override
        public void onRemoteUserVideoStreamEnabled(String userId, boolean enable) {
            if (listener != null) {
                listener.onVideoEnabled(userId, enable);
            }
        }

        @Override
        public void onFail(RTCErrorCode rtcErrorCode) {

        }

        @Override
        public void onUserJoined(String userId) {

        }

        @Override
        public void onUserLeft(String userId) {
            if (listener != null) {
                listener.onRemoveVideoUser(getUnPublicStreamResource(userId, null));
            }
        }

        @Override
        public void onUserOffline(String userId) {
            if (listener != null) {
                listener.onRemoveVideoUser(getUnPublicStreamResource(userId, null));
            }
        }

        @Override
        public void onFirstFrameDraw(String userId, String tag) {
            if (listener != null) {
                listener.onFirstFrameDraw(userId, tag);
            }
        }
    };


    private List<StreamResource> getStreamResourceList(Map<String, List<RongRTCAVInputStream>> userInfos) {
        if (userInfos == null) {
            return null;
        }

        List<StreamResource> resources = new ArrayList<>();
        for (Map.Entry<String, List<RongRTCAVInputStream>> userInfo : userInfos.entrySet()) {
            resources.add(getPublicStreamResource(userInfo.getKey(), userInfo.getValue()));
        }
        return resources;
    }

    private StreamResource getPublicStreamResource(String userId, List<RongRTCAVInputStream> list) {
        StreamResource streamResource = new StreamResource();
        streamResource.userId = userId;
        if (list == null) {
            return streamResource;
        }
        for (RongRTCAVInputStream stream : list) {
            if (stream.getMediaType() == MediaType.AUDIO) {
                streamResource.isHasAudio = true;
            } else if (stream.getMediaType() == MediaType.VIDEO && stream.getTag().equals("screenshare") ) {
                streamResource.isHasScreen = true;
            } else {
                streamResource.isHasVideo = true;
            }
        }
        return streamResource;
    }

    private StreamResource getUnPublicStreamResource(String userId, List<RongRTCAVInputStream> list) {
        StreamResource streamResource = new StreamResource();
        streamResource.userId = userId;
        if (list == null) {
            streamResource.isHasVideo = false;
            streamResource.isHasScreen = false;
            streamResource.isHasVideo = false;
            return streamResource;
        }
        streamResource.isHasVideo = true;
        streamResource.isHasScreen = true;
        streamResource.isHasVideo = true;
        for (RongRTCAVInputStream stream : list) {
            if (stream.getMediaType() == MediaType.AUDIO) {
                streamResource.isHasAudio = false;
            } else if (stream.getMediaType() == MediaType.VIDEO && stream.getTag().equals("screenshare") ) {
                streamResource.isHasScreen = false;
            } else {
                streamResource.isHasVideo = false;
            }
        }
        return streamResource;
    }



}
