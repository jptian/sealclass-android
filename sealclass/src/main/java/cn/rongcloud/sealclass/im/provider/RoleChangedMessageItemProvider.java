package cn.rongcloud.sealclass.im.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.TextView;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.im.message.RoleSingleChangedMessage;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.model.RoleChangedUser;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;

/**
 * 用户角色变化模版
 */
@ProviderTag(messageContent = RoleSingleChangedMessage.class, showPortrait = false, centerInHorizontal = true, showProgress = false, showSummaryWithName = false)
public class RoleChangedMessageItemProvider extends BaseNotificationProvider<RoleSingleChangedMessage> {
    @Override
    public void bindView(TextView view, int i, RoleSingleChangedMessage roleChangedMessage, UIMessage uiMessage) {
        view.setText(getNotifyContent(view.getContext(), roleChangedMessage));
    }

    public String getNotifyContent(Context context, RoleSingleChangedMessage roleChangedMessage) {
        String content = "";

        RoleChangedUser user = roleChangedMessage.getUser();
        Role role;
        // 获取身份，当有多人时身份为统一变动
        role = user.getRole();
        String currentUserId = RongIM.getInstance().getCurrentUserId();

        // 姓名集合，当有多人改变身份时，拼接姓名
        StringBuilder userNameUnionBuilder = new StringBuilder();
        String nameSeparate = context.getString(R.string.mark_name_separate);

        String userName = user.getUserName();

        // 当自己时改变显示称呼
        if (currentUserId.equals(user.getUserId())) {
            userName = context.getString(R.string.you);
        }
        userNameUnionBuilder.append(userName).append(nameSeparate);

        String userNameUnion = userNameUnionBuilder.substring(0, userNameUnionBuilder.length() -1);
        // 根据身份不同显示不同的信息
        switch (role){
            case ASSISTANT:
                content = context.getString(R.string.class_conversation_notify_role_to_assistant, userNameUnion);
                break;
            case LECTURER:
                content = context.getString(R.string.class_conversation_notify_role_to_lecturer, userNameUnion);
                break;
            case STUDENT:
                content = context.getString(R.string.class_conversation_notify_role_to_student, userNameUnion);
                break;
            case LISTENER:
                content = context.getString(R.string.class_conversation_notify_role_to_listener, userNameUnion);
                break;
        }

        return content;
    }

    @Override
    public Spannable getContentSummary(Context context, RoleSingleChangedMessage roleChangedMessage) {
        return new SpannableString(getNotifyContent(context, roleChangedMessage));
    }

    @Override
    public void onItemClick(TextView view, int i, RoleSingleChangedMessage roleChangedMessage, UIMessage uiMessage) {
    }
}
