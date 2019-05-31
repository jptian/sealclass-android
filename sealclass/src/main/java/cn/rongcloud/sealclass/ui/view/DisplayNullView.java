package cn.rongcloud.sealclass.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.Role;

/**
 * 显示空布局
 */
public class DisplayNullView extends LinearLayout {
    private ImageView displayNullImage;
    private TextView displayNullContent;

    public DisplayNullView(Context context) {
        super(context);
        initView();
    }

    public DisplayNullView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DisplayNullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.class_view_display_null, this);
        displayNullImage = view.findViewById(R.id.class_display_null_image);
        displayNullContent = view.findViewById(R.id.class_display_null_content);
    }


    public void setContent(ClassMember member) {
        if (member.getRole() == Role.ASSISTANT || member.getRole() == Role.LECTURER) {
            displayNullContent.setText(R.string.class_share_screen_null_text);
            displayNullImage.setImageResource(R.drawable.class_share_screen_null);
        } else {
            displayNullContent.setText(R.string.class_share_screen_null_text_other);
            displayNullImage.setImageResource(R.drawable.class_share_screen_null_other);
        }
    }
}
