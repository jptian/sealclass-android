package cn.rongcloud.sealclass.common;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import cn.rongcloud.sealclass.model.RequestState;

/**
 *  记录请求状态用的 LiveData
 *  内部实现了当请求成功或失败时取消掉监听的功能
 */
public class StateLiveData extends MutableLiveData<RequestState> {
    private LifecycleOwner observerOwner;

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super RequestState> observer) {
        observerOwner = owner;
        ObserverWrapper observerWrapper = new ObserverWrapper(observer);
        super.observe(owner, observerWrapper);
    }

    public void loading() {
        postValue(RequestState.loading());
    }

    public void success() {
        postValue(RequestState.success());
    }

    public void failed(int errorCode) {
        postValue(RequestState.failed(errorCode));
    }

    private class ObserverWrapper implements Observer<RequestState> {
        Observer<? super RequestState> origin;

        ObserverWrapper(Observer<? super RequestState> origin) {
            this.origin = origin;
        }

        @Override
        public void onChanged(RequestState o) {
            origin.onChanged(o);

            if (o.getState() != RequestState.State.LOADING) {
                removeObservers(observerOwner);
            }
        }
    }
}
