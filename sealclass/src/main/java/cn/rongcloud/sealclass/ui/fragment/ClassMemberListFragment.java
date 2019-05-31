package cn.rongcloud.sealclass.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.common.ErrorCode;
import cn.rongcloud.sealclass.common.ShowToastObserver;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.model.SpeechResult;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.ui.adapter.ClassMemberListAdapter;
import cn.rongcloud.sealclass.ui.dialog.CommonDialog;
import cn.rongcloud.sealclass.ui.view.ClassItemMemberItem;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;
import cn.rongcloud.sealclass.model.RequestState;

public class ClassMemberListFragment extends BaseFragment implements ClassMemberListAdapter.OnItemMemberClickListenr {
    private ClassMemberListAdapter adapter;
    private ClassViewModel classViewModel;
    private String roomId;
    private ListView lvMemberList;

    @Override
    protected int getLayoutResId() {
        return R.layout.class_fragment_member_list;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        lvMemberList = findView(R.id.class_lv_member_list);
        adapter = new ClassMemberListAdapter();
        adapter.setOnItemMemberClickListenr(this);
        lvMemberList.setAdapter(adapter);
    }


    @Override
    protected void onClick(View v, int id) {

    }

    @Override
    protected void onInitViewModel() {

        classViewModel = ViewModelProviders.of(getActivity()).get(ClassViewModel.class);
        classViewModel.getUserInfo().observe(this, new Observer<UserInfo>() {
            @Override
            public void onChanged(UserInfo userInfo) {
                adapter.setCurrentUser(userInfo);
                adapter.notifyDataSetChanged();
            }
        });

        classViewModel.getMemberList().observe(this, new Observer<List<ClassMember>>() {
            @Override
            public void onChanged(List<ClassMember> classMembers) {
                adapter.setListData(classMembers);
                adapter.notifyDataSetChanged();
            }
        });

        classViewModel.getRoomId().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String id) {
                roomId = id;
            }
        });

        classViewModel.getSpeechResult().observe(this, new Observer<SpeechResult>() {
            @Override
            public void onChanged(SpeechResult speechResult) {
                adapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public void onClick(ClassItemMemberItem itemView, ClassItemMemberItem.OperateType type, ClassMember member) {

        if (type == ClassItemMemberItem.OperateType.APPLY_SPEECH) {
            applySpeech(itemView, getRoomId());
        } else {
            showMemListOperteDialog(type, member);
        }
    }

    @Override
    public void onExpandViewStatus(ClassMember member, int position, boolean expand) {
        if (lvMemberList != null) {
            int lastVisiblePosition = lvMemberList.getLastVisiblePosition();
            // 如果是最后一个可见item， 则让其显示全
            if (position == lastVisiblePosition) {
                lvMemberList.smoothScrollToPosition(lastVisiblePosition);
            }
        }
    }

    private void showMemListOperteDialog(final ClassItemMemberItem.OperateType type, final ClassMember member) {
        String[] stringArray = getResources().getStringArray(R.array.class_member_list_operate_dialog_content);
        String content = stringArray[type.getValue()];
        final CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(content);
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                switch (type) {
                    case TRANSFER_ROLE:
                        transferRole(getRoomId(), member.getUserId());
                        break;
                    case UPGRAGE_TO_LECTURER:
                        changeRole(getRoomId(), member.getUserId(),  Role.LECTURER.getValue());
                        break;
                    case DOWNGRADE:
                        downgradeMember(getRoomId(), member);
                        break;
                    case UPGRADE:
                        upgradeIntive(getRoomId(), member.getUserId(), Role.STUDENT.getValue());
                        break;
                    case APPLY_OPEN_MIC: //
                        controlMemberMic(getRoomId(), member.getUserId(), true);
                        break;
                    case CLOSE_MIC:
                        controlMemberMic(getRoomId(), member.getUserId(), false);
                        break;
                    case APPLY_OPEN_CAMERA:
                        controlMemberCamera(getRoomId(), member.getUserId(), true);
                        break;
                    case CLOSE_CAMERA:
                        controlMemberCamera(getRoomId(), member.getUserId(), false);
                        break;
                    case KICK_OFF:
                        kickOffMember(getRoomId(), member.getUserId(), member.getUserName());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {

            }
        });
        CommonDialog dialog = builder.build();
        dialog.show(getFragmentManager(), "mic_dialog");
    }

    private String getRoomId() {
        return roomId;
    }


    //operate
    //踢出成员操作
    private void kickOffMember(String roomId, final String userId, final String userName) {
        if (classViewModel != null) {
            classViewModel.kickOff(roomId, userId).observe(this, new ShowToastObserver(getActivity()) {
                @Override
                public void onChanged(RequestState requestState) {
                    super.onChanged(requestState);
                    if (requestState.getState() == RequestState.State.SUCCESS) {
                        showToast(getString(R.string.toast_you_kick_off_one_from_class_format, userName));
                    }
                }
            });
        }
    }


    //控制成员摄像头设备
    private void controlMemberCamera(String roomId, String userId, boolean cameraOn) {
        if (classViewModel != null) {
            classViewModel.controlCamera(roomId, userId, cameraOn).observe(this, new ShowToastObserver(getActivity()) {
                @Override
                public void onChanged(RequestState requestState) {
                    super.onChanged(requestState);
                    if (requestState.getState() == RequestState.State.SUCCESS) {

                    }
                }
            });
        }
    }

    //控制成员语音设备
    private void controlMemberMic(String roomId, String userId, boolean microphoneOn) {
        if (classViewModel != null) {
            classViewModel.controlMicrophone(roomId, userId, microphoneOn).observe(this, new ShowToastObserver(getActivity()) {
                @Override
                public void onChanged(RequestState requestState) {
                    super.onChanged(requestState);
                    if (requestState.getState() == RequestState.State.SUCCESS) {

                    }
                }
            });
        }
    }

    //降级
    private void downgradeMember(String roomId, ClassMember member) {
        if (classViewModel != null) {
            List<ClassMember> members = new ArrayList<>();
            members.add(member);
            classViewModel.downgrade(roomId, members).observe(this, new ShowToastObserver(getActivity()) {
                @Override
                public void onChanged(RequestState requestState) {
                    super.onChanged(requestState);
                    if (requestState.getState() == RequestState.State.SUCCESS) {

                    }
                }
            });
        }
    }

    //申请发言
    private void applySpeech(final ClassItemMemberItem view, String roomId) {
        if (classViewModel != null) {
            classViewModel.applySpeech(roomId).observe(this, new ShowToastObserver(getActivity()) {
                @Override
                public void onChanged(RequestState requestState) {
                    super.onChanged(requestState);

                }
            });
        }
    }

    //转移角色
    private void transferRole(String roomId, String userId) {
        if (classViewModel != null) {
            classViewModel.transferRole(roomId, userId).observe(this, new ShowToastObserver(getActivity()) {
                @Override
                public void onChanged(RequestState requestState) {
                    super.onChanged(requestState);
                    if (requestState.getState() == RequestState.State.SUCCESS) {

                    }
                }
            });
        }
    }

    //升级为讲师
    private void changeRole(String roomId, String userId, int role) {
        if (classViewModel != null) {
            classViewModel.changeRole(roomId, userId, role).observe(this, new ShowToastObserver() {
                @Override
                public void onChanged(RequestState requestState) {
                    super.onChanged(requestState);
                    if (requestState.getState() == RequestState.State.SUCCESS) {

                    }
                }
            });
        }
    }

    //请求升级
    private void upgradeIntive(String roomId, String userId, int role) {
        if (classViewModel != null) {
            classViewModel.upgradeIntive(roomId, userId, role).observe(this, new ShowToastObserver() {
                @Override
                public void onChanged(RequestState requestState) {
                    super.onChanged(requestState);
                    if (requestState.getState() == RequestState.State.SUCCESS) {

                    }
                }
            });
        }
    }

}
