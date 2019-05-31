package cn.rongcloud.sealclass.im.provider;

import android.view.View;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.SealClassApp;
import cn.rongcloud.sealclass.utils.DisplayUtils;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AutoLinkTextView;
import io.rong.imkit.widget.provider.TextMessageItemProvider;
import io.rong.message.TextMessage;

/**
 * 自定义文本消息样式
 * 修改了背景图
 */
@ProviderTag(messageContent = TextMessage.class)
public class ClassTextMessageItemProvider extends TextMessageItemProvider {
    private static int padding;

    static {
        padding = DisplayUtils.dip2px(SealClassApp.getApplication().getApplicationContext(), 12);
    }

    @Override
    public void bindView(final View v, int position, TextMessage content, final UIMessage data) {
        super.bindView(v, position, content, data);

        AutoLinkTextView message = v.findViewById(android.R.id.text1);
        message.setPadding(padding, padding, padding, padding);
        message.setBackgroundResource(R.drawable.class_bg_conversation_item_message);
    }
}
