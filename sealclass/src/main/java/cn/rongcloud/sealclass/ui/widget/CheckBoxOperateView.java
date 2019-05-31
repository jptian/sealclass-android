package cn.rongcloud.sealclass.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class CheckBoxOperateView extends OperateView {

    public CheckBoxOperateView(Context context) {
        super(context);
    }

    public CheckBoxOperateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View createView(final OperateItem item, final OnOperateItemListener listener) {
        CheckBox box = new CheckBox(getContext());
        LayoutParams layoutParams = new LayoutParams(item.width, item.height);
        layoutParams.setMargins(item.left, item.top, item.right, item.bottom);
        box.setLayoutParams(layoutParams);
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    listener.onCheckedChanged(buttonView, item, isChecked);
                }
            }
        });
        box.setBackground(null);
        box.setButtonDrawable(item.bgResId);
        return box;
    }


}
