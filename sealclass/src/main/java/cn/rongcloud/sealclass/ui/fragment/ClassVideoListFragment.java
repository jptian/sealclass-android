package cn.rongcloud.sealclass.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.sealclass.Config;
import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.DeviceChange;
import cn.rongcloud.sealclass.model.DeviceType;
import cn.rongcloud.sealclass.model.RequestState;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.model.StreamResource;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.model.VideoClassMemberData;
import cn.rongcloud.sealclass.ui.VideoViewManager;
import cn.rongcloud.sealclass.ui.adapter.ClassVideoListAdapter;
import cn.rongcloud.sealclass.ui.view.ClassVideoListItem;
import cn.rongcloud.sealclass.ui.view.RadioRtcVideoView;
import cn.rongcloud.sealclass.utils.DisplayUtils;
import cn.rongcloud.sealclass.utils.log.SLog;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;

/**
 * 视频列表界面
 */
public class ClassVideoListFragment extends BaseFragment {

    private static final String TAG_VIDEO_VIEW = "video_view";

    private ListView videoList;
    private ClassViewModel classViewModel;
    private ClassVideoListAdapter classVideoListAdapter;
    private UserInfo userInfoValue;
    private OnVideoViewItemClickListener listener;

    @Override
    protected int getLayoutResId() {
        return R.layout.class_fragment_video_list;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        videoList = findView(R.id.class_video_list);
        classVideoListAdapter = new ClassVideoListAdapter();
        classVideoListAdapter.setOnUserUpdateListener(new ClassVideoListAdapter.OnUserUpdateListener() {
            @Override
            public void onUpdate(ClassVideoListItem view, int position, ClassMember oldMember, ClassMember newMember) {
                Log.d(TAG_VIDEO_VIEW, "Update video list item, position = " + position + ", view = " + view + "oldMember = " + oldMember + ", newMember = " + newMember);
                updateVideoItem(view, oldMember, newMember);
            }
        });
        videoList.setAdapter(classVideoListAdapter);
    }

    //清除当前listview 中的所有信息缓存以及 VideoView。
    private void clearVideoViewInListView() {
        int firstVisiblePosition = videoList.getFirstVisiblePosition();
        int childCount = videoList.getChildCount();
        for (int i = firstVisiblePosition; i < firstVisiblePosition + childCount; i++) {
            int index = i - firstVisiblePosition;
            View childAt = videoList.getChildAt(index);
            if (childAt != null) {
                ClassVideoListItem itemView = (ClassVideoListItem) childAt;
                itemView.clear();
            }
        }
    }

    //更新列表资源
    private void updateList(List<ClassMember> classMembers, boolean isListNoUpdate) {
        if (classVideoListAdapter != null) {
            if (!isListNoUpdate) {
                // 列表里的人没有发生变化， 则不需要清了数据。 但是数据的状态可能改变。
                // 例如摄像头状态等
                clearVideoViewInListView();
            }

            if (classMembers != null) {
                classVideoListAdapter.setListData(classMembers);
            }
            SLog.d(TAG_VIDEO_VIEW, "Notify list update , updateList");
            classVideoListAdapter.notifyDataSetChanged();
        }
    }

