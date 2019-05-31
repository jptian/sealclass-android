package cn.rongcloud.sealclass.ui.fragment;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.common.ShowToastObserver;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.FirstFrameUserInfo;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.permission.ClassPermission;
import cn.rongcloud.sealclass.ui.VideoViewManager;
import cn.rongcloud.sealclass.ui.dialog.CommonDialog;
import cn.rongcloud.sealclass.ui.widget.OnOperateItemListener;
import cn.rongcloud.sealclass.ui.widget.OperateButtonGroupView;
import cn.rongcloud.sealclass.ui.widget.OperateItem;
import cn.rongcloud.sealclass.utils.DisplayUtils;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;
import cn.rongcloud.sealclass.model.RequestState;

/**
 * 顶部控制按钮布局。
 */
public class ClassTopOperateControlFragment extends BaseFragment {

    private ClassViewModel classViewModel;
    private String roomId;
    private UserInfo userInfoValue;
    private boolean isStartVideo = false;

    public enum TopOperate { // 控制按钮类型
        TO_VERTICAL(0),
        TO_HORIZONTAL(1),
        /**
         * 切花摄像头
         */
        CHANGE_CAMERA(2),
        /**
         * 开关mic
         */
        MIC(3),
        /**
         * 开关摄像头
         */
        CMAERA(4),
        /**
         * 开关外放
         */
        BANED_SOUND(5),
        /**
         * 退出
         */
        HANGUP(6);

        private int value;

        TopOperate(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static TopOperate getType(int value) {
            for (TopOperate type : TopOperate.values()) {
                if (value == type.getValue()) {
                    return type;
                }
            }
            return HANGUP;
        }
    }

    // 按钮的资源布局
    private int[] btnBgResIds = new int[]{
            R.drawable.class_fragment_top_operate_screen_to_vertical_selector,
            R.drawable.class_fragment_top_operate_screen_to_horizontal_selector,
            R.drawable.class_fragment_top_operate_camera_change_selector,
            R.drawable.class_fragment_top_operate_mic_selector,
            R.drawable.class_fragment_top_operate_camera_selector,
            R.drawable.class_fragment_top_operate_sound_selector,
            R.drawable.class_fragment_top_operate_hangup_selector

    };

    private OperateButtonGroupView operateView;

