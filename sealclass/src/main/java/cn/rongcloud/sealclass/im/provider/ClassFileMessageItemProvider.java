package cn.rongcloud.sealclass.im.provider;

import android.view.View;
import android.widget.LinearLayout;

import cn.rongcloud.sealclass.R;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.FileMessageItemProvider;
import io.rong.message.FileMessage;

@ProviderTag(messageContent = FileMessage.class,
        showProgress = false,
        showReadState = true)
/**
 * 自定义文件消息样式
 * 修改了背景图
 */
public class ClassFileMessageItemProvider extends FileMessageItemProvider {

    @Override
    public void bindView(View v, int position, FileMessage content, final UIMessage message) {
        super.bindView(v, position, content, message);

        LinearLayout msgContainer = v.findViewById(io.rong.imkit.R.id.rc_message);
        msgContainer.setBackgroundResource(R.drawable.class_bg_conversation_item_message);
    }
}
