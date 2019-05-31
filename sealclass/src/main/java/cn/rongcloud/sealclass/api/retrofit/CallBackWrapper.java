package cn.rongcloud.sealclass.api.retrofit;

import cn.rongcloud.sealclass.common.ErrorCode;
import cn.rongcloud.sealclass.common.ResultCallback;
import cn.rongcloud.sealclass.model.Result;
import cn.rongcloud.sealclass.utils.log.SLog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallBackWrapper<R> implements Callback<Result<R>> {
    private ResultCallback<R> mCallBack;

    public CallBackWrapper(ResultCallback<R> callBack) {
        mCallBack = callBack;
    }

    @Override
    public void onResponse(Call<Result<R>> call, Response<Result<R>> response) {
        Result<R> body = response.body();
        if (body != null) {
            int errCode = body.getErrCode();
            if (errCode == 0) {
                mCallBack.onSuccess(body.getDataResult());
            } else {
                SLog.e(SLog.TAG_NET, "url:" + call.request().url().toString()
                        + " ,errorMsg:" + body.getErrMsg() + ", errorDetail:" + body.getErrDetail());
                mCallBack.onFail(errCode);
            }
        } else {
            SLog.e(SLog.TAG_NET, "url:" + call.request().url().toString() + ", no response body");
            mCallBack.onFail(ErrorCode.API_ERR_OTHER.getCode());
        }
    }

    @Override
    public void onFailure(Call<Result<R>> call, Throwable t) {
        SLog.e(SLog.TAG_NET, call.request().url().toString() + " - " + (t != null ? t.getMessage() : ""));
        mCallBack.onFail(ErrorCode.NETWORK_ERROR.getCode());
    }
}