    // 视频列表布局刷新，此方法用于VideoView 的填充到 list 中的操作。
    private synchronized void updateVideoItem(final ClassVideoListItem view, final ClassMember oldMember, final ClassMember newMember) {
        if (view != null) {
            if (view.getVideoViewCount() > 0) {
                view.removeVideoView();
            }
        }
        // 当前 View 中的前一个持有的数据
        if (oldMember != null) {
            String oldUserId = oldMember.getUserId();
            if (oldUserId.equals("-1")) { // id 为 -1 ， 则意味着当前没有讲师。
                SLog.d(TAG_VIDEO_VIEW, "Update list item, old member ,lecturer is null");
            } else {
                // 动态订阅的是否才会使用
                if (Config.isDynamicSubscribe) {
                    if (!userInfoValue.getUserId().equals(oldUserId) && !oldMember.getUserId().equals(newMember.getUserId())) {
                        SLog.d(TAG_VIDEO_VIEW, "Update list item, remote unSubscribeVideo  =>" + oldUserId);
                        // 判断有没有此控件，有则高边控件的订阅状态
                        boolean contains = VideoViewManager.getInstance().containsKey(oldUserId);
                        if (contains) {
                            RadioRtcVideoView videoView = VideoViewManager.getInstance().get(oldUserId);
                            videoView.setBindVideo(false);
                        }
                        unSubscribeVideo(oldUserId);
                    }
                }
            }
        }

        // 当前布局要加载的数据
        if (newMember != null) {
            String userId = newMember.getUserId();
            if (userId.equals("-1")) {
                view.removeVideoView();
                view.contentText(R.string.class_video_list_lecturer_comming);
                view.setContentVisibility(View.VISIBLE);
                SLog.d(TAG_VIDEO_VIEW, "Update list item, new member, lecturer is null, return");
                return;
            }

            // 1. 是否有 VideoView
            // 布局展示不负责创建 VideoView， 是根据 RTC 的回调来创建的
            boolean contains = VideoViewManager.getInstance().containsKey(userId);
            // 1.1 存在， 则进行下面流程， 不存在则直接移除
            if (contains) {
                final RadioRtcVideoView videoView = VideoViewManager.getInstance().get(userId);

                if (userInfoValue != null && !userInfoValue.getUserId().equals(userId)) {
                    // 教师控制别的人的摄像头， 刷新摄像头状态
                    if (!newMember.isCamera()) {
                        videoView.clearScreen();
                    }

                    SLog.d(TAG_VIDEO_VIEW, "Update list item, has video view in layout: " + videoView.isHasParent());
                    //2 当 VideoView 没有父容器时， 则可添加到容器中，否则就是被其他容器占用。例如共享区域
                    if (view != null && !videoView.isHasParent()) {
                        view.addVideoView(videoView, getItemLayoutParams());
                    }

                    // 动态分开订阅
                    if (Config.isDynamicSubscribe) {
                        // 3. VideoView 是否被绑定视频流和语音流
                        SLog.d(TAG_VIDEO_VIEW, "Update list item, video view bind stream: " + videoView.isBindVideo());
                        if (!videoView.isBindVideo()) {
                            // 3.1.1 远端用户
                            videoView.setBindVideo(true); // 标记已经绑定了流
                            subscribeVideo(userId, videoView);
                        }
                    }
                } else {
                    if (!newMember.isCamera()) {
                        videoView.clearScreen();
                    }
                    SLog.d(TAG_VIDEO_VIEW, "Update list item, local, has video view in layout: " + videoView.isHasParent());
                    // 1.2 VideoView 存在， 则要看其是否被父类容器使用。如果被使用， 则不添加到布局中。
                    if (view != null && !videoView.isHasParent()) {
                        // 由于本地用户可能是旁听者， 旁听者是没有音视频聊天权限的，所以就没有 VideoView
                        if (newMember.getRole() != Role.LISTENER) {
                            view.addVideoView(videoView, getItemLayoutParams());
                        } else {
                            view.removeVideoView();
                        }
                    }
                }
            } else {
                SLog.d(TAG_VIDEO_VIEW, "Update list item, Video view is null");
                // 1.2 不存在， 直接移除以前的布局。 被移除的布局可以其他的界面容器重写填充
                if (view != null) {
                    view.removeVideoView();
                }
            }

            if (newMember.getRole() == Role.LECTURER) {
                if (view.getVideoViewCount() <= 0) {
                    view.contentText(R.string.class_video_list_lecturer_content);
                    view.setContentVisibility(View.VISIBLE);
                } else {
                    view.contentText("");
                    view.setContentVisibility(View.GONE);
                }
            } else if (newMember.getRole() == Role.ASSISTANT) {
                if (view.getVideoViewCount() <= 0) {
                    view.contentText(R.string.class_video_list_Assistant_content);
                    view.setContentVisibility(View.VISIBLE);
                } else {
                    view.setContentVisibility(View.GONE);
                }
            } else {
                view.setContentVisibility(View.GONE);
            }
        }
    }


