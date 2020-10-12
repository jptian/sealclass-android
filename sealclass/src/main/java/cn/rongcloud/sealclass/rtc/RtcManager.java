package cn.rongcloud.sealclass.rtc;

import android.os.Handler;
import android.util.Log;
import cn.rongcloud.rtc.api.RCRTCConfig.Builder;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCLocalUser;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.callback.IRCRTCStatusReportListener;
import cn.rongcloud.rtc.api.report.StatusBean;
import cn.rongcloud.rtc.api.report.StatusReport;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCOutputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCStreamType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.sealclass.common.ResultUICallback;
import cn.rongcloud.sealclass.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.rongcloud.sealclass.common.ErrorCode;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.utils.log.SLog;

/**
 * Rong RTC 语音业务封装
 */
public class RtcManager {
    private static final String TAG = RtcManager.class.getSimpleName();
    private static RtcManager instance;
    private RCRTCRoom rongRTCRoom;
    private RtcCallback rtcCallback;
    private Handler UIHandler = null;

    //是否需要注册状态回调，目前仅调试打开
    private boolean needRegisteredStatusReportListener = false;

    private RtcManager() {
        UIHandler = new android.os.Handler(Utils.getContext().getMainLooper());
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

    /**
     * 初始化 SDK
     */
    public void initRTCEngine(){
        RCRTCEngine.getInstance().init(Utils.getContext(), Builder.create().build());
    }

    /**
     * 取消初始化，释放资源
     */
    public void unInit(){
        RCRTCEngine.getInstance().unInit();
    }

    public void getUIHandler(Runnable runnable) {
        if(UIHandler != null){
            UIHandler.post(runnable);
        }else{
            Log.e(TAG,"UIHandler = null .");
        }
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
    public void joinRtcRoom(final String roomId, final ResultUICallback<String> callback) {
        RCRTCEngine.getInstance().joinRoom(roomId, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(RCRTCRoom rtcRoom) {
                SLog.d(SLog.TAG_RTC, "Join room success" + rtcRoom);
                rongRTCRoom = rtcRoom;
                setRoomEventListener();
                registerStatusReportListener();

                if (callback != null) {
                    callback.onSuccess(roomId);
                }
            }

            @Override
            public void onFailed(final RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "joinRtcRoom failed - " + rtcErrorCode.getReason());
                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                getUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (rtcCallback != null) {
                            rtcCallback.onFail(rtcErrorCode);
                        }
                    }
                });
            }
        });
    }

    /**
     * 离开聊天室
     *
     * @param roomId
     */
    public void quitRtcRoom(final String roomId, final ResultUICallback<String> callback) {
        removeRoomEventListener();
        unregisterStatusReportListener();
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                SLog.d(SLog.TAG_RTC, "Quit room");
                if (callback != null) {
                    callback.onSuccess(roomId);
                }
            }

            @Override
            public void onFailed(final RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "quitRtcRoom error - " + rtcErrorCode.getReason());

                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                getUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (rtcCallback != null) {
                            rtcCallback.onFail(rtcErrorCode);
                        }
                    }
                });
            }
        });
    }


    public void startRtcChat(RCRTCVideoView view, final ResultUICallback<Boolean> callback) {
        if (getRTCRoom() == null) {
            SLog.e(SLog.TAG_RTC, "getRTCRoom() ==  " + getRTCRoom());
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }
        RCRTCLocalUser localUser = getRTCRoom().getLocalUser();
        if (localUser == null) {
            SLog.e(SLog.TAG_RTC, "startRtcChat failed localUser is null ");
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }
        //设置本地预览视图
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(view);
        //开始采集数据
        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);

        List<RCRTCOutputStream> localAvStreams = new ArrayList<>();
        localAvStreams.add(RCRTCEngine.getInstance().getDefaultAudioStream());
        localAvStreams.add(RCRTCEngine.getInstance().getDefaultVideoStream());
        localUser.publishStreams(localAvStreams, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                SLog.d(SLog.TAG_RTC, "Local publish avStream success");
                if (callback != null) {
                    callback.onSuccess(true);
                }
            }

            @Override
            public void onFailed(final RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "startVoiceChat error - " + rtcErrorCode.getReason());

                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }

                getUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (rtcCallback != null) {
                            rtcCallback.onFail(rtcErrorCode);
                        }
                    }
                });
            }
        });
    }

    /**
     * 停止聊天
     */
    public void stopRTCChat(final ResultUICallback<Boolean> callback) {
        if (getRTCRoom() == null) {
            return;
        }
        //设置本地预览视图
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(null);
        //开始采集数据
        RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
        RCRTCLocalUser localUser = getRTCRoom().getLocalUser();
        if (localUser == null) {
            SLog.e(SLog.TAG_RTC, "stopRTCChat failed localUser is null ");
            return;
        }
        localUser.unpublishStreams(localUser.getStreams(), new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess(true);
                }
            }

            @Override
            public void onFailed(final RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "stopVoiceChat error - " + rtcErrorCode.getReason());
                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }
                getUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (rtcCallback != null) {
                            rtcCallback.onFail(rtcErrorCode);
                        }
                    }
                });
            }
        });
    }


    /**
     * 是否使用语音聊天
     */
    public void setLocalMicEnable(boolean enable) {
        if(RCRTCEngine.getInstance().getDefaultAudioStream()!=null){
            RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(!enable);
        }
    }

    /**
     * 是否使用视频聊天
     */
    public void setLocalVideoEnable(boolean enable) {
        if(RCRTCEngine.getInstance().getDefaultVideoStream()!=null){
            if (enable) {
                RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
            } else {
                RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
            }
        }
    }

    public boolean isMuteVideo() {
        if (RCRTCEngine.getInstance().getDefaultVideoStream() != null) {
            return RCRTCEngine.getInstance().getDefaultVideoStream().isMute();
        }
        return false;
    }

    public void startCapture() {
        if (RCRTCEngine.getInstance().getDefaultVideoStream() != null) {
            RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
        }
    }

    public void stopCapture(){
        if (RCRTCEngine.getInstance().getDefaultVideoStream() != null) {
            RCRTCEngine.getInstance().getDefaultVideoStream().stopCamera();
        }
    }

    public void switchCamera() {
        if(RCRTCEngine.getInstance().getDefaultVideoStream()!=null){
            RCRTCEngine.getInstance().getDefaultVideoStream().switchCamera(null);
        }
    }

    /**
     * 是否打开扬声器
     * @param enabled
     */
    public void setEnableSpeakerphone(boolean enabled) {
        RCRTCEngine.getInstance().enableSpeaker(enabled);
    }


    /**
     * 房间静音设置
     * 目前没有逻辑用到该方法，如用到必须考虑订阅视频的大小流问题
     */
    public void muteRoomVoice(boolean mute, final ResultUICallback<Boolean> callback) {
        if (getRTCRoom() == null) {
            return;
        }
        List<RCRTCRemoteUser> remoteUserList= getRTCRoom().getRemoteUsers();
        List<RCRTCInputStream> receiveVoiceSteamList = new ArrayList<>();
        for (RCRTCRemoteUser remoteUser : remoteUserList) {
            List<RCRTCInputStream> remoteAVStreams = remoteUser.getStreams();
            for (RCRTCInputStream stream : remoteAVStreams) {
                if (stream.getMediaType() == RCRTCMediaType.AUDIO) {
                    receiveVoiceSteamList.addAll(remoteUser.getStreams());
                }
            }
        }

        if (receiveVoiceSteamList.size() > 0) {
            if (mute) {
                getRTCRoom().getLocalUser().unsubscribeStreams(receiveVoiceSteamList, new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {
                        if (callback != null) {
                            callback.onSuccess(true);
                        }
                    }

                    @Override
                    public void onFailed(final RTCErrorCode rtcErrorCode) {
                        SLog.e(SLog.TAG_RTC, "muteRoomVoice error - " + rtcErrorCode.getReason());
                        getUIHandler(new Runnable() {
                            @Override
                            public void run() {
                                if (rtcCallback != null) {
                                    rtcCallback.onFail(rtcErrorCode);
                                }
                            }
                        });
                    }
                });
            } else {
                getRTCRoom().getLocalUser().subscribeStreams(receiveVoiceSteamList, new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {
                        if (callback != null) {
                            callback.onSuccess(false);
                        }
                    }

                    @Override
                    public void onFailed(final RTCErrorCode rtcErrorCode) {
                        SLog.e(SLog.TAG_RTC, "muteRoomVoice error - " + rtcErrorCode.getReason());
                        getUIHandler(new Runnable() {
                            @Override
                            public void run() {
                                if (rtcCallback != null) {
                                    rtcCallback.onFail(rtcErrorCode);
                                }
                            }
                        });
                    }
                });
            }

        }
    }


    /**
     * 订阅所有当前在房间发布资源的用户
     */
    public void subscribeAll(HashMap<String, RCRTCVideoView> videoViews, final ResultUICallback<Boolean> callback) {
        if (getRTCRoom() != null) {
            List<RCRTCRemoteUser> remoteUserList = getRTCRoom().getRemoteUsers();
            List<RCRTCInputStream> receiveSteamList = new ArrayList<>();
            for (RCRTCRemoteUser remoteUser : remoteUserList) {
                for (RCRTCInputStream inputStream : remoteUser.getStreams()) {
                    if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                        ((RCRTCVideoInputStream)inputStream).setStreamType(RCRTCStreamType.TINY);
                        ((RCRTCVideoInputStream)inputStream).setVideoView(videoViews.get(remoteUser.getUserId()));
                    }
                }
                receiveSteamList.addAll(remoteUser.getStreams());
            }
            if (receiveSteamList.size() > 0) {
                getRTCRoom().getLocalUser().subscribeStreams(receiveSteamList, new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {
                        if (callback != null) {
                            callback.onSuccess(true);
                        }
                    }

                    @Override
                    public void onFailed(RTCErrorCode rtcErrorCode) {
                        if (callback != null) {
                            callback.onFail(ErrorCode.RTC_ERROR.getCode());
                        }
                    }
                });
            }
        }

    }

    public void subscribe(String userId, RCRTCVideoView videoView, int role, final ResultUICallback<String> callback) {
        RCRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);
        subscribe(remoteUser, videoView, null, role, callback);
    }

    public void subscribe(String userId, RCRTCVideoView videoView, RCRTCVideoView screenShareView, int role, final ResultUICallback<String> callback) {
        RCRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);
        subscribe(remoteUser, videoView, screenShareView, role,callback);
    }

    // 订阅单人, 视频只订阅 video
    private void subscribe(final RCRTCRemoteUser remoteUser, RCRTCVideoView videoView, RCRTCVideoView screenShareView, int role, final ResultUICallback<String> callback) {
        if (getRTCRoom() == null || remoteUser == null || remoteUser.getStreams() == null) {
            return;
        }

        for (RCRTCInputStream inputStream : remoteUser.getStreams()) {
            if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                SLog.d(SLog.TAG_RTC, "Set remote video view , user = " + remoteUser.getUserId());
                if (inputStream.getTag().equals("screenshare") && screenShareView != null) {
                    ((RCRTCVideoInputStream)inputStream).setVideoView(screenShareView);
                } else {
                    RCRTCStreamType rcrtcStreamType=RCRTCStreamType.TINY;
                    if (role == Role.STUDENT.getValue()) {
                        rcrtcStreamType=RCRTCStreamType.NORMAL;
                    } else {
                        rcrtcStreamType=RCRTCStreamType.TINY;
                    }
                    ((RCRTCVideoInputStream)inputStream).setStreamType(rcrtcStreamType);
                    ((RCRTCVideoInputStream)inputStream).setVideoView(videoView);
                }
            }
        }
        getRTCRoom().getLocalUser().subscribeStreams(remoteUser.getStreams(), new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                SLog.d(SLog.TAG_RTC, "subscribeAvStream success,user = " + remoteUser.getUserId());
                if (callback != null) {
                    callback.onSuccess(remoteUser.getUserId());
                }
            }

            @Override
            public void onFailed(final RTCErrorCode rtcErrorCode) {
                SLog.e(SLog.TAG_RTC, "subscribeAvStream error - " + rtcErrorCode.getReason());

                if (callback != null) {
                    callback.onFail(ErrorCode.RTC_ERROR.getCode());
                }
                getUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (rtcCallback != null) {
                            rtcCallback.onFail(rtcErrorCode);
                        }
                    }
                });
            }
        });
    }

    public void unSubscribe(final String userId, final ResultUICallback<String> callback) {
        RCRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);
        if (getRTCRoom() == null || remoteUser == null || remoteUser.getStreams() == null) {
            return;
        }
        getRTCRoom().getLocalUser().unsubscribeStreams(remoteUser.getStreams(), new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                SLog.d(SLog.TAG_RTC, "unSubscribeAvStream success,user = " + userId);
                if (callback != null) {
                    callback.onSuccess(userId);
                }
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
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


    public void subscribeVideo(final String userId, RCRTCVideoView videoView, final ResultUICallback<String> callback) {
        subscribeStream(userId, RCRTCMediaType.VIDEO, false, videoView, callback);
    }


    public void unSubscribeVideo(String userId, final ResultUICallback<String> callback) {
        unSubscribeStream(userId, RCRTCMediaType.VIDEO, false, callback);
    }

    public void subscribeAudio(final String userId, final ResultUICallback<String> callback) {
        subscribeStream(userId, RCRTCMediaType.AUDIO, false, null, callback);
    }


    public void unSubscribeAudio(String userId, final ResultUICallback<String> callback) {
        unSubscribeStream(userId, RCRTCMediaType.AUDIO, false, callback);
    }

    public void subscribeScreen(String userId, RCRTCVideoView videoView, final ResultUICallback<String> callback) {
        subscribeStream(userId, RCRTCMediaType.VIDEO, true, videoView, callback);

    }

    public void unSubscribeScreen(String userId, final ResultUICallback<String> callback) {
        unSubscribeStream(userId, RCRTCMediaType.VIDEO, true, callback);
    }


    private void subscribeStream(final String userId, final RCRTCMediaType type, boolean isScreenShare, RCRTCVideoView videoView, final ResultUICallback<String> callback) {
        if (getRTCRoom() == null) {
            SLog.e(SLog.TAG_RTC, type + " subscribeStream failed , room = " + getRTCRoom());
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }
        final RCRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);

        if (remoteUser == null || remoteUser.getStreams() == null) {
            SLog.e(SLog.TAG_RTC, type + "subscribeStream failed ,  room = " + getRTCRoom() + "userId = " + userId + "，remoteUser  = " + remoteUser);
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }

        ArrayList<RCRTCInputStream> inputStreams = new ArrayList<>();
        for (RCRTCInputStream inputStream : remoteUser.getStreams()) {
            if (type == RCRTCMediaType.VIDEO && inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                if (isScreenShare && inputStream.getTag().equals("screenshare")) {
                    ((RCRTCVideoInputStream)inputStream).setVideoView(videoView);
                        SLog.d(SLog.TAG_RTC, "subscribeStream, Set remote video screen share view ,  user = " + remoteUser.getUserId() + ",videoView = " + videoView);
                        inputStreams.add(inputStream);

                } else if (!isScreenShare && !inputStream.getTag().equals("screenshare")) {
                    ((RCRTCVideoInputStream)inputStream).setVideoView(videoView);
                    ((RCRTCVideoInputStream)inputStream).setStreamType(RCRTCStreamType.TINY);
                    SLog.d(SLog.TAG_RTC, "subscribeStream, Set remote video view , user = " + remoteUser.getUserId() + ",videoView = " + videoView);
                    inputStreams.add(inputStream);
                }

            } else if (type == RCRTCMediaType.AUDIO && inputStream.getMediaType() == RCRTCMediaType.AUDIO) {
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
        getRTCRoom().getLocalUser().subscribeStreams(inputStreams, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                SLog.d(SLog.TAG_RTC, "subscribeStream " + type + " success,user = " + userId);
                if (callback != null) {
                    callback.onSuccess(userId);
                }
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
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


    private void unSubscribeStream(final String userId, final RCRTCMediaType type, boolean isScreenShare, final ResultUICallback<String> callback) {
        if (getRTCRoom() == null) {
            SLog.e(SLog.TAG_RTC, "unSubscribeStream failed , room = " + getRTCRoom());
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }
        RCRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userId);

        if (remoteUser == null || remoteUser.getStreams() == null) {
            SLog.e(SLog.TAG_RTC, "unSubscribeStream failed , remoteUser  = " + remoteUser);
            if (callback != null) {
                callback.onFail(ErrorCode.RTC_ERROR.getCode());
            }
            return;
        }

        ArrayList<RCRTCInputStream> inputStreams = new ArrayList<>();
        for (RCRTCInputStream inputStream : remoteUser.getStreams()) {
            if (type == RCRTCMediaType.VIDEO && inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                if (isScreenShare && inputStream.getTag().equals("screenshare")) {
                    inputStreams.add(inputStream);
                } else  if (!isScreenShare && !inputStream.getTag().equals("screenshare")) {
                    inputStreams.add(inputStream);
                }

            } else if (type == RCRTCMediaType.AUDIO && inputStream.getMediaType() == RCRTCMediaType.AUDIO) {
                inputStreams.add(inputStream);
            }
        }

        SLog.e(SLog.TAG_RTC, "unSubscribeStream , inputStreams  = " + inputStreams);
        getRTCRoom().getLocalUser().unsubscribeStreams(inputStreams, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                SLog.d(SLog.TAG_RTC, "unSubscribeStream  " + type + " success,user = " + userId);
                if (callback != null) {
                    callback.onSuccess(userId);
                }
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
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
        RCRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userid);
        if (remoteUser != null) {
            remoteUser.switchToNormalStream(new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    SLog.d(SLog.TAG_RTC, "exchangeStreamToNormalStream->onUiSuccess.");
                }

                @Override
                public void onFailed(RTCErrorCode rtcErrorCode) {
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
        RCRTCRemoteUser remoteUser = getRTCRoom().getRemoteUser(userid);
        if (remoteUser != null) {
            remoteUser.switchToTinyStream(new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    SLog.d(SLog.TAG_RTC, "exchangeStreamToTinyStream->onUiSuccess.");
                }

                @Override
                public void onFailed(RTCErrorCode rtcErrorCode) {
                    SLog.e(SLog.TAG_RTC, "exchangeStreamToTinyStream->onUiFailed rtcErrorCode :" + rtcErrorCode.getValue());
                }
            });
        } else {
            SLog.e(SLog.TAG_RTC, "exchangeStreamToTinyStream:remoteUser is empty.");
        }
    }

    private RCRTCRoom getRTCRoom() {
        return rongRTCRoom;
    }


    // 注册房间监听
    private void setRoomEventListener() {
        if (getRTCRoom() != null) {
            SLog.d(SLog.TAG_RTC, "Set room event listener");
            getRTCRoom().registerRoomListener(rtcEventsListener);
        }
    }

    // 注销房间监听
    private void removeRoomEventListener() {
        if (getRTCRoom() != null) {
            SLog.d(SLog.TAG_RTC, "Remove room event listener");
            getRTCRoom().unregisterRoomListener();
        }
    }

    //注册房间统计信息监听
    private void registerStatusReportListener() {
        if (needRegisteredStatusReportListener) {
            RCRTCEngine.getInstance().registerStatusReportListener(statusReportListener);
        }
    }

    //取消注册房间统计信息监听
    private void unregisterStatusReportListener() {
        RCRTCEngine.getInstance().unregisterStatusReportListener();
    }


    private Map<String, List<RCRTCInputStream>> getRemoteUsersInfo() {
        Map<String, List<RCRTCInputStream>> userIdInfo = new HashMap<>();
        if (getRTCRoom() != null) {
            if (getRTCRoom().getRemoteUsers().size() > 0) {
                for (RCRTCRemoteUser user : getRTCRoom().getRemoteUsers()) {
                    SLog.d(SLog.TAG_RTC, "remoteuser=" + user);
                    if (user != null) {
                        userIdInfo.put(user.getUserId(), user.getStreams());
                    }
                }
            }
        }
        return userIdInfo;
    }

    private IRCRTCStatusReportListener statusReportListener =new IRCRTCStatusReportListener() {
        @Override
        public void onAudioReceivedLevel(HashMap<String, String> hashMap) {
            super.onAudioReceivedLevel(hashMap);
        }

        @Override
        public void onAudioInputLevel(String s) {
            super.onAudioInputLevel(s);
        }

        @Override
        public void onConnectionStats(final StatusReport statusReport) {
            super.onConnectionStats(statusReport);
            getUIHandler(new Runnable() {
                @Override
                public void run() {
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
            });
        }
    };

    private IRCRTCRoomEventsListener rtcEventsListener=new IRCRTCRoomEventsListener() {
        @Override
        public void onRemoteUserPublishResource(final RCRTCRemoteUser rcrtcRemoteUser, final List<RCRTCInputStream> list) {
            SLog.d(SLog.TAG_RTC, "Remote user publish resource，user = " + rcrtcRemoteUser.getUserId() + "," + rtcCallback);
            getUIHandler(new Runnable() {
                @Override
                public void run() {
                    if (rtcCallback != null) {
                        rtcCallback.onRemoteUserPublishResource(rcrtcRemoteUser.getUserId(), list);
                    }
                }
            });
        }

        @Override
        public void onRemoteUserMuteAudio(final RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, final boolean b) {
            SLog.d(SLog.TAG_RTC, "Remote User AudioStream Mute，user=" + rcrtcRemoteUser.getUserId() + ", mute = " + b + "," + rtcCallback);
            getUIHandler(new Runnable() {
                @Override
                public void run() {
                    if (rtcCallback != null) {
                        rtcCallback.onRemoteUserAudioStreamMute(rcrtcRemoteUser.getUserId(), b);
                    }
                }
            });
        }

        @Override
        public void onRemoteUserMuteVideo(final RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, final boolean b) {
            SLog.d(SLog.TAG_RTC, "Remote user video stream enable，user=" + rcrtcRemoteUser.getUserId() + ", Enabled = " + b + "," + rtcCallback);
            getUIHandler(new Runnable() {
                @Override
                public void run() {
                    if (rtcCallback != null) {
                        rtcCallback.onRemoteUserVideoStreamEnabled(rcrtcRemoteUser.getUserId(), b);
                    }
                }
            });
        }

        @Override
        public void onRemoteUserUnpublishResource(final RCRTCRemoteUser rcrtcRemoteUser, final List<RCRTCInputStream> list) {
            SLog.d(SLog.TAG_RTC, "Remote user unpublish resource，user=" + rcrtcRemoteUser.getUserId() + "," + rtcCallback);
            getUIHandler(new Runnable() {
                @Override
                public void run() {
                    if (rtcCallback != null) {
                        rtcCallback.onRemoteUserUnPublishResource(rcrtcRemoteUser.getUserId(), list);
                    }
                }
            });
        }

        @Override
        public void onUserJoined(final RCRTCRemoteUser rcrtcRemoteUser) {
            SLog.d(SLog.TAG_RTC, "User join room ，user=" + rcrtcRemoteUser.getUserId());
            getUIHandler(new Runnable() {
                @Override
                public void run() {
                    if (rtcCallback != null) {
                        rtcCallback.onUserJoined(rcrtcRemoteUser.getUserId());
                    }
                }
            });
        }

        @Override
        public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
            SLog.d(SLog.TAG_RTC, "User left room ，user=" + rcrtcRemoteUser.getUserId());
            if (rtcCallback != null) {
                rtcCallback.onUserLeft(rcrtcRemoteUser.getUserId());
            }
        }

        @Override
        public void onUserOffline(final RCRTCRemoteUser rcrtcRemoteUser) {
            SLog.d(SLog.TAG_RTC, "User offline ，user=" + rcrtcRemoteUser.getUserId());
            getUIHandler(new Runnable() {
                @Override
                public void run() {
                    if (rtcCallback != null) {
                        rtcCallback.onUserOffline(rcrtcRemoteUser.getUserId());
                    }
                }
            });
        }

        @Override
        public void onLeaveRoom(int i) {

        }

        @Override
        public void onFirstRemoteVideoFrame(final String s, final String s1) {
            super.onFirstRemoteVideoFrame(s, s1);
            SLog.d(SLog.TAG_RTC, "First frame draw  ，userId = " + s + ", tag = " + s1);
            getUIHandler(new Runnable() {
                @Override
                public void run() {
                    if (rtcCallback != null) {
                        rtcCallback.onFirstFrameDraw(s, s1);
                    }
                }
            });
        }
    };

    public void setVideoResolution(VideoResolution resolution) {
        RCRTCVideoStreamConfig videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create()
            //设置分辨率
            .setVideoResolution(resolution.getProfile())
            .build();
        if (RCRTCEngine.getInstance().getDefaultVideoStream() != null) {
            RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoConfigBuilder);
            RCRTCEngine.getInstance().getDefaultVideoStream().enableTinyStream(true);
        }
    }

    public interface RtcCallback {

        void onInitialRemoteUserList(Map<String, List<RCRTCInputStream>> userInfos);

        void onRemoteUserPublishResource(String userIds, List<RCRTCInputStream> list);

        void onRemoteUserUnPublishResource(String userId, List<RCRTCInputStream> list);

        void onRemoteUserAudioStreamMute(String userId, boolean mute);

        void onRemoteUserVideoStreamEnabled(String userId, boolean enable);

        void onFail(RTCErrorCode rtcErrorCode);

        void onUserJoined(String userId);

        void onUserLeft(String userId);

        void onUserOffline(String userId);

        void onFirstFrameDraw(String userId, String tag);
    }
}
