package cn.rongcloud.sealclass.viewmodel;

import android.app.Application;
import android.os.CountDownTimer;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.sealclass.common.ResultCallback;
import cn.rongcloud.sealclass.common.StateLiveData;
import cn.rongcloud.sealclass.im.IMManager;
import cn.rongcloud.sealclass.model.ApplyForSpeechRequest;
import cn.rongcloud.sealclass.model.ChangedUser;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.ClassMemberChangedAction;
import cn.rongcloud.sealclass.model.DeviceChange;
import cn.rongcloud.sealclass.model.DeviceControlInvite;
import cn.rongcloud.sealclass.model.DeviceType;
import cn.rongcloud.sealclass.model.FirstFrameUserInfo;
import cn.rongcloud.sealclass.model.LoginResult;
import cn.rongcloud.sealclass.model.RequestState;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.model.RoleChangedUser;
import cn.rongcloud.sealclass.model.ScreenDisplay;
import cn.rongcloud.sealclass.model.SpeechResult;
import cn.rongcloud.sealclass.model.StreamResource;
import cn.rongcloud.sealclass.model.TicketExpired;
import cn.rongcloud.sealclass.model.UpgradeRoleInvite;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.model.VideoClassMemberData;
import cn.rongcloud.sealclass.model.WhiteBoard;
import cn.rongcloud.sealclass.model.WhiteBoardAction;
import cn.rongcloud.sealclass.repository.ClassRepository;
import cn.rongcloud.sealclass.repository.OnClassEventListener;
import cn.rongcloud.sealclass.repository.OnClassVideoEventListener;
import cn.rongcloud.sealclass.rtc.RtcManager;
import cn.rongcloud.sealclass.ui.view.RadioRtcVideoView;
import cn.rongcloud.sealclass.utils.log.SLog;

/**
 * 课堂视图模型
 */
public class ClassViewModel extends ViewModel {
    private static final String TAG = "ClassViewModel";

    private ClassRepository classRepository;

    private SimpleDateFormat passTimeDateFormat = new SimpleDateFormat("HH:mm:ss");

    private MutableLiveData<UserInfo> userInfo = new MutableLiveData<>();   // 当前用户自己的信息
    private MutableLiveData<DeviceType> onDisableDevice = new MutableLiveData<>();  // 当被关闭设备
    private MutableLiveData<String> roomId = new MutableLiveData<>();       // 课堂 id
    private MutableLiveData<List<ClassMember>> memberList = new MutableLiveData<>();    // 课堂成员 列表
    private MutableLiveData<List<WhiteBoard>> whiteBoardList = new MutableLiveData<>(); // 白板 列表
    private MutableLiveData<ScreenDisplay> display = new MutableLiveData<>();      // 屏幕显示
    private MutableLiveData<ApplyForSpeechRequest> applyForSpeechRequest = new MutableLiveData<>();   // 旁听者申请成为成员
    private MutableLiveData<DeviceControlInvite> deviceControlInvite = new MutableLiveData<>();     // 申请开启摄像头和麦克风
    private MutableLiveData<SpeechResult> speechResult = new MutableLiveData<>();       // 旁听者申请成员结果
    private MutableLiveData<UpgradeRoleInvite> upgradeRoleInvite = new MutableLiveData<>();         // 升级角色邀请
    private MutableLiveData<TicketExpired> ticketExpired = new MutableLiveData();           // 请求过期
    private MutableLiveData<List<String>> userListUserIds = new MutableLiveData<>();         // 学员视频列表
    private MutableLiveData<ClassMember> assistantRole = new MutableLiveData<>();         // 助教角色
    private MutableLiveData<ClassMember> lecturerRole = new MutableLiveData<>();         // 讲师角色
    private MutableLiveData<List<StreamResource>> initUserVideoList = new MutableLiveData<>();         // 加入房间时的视频列表
    private MutableLiveData<StreamResource> addVideoUser = new MutableLiveData<>();         // 有新人发布视频
    private MutableLiveData<StreamResource> removeVideoUser = new MutableLiveData<>();      // 退出视频发布
    private MutableLiveData<ClassMember> kickedoff = new MutableLiveData<>();    // 退出视频发布
    private MutableLiveData<Integer> unReadMessage = new MutableLiveData<>();   // 未读消息
    private MutableLiveData<ClassMember> roleChangeUser = new MutableLiveData<>();   // 默认身份改变
    private MutableLiveData<Boolean> localUserStartVideoChat = new MutableLiveData<>();   //开始音视频
    private MutableLiveData<DeviceChange> deviceChange = new MutableLiveData<>();   //开始音视频
    private MutableLiveData<Boolean> respondApplySpeechTimeout = new MutableLiveData<>();   // 相应申请发言超时
    private MutableLiveData<FirstFrameUserInfo> fisrtFrameDraw = new MutableLiveData<>();   // 相应申请发言超时

