package cn.rongcloud.sealclass.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.common.ErrorCode;
import cn.rongcloud.sealclass.common.ShowToastObserver;
import cn.rongcloud.sealclass.im.ui.ClassConversationFragment;
import cn.rongcloud.sealclass.model.ApplyForSpeechRequest;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.DeviceControlInvite;
import cn.rongcloud.sealclass.model.DeviceType;
import cn.rongcloud.sealclass.model.InviteAction;
import cn.rongcloud.sealclass.model.LoginResult;
import cn.rongcloud.sealclass.model.RequestState;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.model.SpeechResult;
import cn.rongcloud.sealclass.model.TicketExpired;
import cn.rongcloud.sealclass.model.UpgradeRoleInvite;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.ui.dialog.ApplySpeechRequestDialog;
import cn.rongcloud.sealclass.ui.dialog.CommonDialog;
import cn.rongcloud.sealclass.ui.dialog.DowngradeListDialog;
import cn.rongcloud.sealclass.ui.fragment.ClassBigVideoWindowFragment;
import cn.rongcloud.sealclass.ui.fragment.ClassMemberListFragment;
import cn.rongcloud.sealclass.ui.fragment.ClassResourceFragment;
import cn.rongcloud.sealclass.ui.fragment.ClassScreenControlFragment;
import cn.rongcloud.sealclass.ui.fragment.ClassShareScreenFragment;
import cn.rongcloud.sealclass.ui.fragment.ClassTopOperateControlFragment;
import cn.rongcloud.sealclass.ui.fragment.ClassVideoListFragment;
import cn.rongcloud.sealclass.utils.NotchScreenUtils;
import cn.rongcloud.sealclass.utils.log.SLog;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;
import io.rong.imlib.model.Conversation;


public class ClassActivity extends ExtendBaseActivity implements ToastBySelfComponent {
    public static final String EXTRA_LOGIN_RESULT = "extra_login_result";
    public static final String EXTRA_CLOSE_CAMERA = "extra_open_camera";

    private static final long TOAST_DEFAULT_DURATION = 5000;

    private FrameLayout containerMemberList;
    private FrameLayout containerResourceLibrary;
    private FrameLayout containerIM;
    private FrameLayout containerVideoList;
    private FrameLayout containerBigVideoWindow;

    private TextView classIdTv;
    private TextView timeTv;
    private TextView toastTv;
    private Animation toastShowAnim;
    private Animation toastDismissAnim;

    private ClassViewModel classViewModel;

    private Handler handler;
    private ClassVideoListFragment videoListFragment;
    private ClassScreenControlFragment screenControlFragment;
    private ClassBigVideoWindowFragment videoBigWidowFragment;
    private ClassResourceFragment resourceFragment;

    private View parentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideNavigationBar();
        Intent intent = getIntent();

        if (intent == null) {
            finish();
        }
        LoginResult loginResult = (LoginResult) intent.getSerializableExtra(EXTRA_LOGIN_RESULT);
        boolean isCloseCamera = (Boolean) intent.getBooleanExtra(EXTRA_CLOSE_CAMERA, true);
        // 需要由登录进入此界面，当不含登录信息时关闭
        if (loginResult == null) {
            finish();
        }

        if (loginResult.getUserInfo() != null) {
            loginResult.getUserInfo().setCamera(!isCloseCamera);
        }

        handler = new Handler();

