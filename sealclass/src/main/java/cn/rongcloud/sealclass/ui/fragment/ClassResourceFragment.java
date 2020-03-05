package cn.rongcloud.sealclass.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.common.ShowToastObserver;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.FirstFrameUserInfo;
import cn.rongcloud.sealclass.model.RequestState;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.model.ScreenDisplay;
import cn.rongcloud.sealclass.model.UserDisplayResource;
import cn.rongcloud.sealclass.model.WhiteBoard;
import cn.rongcloud.sealclass.ui.VideoViewManager;
import cn.rongcloud.sealclass.ui.adapter.ClassResourceListAdapter;
import cn.rongcloud.sealclass.ui.view.RadioRtcVideoView;
import cn.rongcloud.sealclass.utils.log.SLog;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;

public class ClassResourceFragment extends BaseFragment {
    private ListView resourceListLv;
    private ClassResourceListAdapter resourceListAdapter;
    private ClassViewModel classViewModel;

    @Override
    protected int getLayoutResId() {
        return R.layout.class_fragment_resource;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        resourceListLv = getView().findViewById(R.id.class_lv_resource_list);
        resourceListAdapter = new ClassResourceListAdapter();
        resourceListLv.setAdapter(resourceListAdapter);
        //点击监听
        resourceListLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = resourceListAdapter.getItem(position);
                if (item instanceof WhiteBoard) {   //点击白板
                    WhiteBoard whiteBoard = (WhiteBoard) item;

                    classViewModel.switchDisplay(classViewModel.getRoomId().getValue(), ScreenDisplay.Display.WHITEBOARD, whiteBoard.getWhiteboardId()).observe(ClassResourceFragment.this, new Observer<RequestState>() {
                        @Override
                        public void onChanged(RequestState requestState) {
                            if (requestState.getState() == RequestState.State.FAILED) {
                                showToast(requestState.getErrorCode().getMessageResId());
                            }
                        }
                    });
                } else if (item instanceof UserDisplayResource) {    // 点击用户视频资源
                    UserDisplayResource userDisplay = (UserDisplayResource) item;
                    ClassMember member = userDisplay.getClassMember();
                    // 判断是否有用户 id ，没有用户 id 代表是空的占位资源
                    if (member.getUserId() != null) {
                        ScreenDisplay.Display type = null;
                        Role role = member.getRole();
                        // 根据用户角色对应到共享画布显示类型
                        switch (role) {
                            case LECTURER:
                                type = ScreenDisplay.Display.LECTURER;
                                break;
                            case ASSISTANT:
                                type = ScreenDisplay.Display.ASSISTANT;
                                break;
                        }
                        if (type != null) {
                            // 切换共享区域显示
                            classViewModel.switchDisplay(classViewModel.getRoomId().getValue(), type, member.getUserId())
                                    .observe(ClassResourceFragment.this, new ShowToastObserver(getActivity()));
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onClick(View v, int id) {

    }

    @Override
    protected void onInitViewModel() {
        classViewModel = ViewModelProviders.of(getActivity()).get(ClassViewModel.class);
        // 获取白板列表
        classViewModel.getWhiteBoardList().observe(this, new Observer<List<WhiteBoard>>() {
            @Override
            public void onChanged(List<WhiteBoard> whiteBoards) {
                resourceListAdapter.setWhiteBoardList(whiteBoards);
            }
        });

        // 获取助教或讲师
        classViewModel.getAssistantAndLecturerList().observe(this, new Observer<List<ClassMember>>() {
            @Override
            public void onChanged(List<ClassMember> classMembers) {
                if (classMembers != null && classMembers.size() > 0) {
                    addUserDisplayResource(classMembers);
                }
            }
        });

//        // 目前由于 RTC 提供一帧方法不稳定， 所以注释掉
//        classViewModel.getFisrtFrameDraw().observe(this, new Observer<FirstFrameUserInfo>() {
//            @Override
//            public void onChanged(FirstFrameUserInfo firstFrameUserInfo) {
//                if (resourceListAdapter == null) {
//                    return;
//                }
//                final List<UserDisplayResource> value = resourceListAdapter.getUserDisplayResource();
//                if (value == null) {
//                    return;
//                }
//
//                for (final UserDisplayResource userDisplay : value) {
//                    if (firstFrameUserInfo.getUserId().equals(userDisplay.getClassMember().getUserId())) {
//                        RadioRtcVideoView videoView = VideoViewManager.getInstance().get(userDisplay.getClassMember().getUserId());
//                        if (videoView == null) {
//                            break;
//                        }
//                        videoView.getFrame(new RadioRtcVideoView.OnFrameCallback() {
//
//                            @Override
//                            public void onFrame(final Bitmap bitmap) {
//                                SLog.d("ss_bitmap", "bitmap = " + bitmap);
//                                userDisplay.setScreenShotBitmap(bitmap);
//                                resourceListAdapter.setUserDisplayResource(value);
//                            }
//                        });
//                    }
//                }
//
//
//            }
//        });

    }

    // 更新列表
    private void addUserDisplayResource(List<ClassMember> members) {
        if (members == null) {
            members = classViewModel.getAssistantAndLecturerList().getValue();
        }

        List<UserDisplayResource> userList = new ArrayList<>();

        if (members != null) {
            for (ClassMember member : members) {
                UserDisplayResource userDisplayResource = new UserDisplayResource();
                userDisplayResource.setClassMember(member);
                userList.add(userDisplayResource);
            }
        }
        resourceListAdapter.setUserDisplayResource(userList);
    }


    // 获取更新一帧图像
    // 目前由于 RTC 提供一帧方法不稳定， 所以注释掉
    public void updateUserDisplayRes() {
        if (resourceListAdapter != null) {
            final List<UserDisplayResource> userDisplayResource = resourceListAdapter.getUserDisplayResource();
            updateUserDisplayResource(userDisplayResource);
        }
    }

    // 更新图像
    private void updateUserDisplayResource(final List<UserDisplayResource> userDisplayResource) {
        if (userDisplayResource != null) {
            for (final UserDisplayResource userDisplay : userDisplayResource) {
                RadioRtcVideoView videoView = VideoViewManager.getInstance().get(userDisplay.getClassMember().getUserId());
                if (videoView == null) {
                    break;
                }
                videoView.getFrame(new RadioRtcVideoView.OnFrameCallback() {

                    @Override
                    public void onFrame(final Bitmap bitmap) {
                        SLog.d("ss_bitmap", "bitmap = " + bitmap);
                        userDisplay.setScreenShotBitmap(bitmap);
                        resourceListAdapter.setUserDisplayResource(userDisplayResource);
                    }
                });
            }
        }
    }
}
