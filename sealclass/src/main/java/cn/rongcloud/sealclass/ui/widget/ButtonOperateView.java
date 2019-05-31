package cn.rongcloud.sealclass.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class ButtonOperateView extends OperateView {

    public ButtonOperateView(Context context) {
        super(context);
    }

    public ButtonOperateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonOperateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View createView(final OperateItem item, final OnOperateItemListener listener) {
        Button button = new Button(getContext());
        LayoutParams layoutParams = new LayoutParams(item.width, item.height);
        layoutParams.setMargins(item.left, item.top, item.right, item.bottom);
        button.setLayoutParams(layoutParams);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClicked(v, item);
                }
            }
        });
        button.setBackgroundResource(item.bgResId);
        return button;
    }
}
