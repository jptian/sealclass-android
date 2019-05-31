package cn.rongcloud.sealclass.im.provider;

import android.content.Context;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.rongcloud.sealclass.R;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.MessageContent;

/**
 * 显示提示消息用基础消息模版
 *
 * @param <T>
 */
public abstract class BaseNotificationProvider<T extends MessageContent> extends IContainerItemProvider.MessageProvider<T> {

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.class_item_conversation_notify_message, viewGroup, false);
        return contentView;
    }

    @Override
    public void bindView(View view, int i, T t, UIMessage uiMessage) {
        bindView((TextView) view, i, t, uiMessage);
    }

    public abstract void bindView(TextView view, int i, T t, UIMessage uiMessage);

    @Override
    public Spannable getContentSummary(T memberChangedMessage) {
        return null;
    }

        @Override
    public abstract Spannable getContentSummary(Context context, T t);

    @Override
    public void onItemClick(View view, int i, T t, UIMessage uiMessage) {
        onItemClick((TextView) view, i, t, uiMessage);
    }

    public abstract void onItemClick(TextView view, int i, T t, UIMessage uiMessage);
}