        classViewModel = ViewModelProviders.of(this, new ClassViewModel.Factory(loginResult, this.getApplication())).get(ClassViewModel.class);
        showFirstMemberToast();
        observeModel();
        enableKeyboardStateListener(true);
    }

    /**
     * 如果是房间中只有自己则根据身份不同弹出不同提示
     */
    private void showFirstMemberToast() {
        List<ClassMember> memberList = classViewModel.getMemberList().getValue();
        if (memberList != null && memberList.size() == 1) {
            UserInfo userInfo = classViewModel.getUserInfo().getValue();
            if (userInfo.getRole() == Role.LISTENER) {
                showToast(R.string.toast_tips_role_is_listener);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showToast(R.string.toast_only_self_in_room);
                    }
                }, TOAST_DEFAULT_DURATION);
            } else {
                showToast(R.string.toast_only_self_in_room);
            }
        }
    }

    private void observeModel() {
        // 课堂 id
        classViewModel.getRoomId().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String classId) {

                classId = classId.length() > 10 ? classId.substring(0, 10) + "..." : classId;
                // 设置标题
                classIdTv.setText(classId);

                /*
                 * 以下会在 Android Studio 报出 fragment 类不匹配错误，因为目前 IMKit 没有使用 Android X
                 * 由于 Android Studio 自带了在编译时替换类的功能所以不会影响在编译和应用的使用
                 */
                ClassConversationFragment conversationFragment = (ClassConversationFragment) getSupportFragmentManager().findFragmentById(R.id.class_container_im);
                if (conversationFragment == null) {
                    conversationFragment = new ClassConversationFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.class_container_im, conversationFragment);
                    fragmentTransaction.commitAllowingStateLoss();
                }

                Uri uri = Uri.parse("rong://" + getApplicationInfo().processName).buildUpon()
                        .appendPath("conversation")
                        .appendPath(Conversation.ConversationType.GROUP.getName().toLowerCase(Locale.US))
                        .appendQueryParameter("targetId", classId)
                        .build();
                conversationFragment.setUri(uri);
            }
        });

        // 设备控制请求
        classViewModel.getDeviceControlInvite().observe(this, new Observer<DeviceControlInvite>() {
            @Override
            public void onChanged(DeviceControlInvite deviceControlInvite) {
                InviteAction action = deviceControlInvite.getAction();
                switch (action) {
                    case INVITE: // 助教控制设备请求
                        DeviceType deviceType = deviceControlInvite.getDeviceType();
                        inviteOpenedDevicesDialog(deviceType, deviceControlInvite.getTicket());
                        break;
                    case REJECT:
                        // 拒绝操作提示
                        showToast(R.string.toast_other_side_reject_your_request);
                        break;
                    case APPROVE:
                        // 同意操作提示
                        showToast(R.string.toast_other_side_accept_your_request);
                        break;
                    default:
                        break;
                }

            }

        });

        classViewModel.getApplyForSpeechRequest().observe(this, new Observer<ApplyForSpeechRequest>() {
            @Override
            public void onChanged(ApplyForSpeechRequest applyForSpeechRequest) {
                showApplySpeechRequestDialog(getRoomId(), applyForSpeechRequest.getTicket(), applyForSpeechRequest.getReqUserName());
            }
        });

        classViewModel.getSpeechResult().observe(this, new Observer<SpeechResult>() {
            @Override
            public void onChanged(SpeechResult speechResult) {
                if (speechResult.isAccept()) {
                    // 同意发言
                    showToast(R.string.toast_assistant_accept_your_speech);
                } else {
                    // 不同意发言
                    showToast(R.string.toast_assistant_reject_your_speech);
                }
            }
        });

        classViewModel.getUpgradeRoleInvite().observe(this, new Observer<UpgradeRoleInvite>() {
            @Override
            public void onChanged(UpgradeRoleInvite upgradeRoleInvite) {
                InviteAction action = upgradeRoleInvite.getAction();
                switch (action) {
                    case INVITE:
                        showUpgradeRoleInviteDialog(getRoomId(), upgradeRoleInvite.getTicket(), upgradeRoleInvite.getRole());
                        break;
                    case APPROVE:
                        // 同意
                        showToast(R.string.toast_other_side_accept_your_request);
                        break;
                    case REJECT:
                        // 拒绝
                        showToast(R.string.toast_other_side_reject_your_request);
                        break;
                    default:
                        break;
                }
            }
        });

        classViewModel.getTicketExpired().observe(this, new Observer<TicketExpired>() {
            @Override
            public void onChanged(TicketExpired ticketExpired) {
                UserInfo userInfo = classViewModel.getUserInfo().getValue();
                // 当为听众时，只能向助教提出申请操作
                if (userInfo != null && userInfo.getRole() == Role.LISTENER) {
                    showToast(R.string.toast_assistant_not_respond);
                } else {
                    showToast(R.string.toast_other_side_not_respond);
                }
            }
        });

        classViewModel.getPassedTime().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String time) {
                timeTv.setText(time);
            }
        });

        classViewModel.getUserInfo().observe(this, new Observer<UserInfo>() {
            private boolean isFirstChanged = false;
            private Role lastRole;

            @Override
            public void onChanged(UserInfo userInfo) {
                if (!isFirstChanged) {
                    isFirstChanged = true;
                    lastRole = userInfo.getRole();
                    return;
                }

                // 当角色改变为旁听者时进行提示
                if (lastRole != Role.LISTENER
                        && userInfo.getRole() == Role.LISTENER) {
                    showToast(R.string.toast_tips_role_is_listener);

                    // 当角色变为学员时进行提示
                } else if (lastRole != Role.STUDENT
                        && userInfo.getRole() == Role.STUDENT) {
                    showToast(R.string.toast_your_role_is_to_student);

                    // 当角色变为讲师时进行提示
                } else if (lastRole != Role.LECTURER
                        && userInfo.getRole() == Role.LECTURER) {
                    showToast(R.string.toast_your_role_is_to_lecturer);

                    // 当角色变为助教时进行提示
                } else if (lastRole != Role.ASSISTANT
                        && userInfo.getRole() == Role.ASSISTANT) {
                    showToast(R.string.toast_your_role_is_to_assistant);
                }

                lastRole = userInfo.getRole();
            }
        });

        // 监听当自己的设备被停用状态
        classViewModel.geOnDisableDevice().observe(this, new Observer<DeviceType>() {
            @Override
            public void onChanged(DeviceType deviceType) {
                if (deviceType == DeviceType.Camera) {
                    showToast(R.string.toast_assistant_close_your_camera);
                } else if (deviceType == DeviceType.Microphone) {
                    showToast(R.string.toast_assistant_close_your_mic);
                }
            }
        });

        // 响应请求发言请求超时
        classViewModel.getRespondApplySpeechTimeout().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Fragment dialog = getSupportFragmentManager().findFragmentByTag("apply_speech_request_dialog");
                if (dialog != null && dialog instanceof ApplySpeechRequestDialog) {
                    ((ApplySpeechRequestDialog) dialog).dismiss();
                    showToast(R.string.toast_assistant_respond_apply_speech_request_timeout);
                }
            }
        });

        //监听助教踢人后，关闭成员列表界面， 设计需求
        classViewModel.getKickedOff().observe(this, new Observer<ClassMember>() {
            @Override
            public void onChanged(ClassMember member) {
                UserInfo userInfoValue = classViewModel.getUserInfo().getValue();
                if (userInfoValue != null && !userInfoValue.getUserId().equals(member.getUserId()) && userInfoValue.getRole() == Role.ASSISTANT) {
                    screenControlFragment.clearChecked();
                }
            }
        });
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.class_activity;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {

        // 刘海屏适配
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            getWindow().setAttributes(lp);
        } else {
            int offset = NotchScreenUtils.getOffset(this);
            RelativeLayout view = findView(R.id.class_root_view);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            layoutParams.leftMargin = offset;
            view.setLayoutParams(layoutParams);
        }


        parentView = findView(R.id.class_parent_view);
        containerMemberList = findView(R.id.class_container_menber_list);
        containerResourceLibrary = findView(R.id.class_container_resource_library);
        containerIM = findView(R.id.class_container_im);
        containerVideoList = findView(R.id.class_container_video_list);
        containerBigVideoWindow = findView(R.id.class_container_video_big_window);
        classIdTv = findView(R.id.class_tv_title);
        timeTv = findView(R.id.class_tv_time);
        toastTv = findView(R.id.class_tv_toast);

        findView(R.id.class_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (screenControlFragment != null) {
                    screenControlFragment.clearChecked();
                }
            }
        });

        findView(R.id.class_fl_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (screenControlFragment != null) {
                    screenControlFragment.clearChecked();
                }
            }
        });

        findView(R.id.class_container_screen_control).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (screenControlFragment != null) {
                    screenControlFragment.clearChecked();
                }
            }
        });

        // 自定义 toast 的显示和消失动画
        toastShowAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_show);
        toastDismissAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_dismiss);
        toastDismissAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (toastTv != null) {
                    toastTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });


        screenControlFragment = new ClassScreenControlFragment();

        HashMap<ClassScreenControlFragment.ControlOperateType, Boolean> values = new HashMap<>();
        values.put(ClassScreenControlFragment.ControlOperateType.WHITE_BOARD, true);
        values.put(ClassScreenControlFragment.ControlOperateType.RES_LIBRARY, true);
        values.put(ClassScreenControlFragment.ControlOperateType.MEMBER_LIST, true);
        values.put(ClassScreenControlFragment.ControlOperateType.VIDEO_LIST, true);
        values.put(ClassScreenControlFragment.ControlOperateType.IM, true);
        screenControlFragment.setButtonEnableStatus(values);
        screenControlFragment.setCheckedButton(ClassScreenControlFragment.ControlOperateType.VIDEO_LIST);
        screenControlFragment.setScreenControlButtonCheckListener(new ClassScreenControlFragment.ScreenControlButtonListener() {
            @Override
            public void onCheckedChanged(ClassScreenControlFragment.ControlOperateType type, boolean isChecked) {
                controlView(type, isChecked);
            }

            @Override
            public void onCreateWhiteBoard() {
                createDisplayWhiteBoard();
            }

        });

        ClassMemberListFragment memberListFragment = new ClassMemberListFragment();

        ClassTopOperateControlFragment topOperateControlFragment = new ClassTopOperateControlFragment();
        final ClassShareScreenFragment shareScreenFragment = new ClassShareScreenFragment();
        // 设置切换全屏监听
        shareScreenFragment.setOnClickFullScreenListener(new ClassShareScreenFragment.OnClickToFullScreenListener() {
            @Override
            public void onClickToFullScreen(boolean isToFullScreen) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fullScreenFragment = fragmentManager.findFragmentById(R.id.class_container_full_screen);
                // 判断当前是否已处于全屏或者窗口模式
                if (fullScreenFragment != null && isToFullScreen
                        || fullScreenFragment == null && !isToFullScreen) {
                    return;
                }
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(shareScreenFragment);
                fragmentTransaction.commitNow();
                fragmentTransaction = fragmentManager.beginTransaction();

                // 切换全屏和窗口的布局
                Bundle bundle = new Bundle();
                bundle.putBoolean(ClassShareScreenFragment.ARGUMENT_BOOLEAN_IS_FULL_SCREEN, isToFullScreen);
                shareScreenFragment.setArguments(bundle);

                if (isToFullScreen) {
                    fragmentTransaction.add(R.id.class_container_full_screen, shareScreenFragment);
                } else {
                    fragmentTransaction.add(R.id.class_container_share_screen, shareScreenFragment);
                }
                fragmentTransaction.commitAllowingStateLoss();
            }
        });

        shareScreenFragment.setOnVideoViewChangeListenr(new ClassShareScreenFragment.OnVideoViewChangeListenr() {
            @Override
            public void onChangeUser(List<String> userIds) {
                if (videoListFragment != null) {
                    videoListFragment.refershUserVideoView(userIds);
                }
            }

            @Override
            public void onChangeUser(ClassMember oldUser) {
                if (videoListFragment != null) {
                    videoListFragment.refershUserVideoView(oldUser);
                }
            }
        });


        resourceFragment = new ClassResourceFragment();
        videoListFragment = new ClassVideoListFragment();
        videoListFragment.setOnVideoViewItemClickListener(new ClassVideoListFragment.OnVideoViewItemClickListener() {
            @Override
            public void onItemClick(ClassMember member) {
                SLog.d("show_win", "show big window => " + member);
//                showBigVideoWindowDialog(member);
                showBigVideoWindow(member, true);
            }
        });


        videoBigWidowFragment = new ClassBigVideoWindowFragment();
        videoBigWidowFragment.setOnWindowCloseListener(new ClassBigVideoWindowFragment.OnWindowCloseListener() {
            @Override
            public void onClosed(ClassMember member) {
                showBigVideoWindow(member, false);
                if (videoListFragment != null) {
                    videoListFragment.refershUserVideoView(member);
                }
            }

            @Override
            public void onUpdate(ClassMember member) {
                if (videoListFragment != null) {
                    videoListFragment.refershUserVideoView(member);
                }
            }
        });
        getSupportFragmentManager().beginTransaction().
                add(R.id.class_container_screen_control, screenControlFragment).
                add(R.id.class_container_menber_list, memberListFragment).
                add(R.id.class_container_rtc_control, topOperateControlFragment).
                add(R.id.class_container_share_screen, shareScreenFragment).
                add(R.id.class_container_resource_library, resourceFragment).
                add(R.id.class_container_video_list, videoListFragment).
                add(R.id.class_container_video_big_window, videoBigWidowFragment).
                commit();

    }

    @Override
    protected void onInitViewModel() {

    }

    private String getRoomId() {
        if (classIdTv != null) {
            return classIdTv.getText().toString();
        }
        return "";
    }

    /*
        通过左侧控制栏的点击触发， 来控制显示那个区域的View
     */
    private void controlView(ClassScreenControlFragment.ControlOperateType type, boolean isChecked) {

        //点击其他操作， 正常收回弹起布局
        if (type != ClassScreenControlFragment.ControlOperateType.IM) {
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) parentView.getLayoutParams();
            layoutParams.bottomMargin = 0;
            parentView.setLayoutParams(layoutParams);
            hideInputKeyboard();
        }

        switch (type) {
            case WHITE_BOARD:
                if (isChecked) {
                    containerMemberList.setVisibility(View.GONE);
                    containerResourceLibrary.setVisibility(View.GONE);
                    containerIM.setVisibility(View.GONE);
                }
                break;
            case RES_LIBRARY:
                if (isChecked) {
                    containerMemberList.setVisibility(View.GONE);
                    containerResourceLibrary.setVisibility(View.VISIBLE);
                    containerIM.setVisibility(View.GONE);
                    // 刷新资源列表，获取最后一帧
                    if (resourceFragment != null) {
                        resourceFragment.updateUserDisplayRes();
                    }
                } else {
                    containerResourceLibrary.setVisibility(View.GONE);
                }
                break;
            case MEMBER_LIST:
                if (isChecked) {
                    containerMemberList.setVisibility(View.VISIBLE);
                    containerResourceLibrary.setVisibility(View.GONE);
                    containerIM.setVisibility(View.GONE);
                } else {
                    containerMemberList.setVisibility(View.GONE);
                }
                break;
            case VIDEO_LIST:
                if (isChecked) {
                    containerMemberList.setVisibility(View.GONE);
                    containerResourceLibrary.setVisibility(View.GONE);
                    containerIM.setVisibility(View.GONE);
                }
                break;
            case IM:
                if (isChecked) {
                    containerMemberList.setVisibility(View.GONE);
                    containerResourceLibrary.setVisibility(View.GONE);
                    containerIM.setVisibility(View.VISIBLE);
                } else {
                    containerIM.setVisibility(View.GONE);
                }
                break;
        }
    }


    // 设备控制请求 dialog
    private void inviteOpenedDevicesDialog(DeviceType deviceType, final String ticket) {
        int contentMessage = R.string.class_dialog_request_invite_camera_connect;
        if (deviceType == DeviceType.Microphone) {
            contentMessage = R.string.class_dialog_request_invite_mic_connect;
        }

        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                // 开启设备
                deviceApprove(getRoomId(), ticket);
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
                deviceReject(getRoomId(), ticket);
            }
        });
        builder.setButtonText(R.string.class_dialog_agree_positive, R.string.class_dialog_reject_negative);
        builder.setContentMessage(getString(contentMessage));
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "devices_dialog");
    }

    // 同意设备开启请求
    private void deviceApprove(String roomId, String ticket) {
        if (classViewModel != null) {
            classViewModel.deviceApprove(roomId, ticket).observe(this, new ShowToastObserver(this));
        }

    }

    //拒绝设备开启请求
    private void deviceReject(String roomId, String ticket) {
        if (classViewModel != null) {
            classViewModel.deviceReject(roomId, ticket).observe(this, new ShowToastObserver(this));
        }

    }


    //用户请求发言dialog
    private void showApplySpeechRequestDialog(final String roomId, final String ticket, final String reqUserName) {
        ApplySpeechRequestDialog.Builder builder = new ApplySpeechRequestDialog.Builder();
        builder.setContentMessage(getString(R.string.class_dialog_apply_speech_request_content, reqUserName));
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                approveSpeech(roomId, ticket, reqUserName);
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
                rejectSpeech(roomId, ticket);
            }
        });
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "apply_speech_request_dialog");
    }


    /**
     * 有旁听者发言，但是非旁听人员已满，弹窗提示是否降级学员
     */
    private void showNeedDownGradeMemberNotifyDialog(final String roomId, final String ticket, final String applyUserName) {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                showDownGradeListDiaolog();
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
                //操作拒绝操作
                rejectSpeech(roomId, ticket);
            }
        });
        builder.setButtonText(R.string.class_dialog_need_downgrade_positive, R.string.class_dialog_need_downgrade_negative);
        builder.setContentMessage(getString(R.string.class_dialog_need_downgrade_content, applyUserName));
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "need_downgrade_dialog");
    }


    //降级学员列表
    private void showDownGradeListDiaolog() {
        DowngradeListDialog.Builder builder = new DowngradeListDialog.Builder();
        builder.setButtonText(R.string.dialog_downgrade_positive_text, R.string.dialog_downgrade_negative_text);
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "downgrade_list_dialog");
    }

    // 同意学员发言
    private void approveSpeech(final String roomId, final String ticket, final String applyUserName) {
        if (classViewModel != null) {
            classViewModel.approveSpeech(roomId, ticket).observe(this, new Observer<RequestState>() {
                @Override
                public void onChanged(RequestState requestState) {
                    if (requestState.getState() == RequestState.State.FAILED) {
                        ErrorCode errorCode = requestState.getErrorCode();
                        //最大人数提示框
                        if (errorCode == ErrorCode.API_ERR_OVER_MAX_COUNT) {
                            showNeedDownGradeMemberNotifyDialog(roomId, ticket, applyUserName);
                        } else {
                            showToast(errorCode.getMessageResId());
                        }
                    }

                }
            });
        }

    }

    //拒绝学员发言
    private void rejectSpeech(String roomId, String ticket) {
        if (classViewModel != null) {
            classViewModel.rejectSpeech(roomId, ticket).observe(this, new ShowToastObserver(this));
        }
    }


    // 角色升级
    // 升级要求
    private void showUpgradeRoleInviteDialog(final String roomId, final String ticket, Role role) {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                approveUpgrade(roomId, ticket);
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
                //操作拒绝操作
                rejectUpgrade(roomId, ticket);
            }
        });
        builder.setButtonText(R.string.class_dialog_agree_positive, R.string.class_dialog_reject_negative);
        builder.setContentMessage(getString(R.string.class_dialog_upgrade_role_content));
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "upgrade_dialog");
    }

    // 同意升级
    private void approveUpgrade(String roomId, String ticket) {
        if (classViewModel != null) {
            classViewModel.upgradeApprove(roomId, ticket).observe(this, new ShowToastObserver(this));
        }
    }


    // 拒绝升级
    private void rejectUpgrade(String roomId, String ticket) {
        if (classViewModel != null) {
            classViewModel.upgradeReject(roomId, ticket).observe(this, new ShowToastObserver(this));
        }
    }


    private void createDisplayWhiteBoard() {
        classViewModel.createWhiteBoard(getRoomId()).observe(this, new ShowToastObserver(this));
    }