    private MutableLiveData<Calendar> passedTime = new MutableLiveData<>();     // 经过的时间
    private Timer countPassTimeTimer = new Timer();
    private CountDownTimer applySpeechRequestTimer = new CountDownTimer(30 * 1000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            respondApplySpeechTimeout.postValue(true);
        }
    };

    /**
     * 列表人员有没有变化(主要是 id 以及顺序)
     */
    private volatile boolean isUserListNoUpdate = false;


    /**
     * 此构造方法不应该被使用
     * 声明此构造方法只为在已经通过{@link ClassViewModel.Factory}生成ViewModel后，
     * 让在生成此 ClassViewModel 的同一生命周期内的其他Fragment 可以不需要参数获取对象时使用
     */
    public ClassViewModel() {
        SLog.e(TAG, "ClassViewModel should be created by Factory with login info.");
    }

    public ClassViewModel(LoginResult result, Application application) {
        roomId.setValue(result.getRoomId());
        userInfo.setValue(result.getUserInfo());
        memberList.setValue(sortClassMember(result.getMembers()));
        whiteBoardList.setValue(result.getWhiteboards());
        ScreenDisplay screenDisplay = ScreenDisplay.createScreenDisplay(result.getDisplay());
        display.setValue(getDisplayWithClassMember(screenDisplay));

        classRepository = new ClassRepository(application.getApplicationContext());
        // 注册课堂事件监听
        classRepository.addOnClassEventListener(result.getRoomId(), onClassEventListener);
//        remoteUserPublicResourceIds.postValue(new ArrayList<String>());
        classRepository.setOnClassVideoEventListener(onClassVideoEventListener);

        // 从成员中筛选出特定的角色用于单独对特殊角色的刷新
        findAndUpdateSpecialRole();

        // 记时进入课堂的时间
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        passedTime.setValue(calendar);
        TimerTask countTimeTask = new TimerTask() {
            @Override
            public void run() {
                Calendar time = passedTime.getValue();
                time.add(Calendar.SECOND, 1);
                passedTime.postValue(time);
            }
        };
        countPassTimeTimer.schedule(countTimeTask, 1000, 1000);
    }

    /**
     * 获取当前用户信息
     *
     * @return
     */
    public LiveData<UserInfo> getUserInfo() {
        return userInfo;
    }

    /**
     * 获取当自己设备被停用状态
     *
     * @return
     */
    public LiveData<DeviceType> geOnDisableDevice(){
        return onDisableDevice;
    }

    public LiveData<String> getRoomId() {
        return roomId;
    }

    /**
     * 获取成员列表
     *
     * @return
     */
    public LiveData<List<ClassMember>> getMemberList() {
        return memberList;
    }

    /**
     * 获取画布列表
     *
     * @return
     */
    public LiveData<List<WhiteBoard>> getWhiteBoardList() {
        return whiteBoardList;
    }

    /**
     * 获取共享画布显示内容
     *
     * @return
     */
    public LiveData<ScreenDisplay> getDisplay() {
        return display;
    }

    /**
     * 获取申请发言邀请
     *
     * @return
     */
    public LiveData<ApplyForSpeechRequest> getApplyForSpeechRequest() {
        return applyForSpeechRequest;
    }

    /**
     * 获取被邀请开启设备（摄像头、麦克风）状态和邀请后对方的响应状态
     *
     * @return
     */
    public LiveData<DeviceControlInvite> getDeviceControlInvite() {
        return deviceControlInvite;
    }

    /**
     * 获取申请发言结果
     *
     * @return
     */
    public LiveData<SpeechResult> getSpeechResult() {
        return speechResult;
    }

    /**
     * 获取非旁听用户的视频资源
     *
     * @return
     */
    public LiveData<List<String>> getVideoListUserIds() {
        return userListUserIds;
    }

    /**
     * 获取助教角色
     *
     * @return
     */
    public LiveData<ClassMember> getAssistant() {
        return assistantRole;
    }

    /**
     * 获取讲师角色
     *
     * @return
     */
    public LiveData<ClassMember> getLecturer() {
        return lecturerRole;
    }

    /**
     * 获取申请提升角色邀请
     *
     * @return
     */
    public LiveData<UpgradeRoleInvite> getUpgradeRoleInvite() {
        return upgradeRoleInvite;
    }

    /**
     * 获取申请过期反馈
     *
     * @return
     */
    public LiveData<TicketExpired> getTicketExpired() {
        return ticketExpired;
    }


    /**
     * 获取非旁听用户的视频资源
     *
     * @return
     */
    public LiveData<List<StreamResource>> getInitVideoList() {
        return initUserVideoList;
    }

    /**
     * 获取视频增加的用户
     *
     * @return
     */
    public LiveData<StreamResource> getVideoAddedUser() {
        return addVideoUser;
    }

    /**
     * 获取视频退出的用户
     *
     * @return
     */
    public LiveData<StreamResource> getVideoRemovedUser() {
        return removeVideoUser;
    }


    /**
     * 用户被踢
     * @return
     */
    public LiveData<ClassMember> getKickedOff() {
        return kickedoff;
    }


    /**
     * 角色变化
     * @return
     */
    public LiveData<ClassMember> getRoleChangeUser() {
        return roleChangeUser;
    }

    /**
     * 本地用户开始进行音视频
     * @return
     */
    public LiveData<Boolean> getLocalUserStartVideoChat() {
        return localUserStartVideoChat;
    }


    /**
     * 获取音视频成员列表
     *
     * @return
     */
    public LiveData<VideoClassMemberData> getVideoMembersList() {
        return  Transformations.map(memberList, new Function<List<ClassMember>, VideoClassMemberData>() {
            @Override
            public VideoClassMemberData apply(List<ClassMember> input) {
                if (input == null || input.size() <= 0) {
                    return null;
                }

                // 重写创建新的集合， 防止对原油顺序造成影响
                ArrayList<ClassMember> tmp = new ArrayList<>();
                tmp.addAll(input);

                Collections.sort(tmp, new Comparator<ClassMember>() {
                    @Override
                    public int compare(ClassMember o1, ClassMember o2) {
                        long l = o1.getJoinTime() - o2.getJoinTime();
                        int i = l > 0 ? 1: l < 0 ? -1 : 0;
                        return i;
                    }
                });

                UserInfo userInfoValue = userInfo.getValue();
                ArrayList<ClassMember> members = new ArrayList<>();
                boolean hasLecture = false;
                for (ClassMember member : tmp) {
                    if (member.getRole() != Role.LISTENER || (userInfoValue != null && userInfoValue.getUserId().equals(member.getUserId()))) {
                        if (member.getRole() == Role.LECTURER) {
                            hasLecture = true;
                            members.add(0, member);
                        } else {
                            members.add(member);
                        }
                    }
                }

                tmp.clear();

                if (!hasLecture) {
                    ClassMember member = new ClassMember();
                    member.setUserName("");
                    member.setRole(Role.LECTURER.getValue());
                    member.setUserId("-1");
                    members.add(0 , member);
                }


                VideoClassMemberData data = new VideoClassMemberData();
                data.setMember(members);
                data.setNoUpdate(isUserListNoUpdate);
                isUserListNoUpdate = false;
                return data;
            }
        });
    }


    /**
     * 获取讲师和助教列表
     *
     * @return
     */
    public LiveData<List<ClassMember>> getAssistantAndLecturerList() {
        return  Transformations.map(memberList, new Function<List<ClassMember>, List<ClassMember>>() {
            @Override
            public List<ClassMember> apply(List<ClassMember> input) {

                if (input == null || input.size() <= 0) {
                    return null;
                }

                ArrayList<ClassMember> members = new ArrayList<>();
                for (ClassMember member : input) {
                    if (member.getRole() == Role.ASSISTANT) {
                        members.add(0, member);
                    } else if (member.getRole() == Role.LECTURER) {
                        members.add( member);
                    }
                }
                return members;
            }
        });
    }



    // 设备状态变化
    public LiveData<DeviceChange> getDeviceChange() {
        return deviceChange;
    }

    public LiveData<Boolean> getRespondApplySpeechTimeout() {
        return respondApplySpeechTimeout;
    }

    //第一绘画通知
    public LiveData<FirstFrameUserInfo> getFisrtFrameDraw() {
        return fisrtFrameDraw;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private LoginResult loginResult;
        private Application application;

        public Factory(LoginResult result, Application application) {
            this.loginResult = result;
            this.application = application;

        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(LoginResult.class, Application.class).newInstance(loginResult, application);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }

    /**
     * 退出房间
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> leaveRoom(String roomId) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.leave(roomId, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }


    /**
     * 踢人操作
     *
     * @param roomId
     * @param userId
     * @return
     */
    public LiveData<RequestState> kickOff(String roomId, String userId) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.kickOff(roomId, userId, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }


    /**
     * 控制摄像头
     *
     * @param roomId
     * @param userId
     * @param cameraOn
     * @return
     */
    public LiveData<RequestState> controlCamera(String roomId, String userId, boolean cameraOn) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.controlCamera(roomId, userId, cameraOn, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 控制语音设备
     *
     * @param roomId
     * @param userId
     * @param microphone
     * @return
     */
    public LiveData<RequestState> controlMicrophone(String roomId, String userId, boolean microphone) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.controlMicrophone(roomId, userId, microphone, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 同意开启设备
     *
     * @param roomId
     * @param ticket
     * @return
     */
    public LiveData<RequestState> deviceApprove(String roomId, String ticket) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.deviceApprove(roomId, ticket, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 拒绝开启设备
     *
     * @param roomId
     * @param ticket
     * @return
     */
    public LiveData<RequestState> deviceReject(String roomId, String ticket) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.deviceReject(roomId, ticket, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 同步摄像头状态
     *
     * @param roomId
     * @param cameraOn
     * @return
     */
    public LiveData<RequestState> deviceSyncCamera (String roomId, boolean cameraOn) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.deviceSyncCamera(roomId, cameraOn, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }


    /**
     * 同步语音状态
     *
     * @param roomId
     * @param microphoneOn
     * @return
     */
    public LiveData<RequestState> deviceSyncMic (String roomId, boolean microphoneOn) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.deviceSyncMic(roomId, microphoneOn, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }



    /**
     * 批量降级
     *
     * @param roomId
     * @param members
     * @return
     */
    public LiveData<RequestState> downgrade(String roomId, List<ClassMember> members) {
        List<ChangedUser> users = new ArrayList<>();
        for (ClassMember member : members) {
            ChangedUser user = new ChangedUser();
            user.role = Role.LISTENER.getValue(); // 降级到旁听
            user.userId = member.getUserId();
            users.add(user);
        }

        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.downgrade(roomId, users, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 请求发言
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> applySpeech(String roomId) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();

        // 申请中
        UserInfo userInfoValue = userInfo.getValue();
        userInfoValue.setApplySpeeching(true);
        userInfo.postValue(userInfoValue);

        classRepository.applySpeech(roomId, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();

            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
                // 申请失败
                UserInfo userInfoValue = userInfo.getValue();
                userInfoValue.setApplySpeeching(false);
                userInfo.postValue(userInfoValue);
            }
        });

        return stateLiveData;
    }


    /**
     * 同意发言
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> approveSpeech(String roomId, String ticket) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        applySpeechRequestTimer.cancel();
        classRepository.approveSpeech(roomId, ticket, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 拒绝发言
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> rejectSpeech(String roomId, String ticket) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        applySpeechRequestTimer.cancel();
        classRepository.rejectSpeech(roomId, ticket, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }


    /**
     * 转移角色
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> transferRole(String roomId, String userId) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.transferRole(roomId, userId, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }


    /**
     * 邀请升级
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> upgradeIntive(String roomId, String userId, int role) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.upgradeIntive(roomId, userId, role, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 接受升级
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> upgradeApprove(String roomId, String ticket) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.upgradeApprove(roomId, ticket, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 拒绝升级
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> upgradeReject(String roomId, String ticket) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.upgradeReject(roomId, ticket, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 升级为讲师
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> changeRole(String roomId, String userId, int role) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.changeRole(roomId, userId, role, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 创建白板
     *
     * @param roomId
     * @return
     */
    public LiveData<RequestState> createWhiteBoard(String roomId) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.createWhiteBoard(roomId, new ResultCallback<String>() {

            @Override
            public void onSuccess(String whiteBoardId) {
                stateLiveData.success();

                updateWhiteBoardList();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 删除白板
     *
     * @param roomId
     * @param whiteBoardId
     * @return
     */
    public LiveData<RequestState> deleteWhiteBoard(String roomId, String whiteBoardId) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.deleteWhiteBoard(roomId, whiteBoardId, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });
        return stateLiveData;
    }

    /**
     * 获取白板列表
     *
     * @return
     */
    public LiveData<RequestState> updateWhiteBoardList() {
        String roomId = getRoomId().getValue();

        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.getWhiteBoardList(roomId, new ResultCallback<List<WhiteBoard>>() {
            @Override
            public void onSuccess(List<WhiteBoard> whiteBoards) {
                // 目前服务器是以创建顺序排列，需要转为后创建先显示
                List<WhiteBoard> reverseList = new ArrayList<>();
                if(whiteBoards != null){
                    for(WhiteBoard wb : whiteBoards){
                        reverseList.add(0, wb);
                    }
                }
                whiteBoardList.postValue(reverseList);
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 切换共享画布区显示内容
     *
     * @param roomId
     * @param type   显示类型，参考{@link ScreenDisplay.Display}
     * @param id     当显示用户时传入用户id，当为白板时传入白板id
     * @return
     */
    public LiveData<RequestState> switchDisplay(String roomId, final ScreenDisplay.Display type, String id) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        final String userId;
        final String whiteBoardId;
        switch (type) {
            case LECTURER:
            case ASSISTANT:
            case SCREEN:
                userId = id;
                whiteBoardId = null;
                break;
            case WHITEBOARD:
                userId = null;
                whiteBoardId = id;
                break;
            default:
                userId = null;
                whiteBoardId = null;
        }

        classRepository.switchDisplay(roomId, type.getType(), userId, whiteBoardId, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                stateLiveData.success();
                ScreenDisplay toDisplay = new ScreenDisplay();
                toDisplay.setType(type);
                toDisplay.setUserId(userId);
                toDisplay.setWhiteBoardUri(whiteBoardId);
                ScreenDisplay displayWithClassMember = getDisplayWithClassMember(toDisplay);
                display.postValue(displayWithClassMember);
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    /**
     * 获取未读消息
     *
     * @return
     */
    public LiveData<Integer> getUnReadMessage() {
        return unReadMessage;
    }

    /**
     * 获取已经过的时间
     *
     * @return
     */
    public LiveData<String> getPassedTime() {
        return Transformations.map(passedTime, new Function<Calendar, String>() {
            @Override
            public String apply(Calendar calendar) {
                return passTimeDateFormat.format(calendar.getTime());
            }
        });
    }


    /**
     * 课堂事件监听
     * 通过注册监听，更新当前房间内的信息
     */
    private OnClassEventListener onClassEventListener = new OnClassEventListener() {
        @Override
        public void onMemberChanged(ClassMemberChangedAction action, ClassMember classMember) {
            switch (action) {
                case JOIN:
                    addClassMember(classMember);
                    tryToUpdateSpecialRole(classMember);
                    break;
                case LEAVE:
                    removeClassMember(classMember);
                    break;
                case KICK:
                    kickedoff.postValue(classMember);
                    removeClassMember(classMember);

                    break;
            }
        }

        @Override
        public void onWhiteBoardChanged(WhiteBoardAction action, WhiteBoard whiteBoard) {
            // 当白板发生改版时重新获取白板列表
            updateWhiteBoardList();

            // 暂时不通过消息来维护白板列表
            //switch (action) {
            //    case CREATE:
            //        addWhiteBoard(whiteBoard);
            //        break;
            //    case DELETE:
            //        deleteWhiteBoard(whiteBoard);
            //        break;
            //}
        }

        @Override
        public void onOpenDeviceInvite(DeviceControlInvite deviceInvite) {
            deviceControlInvite.postValue(deviceInvite);
        }

        @Override
        public void onDeviceStateChanged(String userId, DeviceType deviceType, boolean isEnable) {
            updateDeviceState(userId, deviceType, isEnable);
        }

        @Override
        public void onDisplayChanged(ScreenDisplay screenDisplay) {
            ScreenDisplay displayWithClassMember = getDisplayWithClassMember(screenDisplay);
            display.postValue(displayWithClassMember);
        }

        @Override
        public void onWhiteBoardPageChanged(String whiteBoardId, int curPage, String userId) {
            // 目前白板翻页由 web 页实现
        }

        @Override
        public void onRoleChanged(List<RoleChangedUser> roleChangedUserList, String optUserId) {
            if (roleChangedUserList == null || roleChangedUserList.size() == 0) return;

            List<ClassMember> memberListValue = memberList.getValue();
            UserInfo userInfoValue = userInfo.getValue();
            if (memberListValue == null || memberListValue.size() == 0) return;

            for (RoleChangedUser role : roleChangedUserList) {
                // 判断自己是不是变化了
                if (userInfoValue.getUserId().equals(role.getUserId())) {
                    userInfoValue.setRole(role.getRole().getValue());
                    userInfo.postValue(userInfoValue);

                    // 保存用户名，用于显示角色变化消息
                    role.setUserName(userInfoValue.getUserName());
                }

                for (ClassMember member : memberListValue) {
                    if (member.getUserId().equals(role.getUserId())) {
                        Role r = role.getRole();
                        member.setRole(r.getValue());
                        //个人改变后的信息刷新， 主要用户共享区域的 讲师和助手
                        roleChangeUser.postValue(member);
                        //判断是否包含在需要单独的刷新角色
                        tryToUpdateSpecialRole(member);

                        // 保存用户名，用于显示角色变化消息
                        role.setUserName(member.getUserName());
                        break;
                    }
                }

            }

            // 由于此消息中包含多个角色变化信息，需要转为单条并逐个显示在聊天列表中
            IMManager.getInstance().saveRoleChangedMessageToSingle(roleChangedUserList, optUserId, roomId.getValue());

            memberList.postValue(sortClassMember(memberListValue));
        }

        @Override
        public void onApplyForSpeechRequest(ApplyForSpeechRequest request) {
            applySpeechRequestTimer.start();
            if (request != null && TextUtils.isEmpty(request.getReqUserName())) {
                List<ClassMember> value = memberList.getValue();
                if (value != null) {
                    for (ClassMember member : value) {
                        if (member.getUserId().equals(request.getReqUserId())) {
                            request.setReqUserName(member.getUserName());
                        }
                    }
                }
            }
            applyForSpeechRequest.postValue(request);
        }

        @Override
        public void onTicketExpired(TicketExpired expired) {
            // 目前有两个地方使用到了超时， 升级和请求发言
            UserInfo userInfoValue = userInfo.getValue();
            userInfoValue.setApplySpeeching(false);
            userInfo.postValue(userInfoValue);
            ticketExpired.postValue(expired);
        }

        @Override
        public void onRequestSpeechResult(SpeechResult result) {
            //
            if (result.isAccept()) {
                List<ClassMember> memberListValue = memberList.getValue();
                if (memberListValue != null) {
                    for (ClassMember member : memberListValue) {
                        if (member.getUserId().equals(result.getReqUserId())) {
                            member.setRole(Role.STUDENT.getValue());
                            member.setCamera(true);
                            member.setMicrophone(true);
                            member.setUserName(result.getReqUserName());
                            memberList.postValue(memberListValue);
                            break;
                        }
                    }
                }

                UserInfo userInfoValue = userInfo.getValue();
                if (userInfoValue.getUserId().equals(result.getReqUserId())) {
                    userInfoValue.setRole(Role.STUDENT.getValue());
                    userInfoValue.setCamera(true);
                    userInfoValue.setMicrophone(true);
                    userInfoValue.setUserName(result.getReqUserName());
                    userInfoValue.setApplySpeeching(false);
                    userInfo.postValue(userInfoValue);
                }
            } else {
                // 拒绝申请
                UserInfo userInfoValue = userInfo.getValue();
                userInfoValue.setApplySpeeching(false);
                userInfo.postValue(userInfoValue);
            }

            speechResult.postValue(result);
        }

        @Override
        public void onInviteUpgradeRole(UpgradeRoleInvite invite) {
            upgradeRoleInvite.postValue(invite);
        }

        @Override
        public void onExistUnReadMessage(int count) {
            unReadMessage.postValue(count);
        }
    };

    /**
     * 移除已存在成员
     *
     * @param classMember
     */
    private void removeClassMember(ClassMember classMember) {
        List<ClassMember> memberListValue = memberList.getValue();
        ClassMember removeMember = null;
        if (memberListValue != null) {
            for (ClassMember member : memberListValue) {
                if (member.getUserId().equals(classMember.getUserId())) {
                    removeMember = member;
                    break;
                }
            }

            if (removeMember != null) {
                memberListValue.remove(removeMember);
                memberList.postValue(memberListValue);
            }
        }
    }

    /**
     * 添加新的课堂成员
     *
     * @param classMember
     */
    private void addClassMember(ClassMember classMember) {
        List<ClassMember> memberListValue = memberList.getValue();
        if (memberListValue != null) {
            boolean isInList = false; //成员是否已在列表
            for (ClassMember member : memberListValue) {
                if (member.getUserId().equals(classMember.getUserId())) {
                    isInList = true;
                    break;
                }
            }

            if (!isInList) {
                memberListValue.add(classMember);
                memberList.postValue(sortClassMember(memberListValue));
            }
        } else {
            memberListValue = new ArrayList<>();
            memberListValue.add(classMember);
            memberList.postValue(sortClassMember(memberListValue));
        }
    }

    /**
     * 更新课堂成员
     *
     * @param classMember
     */
    private void updateClassMember(ClassMember classMember) {
        List<ClassMember> memberListValue = memberList.getValue();
        if (memberListValue != null) {
            for (ClassMember member : memberListValue) {
                if (member.getUserId().equals(classMember.getUserId())) {
                    member.setRole(classMember.getRole().getValue());
                    member.setCamera(classMember.isCamera());
                    member.setMicrophone(classMember.isCamera());
                    member.setJoinTime(classMember.getJoinTime());
                    member.setUserName(classMember.getUserName());
                    memberList.postValue(memberListValue);
                    break;
                }
            }
        }
    }

    /**
     * 更新课堂成员角色
     *
     * @param userId
     * @param role
     */
    private void updateClassMember(String userId, Role role) {
        List<ClassMember> memberListValue = memberList.getValue();
        if (memberListValue != null) {
            for (ClassMember member : memberListValue) {
                if (member.getUserId().equals(userId)) {
                    member.setRole(role.getValue());
                    memberList.postValue(memberListValue);
                    break;
                }
            }
        }
    }


    //排序
    private  List<ClassMember>  sortClassMember (List<ClassMember> members) {
        // 自己永远在第一个， 然后是助教讲师，学员， 旁听， 学员和旁听是按照时间顺序排列
        if (members == null || members.size() <= 0) {
            return null;
        }

        List<ClassMember> sortList = new ArrayList<>();
        List<ClassMember> students = new ArrayList<>();
        List<ClassMember> listeners = new ArrayList<>();

        for (ClassMember member : members) {
            if (userInfo.getValue().getUserId().equals(member.getUserId())) {
                sortList.add(0, member);
            } else if (member.getRole() == Role.ASSISTANT) {
                if (sortList.size() > 1) {
                    sortList.add(1, member);
                } else {
                    sortList.add(member);
                }
            } else if (member.getRole() == Role.LECTURER) {
                sortList.add(member);
            } else if (member.getRole() == Role.STUDENT) {
                students.add(member);
            } else if (member.getRole() == Role.LISTENER) {
                listeners.add(member);
            }
        }

        Collections.sort(students, new Comparator<ClassMember>() {
            @Override
            public int compare(ClassMember o1, ClassMember o2) {
                long l = o1.getJoinTime() - o2.getJoinTime();
                int i = l > 0 ? 1: l < 0 ? -1 : 0;
                return i;
            }
        });

        Collections.sort(listeners, new Comparator<ClassMember>() {
            @Override
            public int compare(ClassMember o1, ClassMember o2) {
                long l = o1.getJoinTime() - o2.getJoinTime();
                int i = l > 0 ? 1: l < 0 ? -1 : 0;
                return i;
            }
        });

        sortList.addAll(students);
        sortList.addAll(listeners);
        return sortList;
    }

    /**
     * 更新课堂成员设备状态
     *
     * @param userId
     * @param deviceType
     * @param isEnable
     */
    private void updateDeviceState(String userId, DeviceType deviceType, boolean isEnable) {

        //此处应优化为只更新可发言的用户
        List<ClassMember> memberListValue = memberList.getValue();
        UserInfo info = userInfo.getValue();

        if (info.getUserId().equals(userId)) {
            // 相同的话就不要提示了
            boolean isNotify = false; // 如果提前值变了， 那说明是自己手动禁止的
            switch (deviceType) {
                case Camera:
                    if (info.isCamera() != isEnable) {
                        info.setCamera(isEnable);
                        isNotify = true;
                    }
                    break;
                case Microphone:
                    if (info.isMicrophone() != isEnable) {
                        info.setMicrophone(isEnable);
                        isNotify = true;
                    }
                    break;
            }

            if (isNotify) {
                userInfo.postValue(info);
                // 自己的设备被停用时提醒
                if(!isEnable ) {
                    onDisableDevice.postValue(deviceType);
                }
            }
        }


        if (memberListValue != null) {
            for (ClassMember member : memberListValue) {
                if (member.getUserId().equals(userId)) {
                    switch (deviceType) {
                        case Camera:
                            member.setCamera(isEnable);
                            // 如果当前的用户是助教或者讲师， 则需要判断一下关闭设备的人
                            // 假如关闭设备的人是正是在共享区域显示的人， 则需要本地显示null，提示可以创建白板
//                            if (userInfo.getValue().getRole() == Role.ASSISTANT || userInfo.getValue().getRole() == Role.LECTURER) {
                                ScreenDisplay value = display.getValue();
                                if (value != null ) {
                                    if (!isEnable && (value.getType() == ScreenDisplay.Display.LECTURER || value.getType() == ScreenDisplay.Display.ASSISTANT) && userId.equals(value.getUserId())) {
                                        value.setType(ScreenDisplay.Display.NONE);
                                        display.postValue(value);
                                    } else if (isEnable && value.getType() == ScreenDisplay.Display.NONE && value.getClassMember() != null) {
                                        ClassMember classMember = value.getClassMember();
                                        if (classMember.getRole() == Role.LECTURER) {
                                            value.setType(ScreenDisplay.Display.LECTURER);
                                        } else if (classMember.getRole() == Role.ASSISTANT) {
                                            value.setType(ScreenDisplay.Display.ASSISTANT);
                                        }
                                        display.postValue(value);
                                    }
//                                }
                            }
                            break;
                        case Microphone:
                            member.setMicrophone(isEnable);
                            break;
                    }

                    isUserListNoUpdate = true;
                    memberList.postValue(memberListValue);
                    break;
                }
            }
        }

        // 设备变化通知
        deviceChange.postValue(new DeviceChange(userId, deviceType, isEnable ));
    }

    /**
     * 添加白板，若有重复则不进行添加
     *
     * @param whiteBoard
     */
    private void addWhiteBoard(WhiteBoard whiteBoard) {
        List<WhiteBoard> whiteBoardListValue = whiteBoardList.getValue();
        if (whiteBoardListValue != null) {
            boolean isAdded = false;
            for (WhiteBoard board : whiteBoardListValue) {
                if (board.getWhiteboardId().equals(whiteBoard.getWhiteboardId())) {
                    isAdded = true;
                    break;
                }
            }

            if (!isAdded) {
                whiteBoardListValue.add(whiteBoard);
                whiteBoardList.postValue(whiteBoardListValue);
            }
        } else {
            whiteBoardListValue = new ArrayList<>();
            whiteBoardListValue.add(whiteBoard);
            whiteBoardList.postValue(whiteBoardListValue);
        }
    }

    /**
     * 删除白板
     *
     * @param whiteBoard
     */
    private void deleteWhiteBoard(WhiteBoard whiteBoard) {
        List<WhiteBoard> whiteBoardListValue = whiteBoardList.getValue();
        if (whiteBoardListValue != null) {
            WhiteBoard removeBoard = null;
            for (WhiteBoard board : whiteBoardListValue) {
                if (board.getWhiteboardId().equals(whiteBoard.getWhiteboardId())) {
                    removeBoard = board;
                    break;
                }
            }

            if (removeBoard != null) {
                whiteBoardListValue.remove(removeBoard);
                whiteBoardList.postValue(whiteBoardListValue);
            }
        }
    }

    /**
     * 从成员列表中差出特定的角色，并刷新
     */
    private void findAndUpdateSpecialRole() {
        List<ClassMember> list = memberList.getValue();
        if (list != null && list.size() > 0) {
            for (ClassMember member : list) {
                tryToUpdateSpecialRole(member);
            }
        }
    }

    /**
     * 刷新特定的角色信息，用于单独刷新该角色触发刷新
     *
     * @param member
     */
    private void tryToUpdateSpecialRole(ClassMember member) {
        Role role = member.getRole();

        if (role == Role.LECTURER) {
            ClassMember lecturer = lecturerRole.getValue();
            if (lecturer == null
                    || !lecturer.getUserId().equals(member.getUserId())) {
                lecturerRole.postValue(member);
            }
        } else if (role == Role.ASSISTANT) {
            ClassMember assistant = assistantRole.getValue();
            if (assistant == null
                    || !assistant.getUserId().equals(member.getUserId())) {
                assistantRole.postValue(member);
            }
        }
    }


    /**
     * 按加入时间顺序排序成员列表
     *
     * @param list
     */
    private void sortClassMemberListByJoinTimeASC(List<ClassMember> list) {
        Collections.sort(list, new Comparator<ClassMember>() {
            @Override
            public int compare(ClassMember member1, ClassMember member2) {
                return (int) (member1.getJoinTime() - member2.getJoinTime());
            }
        });
    }


    private ScreenDisplay getDisplayWithClassMember(ScreenDisplay screenDisplay) {
        if (!TextUtils.isEmpty(screenDisplay.getUserId())) {
            List<ClassMember> value = memberList.getValue();
            if (value != null && value.size() > 0) {
                for (ClassMember member : value) {
                    if (screenDisplay.getUserId().equals(member.getUserId())) {
                        screenDisplay.setClassMember(member);
                        break;
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(screenDisplay.getUserId()) && screenDisplay.getClassMember() == null ) {
            ClassMember classMember = new ClassMember();
            classMember.setUserId(screenDisplay.getUserId());
            if (screenDisplay.getType() == ScreenDisplay.Display.ASSISTANT) {
                classMember.setRole(Role.ASSISTANT.getValue());
            } else if (screenDisplay.getType() == ScreenDisplay.Display.LECTURER) {
                classMember.setRole(Role.LECTURER.getValue());
            }
            screenDisplay.setClassMember(classMember);
        }

        return screenDisplay;
    }

    public LiveData<RequestState> joinRtcRoom(String roomId) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        RtcManager.getInstance().joinRtcRoom(roomId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String roomId) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });
        return stateLiveData;
    }

    public LiveData<RequestState> quitRtcRoom(String roomId) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.quitRtcRoom(roomId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String roomId) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });
        return stateLiveData;

    }

    public LiveData<RequestState> startRtcChat(RongRTCVideoView view) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.startRtcChat(view, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                stateLiveData.success();
                localUserStartVideoChat.postValue(true);
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });
        return stateLiveData;
    }

    public LiveData<RequestState> stopRtcChat() {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.stopRtcChat(new ResultCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean aBoolean) {
                stateLiveData.success();
                localUserStartVideoChat.postValue(false);

            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });
        return stateLiveData;
    }

    public void setLocalVideoEnable(boolean enable) {
        UserInfo userInfoValue = userInfo.getValue();
        userInfoValue.setCamera(enable);
        userInfo.postValue(userInfoValue);
        classRepository.setLocalVideoEnable(enable);
    }

    public void setLocalMicEnable(boolean enable) {
        UserInfo userInfoValue = userInfo.getValue();
        userInfoValue.setMicrophone(enable);
        userInfo.postValue(userInfoValue);
        RtcManager.getInstance().setLocalMicEnable(enable);
    }

    public void switchCamera() {
        RtcManager.getInstance().switchCamera();

    }

    public void setEnableSpeakerphone(boolean enable) {
        RtcManager.getInstance().setEnableSpeakerphone(enable);
    }

    public LiveData<RequestState> muteRoomVoice(boolean mute) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.muteRoomVoice(mute, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean bmete) {
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });
        return stateLiveData;
    }


    public LiveData<RequestState> subscribeAllResource(HashMap<String, RongRTCVideoView> videoViews) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.subscribeAllResource(videoViews, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean b) {
                stateLiveData.success();

            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });
        return stateLiveData;
    }

    public LiveData<RequestState> subscribeResource(String userId, RongRTCVideoView videoView) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.subscribeResource(userId, videoView, new ResultCallback<String>() {
            @Override
            public void onSuccess(String b) {
                stateLiveData.success();

            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);

            }
        });
        return stateLiveData;
    }

    public LiveData<RequestState> subscribeResource(String userId, RongRTCVideoView videoView, RongRTCVideoView screenShareVideo) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        classRepository.subscribeResource(userId, videoView, screenShareVideo, new ResultCallback<String>() {
            @Override
            public void onSuccess(String b) {
                stateLiveData.success();

            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);

            }
        });
        return stateLiveData;
    }


    public LiveData<RequestState>  unSubscribeResource(String userId) {
        final MutableLiveData<RequestState> stateLiveData = new MutableLiveData<>();
        stateLiveData.postValue(RequestState.loading());
        classRepository.unSubscribeResource(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String b) {
                stateLiveData.setValue(RequestState.success());

            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.setValue(RequestState.failed(errorCode));

            }
        });
        return stateLiveData;
    }


    public LiveData<RequestState>  subscribeVideo(String userId, RongRTCVideoView videoView) {
        final MutableLiveData<RequestState> stateLiveData = new MutableLiveData<>();
        stateLiveData.postValue(RequestState.loading());
        classRepository.subscribeVideo(userId, videoView, new ResultCallback<String>() {
            @Override
            public void onSuccess(String b) {
                stateLiveData.setValue(RequestState.success());

            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.setValue(RequestState.failed(errorCode));

            }
        });
        return stateLiveData;
    }

    public LiveData<RequestState>  unSubscribeVideo(String userId) {
        final MutableLiveData<RequestState> stateLiveData = new MutableLiveData<>();
        stateLiveData.postValue(RequestState.loading());
        classRepository.unSubscribeVideo(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String b) {
                stateLiveData.setValue(RequestState.success());

            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.setValue(RequestState.failed(errorCode));

            }
        });
        return stateLiveData;
    }

    public LiveData<RequestState>  subscribeAudio(String userId) {
        final MutableLiveData<RequestState> stateLiveData = new MutableLiveData<>();
        stateLiveData.postValue(RequestState.loading());
        classRepository.subscribeAudio(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String b) {
                stateLiveData.setValue(RequestState.success());

            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.setValue(RequestState.failed(errorCode));

            }
        });
        return stateLiveData;
    }

    public LiveData<RequestState>  unSubscribeAudio(String userId) {
        final MutableLiveData<RequestState> stateLiveData = new MutableLiveData<>();
        stateLiveData.postValue(RequestState.loading());
        classRepository.unSubscribeAudio(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String b) {
                stateLiveData.setValue(RequestState.success());
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.setValue(RequestState.failed(errorCode));

            }
        });
        return stateLiveData;
    }


    public LiveData<RequestState>  subscribeScreen(String userId, RongRTCVideoView videoView) {
        final MutableLiveData<RequestState> stateLiveData = new MutableLiveData<>();
        stateLiveData.postValue(RequestState.loading());
        classRepository.subscribeScreen(userId, videoView, new ResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                stateLiveData.setValue(RequestState.success());
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.setValue(RequestState.failed(errorCode));
            }
        });
        return stateLiveData;
    }

    public LiveData<RequestState>  unSubscribeScreen(String userId) {
        final MutableLiveData<RequestState> stateLiveData = new MutableLiveData<>();
        stateLiveData.postValue(RequestState.loading());
        classRepository.unSubscribeScreen(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                stateLiveData.setValue(RequestState.success());
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.setValue(RequestState.failed(errorCode));
            }
        });
        return stateLiveData;
    }

    OnClassVideoEventListener onClassVideoEventListener = new OnClassVideoEventListener() {

        @Override
        public void onInitVideoList(List<StreamResource> resources) {
            SLog.d("ss_rtcuser", "onInitVideoList==>" + resources);
            if (resources != null && resources.size() > 0) {
                initUserVideoList.postValue(resources);
            }
        }

        @Override
        public void onAddVideoUser(StreamResource resource) {
            addVideoUser.postValue(resource);
        }

        @Override
        public void onRemoveVideoUser(StreamResource resource) {
            removeVideoUser.postValue(resource);
        }

        @Override
        public void onFirstFrameDraw(String userId, String tag) {
            FirstFrameUserInfo info  = new FirstFrameUserInfo();
            info.setUserId(userId);
            info.setTag(tag);
            fisrtFrameDraw.postValue(info);
        }

        @Override
        public void onVideoEnabled(String userId, boolean enable) {

        }
    };

    private ClassMember getVideoClassMember(String userId) {
        ClassMember member = null;
        List<ClassMember> value = memberList.getValue();
        for (int j = 0; j < value.size(); j++) {
            if (userId.equals(value.get(j).getUserId())) {
                member = value.get(j);
                break;
            }
        }
        return member;
    }

    private List<ClassMember> getVideoClassMember(List<String> userIds) {
        List<ClassMember> tmpList = new ArrayList<>();
        List<ClassMember> value = memberList.getValue();

        if (userIds != null && value != null) {
            for (int i = 0; i < userIds.size(); i++) {
                for (int j = 0; j < value.size(); j++) {
                    if (userIds.get(i).equals(value.get(j).getUserId())) {
                        tmpList.add(value.get(j));
                        break;
                    }
                }
            }
        }
        return tmpList;
    }

    @Override
    protected void onCleared() {
        classRepository.removeOnClassEventListener(onClassEventListener);
        countPassTimeTimer.cancel();
    }
}