    @Override
    protected void onInitViewModel() {

        classViewModel = ViewModelProviders.of(getActivity()).get(ClassViewModel.class);

        // 用户信息
        classViewModel.getUserInfo().observe(this, new Observer<UserInfo>() {
            @Override
            public void onChanged(UserInfo userInfo) {
                SLog.d(SLog.TAG_RTC, "UserInfo, userInfo =" + userInfo);
                userInfoValue = userInfo;
            }
        });

        // 音视频进入房间后，就已经存在的用户
        classViewModel.getInitVideoList().observe(this, new Observer<List<StreamResource>>() {
            @Override
            public void onChanged(List<StreamResource> resources) {
                if (resources == null || resources.size() <= 0) {
                    return;
                }

                for (StreamResource resource : resources) {
                    String userId = resource.userId;
                    createVideoView(userId);
                    RadioRtcVideoView videoView = VideoViewManager.getInstance().get(userId);
                    if (Config.isDynamicSubscribe) {
                        // 动态订阅视频，
                        // 只管创建视频videoview， 并订阅音频流， 不管布局加载和视频流的订阅
                        if (!videoView.isSubscribeAudio()) {
                            videoView.setSubscribeAudio(true);
                            subscribeAudio(userId);
                        }
                    } else {
                        // 非动态订阅视频
                        // 人员一加入就立即订阅
                        //只管创建视频videoview， 并订阅音视频流， 不管布局加载
                        if (resource.isHasVideo && resource.isHasScreen) { // 既有视频流， 又有桌面共享流
                            if (!videoView.isBindVideo()) {
                                videoView.setBindVideo(true); // 标记已经绑定了流
                                // 桌面共享的 VideoView 是否存在， 没有则创建
                                RadioRtcVideoView shareScreenVideoView = VideoViewManager.getInstance().getShareScreenVideoView();
                                if (shareScreenVideoView != null && !shareScreenVideoView.isBindVideo()) {
                                    subscribe(userId, videoView, shareScreenVideoView);
                                } else {
                                    subscribe(userId, videoView);
                                }
                            }
                        } else if (resource.isHasVideo) { // 有视频流
                            if (!videoView.isBindVideo()) {
                                videoView.setBindVideo(true); // 标记已经绑定了流
                                subscribe(userId, videoView);
                            }
                        } else if (resource.isHasScreen) { // 有桌面流
                            RadioRtcVideoView shareScreenRtcVideoView = VideoViewManager.getInstance().getShareScreenVideoView();
                            if (shareScreenRtcVideoView == null) {
                                shareScreenRtcVideoView = new RadioRtcVideoView(getActivity());
                                final RadioRtcVideoView finalShareScreenVideoView = shareScreenRtcVideoView;
                                shareScreenRtcVideoView.setOnSizeChangedListener(new RongRTCVideoView.OnSizeChangedListener() {
                                    @Override
                                    public void onChanged(RongRTCVideoView.Size size) {
                                        if (size != null) {
                                            float radio = (float) size.with / (float) size.height;
                                            SLog.d("video_size", "size == " + size.with + ", " + size.height + ", " + radio);
                                            // 设置显示区比例
                                            finalShareScreenVideoView.setRadio(radio);
                                        }
                                    }
                                });
                                VideoViewManager.getInstance().setShareScreenVideoView(shareScreenRtcVideoView);
                            }
                            if (!shareScreenRtcVideoView.isBindVideo()) {
                                subscribeScreen(userId, shareScreenRtcVideoView);
                            }
                        }
                    }
                }

                // 通知布局刷新
                if (classVideoListAdapter != null) { // 通知布局重写加载订阅
                    List<String> userIds = new ArrayList<>();
                    for (StreamResource resource : resources) {
                        userIds.add(resource.userId);
                    }
                    SLog.d(TAG_VIDEO_VIEW, "Notify list update, init videos = " + userIds);
                    classVideoListAdapter.notifyDataSetChanged(userIds);
                }
            }
        });

        // 有新用户加入音视频房间
        classViewModel.getVideoAddedUser().observe(this, new Observer<StreamResource>() {
            @Override
            public void onChanged(final StreamResource resource) {
                //只管创建视频videoview， 并订阅音频流， 不管布局加载和视频流的订阅
                String userId = resource.userId;
                createVideoView(userId);
                RadioRtcVideoView videoView = VideoViewManager.getInstance().get(userId);
                if (Config.isDynamicSubscribe) {
                    // 动态订阅视频，
                    // 只管创建视频videoview， 并订阅音频流， 不管布局加载和视频流的订阅
                    if (!videoView.isSubscribeAudio()) {
                        videoView.setSubscribeAudio(true);
                        subscribeAudio(userId);
                    }
                } else {
                    // 非动态订阅视频
                    // 人员一加入就立即订阅
                    //只管创建视频videoview， 并订阅音视频流， 不管布局加载
                    if (resource.isHasVideo && resource.isHasScreen) {
                        if (!videoView.isBindVideo()) {
                            videoView.setBindVideo(true); // 标记已经绑定了流
                            RadioRtcVideoView shareScreenVideoView = VideoViewManager.getInstance().getShareScreenVideoView();
                            if (shareScreenVideoView != null && !shareScreenVideoView.isBindVideo()) {
                                subscribe(userId, videoView, shareScreenVideoView);
                            } else {
                                subscribe(userId, videoView);
                            }
                        }
                    } else if (resource.isHasVideo) {
                        if (!videoView.isBindVideo()) {
                            videoView.setBindVideo(true); // 标记已经绑定了流
                            subscribe(userId, videoView);
                        } else {
                            videoView.setBindVideo(true); // 标记已经绑定了流
                            subscribe(userId, videoView);
                        }
                    } else if (resource.isHasScreen) {
                        RadioRtcVideoView shareScreenRtcVideoView = VideoViewManager.getInstance().getShareScreenVideoView();
                        if (shareScreenRtcVideoView == null) {
                            shareScreenRtcVideoView = new RadioRtcVideoView(getActivity());
                            final RadioRtcVideoView finalShareScreenVideoView = shareScreenRtcVideoView;
                            shareScreenRtcVideoView.setOnSizeChangedListener(new RongRTCVideoView.OnSizeChangedListener() {
                                @Override
                                public void onChanged(RongRTCVideoView.Size size) {
                                    if (size != null) {
                                        float radio = (float) size.with / (float) size.height;
                                        SLog.d("video_size", "size == " + size.with + ", " + size.height + ", " + radio);
                                        finalShareScreenVideoView.setRadio(radio);
                                    }
                                }
                            });
                            VideoViewManager.getInstance().setShareScreenVideoView(shareScreenRtcVideoView);
                        }
                        if (!shareScreenRtcVideoView.isBindVideo()) {
                            subscribeScreen(userId, shareScreenRtcVideoView);
                        }
                    }
                }

                List<String> userIds = new ArrayList<>();
                userIds.add(userId);
                if (classVideoListAdapter != null) { // 通知布局重写加载订阅
                    SLog.d(TAG_VIDEO_VIEW, "Notify list update, add view = " + userIds);
                    classVideoListAdapter.notifyDataSetChanged(userIds);
                }

            }
        });

        // 有用户退出音视频房间
        classViewModel.getVideoRemovedUser().observe(this, new Observer<StreamResource>() {
            @Override
            public void onChanged(StreamResource resource) {
                SLog.d(SLog.TAG_RTC, "Video User remove, userId = " + resource);
                // 加入音视频离开， 则取消音视频的订阅， 并清理 VideoView。 视频列表布局由 成员列表控制刷新
                if (!resource.isHasVideo && !resource.isHasAudio) {
                    unSubscribe(resource.userId);
                    removeVideoView(resource.userId);
                } else if (!resource.isHasScreen) {
                    VideoViewManager.getInstance().getShareScreenVideoView().setBindVideo(false);
                    unSubscribeScreen(resource.userId);
                }
            }
        });

        // 视频用户列表
        classViewModel.getVideoMembersList().observe(this, new Observer<VideoClassMemberData>() {
            @Override
            public void onChanged(VideoClassMemberData videoClassMemberData) {
                if (userInfoValue != null) {
                    updateList(videoClassMemberData.getMember(), videoClassMemberData.isNoUpdate());
                } else {
                    SLog.e(TAG_VIDEO_VIEW, " Members List, userinfo is null, no update list ");
                }
            }
        });

        // 设备状态变化
        classViewModel.getDeviceChange().observe(this, new Observer<DeviceChange>() {
            @Override
            public void onChanged(DeviceChange deviceChange) {
                RadioRtcVideoView videoView = VideoViewManager.getInstance().get(deviceChange.userId);
                if (videoView != null && deviceChange.deviceType == DeviceType.Camera) {
                    if (!deviceChange.isEnable) {
                        // 清了最后一帧
                        videoView.setAllowRenderer(false);
                        videoView.clearScreen();
                    } else {
                        videoView.setAllowRenderer(true);
                    }
                }
            }
        });

    }