//    private void showBigVideoWindowDialog(ClassMember member) {
//        BigVideoWindowDialog.Builder builder = new BigVideoWindowDialog.Builder();
//        builder.setClassMember(member);
//        builder.setOnDialogCloseListener(new BigVideoWindowDialog.OnDialogCloseListener() {
//            @Override
//            public void onClosed(ClassMember member) {
//                if (videoListFragment != null) {
//                    videoListFragment.refershUserVideoView(member);
//                }
//            }
//        });
//        BigVideoWindowDialog dialog = builder.build();
//        dialog.show(getSupportFragmentManager(), "big_window_dialog");
//    }

    private void showBigVideoWindow(ClassMember member, boolean show) {
        if (videoBigWidowFragment != null) {
            if (show) {
                videoBigWidowFragment.show(member);
                containerBigVideoWindow.setVisibility(View.VISIBLE);
            } else {
                containerBigVideoWindow.setVisibility(View.GONE);
            }

        }
    }

    /**
     * 关闭全屏显示的窗口
     */
    private void closeFullSharedScreenFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fullScreenFragment = fragmentManager.findFragmentById(R.id.class_container_full_screen);
        // 判断当前共享显示区是否已处于全屏模式
        if (fullScreenFragment instanceof ClassShareScreenFragment) {
            ClassShareScreenFragment shareScreenFragment = (ClassShareScreenFragment) fullScreenFragment;
            shareScreenFragment.checkFullScreen(false);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 取消 toast 显示用 runnable
     * 声明变量用于 handler 移除回调使用
     */
    private Runnable toastDismissRunnable = new Runnable() {
        @Override
        public void run() {
            if (toastTv == null) return;
            toastTv.startAnimation(toastDismissAnim);
        }
    };

    @Override
    public void showToast(String content, long duration) {
        if (toastTv == null || TextUtils.isEmpty(content)) return;

        toastShowAnim.reset();
        toastDismissAnim.reset();
        handler.removeCallbacks(toastDismissRunnable);

        toastTv.setVisibility(View.VISIBLE);
        toastTv.setText(content);

        toastTv.startAnimation(toastShowAnim);
        if (duration == 0) {
            duration = TOAST_DEFAULT_DURATION;
        }
        handler.postDelayed(toastDismissRunnable, duration);
    }

    @Override
    public void showToast(String content) {
        showToast(content, TOAST_DEFAULT_DURATION);
    }

    @Override
    public void showToast(int resId, long duration) {
        showToast(getString(resId), duration);
    }

    @Override
    public void showToast(int resId) {
        showToast(resId, TOAST_DEFAULT_DURATION);
    }


    @Override
    public void onBackPressed() {
        // 关闭全屏显示的共享显示区
        closeFullSharedScreenFragment();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        hideNavigationBar();
    }

    @Override
    public void onKeyboardStateChanged(boolean isShown, final int height) {
        // 监听键盘弹出， 然后把布局抬起
//        if (containerIM.getVisibility() == View.VISIBLE) {
//            /*
//             * 监听软键盘弹出，当有软键盘时使输入框加入软键盘等高的间距
//             */
//            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) parentView.getLayoutParams();
//            if (isShown) {
//                layoutParams.bottomMargin = height;
//                parentView.setLayoutParams(layoutParams);
//                TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f, 0f, 0f);
//                translateAnimation.setRepeatCount(0);
//                translateAnimation.setDuration(500);
//                translateAnimation.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//                        layoutParams.bottomMargin = height;
//                        parentView.setLayoutParams(layoutParams);
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//
//                    }
//                });
//                parentView.startAnimation(translateAnimation);
//            } else {
//                layoutParams.bottomMargin = 0;
//                parentView.setLayoutParams(layoutParams);
//            }
//        }

        hideNavigationBar();
    }
}

