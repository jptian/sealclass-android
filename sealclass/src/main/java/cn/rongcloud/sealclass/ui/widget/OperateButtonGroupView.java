package cn.rongcloud.sealclass.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

/**
 * 动态设置按钮布局的Button 组View
 */
public class OperateButtonGroupView extends OperateView {

    public OperateButtonGroupView(Context context) {
        super(context);
    }

    public OperateButtonGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OperateButtonGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View createView(final OperateItem item, final OnOperateItemListener listener) {
        View view = null;
        if (item.type == OperateItem.Type.CHECKBOX) {
            CheckBox box = new CheckBox(getContext());
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) {
                        listener.onCheckedChanged(buttonView, item, isChecked);
                    }
                }
            });
            box.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            box.setButtonDrawable(item.bgResId);
            view = box;
        } else if (item.type == OperateItem.Type.RADIOBUTTON) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) {
                        listener.onCheckedChanged(buttonView, item, isChecked);
                    }
                }
            });
            radioButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            radioButton.setButtonDrawable(item.bgResId);
            view = radioButton;
        } else {
            Button button = new Button(getContext());
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(v, item);
                    }
                }
            });
            button.setBackgroundResource(item.bgResId);
            view = button;
        }

        LayoutParams layoutParams = new LayoutParams(item.width, item.height);
        layoutParams.setMargins(item.left, item.top, item.right, item.bottom);
        view.setLayoutParams(layoutParams);
        return view;
    }
}
