package cn.rongcloud.sealclass.im.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.Role;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.adapter.MessageListAdapter;
import io.rong.imlib.model.Message;

/**
 * 自定义消息列表
 * 修改了头像的显示规则
 */
public class ClassMessageListAdapter extends MessageListAdapter {
    private List<ClassMember> classMemberInfoList;

    public ClassMessageListAdapter(Context context) {
        super(context);
    }

    @Override
    protected void bindView(View v, int position, UIMessage data) {
        super.bindView(v, position, data);

        TextView leftAvatar = v.findViewById(R.id.rc_left_custom);
        TextView rightAvatar = v.findViewById(R.id.rc_right_custom);

        leftAvatar.setVisibility(View.GONE);
        rightAvatar.setVisibility(View.GONE);

        if (data == null) {
            return;
        }
        final ViewHolder holder = (ViewHolder) v.getTag();
        if (holder != null && holder.rightIconView.getVisibility() == View.GONE && holder.leftIconView.getVisibility() == View.GONE) {
            // 当原逻辑不显示头像时，不进行头像更新
            return;
        }
        holder.rightIconView.setVisibility(View.INVISIBLE);
        holder.leftIconView.setVisibility(View.INVISIBLE);

        ClassMember memberInfo = getUserInfo(data.getSenderUserId());

        String name = "";
        // 先从用户成员列表中取得姓名
        if (memberInfo != null) {
            name = memberInfo.getUserName();
        } else {
            // 因同步失败等情况没有从获得成员信息时从消息携带的用户信息获取用户信息
            io.rong.imlib.model.UserInfo userInfo = data.getUserInfo();
            RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
            if (userInfo == null) {
                // 当消息中没有携带用户信息时（如自己发送的消息），从用户管理中获取用户信息
                userInfo = RongUserInfoManager.getInstance().getUserInfo(data.getSenderUserId());
            }

            if (userInfo != null) {
                name = userInfo.getName();
            }
        }

        if (data.getMessageDirection() == Message.MessageDirection.SEND) {
            rightAvatar.setVisibility(View.VISIBLE);
            rightAvatar.setBackgroundResource(getAvatarResource(data.getSenderUserId()));
            holder.nameView.setVisibility(View.VISIBLE);
            holder.nameView.setText(name);
            if (!TextUtils.isEmpty(name)) {
                // 取最后一个字为头像中的文字
                rightAvatar.setText(name.substring(name.length() - 1));
            } else {
                rightAvatar.setText("");
            }
        } else {
            leftAvatar.setBackgroundResource(getAvatarResource(data.getSenderUserId()));
            leftAvatar.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(name)) {
                // 取最后一个字为头像中的文字
                leftAvatar.setText(name.substring(name.length() - 1));
            } else {
                leftAvatar.setText("");
            }
        }
    }

    /**
     * 设置当前所有房间内成员列表，用户更新在聊天中的信息
     *
     * @param classMemberInfoList
     */
    public void setClassMemberInfoList(List<ClassMember> classMemberInfoList) {
        this.classMemberInfoList = classMemberInfoList;
        notifyDataSetChanged();
    }

    /**
     * 根据发信者的用户 id 找出当前用户的用户信息，根据用户的角色返回相应的头像背景
     *
     * @param userId
     * @return
     */
    private int getAvatarResource(String userId) {
        int resourceId = R.drawable.class_portrait_listener;
        if (classMemberInfoList != null) {
            for (ClassMember info : classMemberInfoList) {
                String infoId = info.getUserId();
                if (infoId != null && infoId.equals(userId)) {
                    Role role = info.getRole();
                    switch (role) {
                        case LISTENER:
                            resourceId = R.drawable.class_portrait_listener;
                            break;
                        case STUDENT:
                            resourceId = R.drawable.class_portrait_student;
                            break;
                        case LECTURER:
                            resourceId = R.drawable.class_portrait_lecturer;
                            break;
                        case ASSISTANT:
                            resourceId = R.drawable.class_portrait_assistant;
                            break;
                        default:
                            resourceId = R.drawable.class_portrait_listener;
                    }
                    break;
                }
            }
        }
        return resourceId;
    }

    private ClassMember getUserInfo(String userId) {
        if (classMemberInfoList != null) {
            for (ClassMember info : classMemberInfoList) {
                String infoId = info.getUserId();
                if (infoId != null && infoId.equals(userId)) {
                    return info;
                }
            }
        }
        return null;
    }
}