    // 创建 VideoView
    private void createVideoView(String userId) {
        if (!VideoViewManager.getInstance().containsKey(userId)) {
            RadioRtcVideoView videoView = new RadioRtcVideoView(getContext());
            VideoViewManager.getInstance().put(userId, videoView);
        }
    }

    // 移除videoView
    private void removeVideoView(String userId) {
        if (VideoViewManager.getInstance().containsKey(userId)) {
            RadioRtcVideoView remoteVideoView = VideoViewManager.getInstance().remove(userId);
            remoteVideoView.setBindVideo(false);
            remoteVideoView.clearScreen();
        }
    }

    // 视频订阅
    private synchronized void startVideo(RadioRtcVideoView videoView, final ClassMember member) {
        if (classViewModel != null) {
            classViewModel.startRtcChat(videoView).observe(this, new Observer<RequestState>() {
                @Override
                public void onChanged(RequestState requestState) {
                    classViewModel.setLocalVideoEnable(member.isCamera());
                }
            });
        }
    }

    // 视频订阅
    private synchronized void subscribeVideo(String userId, final RadioRtcVideoView videoView) {
        if (classViewModel != null) {
            classViewModel.subscribeVideo(userId, videoView).observe(this, new Observer<RequestState>() {
                @Override
                public void onChanged(RequestState requestState) {
                    if (requestState.getState() == RequestState.State.FAILED) {
                        videoView.setBindVideo(false);
                    }
                }
            });
        }
    }

