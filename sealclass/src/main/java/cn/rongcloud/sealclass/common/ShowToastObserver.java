package cn.rongcloud.sealclass.common;

import androidx.lifecycle.Observer;

import cn.rongcloud.sealclass.model.RequestState;
import cn.rongcloud.sealclass.ui.ToastBySelfComponent;
import cn.rongcloud.sealclass.utils.ToastUtils;

/**
 * 用于请求结果出错时弹出提示用监听
 */
public class ShowToastObserver implements Observer<RequestState> {
    private ToastBySelfComponent component;

    public ShowToastObserver() {
    }

    public ShowToastObserver(Object toastComponent) {
        if(toastComponent instanceof ToastBySelfComponent) {
            this.component = (ToastBySelfComponent)toastComponent;
        }
    }

    @Override
    public void onChanged(RequestState state) {
        if (state.getState() == RequestState.State.FAILED) {
            if (component != null) {
                component.showToast(state.getErrorCode().getMessageResId());
            } else {
                ToastUtils.showToast(state.getErrorCode().getMessageResId());
            }
        }
    }
}
