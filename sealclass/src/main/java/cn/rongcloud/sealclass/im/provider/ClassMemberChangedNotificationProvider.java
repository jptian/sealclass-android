package cn.rongcloud.sealclass.im.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.TextView;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.im.message.MemberChangedMessage;
import cn.rongcloud.sealclass.model.ClassMemberChangedAction;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;

/**
 * 用户进出课堂消息提示模版
 */
@ProviderTag(messageContent = MemberChangedMessage.class, showPortrait = false, centerInHorizontal = true, showProgress = false, showSummaryWithName = false)
public class ClassMemberChangedNotificationProvider extends BaseNotificationProvider<MemberChangedMessage> {

    @Override
    public void bindView(TextView view, int i, MemberChangedMessage memberChangedMessage, UIMessage uiMessage) {
        view.setText(getNotifyContent(view.getContext(), memberChangedMessage));
    }

    public String getNotifyContent(Context context, MemberChangedMessage memberChangedMessage) {
        String content = "";

        String userName = memberChangedMessage.getUserName();
        String currentUserId = RongIM.getInstance().getCurrentUserId();
        if (userName != null && userName.equals(currentUserId)) {
            userName = context.getString(R.string.you);
        }
        ClassMemberChangedAction action = memberChangedMessage.getAction();
        switch (action) {
            case JOIN:
                content = context.getString(R.string.class_conversation_notify_member_join_format, userName);
                break;
            case LEAVE:
                content = context.getString(R.string.class_conversation_notify_member_leave_format, userName);
                break;
            case KICK:
                content = context.getString(R.string.class_conversation_notify_member_kick_format, userName);
                break;
        }

        return content;
    }

    @Override
    public Spannable getContentSummary(Context context, MemberChangedMessage memberChangedMessage) {
        return new SpannableString(getNotifyContent(context, memberChangedMessage));
    }

    @Override
    public void onItemClick(TextView view, int i, MemberChangedMessage memberChangedMessage, UIMessage uiMessage) {
    }
}
