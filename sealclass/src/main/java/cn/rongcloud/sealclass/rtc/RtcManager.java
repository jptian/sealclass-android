package cn.rongcloud.sealclass.rtc;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.rtc.CenterManager;
import cn.rongcloud.rtc.RTCErrorCode;
import cn.rongcloud.rtc.RongRTCConfig;
import cn.rongcloud.rtc.RongRTCEngine;
import cn.rongcloud.rtc.callback.JoinRoomUICallBack;
import cn.rongcloud.rtc.callback.RongRTCResultUICallBack;
import cn.rongcloud.rtc.engine.report.StatusBean;
import cn.rongcloud.rtc.engine.report.StatusReport;
import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.rtc.events.RongRTCEventsListener;
import cn.rongcloud.rtc.events.RongRTCStatusReportListener;
import cn.rongcloud.rtc.room.RongRTCRoom;
import cn.rongcloud.rtc.stream.MediaType;
import cn.rongcloud.rtc.stream.local.RongRTCCapture;
import cn.rongcloud.rtc.stream.local.RongRTCLocalSourceManager;
import cn.rongcloud.rtc.stream.remote.RongRTCAVInputStream;
import cn.rongcloud.rtc.user.RongRTCLocalUser;
import cn.rongcloud.rtc.user.RongRTCRemoteUser;
import cn.rongcloud.sealclass.common.ErrorCode;
import cn.rongcloud.sealclass.common.ResultCallback;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.utils.log.SLog;
import io.rong.imlib.model.Message;

/**
 * Rong RTC 语音业务封装
 */
public class RtcManager {
    private static final String TAG = RtcManager.class.getSimpleName();
    private static RtcManager instance;
    private RongRTCRoom rongRTCRoom;
    private RtcCallback rtcCallback;

    //是否需要注册状态回调，目前仅调试打开
    private boolean needRegisteredStatusReportListener = false;

    private RtcManager() {
    }

    public static RtcManager getInstance() {
        if (instance == null) {
            synchronized (RtcManager.class) {
                if (instance == null) {
                    instance = new RtcManager();
                }
            }
        }

        return instance;
    }

    public void setRtcCallback(RtcCallback callBack) {
        this.rtcCallback = callBack;
        if (rtcCallback != null && getRTCRoom() != null) {
            SLog.d(SLog.TAG_RTC, "setRtcCallback");
            rtcCallback.onInitialRemoteUserList(getRemoteUsersInfo());
        }
    }


