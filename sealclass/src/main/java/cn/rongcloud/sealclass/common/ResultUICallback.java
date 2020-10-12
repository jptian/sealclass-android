package cn.rongcloud.sealclass.common;

import cn.rongcloud.sealclass.rtc.RtcManager;

public abstract class ResultUICallback<Result> implements ResultCallback<Result>{

    @Override
    public void onSuccess(final Result result) {
        RtcManager.getInstance().getUIHandler(new Runnable() {
            @Override
            public void run() {
                onUISuccess(result);
            }
        });
    }

    @Override
    public void onFail(final int errorCode) {
        RtcManager.getInstance().getUIHandler(new Runnable() {
            @Override
            public void run() {
                onUIFail(errorCode);
            }
        });
    }
    public abstract void onUISuccess(Result result);

    public abstract void onUIFail(int errorCode);
}
