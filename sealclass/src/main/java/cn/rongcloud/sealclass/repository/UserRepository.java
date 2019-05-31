package cn.rongcloud.sealclass.repository;

import android.content.Context;
import android.net.Uri;

import java.util.HashMap;

import cn.rongcloud.sealclass.api.SealClassApi;
import cn.rongcloud.sealclass.api.retrofit.CallBackWrapper;
import cn.rongcloud.sealclass.api.retrofit.RetrofitUtil;
import cn.rongcloud.sealclass.common.ResultCallback;
import cn.rongcloud.sealclass.im.IMManager;
import cn.rongcloud.sealclass.model.LoginResult;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.rtc.RtcManager;
import cn.rongcloud.sealclass.utils.log.SLog;

/**
 * 用户数据仓库
 */
public class UserRepository extends BaseRepository {
    private SealClassApi sealClassService;
    private IMManager imManager;

    public UserRepository(Context context) {
        super(context);
        imManager = IMManager.getInstance();
        sealClassService = getService(SealClassApi.class);
    }

    public void login(String roomId, boolean isListener, String userName, final ResultCallback<LoginResult> callBack) {
        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("roomId", roomId);
        bodyMap.put("audience", String.valueOf(isListener));
        bodyMap.put("userName", userName);

        sealClassService.login(RetrofitUtil.createJsonRequest(bodyMap)).enqueue(new CallBackWrapper<>(new ResultCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult result) {
                // 设置 http请求 的用户认证
                String authorization = result.getAuthorization();
                getHttpManager().setAuthHeader(authorization);

                // 登录到 IM 服务器
                imManager.login(result.getImToken(), new ResultCallback<String>() {
                    @Override
                    public void onSuccess(String s) {

                        // 进入音视频房间
                        RtcManager.getInstance().joinRtcRoom(result.getRoomId(), new ResultCallback<String>() {
                            @Override
                            public void onSuccess(String roomId) {
                                SLog.d("class_rtc", "joinroom =>" + roomId);
                                UserInfo loginUser = result.getUserInfo();
                                if (loginUser != null) {
                                    io.rong.imlib.model.UserInfo userInfo
                                            = new io.rong.imlib.model.UserInfo(
                                            loginUser.getUserId(), loginUser.getUserName(), Uri.parse(""));
                                    imManager.setCurrentUserInfo(userInfo);
                                }
                                callBack.onSuccess(result);
                            }

                            @Override
                            public void onFail(int errorCode) {
                                SLog.d("class_rtc", "joinroom =>" + errorCode);
                            }
                        });

                    }

                    @Override
                    public void onFail(int errorCode) {
                        callBack.onFail(errorCode);
                    }
                });



            }

            @Override
            public void onFail(int errorCode) {
                callBack.onFail(errorCode);
            }
        }));
    }

}
