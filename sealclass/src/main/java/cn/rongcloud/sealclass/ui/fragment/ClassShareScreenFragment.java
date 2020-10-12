package cn.rongcloud.sealclass.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.sealclass.whiteboard.WhiteBoardConsant;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.PptPage;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SceneState;
import com.herewhite.sdk.domain.UrlInterrupter;

import com.herewhite.sdk.domain.ViewMode;
import io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.api.HttpClientManager;
import cn.rongcloud.sealclass.common.ShowToastObserver;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.RequestState;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.model.ScreenDisplay;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.permission.ClassPermission;
import cn.rongcloud.sealclass.ui.VideoViewManager;
import cn.rongcloud.sealclass.ui.view.ClassVideoListItem;
import cn.rongcloud.sealclass.ui.view.DisplayNullView;
import cn.rongcloud.sealclass.ui.view.RadioRtcVideoView;
import cn.rongcloud.sealclass.utils.log.SLog;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;
import cn.rongcloud.sealclass.whiteboard.PencilColorPopupWindow;
import io.rong.imlib.RongIMClient;

/**
 * 共享区域的界面
 */
public class ClassShareScreenFragment extends BaseFragment {
    private final static String TAG = "share_display";
    public final static String ARGUMENT_BOOLEAN_IS_FULL_SCREEN = "is_full_screen";
    private final static int REQUEST_WHITE_BOARD_UPLOAD_FILE = 1001;

    private WhiteboardView mWebView;
    private View mWebViewContianer;
    private ClassVideoListItem shareVideoView;
    private ClassVideoListItem shareScreenVideoView;
    private ClassViewModel classViewModel;
    private CheckBox fullScreenCb;
    private OnClickToFullScreenListener onClickFullScreenListener;
    private OnVideoViewChangeListenr onVideoViewChangeListenr;
    private UserInfo userInfoValue;
    private DisplayNullView displayNull;
    private boolean isFullScreen = false;
    private ValueCallback<Uri[]> webUploadCallback;
    private View mWebViewLoading;

    //白板相关功能,集成第三方 herewhite(http://www.herewhite.com/) 白板
    private View whiteBoardAction;
    private PencilColorPopupWindow pencilColorPopupWindow;
    private Scene[] whiteBoardScenes;
    private String currentSceneName;
    private int currentSceneIndex;
    private Room whiteBoardRoom;
    private Button whiteBoardPagesPrevious;
    private Button whiteBoardPagesNext;
    private String uuid;
    private RoomPhase roomPhase;

