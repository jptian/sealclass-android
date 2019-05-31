package cn.rongcloud.sealclass.im.provider;

import android.view.View;
import android.widget.TextView;

import cn.rongcloud.sealclass.R;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.provider.ImageMessageItemProvider;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

@ProviderTag(messageContent = ImageMessage.class,
        showProgress = false,
        showReadState = true)
/**
 * 自定义图片消息样式
 * 修改了背景
 */
public class ClassImageMessageItemProvider extends ImageMessageItemProvider {

    @Override
    public void bindView(View v, int position, ImageMessage content, UIMessage message) {
        AsyncImageView imageView = v.findViewById(R.id.rc_img);
        TextView messageTv = v.findViewById(io.rong.imkit.R.id.rc_msg);

        // 设置背景
        v.setBackgroundResource(R.drawable.class_bg_conversation_image);

        // 设置图片
        imageView.setResource(content.getThumUri());

        // 设置图片加载
        int progress = message.getProgress();
        Message.SentStatus status = message.getSentStatus();

        if (status.equals(Message.SentStatus.SENDING) && progress < 100) {
            messageTv.setText(progress + "%");
            messageTv.setVisibility(View.VISIBLE);
        } else {
            messageTv.setVisibility(View.GONE);
        }
    }
}
