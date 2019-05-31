package cn.rongcloud.sealclass.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.utils.DisplayUtils;

/**
 * 创建白板对话框
 */
public class CreateWhiteBoardDialog extends DialogFragment {
    private View toRightParentView;

    private TextView createWhiteBoardTv;
    private View contentView;
    private OnItemClickedListener onItemClickedListener;
    private OnCancelListener onCancelListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.class_dialog_create_whiteboard, container, false);
        createWhiteBoardTv = contentView.findViewById(R.id.class_dialog_tv_create_whiteboard);

        createWhiteBoardTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onItemClickedListener != null) {
                    onItemClickedListener.onCreateWhiteBoardClicked();
                }
            }
        });

        Dialog dialog = getDialog();
        if(dialog != null){
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(true);
        }

        return contentView;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null) {
            onCancelListener.onCancel();
        }
    }

    public void showToRight(View parent, FragmentManager fragmentManager) {
        this.toRightParentView = parent;
        show(fragmentManager, "CreateWhiteBoardDialog");
    }

    @Override
    public void onStart() {
        super.onStart();
        contentView.post(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = getDialog();
                Window window = dialog.getWindow();
                // 透明化背景色
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // 取消对话框的背景蒙层
                window.setDimAmount(0f);

                android.view.WindowManager.LayoutParams layoutParams = window.getAttributes();
                int[] location = new int[2];
                toRightParentView.getLocationInWindow(location);
                layoutParams.x = location[0] + toRightParentView.getWidth() + DisplayUtils.dp2px(contentView.getContext(), 8);
                layoutParams.y = location[1] + toRightParentView.getHeight() / 2 - contentView.getHeight() / 2;
                layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                window.setAttributes(layoutParams);
            }
        });

        View decorView = getDialog().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


    public static class Builder {
        private OnItemClickedListener listener;
        private OnCancelListener cancelListener;

        public Builder() {
        }

        public Builder setOnItemClickedListener(OnItemClickedListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener listener) {
            this.cancelListener = listener;
            return this;
        }

        public CreateWhiteBoardDialog create() {
            CreateWhiteBoardDialog dialog = new CreateWhiteBoardDialog();
            dialog.onItemClickedListener = listener;
            dialog.onCancelListener = cancelListener;
            return dialog;
        }

    }

    public interface OnItemClickedListener {
        void onCreateWhiteBoardClicked();
    }
    public interface OnCancelListener {
        void onCancel();
    }
}