    @Override
    protected int getLayoutResId() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            isFullScreen = arguments.getBoolean(ARGUMENT_BOOLEAN_IS_FULL_SCREEN, false);
            if (isFullScreen) {
                return R.layout.class_fragment_share_screen_full_screen;
            } else {
                return R.layout.class_fragment_share_screen;
            }
        }
        return R.layout.class_fragment_share_screen;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        // 设置点击事件拦截
        mWebView = findView(R.id.class_whiteboard_display);
        mWebViewContianer = findView(R.id.class_contianer_whiteboard_display);
        mWebViewLoading = findView(R.id.class_whiteboard_loading);
        whiteBoardAction = findView(R.id.white_board_action);
        whiteBoardPagesPrevious = findView(R.id.white_board_pages_previous);
        whiteBoardPagesNext =  findView(R.id.white_board_pages_next);
        // 视频
        shareVideoView = findView(R.id.class_share_videoview);
        // 桌面共享
        shareScreenVideoView = findView(R.id.class_share_screen_videoview);
        shareVideoView.setHideRole(true);
        shareScreenVideoView.setHideRole(true);
        displayNull = findView(R.id.class_share_screen_null);

        // 全屏按钮
        fullScreenCb = findView(R.id.class_share_screen_cb_full_screen);
        fullScreenCb.setBackground(null);
        fullScreenCb.setButtonDrawable(R.drawable.class_fragment_share_screen_ic_to_full_screen_selector);
        fullScreenCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (onClickFullScreenListener != null) {
                    onClickFullScreenListener.onClickToFullScreen(isChecked);
                }
            }
        });

        // 全屏切换的原因， 需要重写对显示区域设置涂层等
        if (isFullScreen) {
            shareVideoView.setVideViewToCenter(true);
            shareScreenVideoView.setVideViewToCenter(true);
            findView(R.id.class_share_root_view, true);
        } else {
            shareVideoView.setVideViewToCenter(false);
            shareScreenVideoView.setVideViewToCenter(true);
        }
        //清理资源
        shareVideoView.clear();
        shareScreenVideoView.clear();

        //initWebView();

    }

    @Override
    protected void onInitViewModel() {
        classViewModel = ViewModelProviders.of(getActivity()).get(ClassViewModel.class);

        // 房间 id
        classViewModel.getRoomId().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {

            }
        });

        // 用户信息变化
        classViewModel.getUserInfo().observe(this, new Observer<UserInfo>() {
            @Override
            public void onChanged(final UserInfo userInfo) {

                ScreenDisplay screenDisplay = classViewModel.getDisplay().getValue();

                if (screenDisplay != null && (userInfoValue == null || userInfoValue.getRole().getValue() != userInfo.getRole().getValue())) {
                    ScreenDisplay.Display type = screenDisplay.getType();
                    if (type == ScreenDisplay.Display.WHITEBOARD) {
                        changeWhiteBoardRole(userInfo.getRole(), screenDisplay.getWhiteBoardUri());
                    }
                }
                SLog.d(TAG, "userInfo , userInfo ==" + userInfo);

                userInfoValue = userInfo;

                // 为了统一管理本地的音视频资源发布， 所以在此处统一发布和取消发布资源。如果设计到视频列表的刷新，
                // 则直接回调到 OnVideoViewChangeListenr#onChangeUser 方法即可。
                // 首先判断当前的用户是否有音视频的权限。
                // 当前助教，老师，学员都有音视频权限。
                if (userInfo.getRole().hasPermission(ClassPermission.VIDEO_CHAT)) {
                    // 首先查询是否有对应的 VideoView 控件
                    RadioRtcVideoView videoView = null;
                    if (!VideoViewManager.getInstance().containsKey(userInfo.getUserId())) {
                        // 如果没有则需要创建
                        videoView = new RadioRtcVideoView(getActivity());
                        VideoViewManager.getInstance().put(userInfo.getUserId(), videoView);
                    } else {
                        videoView = VideoViewManager.getInstance().get(userInfo.getUserId());
                    }

                    // 然后查看一下， videoview 是否进行了资源绑定
                    if (!videoView.isBindVideo()) {
                        //没有绑定则进行绑定
                        videoView.setBindVideo(true);
                        classViewModel.startRtcChat(videoView).observe(ClassShareScreenFragment.this, new ShowToastObserver(getActivity()) {
                            @Override
                            public void onChanged(RequestState state) {
                                super.onChanged(state);
                                // 假如非旁听用户， 登陆时， 选择关闭摄像头则， 这里即可设置
                                if (!userInfo.isCamera()) {
                                    classViewModel.setLocalVideoEnable(userInfo.isCamera());
                                }
                            }
                        });
                    }

                    // 通知视频列表刷新
                    if (onVideoViewChangeListenr != null) {
                        onVideoViewChangeListenr.onChangeUser(userInfo);
                    }

                } else {
                    // 没有音视频权限
                    // 检测有没有VideoView, 没有的话，则不用处理
                    if (VideoViewManager.getInstance().containsKey(userInfo.getUserId())) {
                        RadioRtcVideoView videoView = VideoViewManager.getInstance().get(userInfo.getUserId());
                        videoView.setBindVideo(false);
                        // 停止资源发布
                        classViewModel.stopRtcChat();
                        // 通知视频列表刷新
                        if (onVideoViewChangeListenr != null) {
                            onVideoViewChangeListenr.onChangeUser(userInfo);
                        }
                    }
                }
            }
        });

        classViewModel.getRoleChangeUser().observe(this, new Observer<ClassMember>() {
            @Override
            public void onChanged(ClassMember member) {
                SLog.d(TAG, "RoleChangeUser , member ==" + member);
                // 白板和空布局
                // 是对视频中的用户信息的更新
                ClassMember data = shareVideoView.getData();

                ScreenDisplay screenDisplay = classViewModel.getDisplay().getValue();
                if (data != null && screenDisplay != null) {
                    if (!reloadWhiteBoard()) {
                        if (screenDisplay.getType() == null || screenDisplay.getType() == ScreenDisplay.Display.NONE) {
                            displayNull.setContent(classViewModel.getUserInfo().getValue());
                        } else {
                            if (data.getUserId().equals(member.getUserId())) { // 刷新
                                // 如果现在的角色是助教或者讲师， 则需要对视频的信息更新
                                if (member.getRole() == Role.LECTURER || member.getRole() == Role.ASSISTANT) {
                                    shareVideoView.setData(member);
                                } else {
                                    // 如果其他的用户， 则移除
                                    shareVideoView.removeVideoView();
                                    // 通知视频列表刷新
                                    if (onVideoViewChangeListenr != null) {
                                        onVideoViewChangeListenr.onChangeUser(member);
                                    }
                                }
                            }
                        }
                    }
                }


            }
        });

        // 共享区显示资源的变化
        classViewModel.getDisplay().observe(this, new Observer<ScreenDisplay>() {
            @Override
            public void onChanged(ScreenDisplay screenDisplay) {
                SLog.d(TAG, "Display , screenDisplay ==" + screenDisplay);
                if (shareScreenVideoView.getVisibility() == View.VISIBLE || isFullScreen) {
                    RadioRtcVideoView videoView = VideoViewManager.getInstance().getShareScreenVideoView();
                    if (videoView != null) {
                        videoView.setBindVideo(false);
                        if (isFullScreen) {
                            videoView.setZOrderOnTop(false);
                            videoView.setZOrderMediaOverlay(false);
                        }
                    }
                    shareScreenVideoView.clear();
                }

                boolean isVideoViewChange = shareVideoView.getVisibility() == View.VISIBLE || shareVideoView.getVideoViewCount() > 0;
                List<String> userIds = new ArrayList<>();

                // 前一个显示是视频， 所以当前显示资源的时候要把上一个视频资源记录， 并通知
                // 视频列表刷新
                if (isVideoViewChange) {
                    ClassMember data = shareVideoView.getData();
                    if (data != null) {
                        userIds.add(data.getUserId());
                    }
                }

                ScreenDisplay.Display type = screenDisplay.getType();
                if (type == ScreenDisplay.Display.WHITEBOARD) {
                    if (isVideoViewChange) {
                        // 前一次假如是视频共享的话， 则需要移除 VideoView
                        shareVideoView.removeVideoView();
                        shareVideoView.setData(null);
                    }
                    shareVideoView.setVisibility(View.GONE);
                    mWebView.setVisibility(View.VISIBLE);
                    mWebView.requestFocus();
                    mWebViewContianer.setVisibility(View.VISIBLE);
                    whiteBoardAction.setVisibility(View.VISIBLE);
                    whiteBoardPagesPrevious.setVisibility(View.VISIBLE);
                    whiteBoardPagesNext.setVisibility(View.VISIBLE);
                    displayNull.setVisibility(View.GONE);
                    shareScreenVideoView.setVisibility(View.GONE);
                    mWebViewLoading.setVisibility(View.VISIBLE);
                    joinWhiteBoardRoom(screenDisplay);
                } else if (type == ScreenDisplay.Display.LECTURER || type == ScreenDisplay.Display.ASSISTANT) {
                    //根据userId，切换到对应的视频
                    shareVideoView.setVisibility(View.VISIBLE);
                    mWebView.setVisibility(View.GONE);
                    mWebViewContianer.setVisibility(View.GONE);
                    whiteBoardAction.setVisibility(View.GONE);
                    mWebViewLoading.setVisibility(View.GONE);
                    hideInputKeyboard();
                    roomPhase = null;
                    whiteBoardPagesPrevious.setVisibility(View.GONE);
                    whiteBoardPagesNext.setVisibility(View.GONE);
                    displayNull.setVisibility(View.GONE);
                    shareScreenVideoView.setVisibility(View.GONE);

                    String userId = screenDisplay.getUserId();

                    if (!VideoViewManager.getInstance().containsKey(userId)) {
                        VideoViewManager.getInstance().put(userId, new RadioRtcVideoView(getContext()));
                    }

                    RadioRtcVideoView videoView = VideoViewManager.getInstance().get(userId);
                    if (videoView.isHasParent()) {
                        ((ViewGroup) videoView.getParent()).removeAllViews();
                    }

                    shareVideoView.addVideoView(videoView, getItemLayoutParams());
                    shareVideoView.setData(screenDisplay.getClassMember());
                    // 设置 VideoView 的图层
                    if (isFullScreen) {
                        videoView.setZOrderOnTop(true);
                        videoView.setZOrderMediaOverlay(true);
                    } else {
                        videoView.setZOrderOnTop(false);
                        videoView.setZOrderMediaOverlay(false);
                    }

                } else if (type == ScreenDisplay.Display.SCREEN) { // 视频共享
                    if (isVideoViewChange) { // 前一次假如是视频共享的话， 则需要移除 VideoView
                        shareVideoView.removeVideoView();
                        shareVideoView.setData(null);
                    }

                    shareVideoView.setVisibility(View.GONE);
                    mWebView.setVisibility(View.GONE);
                    mWebViewContianer.setVisibility(View.GONE);
                    mWebViewLoading.setVisibility(View.GONE);
                    hideInputKeyboard();
                    roomPhase = null;
                    whiteBoardAction.setVisibility(View.GONE);
                    whiteBoardPagesPrevious.setVisibility(View.GONE);
                    whiteBoardPagesNext.setVisibility(View.GONE);
                    displayNull.setVisibility(View.GONE);
                    shareScreenVideoView.setData(screenDisplay.getClassMember());
                    shareScreenVideoView.setVisibility(View.VISIBLE);

                    // 获取共享桌面的 VideoView， 并设置
                    RadioRtcVideoView shareScreenRtcVideoView = VideoViewManager.getInstance().getShareScreenVideoView();
                    if (shareScreenRtcVideoView == null) {
                        shareScreenRtcVideoView = new RadioRtcVideoView(getActivity());
                        final RadioRtcVideoView finalShareScreenVideoView = shareScreenRtcVideoView;
                        shareScreenRtcVideoView.setOnSizeChangedListener(new RCRTCVideoView.OnSizeChangedListener() {
                            @Override
                            public void onChanged(RCRTCVideoView.Size size) {
                                if (size != null) {
                                    float radio = (float) size.with / (float) size.height;
                                    SLog.d("video_size", "size == " + size.with + ", " + size.height + ", " + radio);
                                    finalShareScreenVideoView.setRadio(radio);
                                }
                            }
                        });
                        VideoViewManager.getInstance().setShareScreenVideoView(shareScreenRtcVideoView);
                    }

                    if (shareScreenRtcVideoView.isHasParent()) {
                        ((ViewGroup) shareScreenRtcVideoView.getParent()).removeAllViews();
                    }
                    shareScreenVideoView.addVideoView(shareScreenRtcVideoView, getItemLayoutParams());

                    // 设置浮层
                    if (isFullScreen) {
                        shareScreenRtcVideoView.setZOrderOnTop(true);
                        shareScreenRtcVideoView.setZOrderMediaOverlay(true);
                    } else {
                        shareScreenRtcVideoView.setZOrderOnTop(false);
                        shareScreenRtcVideoView.setZOrderMediaOverlay(false);
                    }

                } else if (type == null || type == ScreenDisplay.Display.NONE) {
                    // 空资源
                    shareVideoView.setData(null);
                    shareVideoView.setVisibility(View.GONE);
                    shareVideoView.removeVideoView();
                    shareScreenVideoView.setData(null);
                    shareScreenVideoView.setVisibility(View.GONE);
                    hideInputKeyboard();
                    roomPhase = null;
                    mWebViewLoading.setVisibility(View.GONE);
                    mWebView.setVisibility(View.GONE);
                    mWebViewContianer.setVisibility(View.GONE);
                    whiteBoardAction.setVisibility(View.GONE);
                    whiteBoardPagesPrevious.setVisibility(View.GONE);
                    whiteBoardPagesNext.setVisibility(View.GONE);
                    displayNull.setVisibility(View.VISIBLE);
                    displayNull.setContent(classViewModel.getUserInfo().getValue());
                }

                //通知刷新视频列表的
                if (onVideoViewChangeListenr != null) {
                    userIds.add(screenDisplay.getUserId());
                    onVideoViewChangeListenr.onChangeUser(userIds);
                }
            }
        });
        classViewModel.getConnectionStatusLiveData().observe(this,
            new Observer<ConnectionStatus>() {
                @Override
                public void onChanged(ConnectionStatus connectionStatus) {
                    if (connectionStatus != null && connectionStatus
                        .equals(ConnectionStatus.CONNECTED) &&
                        roomPhase != null && roomPhase.equals(RoomPhase.disconnected)) {
                        joinWhiteBoardRoom(classViewModel.getDisplay().getValue());
                    }
                }
            });

    }

    // 重写更具Role 加载
    private boolean reloadWhiteBoard() {
        ScreenDisplay screenDisplay = classViewModel.getDisplay().getValue();
        if (screenDisplay != null) {
            ScreenDisplay.Display type = screenDisplay.getType();
            if (type == ScreenDisplay.Display.WHITEBOARD) {
                changeWhiteBoardRole(classViewModel.getUserInfo().getValue().getRole(), screenDisplay.getWhiteBoardUri());
                return true;
            }
        }
        return false;
    }


    // 加载白板
    private void loadWhiteBoard(ScreenDisplay display) {
        mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        mWebView.clearView();
        String roomId = classViewModel.getRoomId().getValue();
        int role = Role.LISTENER.getValue();
        UserInfo userInfo = classViewModel.getUserInfo().getValue();
        if (userInfo != null && userInfo.getRole() != null) {
            role = userInfo.getRole().getValue();
        }
        String currentAuth = HttpClientManager.getInstance(getContext()).getCurrentAuth();

        // 加入角色，课堂id，登录认证信息以显示对应角色和课堂的白板
        String showWhiteUlr = display.getWhiteBoardUri()
                + "&role=" + role + "&roomId=" + roomId + "&authorization=" + currentAuth;
        mWebView.loadUrl(showWhiteUlr);
    }

    private void joinWhiteBoardRoom(ScreenDisplay display) {
        if (display == null) {
            return;
        }
        uuid = display.getWhiteboardId();
        String roomToken = display.getWhiteboardRoomToken();
        if (TextUtils.isEmpty(uuid) || TextUtils.isEmpty(roomToken)) {
            SLog.d(TAG, "here white uuid or roomToken is null, got error ");
            return;
        }
        SLog.d(TAG, "here white uuid = " + uuid + " , roomToken = " + roomToken);
        WhiteSdk whiteSdk = new WhiteSdk(
                mWebView,
                getContext(),
                new WhiteSdkConfiguration(com.herewhite.sdk.domain.DeviceType.touch, 10, 0.1)
                // 如果需要拦截并重写 URL ,请实现如下方法, 但请注意不要每次都生成不同的 URL,尽量 cache 结果并请在必要的时候进行更新(如 更新私有 Token)
                , new UrlInterrupter() {
            @Override
            public String urlInterrupter(String s) {
                // 修改资源 URL
                return s;
            }
        }
        );
        whiteSdk.joinRoom(new RoomParams(uuid, roomToken), new AbstractRoomCallbacks() {
            @Override
            public void onPhaseChanged(RoomPhase phase) {
                SLog.d(TAG, "here white AbstractRoomCallbacks onPhaseChanged phase = " + phase);
                roomPhase = phase;
            }

            @Override
            public void onBeingAbleToCommitChange(boolean isAbleToCommit) {
                SLog.d(TAG, "here white AbstractRoomCallbacks onPhaseChanged");
            }

            @Override
            public void onRoomStateChanged(final RoomState modifyState) {
                SLog.d(TAG, "here white AbstractRoomCallbacks onRoomStateChanged");
                if (modifyState != null && modifyState.getSceneState() != null) {
                    updateSceneState(modifyState.getSceneState());
                }
                if (modifyState != null && modifyState.getZoomScale() != null) {
                    SLog.d(TAG, "here white AbstractRoomCallbacks zoom scale changed = " + modifyState.getZoomScale());
                }
            }

            @Override
            public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
                SLog.d(TAG, "here white AbstractRoomCallbacks onCatchErrorWhenAppendFrame userId = "+userId+" ，error = "+error.getMessage());
            }


            @Override
            public void onDisconnectWithError(Exception e) {
                SLog.d(TAG, "here white joinRoom onDisconnectWithError = "+e.getMessage());
            }

            @Override
            public void onKickedWithReason(String reason) {
                SLog.d(TAG, "here white joinRoom onKickedWithReason reason = " + reason);
            }
        }, new Promise<Room>() {
            @Override
            public void then(final Room room) {
                SLog.d(TAG, "here white joinRoom Promise  Room");
                room.zoomChange(0.3);
               /* if (isObserver) {
                    room.disableOperations(true);
                }*/
                //设置跟随老师视角模式
                room.setViewMode(ViewMode.Follower);
                // 禁止用户主动改变视野
                room.disableCameraTransform(true);
                mWebViewLoading.setVisibility(View.GONE);
                whiteBoardRoom = room;
                initPencilColor();
                bindWhiteBoardActions(room);
                room.getSceneState(new Promise<SceneState>() {
                    @Override
                    public void then(SceneState sceneState) {
                        SLog.d(TAG, "here white joinRoom and then getSceneState");
                        if (sceneState != null) {
                            if (sceneState.getScenePath().equals(WhiteBoardConsant.WHITE_BOARD_INIT_SCENE_PATH)) {
                                currentSceneName = generateSceneName();
                                whiteBoardScenes = new Scene[]{
                                        new Scene(currentSceneName, new PptPage("", 0d, 0d))
                                };
                                room.putScenes(WhiteBoardConsant.WHITE_BOARD_SCENE_PATH, whiteBoardScenes, 0);
                                room.setScenePath(WhiteBoardConsant.WHITE_BOARD_SCENE_PATH + "/" + currentSceneName);
                                mWebView.setVisibility(View.VISIBLE);
                                currentSceneIndex = 0;
                            } else {
                                updateSceneState(sceneState);
                                room.setScenePath(WhiteBoardConsant.WHITE_BOARD_SCENE_PATH + "/" + currentSceneName);
                            }
                        }
                    }

                    @Override
                    public void catchEx(SDKError sdkError) {
                        SLog.d(TAG, "here white joinRoom and then getSceneState error = " + sdkError.getMessage());
                    }
                });
            }

            @Override
            public void catchEx(SDKError t) {
                SLog.d(TAG, "here white joinRoom Promise  catchEx = " + t.toString());
                showToast(R.string.white_board_service_error);
            }
        });
    }

    private void updateSceneState(SceneState sceneState) {
        currentSceneIndex = sceneState.getIndex();
        whiteBoardScenes = sceneState.getScenes();
        String scenePath = sceneState.getScenePath();
        currentSceneName = scenePath.substring(scenePath.lastIndexOf("/") + 1);
        updateWhiteBoardPagesView();
    }

    private void bindWhiteBoardActions(final Room room) {
        //画笔颜色
        whiteBoardAction.findViewById(R.id.white_action_pencil).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePencilColorPopupWindow();
                pencilColorPopupWindow = new PencilColorPopupWindow(getContext(),room);
                pencilColorPopupWindow.show(v);
                hideInputKeyboard();

            }
        });
        //文字输入
        whiteBoardAction.findViewById(R.id.white_action_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemberState memberState = new MemberState();
                memberState.setCurrentApplianceName(Appliance.TEXT);
                room.setMemberState(memberState);
            }
        });
        //橡皮擦
        whiteBoardAction.findViewById(R.id.white_action_eraser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemberState memberState = new MemberState();
                memberState.setCurrentApplianceName(Appliance.ERASER);
                room.setMemberState(memberState);
                hideInputKeyboard();
            }
        });
        //清空当前页
        whiteBoardAction.findViewById(R.id.white_action_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (room != null) {
                    room.cleanScene(true);
                }
                hideInputKeyboard();
            }
        });
        //新增白板页
        whiteBoardAction.findViewById(R.id.white_action_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sceneName = generateSceneName();
                room.putScenes(WhiteBoardConsant.WHITE_BOARD_SCENE_PATH, new Scene[]{
                        new Scene(sceneName, new PptPage("", 0d, 0d)),
                }, Integer.MAX_VALUE);
                room.setScenePath(WhiteBoardConsant.WHITE_BOARD_SCENE_PATH + "/" + sceneName);
                currentSceneName = sceneName;
                hideInputKeyboard();
            }
        });
        //删除当前白板页
        whiteBoardAction.findViewById(R.id.white_action_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (whiteBoardScenes.length == 1) {
                    room.cleanScene(true);
                } else {
                    room.removeScenes(WhiteBoardConsant.WHITE_BOARD_SCENE_PATH + "/" + currentSceneName);
                }
                hideInputKeyboard();
            }
        });
        //上一页
        whiteBoardPagesPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSceneIndex > 0) {
                    String sceneName = whiteBoardScenes[--currentSceneIndex].getName();
                    room.setScenePath(WhiteBoardConsant.WHITE_BOARD_SCENE_PATH + "/" + sceneName);
                }
                updateWhiteBoardPagesView();
                hideInputKeyboard();
            }
        });
        //下一页
        whiteBoardPagesNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSceneIndex < whiteBoardScenes.length - 1) {
                    String sceneName = whiteBoardScenes[++currentSceneIndex].getName();
                    room.setScenePath(WhiteBoardConsant.WHITE_BOARD_SCENE_PATH + "/" + sceneName);
                }
                updateWhiteBoardPagesView();
                hideInputKeyboard();
            }
        });
    }

    private void initPencilColor() {
        int color = getResources().getColor(R.color.white_board_pencil_color_red);
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        MemberState memberState = new MemberState();
        memberState.setStrokeColor(new int[]{red, green, blue});
        memberState.setCurrentApplianceName(Appliance.PENCIL);
        whiteBoardRoom.setMemberState(memberState);
    }

    private void updateWhiteBoardPagesView() {
        if (currentSceneIndex == 0) {
            whiteBoardPagesPrevious.setEnabled(false);
        } else {
            whiteBoardPagesPrevious.setEnabled(true);
        }
        if (currentSceneIndex == whiteBoardScenes.length - 1) {
            whiteBoardPagesNext.setEnabled(false);
        } else {
            whiteBoardPagesNext.setEnabled(true);
        }
    }

    private String generateSceneName() {
        String userId = RongIMClient.getInstance().getCurrentUserId();
        return shortMD5(userId, System.currentTimeMillis() + "");
    }

    private void hidePencilColorPopupWindow(){
        if (pencilColorPopupWindow != null) {
            pencilColorPopupWindow.dismiss();
        }
    }

    private String shortMD5(String... args) {
        try {
            StringBuilder builder = new StringBuilder();

            for (String arg : args) {
                builder.append(arg);
            }

            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(builder.toString().getBytes());
            byte[] mds = mdInst.digest();
            mds = Base64.encode(mds, Base64.NO_WRAP);
            String result = new String(mds);
            result = result.replace("=", "").replace("+", "-").replace("/", "_").replace("\n", "");
            return result;
        } catch (Exception e) {
            SLog.e(TAG, "shortMD5", e);
        }
        return "";
    }

    /*------------------------------------白板，集成第三方 here white 服务 end --------------------------*/


    /**
     * 更改当前白板的角色
     *
     * @param role
     * @param whiteBoardId
     */
    private void changeWhiteBoardRole(Role role, String whiteBoardId) {
        mWebView.evaluateJavascript("changeRole('" + role.getValue() + "','" + whiteBoardId + "')", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                SLog.d(SLog.TAG_DATA, "WhiteBoard change Role return:" + value);
            }
        });
    }

    // 初始化设置 WebView
    private void initWebView() {
        WebSettings settings = mWebView.getSettings();
        //支持缩放
//        settings.setSupportZoom(true);
        //设置出现缩放工具
//        settings.setBuiltInZoomControls(true);
        //扩大比例的缩放
        settings.setUseWideViewPort(true);
        //js交互
        settings.setJavaScriptEnabled(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        mWebView.setScrollContainer(false);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                SLog.d("web_view", "onReceivedError ==" + request.getUrl() + ", error = " + error.getErrorCode());
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                SLog.d("web_view", "onReceivedError ==" + failingUrl + ", error = " + errorCode);

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                SLog.d("web_view", "onPageStarted ==" + url);
                mWebViewLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                SLog.d("web_view", "onPageFinished ==" + url);
                mWebViewLoading.setVisibility(View.GONE);

            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // 白板上传文件
                webUploadCallback = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(
                        Intent.createChooser(i, getString(R.string.class_white_board_select_upload_file)),
                        REQUEST_WHITE_BOARD_UPLOAD_FILE);
                return true;
            }
        });
    }

    private LinearLayout.LayoutParams getItemLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        return params;
    }

    /**
     * 设置全屏按钮监听
     * @param onClickFullScreenListener
     */
    public void setOnClickFullScreenListener(OnClickToFullScreenListener onClickFullScreenListener) {
        this.onClickFullScreenListener = onClickFullScreenListener;
    }

    /**
     * 设置显示布局监听
     * @param listenr
     */
    public void setOnVideoViewChangeListenr(OnVideoViewChangeListenr listenr) {
        this.onVideoViewChangeListenr = listenr;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 白板上传文件，选择文件后回调
        if (requestCode == REQUEST_WHITE_BOARD_UPLOAD_FILE && webUploadCallback != null) {
            if (data != null) {
                Uri[] uris = new Uri[]{data.getData()};
                webUploadCallback.onReceiveValue(uris);
                webUploadCallback = null;
            } else {
                webUploadCallback.onReceiveValue(null);
                webUploadCallback = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 设置全屏状态
     *
     * @param isFullScreen
     */
    public void checkFullScreen(boolean isFullScreen) {
        fullScreenCb.setChecked(isFullScreen);
    }

    /**
     * 全屏按钮监听接口
     */
    public interface OnClickToFullScreenListener {
        void onClickToFullScreen(boolean isToFullScreen);
    }

    /**
     * 显示变化，通知刷新的监听接口
     */
    public interface OnVideoViewChangeListenr {
        void onChangeUser(List<String> userIds);

        void onChangeUser(ClassMember oldUser);
    }

    private void hideInputKeyboard() {
        View currentFocus = getActivity().getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }
}
