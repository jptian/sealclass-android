package cn.rongcloud.sealclass.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;

import cn.rongcloud.sealclass.R;

/**
 * 接受申请发言对话框
 */
public class ApplySpeechRequestDialog extends CommonDialog {
    @Override
    public void onStart() {
        super.onStart();

        //透明化背景
        Window window = getDialog().getWindow();
        //背景色
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //全屏化对话框
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getDialog().getWindow().setLayout(dm.widthPixels, dm.heightPixels);
    }

    @Override
    protected View getDialogView() {
        return View.inflate(getContext(), R.layout.class_dialog_apply_speech_request, null);
    }

    public static class Builder extends CommonDialog.Builder {
        @Override
        protected CommonDialog getCurrentDialog() {
            return new ApplySpeechRequestDialog();
        }
    }
}