    /**
     * 加入聊天房间
     *
     * @param roomId
     */
    public void joinRtcRoom(final String roomId, final ResultCallback<String> callback) {
        RongRTCEngine.getInstance().joinRoom(roomId, new JoinRoomUICallBack() {
            @Override
            protected void onUiSuccess(RongRTCRoom rtcRoom) {
                SLog.d(SLog.TAG_RTC, "Join room success" + rtcRoom);
                rongRTCRoom = rtcRoom;
                setRoomEventListener();
                registerStatusReportListener();

                if (callback != null) {
                    callback.onSuccess(roomId);
                }
            }

            @Override
            protected void onUiFailed(RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "joinRtcRoom failed - " + rtcErrorCode.getReason());
                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                if (rtcCallback != null) {
                    rtcCallback.onFail(rtcErrorCode);
                }
            }
        });
    }

    /**
     * 离开聊天室
     *
     * @param roomId
     */
    public void quitRtcRoom(final String roomId, final ResultCallback<String> callback) {
        removeRoomEventListener();
        unregisterStatusReportListener();
        RongRTCEngine.getInstance().quitRoom(roomId, new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                SLog.d(SLog.TAG_RTC, "Quit room");
                if (callback != null) {
                    callback.onSuccess(roomId);
                }
            }

            @Override
            public void onUiFailed(RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "quitRtcRoom error - " + rtcErrorCode.getReason());

                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                if (rtcCallback != null) {
                    rtcCallback.onFail(rtcErrorCode);
                }
            }
        });
    }


    public void startRtcChat(RongRTCVideoView view, final ResultCallback<Boolean> callback) {
        if (getRTCRoom() == null) {
            SLog.e(SLog.TAG_RTC, "getRTCRoom() ==  " + getRTCRoom());
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }

        RongRTCLocalUser localUser = getRTCRoom().getLocalUser();
        if (localUser == null) {
            SLog.e(SLog.TAG_RTC, "startRtcChat failed localUser is null ");
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }

        RongRTCCapture.getInstance().setRongRTCVideoView(view); //设置本地预览视图
        RongRTCCapture.getInstance().startCameraCapture();       //开始采集数据
        localUser.publishDefaultAVStream(new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                SLog.d(SLog.TAG_RTC, "Local publish avStream success");
                if (callback != null) {
                    callback.onSuccess(true);
                }
            }

            @Override
            public void onUiFailed(RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "startVoiceChat error - " + rtcErrorCode.getReason());

                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                if (rtcCallback != null) {
                    rtcCallback.onFail(rtcErrorCode);
                }
            }
        });
    }

    /**
     * 停止聊天
     */
    public void stopRTCChat(final ResultCallback<Boolean> callback) {
        if (getRTCRoom() == null) {
            return;
        }
        RongRTCCapture.getInstance().setRongRTCVideoView(null); //设置本地预览视图
        RongRTCCapture.getInstance().stopCameraCapture();
        RongRTCLocalUser localUser = getRTCRoom().getLocalUser();
        if (localUser == null) {
            SLog.e(SLog.TAG_RTC, "stopRTCChat failed localUser is null ");
            return;
        }

        localUser.unpublishDefaultAVStream(new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                if (callback != null) {
                    callback.onSuccess(true);
                }
            }

            @Override
            public void onUiFailed(RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "stopVoiceChat error - " + rtcErrorCode.getReason());
                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                if (rtcCallback != null) {
                    rtcCallback.onFail(rtcErrorCode);
                }
            }
        });

    }


    /**
     * 是否使用语音聊天
     */
    public void setLocalMicEnable(boolean enable) {
        RongRTCCapture.getInstance().muteMicrophone(!enable);
    }

    /**
     * 是否使用视频聊天
     */
    public void setLocalVideoEnable(boolean enable) {
        RongRTCCapture.getInstance().muteLocalVideo(!enable);
    }

    public boolean isMuteVideo(){
        return RongRTCCapture.getInstance().isMuteVideo();
    }

    public boolean isInRoom(){
        return CenterManager.getInstance().isInRoom();
    }

    public void startCapture(){
        RongRTCLocalSourceManager.getInstance().startCapture();
    }

    public void switchCamera() {
        RongRTCCapture.getInstance().switchCamera();
    }

    /**
     * 是否打开扬声器
     * @param enabled
     */
    public void setEnableSpeakerphone(boolean enabled) {
        RongRTCCapture.getInstance().setEnableSpeakerphone(enabled);
    }


    /**
     * 房间静音设置
     * 目前没有逻辑用到该方法，如用到必须考虑订阅视频的大小流问题
     */
    public void muteRoomVoice(boolean mute, final ResultCallback<Boolean> callback) {
        if (getRTCRoom() == null) {
            return;
        }
        Map<String, RongRTCRemoteUser> remoteUserMap = getRTCRoom().getRemoteUsers();
        List<RongRTCAVInputStream> receiveVoiceSteamList = new ArrayList<>();
        Collection<RongRTCRemoteUser> remoteUsers = remoteUserMap.values();
        for (RongRTCRemoteUser remoteUser : remoteUsers) {
            List<RongRTCAVInputStream> remoteAVStreams = remoteUser.getRemoteAVStreams();
            for (RongRTCAVInputStream stream : remoteAVStreams) {
                if (stream.getMediaType() == MediaType.AUDIO) {
                    receiveVoiceSteamList.addAll(remoteUser.getRemoteAVStreams());
                }
            }
        }

        if (receiveVoiceSteamList.size() > 0) {
            if (mute) {
                getRTCRoom().unsubscribeAVStream(receiveVoiceSteamList, new RongRTCResultUICallBack() {
                    @Override
                    public void onUiSuccess() {
                        if (callback != null) {
                            callback.onSuccess(true);
                        }
                    }

                    @Override
                    public void onUiFailed(RTCErrorCode rtcErrorCode) {
                        SLog.e(SLog.TAG_RTC, "muteRoomVoice error - " + rtcErrorCode.getReason());
                        if (rtcCallback != null) {
                            rtcCallback.onFail(rtcErrorCode);
                        }
                    }
                });
            } else {
                getRTCRoom().subscribeAvStream(receiveVoiceSteamList, new RongRTCResultUICallBack() {
                    @Override
                    public void onUiSuccess() {
                        if (callback != null) {
                            callback.onSuccess(false);
                        }
                    }

                    @Override
                    public void onUiFailed(RTCErrorCode rtcErrorCode) {
                        SLog.e(SLog.TAG_RTC, "muteRoomVoice error - " + rtcErrorCode.getReason());
                        if (rtcCallback != null) {
                            rtcCallback.onFail(rtcErrorCode);
                        }
                    }
                });
            }

        }
    }


    /**
     * 订阅所有当前在房间发布资源的用户
     */
    public void subscribeAll(HashMap<String, RongRTCVideoView> videoViews, final ResultCallback<Boolean> callback) {
        if (getRTCRoom() != null) {
            Map<String, RongRTCRemoteUser> remoteUserMap = getRTCRoom().getRemoteUsers();
            List<RongRTCAVInputStream> receiveSteamList = new ArrayList<>();
            Collection<RongRTCRemoteUser> remoteUsers = remoteUserMap.values();
            for (RongRTCRemoteUser remoteUser : remoteUsers) {
                for (RongRTCAVInputStream inputStream : remoteUser.getRemoteAVStreams()) {
                    if (inputStream.getMediaType() == MediaType.VIDEO) {
                        inputStream.setSimulcast("2");//2:小流
                        inputStream.setRongRTCVideoView(videoViews.get(remoteUser.getUserId()));
                    }
                }
                receiveSteamList.addAll(remoteUser.getRemoteAVStreams());
            }
            if (receiveSteamList.size() > 0) {
                getRTCRoom().subscribeAvStream(receiveSteamList, new RongRTCResultUICallBack() {
                    @Override
                    public void onUiSuccess() {
                        if (callback != null) {
                            callback.onSuccess(true);
                        }
                    }

                    @Override
                    public void onUiFailed(RTCErrorCode rtcErrorCode) {
                        if (callback != null) {
                            callback.onFail(ErrorCode.RTC_ERROR.getCode());
                        }
                    }
                });
            }
        }

    }

    public void subscribe(String userId, RongRTCVideoView videoView, int role, final ResultCallback<String> callback) {
        RongRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);
        subscribe(remoteUser, videoView, null, role, callback);
    }

    public void subscribe(String userId, RongRTCVideoView videoView, RongRTCVideoView screenShareView, int role, final ResultCallback<String> callback) {
        RongRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);
        subscribe(remoteUser, videoView, screenShareView, role,callback);
    }

    // 订阅单人, 视频只订阅 video
    private void subscribe(final RongRTCRemoteUser remoteUser, RongRTCVideoView videoView, RongRTCVideoView screenShareView, int role, final ResultCallback<String> callback) {
        if (getRTCRoom() == null || remoteUser == null || remoteUser.getRemoteAVStreams() == null) {
            return;
        }

        for (RongRTCAVInputStream inputStream : remoteUser.getRemoteAVStreams()) {
            if (inputStream.getMediaType() == MediaType.VIDEO) {
                SLog.d(SLog.TAG_RTC, "Set remote video view , user = " + remoteUser.getUserId());
                if (inputStream.getTag().equals("screenshare") && screenShareView != null) {
                    inputStream.setRongRTCVideoView(screenShareView);
                } else {
                    String simulcast = "1";//2:小流
                    if (role == Role.STUDENT.getValue()) {
                        simulcast = "2";
                    } else {
                        simulcast = "1";
                    }
                    inputStream.setSimulcast(simulcast);
                    inputStream.setRongRTCVideoView(videoView);
                }
            }
        }

        remoteUser.subscribeAVStream(remoteUser.getRemoteAVStreams(), new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                SLog.d(SLog.TAG_RTC, "subscribeAvStream success,user = " + remoteUser.getUserId());
                if (callback != null) {
                    callback.onSuccess(remoteUser.getUserId());
                }
            }

            @Override
            public void onUiFailed(RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "subscribeAvStream error - " + rtcErrorCode.getReason());

                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                if (rtcCallback != null) {
                    rtcCallback.onFail(rtcErrorCode);
                }
            }
        });

    }

    public void unSubscribe(final String userId, final ResultCallback<String> callback) {
        RongRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);
        if (getRTCRoom() == null || remoteUser == null || remoteUser.getRemoteAVStreams() == null) {
            return;
        }
        remoteUser.unsubscribeAVStream(remoteUser.getRemoteAVStreams(), new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                SLog.d(SLog.TAG_RTC, "unSubscribeAvStream success,user = " + userId);
                if (callback != null) {
                    callback.onSuccess(userId);
                }
            }

            @Override
            public void onUiFailed(RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "unSubscribeAvStream error - " + rtcErrorCode.getReason());

                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                if (rtcCallback != null) {
                    rtcCallback.onFail(rtcErrorCode);
                }
            }
        });

    }


    public void subscribeVideo(final String userId, RongRTCVideoView videoView, final ResultCallback<String> callback) {
        subscribeStream(userId, MediaType.VIDEO, false, videoView, callback);
    }


    public void unSubscribeVideo(String userId, final ResultCallback<String> callback) {
        unSubscribeStream(userId, MediaType.VIDEO, false, callback);
    }

    public void subscribeAudio(final String userId, final ResultCallback<String> callback) {
        subscribeStream(userId, MediaType.AUDIO, false, null, callback);
    }


    public void unSubscribeAudio(String userId, final ResultCallback<String> callback) {
        unSubscribeStream(userId, MediaType.AUDIO, false, callback);
    }

    public void subscribeScreen(String userId, RongRTCVideoView videoView, final ResultCallback<String> callback) {
        subscribeStream(userId, MediaType.VIDEO, true, videoView, callback);

    }

    public void unSubscribeScreen(String userId, final ResultCallback<String> callback) {
        unSubscribeStream(userId, MediaType.VIDEO, true, callback);
    }


    private void subscribeStream(final String userId, final MediaType type, boolean isScreenShare, RongRTCVideoView videoView, final ResultCallback<String> callback) {
        if (getRTCRoom() == null) {
            SLog.e(SLog.TAG_RTC, type + " subscribeStream failed , room = " + getRTCRoom());
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }
        final RongRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);

        if (remoteUser == null || remoteUser.getRemoteAVStreams() == null) {
            SLog.e(SLog.TAG_RTC, type + "subscribeStream failed ,  room = " + getRTCRoom() + "userId = " + userId + "，remoteUser  = " + remoteUser);
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }

        ArrayList<RongRTCAVInputStream> inputStreams = new ArrayList<>();
        for (RongRTCAVInputStream inputStream : remoteUser.getRemoteAVStreams()) {
            if (type == MediaType.VIDEO && inputStream.getMediaType() == MediaType.VIDEO) {
                if (isScreenShare && inputStream.getTag().equals("screenshare")) {
                        inputStream.setRongRTCVideoView(videoView);
                        SLog.d(SLog.TAG_RTC, "subscribeStream, Set remote video screen share view ,  user = " + remoteUser.getUserId() + ",videoView = " + videoView);
                        inputStreams.add(inputStream);

                } else if (!isScreenShare && !inputStream.getTag().equals("screenshare")) {
                        inputStream.setRongRTCVideoView(videoView);
                        inputStream.setSimulcast("2");//2:小流
                        SLog.d(SLog.TAG_RTC, "subscribeStream, Set remote video view , user = " + remoteUser.getUserId() + ",videoView = " + videoView);
                        inputStreams.add(inputStream);
                }

            } else if (type == MediaType.AUDIO && inputStream.getMediaType() == MediaType.AUDIO) {
                inputStreams.add(inputStream);
            }

        }

        if (inputStreams.size() <= 0) {
            SLog.e(SLog.TAG_RTC, "subscribeStream, inputStreams size = " + inputStreams.size());
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }

        SLog.d(SLog.TAG_RTC, "subscribeStream, inputStreams = " + inputStreams);
        remoteUser.subscribeAVStream(inputStreams, new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                SLog.d(SLog.TAG_RTC, "subscribeStream " + type + " success,user = " + userId);
                if (callback != null) {
                    callback.onSuccess(userId);
                }
            }

            @Override
            public void onUiFailed(RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "subscribeStream " + type + " error - " + rtcErrorCode.getReason());

                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                if (rtcCallback != null) {
                    rtcCallback.onFail(rtcErrorCode);
                }
            }
        });

    }


    private void unSubscribeStream(final String userId, final MediaType type, boolean isScreenShare, final ResultCallback<String> callback) {
        if (getRTCRoom() == null) {
            SLog.e(SLog.TAG_RTC, "unSubscribeStream failed , room = " + getRTCRoom());
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }
        RongRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);

        if (remoteUser == null || remoteUser.getRemoteAVStreams() == null) {
            SLog.e(SLog.TAG_RTC, "unSubscribeStream failed , remoteUser  = " + remoteUser);
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }

        ArrayList<RongRTCAVInputStream> inputStreams = new ArrayList<>();
        for (RongRTCAVInputStream inputStream : remoteUser.getRemoteAVStreams()) {
            if (type == MediaType.VIDEO && inputStream.getMediaType() == MediaType.VIDEO) {
                if (isScreenShare && inputStream.getTag().equals("screenshare")) {
                    inputStreams.add(inputStream);
                } else  if (!isScreenShare && !inputStream.getTag().equals("screenshare")) {
                    inputStreams.add(inputStream);
                }

            } else if (type == MediaType.AUDIO && inputStream.getMediaType() == MediaType.AUDIO) {
                inputStreams.add(inputStream);
            }
        }

        SLog.e(SLog.TAG_RTC, "unSubscribeStream , inputStreams  = " + inputStreams);
        remoteUser.unsubscribeAVStream(inputStreams, new RongRTCResultUICallBack() {
            @Override
            public void onUiSuccess() {
                SLog.d(SLog.TAG_RTC, "unSubscribeStream  " + type + " success,user = " + userId);
                if (callback != null) {
                    callback.onSuccess(userId);
                }
            }

            @Override
            public void onUiFailed(RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "unSubscribeStream  " + type + " error - " + rtcErrorCode.getReason());

                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                if (rtcCallback != null) {
                    rtcCallback.onFail(rtcErrorCode);
                }
            }
        });

    }

    /**
     * 订阅大流
     */
    public void exchangeStreamToNormalStream(String userid) {
        Map<String, RongRTCRemoteUser> remoteUserMap = getRTCRoom().getRemoteUsers();
        RongRTCRemoteUser remoteUser = remoteUserMap.get(userid);
        if (remoteUser != null) {
            remoteUser.exchangeStreamToNormalStream(new RongRTCResultUICallBack() {
                @Override
                public void onUiSuccess() {
                    SLog.d(SLog.TAG_RTC, "exchangeStreamToNormalStream->onUiSuccess.");
                }

                @Override
                public void onUiFailed(RTCErrorCode rtcErrorCode) {
                    SLog.e(SLog.TAG_RTC, "exchangeStreamToNormalStream->onUiFailed rtcErrorCode :" + rtcErrorCode.getValue());
                }
            });
        } else {
            SLog.e(SLog.TAG_RTC, "exchangeStreamToNormalStream:remoteUser is empty.");
        }
    }

    /**
     * 订阅小流
     */
    public void exchangeStreamToTinyStream(String userid){
        Map<String, RongRTCRemoteUser> remoteUserMap = getRTCRoom().getRemoteUsers();
        RongRTCRemoteUser remoteUser = remoteUserMap.get(userid);
        if (remoteUser != null) {
            remoteUser.exchangeStreamToTinyStream(new RongRTCResultUICallBack() {
                @Override
                public void onUiSuccess() {
                    SLog.d(SLog.TAG_RTC, "exchangeStreamToTinyStream->onUiSuccess.");
                }

                @Override
                public void onUiFailed(RTCErrorCode rtcErrorCode) {
                    SLog.e(SLog.TAG_RTC, "exchangeStreamToTinyStream->onUiFailed rtcErrorCode :" + rtcErrorCode.getValue());
                }
            });
        } else {
            SLog.e(SLog.TAG_RTC, "exchangeStreamToTinyStream:remoteUser is empty.");
        }
    }

    private RongRTCRoom getRTCRoom() {
        return rongRTCRoom;
    }


    // 注册房间监听
    private void setRoomEventListener() {
        if (getRTCRoom() != null) {
            SLog.d(SLog.TAG_RTC, "Set room event listener");
            getRTCRoom().registerEventsListener(rtcEventsListener);
        }
    }

    // 注销房间监听
    private void removeRoomEventListener() {
        if (getRTCRoom() != null) {
            SLog.d(SLog.TAG_RTC, "Remove room event listener");
            getRTCRoom().unregisterEventsListener(rtcEventsListener);
        }
    }

    //注册房间统计信息监听
    private void registerStatusReportListener() {
        if (getRTCRoom() != null && needRegisteredStatusReportListener) {
            getRTCRoom().registerStatusReportListener(statusReportListener);
        }
    }

    //取消注册房间统计信息监听
    private void unregisterStatusReportListener() {
        if (getRTCRoom() != null && needRegisteredStatusReportListener) {
            getRTCRoom().unregisterStatusReportListener(statusReportListener);
        }
    }


    private Map<String, List<RongRTCAVInputStream>> getRemoteUsersInfo() {
        Map<String, List<RongRTCAVInputStream>> userIdInfo = new HashMap<>();
        if (getRTCRoom() != null) {
            Collection<RongRTCRemoteUser> values = getRTCRoom().getRemoteUsers().values();
            if (values != null && values.size() > 0) {
                for (RongRTCRemoteUser user : values) {
                    SLog.d(SLog.TAG_RTC, "remoteuser=" + user);
                    if (user != null) {
                        userIdInfo.put(user.getUserId(), user.getRemoteAVStreams());
                    }
                }
            }
        }
        return userIdInfo;
    }


    private static RongRTCStatusReportListener statusReportListener=new RongRTCStatusReportListener() {
        @Override
        public void onAudioReceivedLevel(HashMap<String, String> hashMap) {
        }

        @Override
        public void onAudioInputLevel(String s) {
        }

        @Override
        public void onConnectionStats(StatusReport statusReport) {
            if (statusReport != null) {
                HashMap<String, StatusBean> map = statusReport.statusVideoRcvs;
                if (map != null && map.size() > 0) {
                    StatusBean bean = null;
                    for (Map.Entry<String, StatusBean> entry : map.entrySet()) {
                        bean = entry.getValue();
                        SLog.d("BUGTAGS", "id :" + (bean.isSend ? "本地" : bean.id) + " , WxH:" + bean.frameWidth + " x " + bean.frameHeight + " ,bitRate : " + bean.bitRate + " , packetLostRate :" + bean.packetLostRate);
                    }
                }

                HashMap<String, StatusBean> statusVideoSends = statusReport.statusVideoSends;
                if (map != null && statusVideoSends.size() > 0) {
                    StatusBean videoSend = null;
                    for (Map.Entry<String, StatusBean> entry : statusVideoSends.entrySet()) {
                        videoSend = entry.getValue();
                        SLog.d("BUGTAGS", "id : local" + ", WxH:" + videoSend.frameWidth + " x " + videoSend.frameHeight + " ,bitRate : " + videoSend.bitRate + " , packetLostRate :" + videoSend.packetLostRate);
                    }
                }
            }
        }
    };

    // 房间事件监听
    private RongRTCEventsListener rtcEventsListener = new RongRTCEventsListener() {

        @Override
        public void onRemoteUserPublishResource(RongRTCRemoteUser rongRTCRemoteUser, List<RongRTCAVInputStream> list) {
            SLog.d(SLog.TAG_RTC, "Remote user publish resource，user = " + rongRTCRemoteUser.getUserId() + "," + rtcCallback);
            if (rtcCallback != null) {
                rtcCallback.onRemoteUserPublishResource(rongRTCRemoteUser.getUserId(), list);
            }
        }

        @Override
        public void onRemoteUserAudioStreamMute(RongRTCRemoteUser rongRTCRemoteUser, RongRTCAVInputStream rongRTCAVInputStream, boolean b) {
            SLog.d(SLog.TAG_RTC, "Remote User AudioStream Mute，user=" + rongRTCRemoteUser.getUserId() + ", mute = " + b + "," + rtcCallback);
            if (rtcCallback != null) {
                rtcCallback.onRemoteUserAudioStreamMute(rongRTCRemoteUser.getUserId(), b);
            }
        }

        @Override
        public void onRemoteUserVideoStreamEnabled(RongRTCRemoteUser rongRTCRemoteUser, RongRTCAVInputStream rongRTCAVInputStream, boolean b) {
            SLog.d(SLog.TAG_RTC, "Remote user video stream enable，user=" + rongRTCRemoteUser.getUserId() + ", Enabled = " + b + "," + rtcCallback);
            if (rtcCallback != null) {
                rtcCallback.onRemoteUserVideoStreamEnabled(rongRTCRemoteUser.getUserId(), b);
            }
        }

        @Override
        public void onRemoteUserUnpublishResource(RongRTCRemoteUser rongRTCRemoteUser, List<RongRTCAVInputStream> list) {
            SLog.d(SLog.TAG_RTC, "Remote user unpublish resource，user=" + rongRTCRemoteUser.getUserId() + "," + rtcCallback);
            if (rtcCallback != null) {
                rtcCallback.onRemoteUserUnPublishResource(rongRTCRemoteUser.getUserId(), list);
            }
        }

        @Override
        public void onUserJoined(RongRTCRemoteUser rongRTCRemoteUser) {
            SLog.d(SLog.TAG_RTC, "User join room ，user=" + rongRTCRemoteUser.getUserId());
            if (rtcCallback != null) {
                rtcCallback.onUserJoined(rongRTCRemoteUser.getUserId());
            }
        }

        @Override
        public void onUserLeft(RongRTCRemoteUser rongRTCRemoteUser) {
            SLog.d(SLog.TAG_RTC, "User left room ，user=" + rongRTCRemoteUser.getUserId());
            if (rtcCallback != null) {
                rtcCallback.onUserLeft(rongRTCRemoteUser.getUserId());
            }
        }

        @Override
        public void onUserOffline(RongRTCRemoteUser rongRTCRemoteUser) {
            SLog.d(SLog.TAG_RTC, "User offline ，user=" + rongRTCRemoteUser.getUserId());
            if (rtcCallback != null) {
                rtcCallback.onUserOffline(rongRTCRemoteUser.getUserId());
            }
        }

        @Override
        public void onVideoTrackAdd(String s, String s1) {
            SLog.d(SLog.TAG_RTC, "Video Track Add  ，s = " + s + ", s1 = " + s1);
        }

        @Override
        public void onFirstFrameDraw(String userId, String tag) {
            SLog.d(SLog.TAG_RTC, "First frame draw  ，userId = " + userId + ", tag = " + tag);
            if (rtcCallback != null) {
                rtcCallback.onFirstFrameDraw(userId, tag);
            }
        }

        @Override
        public void onLeaveRoom() {
            SLog.d(SLog.TAG_RTC, "Leave room");
        }

        @Override
        public void onReceiveMessage(Message message) {

        }

        @Override
        public void onKickedByServer() {

        }
    };

    public void setVideoResolution(VideoResolution resolution) {
        RongRTCConfig config = new RongRTCConfig.Builder().setVideoProfile(resolution.getProfile())
                .enableTinyStream(true)
                .build();
        RongRTCCapture.getInstance().setRTCConfig(config);
    }

    public interface RtcCallback {

        void onInitialRemoteUserList(Map<String, List<RongRTCAVInputStream>> userInfos);

        void onRemoteUserPublishResource(String userIds, List<RongRTCAVInputStream> list);

        void onRemoteUserUnPublishResource(String userId, List<RongRTCAVInputStream> list);

        void onRemoteUserAudioStreamMute(String userId, boolean mute);

        void onRemoteUserVideoStreamEnabled(String userId, boolean enable);

        void onFail(RTCErrorCode rtcErrorCode);

        void onUserJoined(String userId);

        void onUserLeft(String userId);

        void onUserOffline(String userId);

        void onFirstFrameDraw(String userId, String tag);
    }
}
