package cn.rongcloud.sealclass.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.DeviceChange;
import cn.rongcloud.sealclass.model.ScreenDisplay;
import cn.rongcloud.sealclass.permission.ClassPermission;
import cn.rongcloud.sealclass.ui.VideoViewManager;
import cn.rongcloud.sealclass.ui.view.ClassVideoListItem;
import cn.rongcloud.sealclass.ui.view.RadioRtcVideoView;
import cn.rongcloud.sealclass.utils.log.SLog;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;

/**
 * 大窗口界面
 */
public class ClassBigVideoWindowFragment extends BaseFragment {

    private ClassVideoListItem videoViewItem;
    private ClassMember member;
    private OnWindowCloseListener listener;

    private static final String TAG = "big_video";
    private View rootView;
    private ClassViewModel classViewModel;

    @Override
    protected int getLayoutResId() {
        return R.layout.class_fragment_big_video_window;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        SLog.d(TAG, "Init view");
        rootView = findView(R.id.class_big_window_root);
        videoViewItem = findView(R.id.class_video_view_item);
        videoViewItem.setVideViewToCenter();
        ImageButton button = findView(R.id.class_btn_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SLog.d(TAG, "close big window");
                if (member != null) {
                    RadioRtcVideoView videoView = VideoViewManager.getInstance().get(member.getUserId());
                    if (videoView != null) {
                        videoView.setZOrderOnTop(false);
                        videoView.setZOrderMediaOverlay(false);
                    }
                }

                videoViewItem.clear();
                if (listener != null) {
                    listener.onClosed(member);
                }
                member = null;
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    @Override
    protected void onInitViewModel() {
        super.onInitViewModel();
        classViewModel = ViewModelProviders.of(getActivity()).get(ClassViewModel.class);
        // 设备状态变化
        classViewModel.getDeviceChange().observe(this, new Observer<DeviceChange>() {
            @Override
            public void onChanged(DeviceChange deviceChange) {
                if (member != null && member.getUserId().equals(deviceChange.userId) && !deviceChange.isEnable) {
                    RadioRtcVideoView videoView = VideoViewManager.getInstance().get(member.getUserId());
                    videoView.clearScreen();
                }
            }
        });

        // 角色变化
        classViewModel.getRoleChangeUser().observe(this, new Observer<ClassMember>() {
            @Override
            public void onChanged(ClassMember classMember) {
                // 当用户的角色发生改变， 并没有了音视频权限， 则清理其最后一帧
                if (member != null && member.getUserId().equals(classMember.getUserId())) {
                    if (!classMember.getRole().hasPermission(ClassPermission.VIDEO_CHAT)) {
                        RadioRtcVideoView videoView = VideoViewManager.getInstance().get(member.getUserId());
                        if (videoView != null) {
                            videoView.clearScreen();
                        }
                    }
                }
            }
        });

        // 用户被踢
        classViewModel.getKickedOff().observe(this, new Observer<ClassMember>() {
            @Override
            public void onChanged(ClassMember classMember) {
                // 当用户被踢的时候， 则清理最后一帧
                if (member != null && member.getUserId().equals(classMember.getUserId())) {
                    RadioRtcVideoView videoView = VideoViewManager.getInstance().get(member.getUserId());
                    if (videoView != null) {
                        videoView.clearScreen();
                    } else {
                        videoViewItem.clear();
                    }
                }
            }
        });
    }

    private LinearLayout.LayoutParams getItemLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return params;
    }

    /**
     * 设置窗口监听
     * @param listener
     */
    public void setOnWindowCloseListener(OnWindowCloseListener listener) {
        this.listener = listener;
    }

    /**
     *  显示窗口操作
     */
    public void show(ClassMember member) {
        RadioRtcVideoView videoView = VideoViewManager.getInstance().get(member.getUserId());

        // 当有新的用户显示，要移除旧用户的， 并要通知视频列表刷新
        if (videoViewItem.getChildCount() > 0 && !videoViewItem.hasVideoView(videoView)) {
            videoViewItem.removeVideoView();
            if (listener != null) {
                listener.onUpdate(this.member);
            }
        }

        this.member = member;
        videoViewItem.setData(null);
        if (videoView.isHasParent()) {
            ((ViewGroup) videoView.getParent()).removeAllViews();
        }
        videoViewItem.addVideoView(videoView, getItemLayoutParams());
        videoView.setZOrderOnTop(true);
        videoView.setZOrderMediaOverlay(true);

        if (isShowVideo()) {
            rootView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        } else {
            rootView.setBackgroundColor(getResources().getColor(R.color.colorClassMainBg));
        }
    }


    /**
     * 判断当前共享区域显示类型
     * @return
     */
    public boolean isShowVideo() {
        if (classViewModel == null) {
            return false;
        }
        ScreenDisplay value = classViewModel.getDisplay().getValue();
        if (value == null) {
            return false;
        }
        if (value.getType() != ScreenDisplay.Display.ASSISTANT && value.getType() != ScreenDisplay.Display.LECTURER) {
            return false;
        }

        return true;
    }

    /**
     * 设置选择监听
     */
    public interface OnWindowCloseListener {
        void onClosed(ClassMember member);

        void onUpdate(ClassMember member);
    }
}
