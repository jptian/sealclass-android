package cn.rongcloud.sealclass.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import cn.rongcloud.sealclass.common.ResultCallback;
import cn.rongcloud.sealclass.common.StateLiveData;
import cn.rongcloud.sealclass.model.LoginResult;
import cn.rongcloud.sealclass.model.RequestState;
import cn.rongcloud.sealclass.repository.UserRepository;
import cn.rongcloud.sealclass.rtc.RtcManager;
import cn.rongcloud.sealclass.rtc.VideoResolution;

/**
 * 登录界面 视图模型
 */
public class LoginViewModel extends AndroidViewModel {
    private MutableLiveData<LoginResult> loginResultLiveData = new MutableLiveData<>();
    private UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application.getApplicationContext());
    }

    public LiveData<RequestState> login(String roomId, boolean isListener, String userPhone, String schoolId,int role, String password,int selectedResolutionId) {
        final StateLiveData stateLiveData = new StateLiveData();
        stateLiveData.loading();
        userRepository.login(roomId, isListener, userPhone, schoolId, password,role, selectedResolutionId,new ResultCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                loginResultLiveData.postValue(result);
                stateLiveData.success();
            }

            @Override
            public void onFail(int errorCode) {
                stateLiveData.failed(errorCode);
            }
        });

        return stateLiveData;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResultLiveData;
    }

    /**
     * 设置视频分辨率
     * @param resolution
     */
    public void setVideoResolution(VideoResolution resolution){
        RtcManager.getInstance().setVideoResolution(resolution);
    }

}