    // 取消订阅视频
    private synchronized void unSubscribeVideo(String userId) {
        if (classViewModel != null) {
            classViewModel.unSubscribeVideo(userId);
        }
    }

    // 音频订阅
    private synchronized void subscribeAudio(String userId) {
        if (classViewModel != null) {
            classViewModel.subscribeAudio(userId);
        }
    }

    // 取消音频订阅
    private synchronized void unSubscribeAudio(String userId) {
        if (classViewModel != null) {
            classViewModel.unSubscribeAudio(userId);
        }
    }

    // 订阅
    private synchronized void subscribe(String userId, final RadioRtcVideoView videoView) {
        if (classViewModel != null) {
            classViewModel.subscribeResource(userId, videoView).observe(this, new Observer<RequestState>() {
                @Override
                public void onChanged(RequestState requestState) {
                    if (requestState.getState() == RequestState.State.FAILED) {
                        videoView.setBindVideo(false);
                    } else if (requestState.getState() == RequestState.State.SUCCESS) {
                        SLog.d(TAG_VIDEO_VIEW, "Update list item, subVideo , has video view in layout: " + videoView.isHasParent() + ", videoview = " + videoView);
                    }
                }
            });
        }
    }

    // 订阅
    private synchronized void subscribe(String userId, final RadioRtcVideoView videoView, RadioRtcVideoView screenShareView) {
        if (classViewModel != null) {
            classViewModel.subscribeResource(userId, videoView, screenShareView).observe(this, new Observer<RequestState>() {
                @Override
                public void onChanged(RequestState requestState) {
                    if (requestState.getState() == RequestState.State.FAILED) {
                        videoView.setBindVideo(false);
                    } else if (requestState.getState() == RequestState.State.SUCCESS) {
                        SLog.d(TAG_VIDEO_VIEW, "Update list item, subVideo , has video view in layout: " + videoView.isHasParent() + ", videoview = " + videoView);
                    }
                }
            });
        }
    }

    // 取消订阅
    private synchronized void unSubscribe(String userId) {
        if (classViewModel != null) {
            classViewModel.unSubscribeResource(userId);
        }
    }

    // 订阅共享桌面
    private void subscribeScreen(String userId, final RadioRtcVideoView videoView) {
        if (classViewModel != null) {
            classViewModel.subscribeScreen(userId, videoView).observe(this, new Observer<RequestState>() {
                @Override
                public void onChanged(RequestState requestState) {
                    if (requestState.getState() == RequestState.State.FAILED) {
                        videoView.setBindVideo(false);
                    } else if (requestState.getState() == RequestState.State.SUCCESS) {

                    }
                }
            });
        }

    }

    // 取消订阅共享桌面
    private void unSubscribeScreen(String userId) {
        if (classViewModel != null) {
            classViewModel.unSubscribeScreen(userId);
        }
    }

    private LinearLayout.LayoutParams getItemLayoutParams() {
        int width = DisplayUtils.dip2px(getContext(), Math.round(84 * 1.333));
        int height = DisplayUtils.dip2px(getContext(), 84);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        return params;

    }

    /**
     * 刷新 listView ， 其他布局联动视频列表更新
     * @param member
     */
    public void refershUserVideoView(ClassMember member) {
        if (classVideoListAdapter != null && member != null) {
            List<String> userIds = new ArrayList<>();
            userIds.add(member.getUserId());
            SLog.d(TAG_VIDEO_VIEW, "Refersh list , " + userIds);
            classVideoListAdapter.notifyDataSetChanged(userIds);
        }
    }

    /**
     * 刷新 listView ， 其他布局联动视频列表更新
     * @param userIds
     */
    public void refershUserVideoView(List<String> userIds) {
        if (classVideoListAdapter != null && userIds != null && userIds.size() > 0) {
            SLog.d(TAG_VIDEO_VIEW, "Refersh list , " + userIds);
            classVideoListAdapter.notifyDataSetChanged(userIds);
        }
    }

    /**
     * 设置 Item 点击回调
     * @param listener
     */
    public void setOnVideoViewItemClickListener(OnVideoViewItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 点击监听回调
     */
    public interface OnVideoViewItemClickListener {
        void onItemClick(ClassMember member);
    }

}