    @Override
    protected int getLayoutResId() {
        return R.layout.class_fragment_top_operate_control;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        operateView = findView(R.id.class_top_operate_view);

        // 初始化动态添加控制按钮
        List<OperateItem> items = new ArrayList<>();
        int value = DisplayUtils.dip2px(getContext(), 22);
        int marginLeft = DisplayUtils.dip2px(getContext(), 20);
        for (TopOperate operate : TopOperate.values()) {
            OperateItem item = new OperateItem();
            item.id = operate.getValue();
            item.bgResId = btnBgResIds[operate.getValue()];
            item.width = value;
            item.height = value;
            item.left = marginLeft;
            if (operate == TopOperate.MIC || operate == TopOperate.CMAERA || operate == TopOperate.BANED_SOUND) {
                item.type = OperateItem.Type.CHECKBOX;
            } else {
                item.type = OperateItem.Type.BUTTON;
            }
            items.add(item);
        }

        // 初始化操作布局
        operateView.initView(items, new OnOperateItemListener() {
            @Override
            public void onItemClicked(View v, OperateItem item) {
                TopOperate type = TopOperate.getType(item.id);
                switch (type) {
                    case CHANGE_CAMERA:
                        classViewModel.switchCamera();
                        break;
                    case HANGUP:
                        showExitDialog();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onCheckedChanged(View v, OperateItem item, boolean isChecked) {
                TopOperate type = TopOperate.getType(item.id);
                switch (type) {
                    case CMAERA:
                        classViewModel.setLocalVideoEnable(!isChecked);
                        deviceSyncCamera(getRoomId(), !isChecked);
                        break;
                    case MIC:
                        classViewModel.setLocalMicEnable(!isChecked);
                        deviceSyncMic(getRoomId(), !isChecked);
                        break;
                    case BANED_SOUND:
                        //true 禁止， false 不禁止
                        AudioManager audioMgr = (AudioManager) getContext().getSystemService("audio");
                        if (!isChecked) {
                            audioMgr.setMode(AudioManager.MODE_NORMAL);
                            audioMgr.setSpeakerphoneOn(true);
                        } else {
                            audioMgr.setSpeakerphoneOn(false);
                            audioMgr.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        operateView.setGravity(Gravity.CENTER);
        operateView.setOrientation(LinearLayout.HORIZONTAL);
        operateView.setItemVisibility(TopOperate.TO_VERTICAL.getValue(), View.GONE);
        operateView.setItemVisibility(TopOperate.TO_HORIZONTAL.getValue(), View.GONE);
        operateView.getView(TopOperate.CHANGE_CAMERA.getValue()).setEnabled(false);
        operateView.getView(TopOperate.MIC.getValue()).setEnabled(false);
        operateView.getView(TopOperate.CMAERA.getValue()).setEnabled(false);
        operateView.getView(TopOperate.BANED_SOUND.getValue()).setEnabled(false);
    }

    @Override
    protected void onInitViewModel() {

        classViewModel = ViewModelProviders.of(getActivity()).get(ClassViewModel.class);
        classViewModel.getRoomId().observe(this, new Observer<String>() {

            @Override
            public void onChanged(String s) {
                roomId = s;
            }
        });

        // 自己用户信息
        classViewModel.getUserInfo().observe(this, new Observer<UserInfo>() {
            @Override
            public void onChanged(UserInfo userInfo) {
                if (userInfo != null) {
                    userInfoValue = userInfo;
                    setViewStatus(userInfo, isStartVideo);
                }
            }
        });

        // 踢人监听，操作自己被踢退出课堂
        classViewModel.getKickedOff().observe(this, new Observer<ClassMember>() {
            @Override
            public void onChanged(ClassMember member) {
                UserInfo userInfoValue = classViewModel.getUserInfo().getValue();
                if (userInfoValue.getUserId().equals(member.getUserId())) {
                    leaveRoom(getRoomId());
                }
            }
        });

        // 当前用户启动音视频
        classViewModel.getLocalUserStartVideoChat().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean start) {
                if (isStartVideo != start) {
                    isStartVideo = start;
                    setViewStatus(userInfoValue, isStartVideo);
                    classViewModel.setEnableSpeakerphone(true);
                }
            }
        });

        //由于有时因为网络问题， 导致心里延时报错， 导致StartVideoChat 没有回调。
        classViewModel.getFisrtFrameDraw().observe(this, new Observer<FirstFrameUserInfo>() {
            @Override
            public void onChanged(FirstFrameUserInfo firstFrameUserInfo) {
                if (!isStartVideo && classViewModel.getUserInfo().getValue().getUserId().equals(firstFrameUserInfo.getUserId())) {
                    isStartVideo = true;
                    setViewStatus(userInfoValue, isStartVideo);
                    classViewModel.setEnableSpeakerphone(true);
                }
            }
        });

    }

    // 设置控制按钮的状态
    private void setViewStatus(UserInfo userInfo, boolean isStartVideo) {

        if (userInfo == null) {
            return;
        }

        boolean hasVideoPermssion = userInfo.getRole().hasPermission(ClassPermission.VIDEO_CHAT);
        boolean hasAudioPermssion = userInfo.getRole().hasPermission(ClassPermission.AUDIO_CHAT);

        // 首先判断是否有音视频权限
        // 然后判断用户的音视频状态， 如果是关闭的则直接显示关闭
        if (hasVideoPermssion && isStartVideo) {
            operateView.setItemEnabled(TopOperate.CMAERA.getValue(), true);
            CheckBox banedCameraBox = operateView.getView(TopOperate.CMAERA.getValue());
            // 因为按钮 false 是否代表不禁止， true 待变禁止， 所以和 用户信息携带的状态值相反
            if (banedCameraBox.isChecked() == userInfo.isCamera()) {
                banedCameraBox.setChecked(!userInfo.isCamera());
            }
            operateView.getView(TopOperate.CHANGE_CAMERA.getValue()).setEnabled(true);
        } else {
            operateView.setItemEnabled(TopOperate.CMAERA.getValue(), false);
            operateView.getView(TopOperate.CHANGE_CAMERA.getValue()).setEnabled(false);
        }

        if (hasAudioPermssion && isStartVideo) {
            operateView.setItemEnabled(TopOperate.MIC.getValue(), true);
            CheckBox banedMicBox = operateView.getView(TopOperate.MIC.getValue());

            // 因为按钮 false 是否代表不禁止， true 待变禁止， 所以和 用户信息携带的状态值相反
            if (banedMicBox.isChecked() == userInfo.isMicrophone()) {
                banedMicBox.setChecked(!userInfo.isMicrophone());
            }
        } else {
            operateView.setItemEnabled(TopOperate.MIC.getValue(), false);
        }

        operateView.getView(TopOperate.BANED_SOUND.getValue()).setEnabled(true);

    }

    private String getRoomId() {
        return roomId;
    }

    // 退出课堂dialog
    private void showExitDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.class_dialog_leave_room_content));
        builder.setButtonText(R.string.class_dialog_leave_room_positive_text, R.string.class_dialog_leave_room_negative_text);
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                leaveRoom(getRoomId());
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {

            }
        });
        CommonDialog dialog = builder.build();
        dialog.show(getFragmentManager(), "leave_dialog");
    }

    // 离开房间操作
    private void leaveRoom(final String roomId) {
        VideoViewManager.getInstance().clear();
        if (classViewModel != null) {
            classViewModel.leaveRoom(roomId).observe(this, new ShowToastObserver(getActivity()) {
                @Override
                public void onChanged(RequestState requestState) {
                    super.onChanged(requestState);

                    if (requestState.getState() != RequestState.State.LOADING) {
                        classViewModel.quitRtcRoom(roomId).observe(ClassTopOperateControlFragment.this, new Observer<RequestState>() {
                            @Override
                            public void onChanged(RequestState requestState) {
                                getActivity().finish();
                            }
                        });
                    }

                }
            });
        }
    }

    // 同步摄像头方法
    private void deviceSyncCamera(String roomId, boolean on) {
        if (classViewModel != null) {
            classViewModel.deviceSyncCamera(roomId, on);
        }
    }

    // 同步话筒状态
    private void deviceSyncMic(String roomId, boolean on) {
        if (classViewModel != null) {
            classViewModel.deviceSyncMic(roomId, on);
        }
    }

}
