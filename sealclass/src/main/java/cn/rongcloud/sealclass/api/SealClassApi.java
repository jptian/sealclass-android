package cn.rongcloud.sealclass.api;

import java.util.List;

import cn.rongcloud.sealclass.model.LoginResult;
import cn.rongcloud.sealclass.model.Result;
import cn.rongcloud.sealclass.model.WhiteBoard;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SealClassApi {
    /**
     * 登录
     *
     * @param body
     * @return
     */
    @POST(SealClassUrls.LOGIN)
    Call<Result<LoginResult>> login(@Body RequestBody body);


    /**
     * 退出房间
     *
     * @param body
     * @return
     */
    @POST(SealClassUrls.ROOM_LEAVE)
    Call<Result<Boolean>> leave(@Body RequestBody body);



    /**
     * 踢人
     * @param body
     * @return
     */
    @POST(SealClassUrls.KICK_OFF)
    Call<Result<Boolean>> kickOff(@Body RequestBody body);

    /**
     * 控制设备
     * @param body
     * @return
     */
    @POST(SealClassUrls.DEVICE_CONTROL)
    Call<Result<Boolean>> deviceControl(@Body RequestBody body);

    /**
     * 同意开启设备
     * @param body
     * @return
     */
    @POST(SealClassUrls.DEVICE_APPROVE)
    Call<Result<Boolean>> deviceApprove(@Body RequestBody body);

    /**
     * 拒绝开启设备
     * @param body
     * @return
     */
    @POST(SealClassUrls.DEVICE_REJECT)
    Call<Result<Boolean>> deviceReject(@Body RequestBody body);

    /**
     * 拒绝开启设备
     * @param body
     * @return
     */
    @POST(SealClassUrls.DEVICE_SYNC)
    Call<Result<Boolean>> deviceSync(@Body RequestBody body);


    /**
     * 降级
     * @param body
     * @return
     */
    @POST(SealClassUrls.DOWNGRADE)
    Call<Result<Boolean>> downgrade(@Body RequestBody body);

    /**
     * 申请发言
     * @param body
     * @return
     */
    @POST(SealClassUrls.SPEECH_APPLY)
    Call<Result<Boolean>> applySpeech(@Body RequestBody body);


    /**
     * 同意发言
     * @param body
     * @return
     */
    @POST(SealClassUrls.SPEECH_APPROVE)
    Call<Result<Boolean>> applyApprove(@Body RequestBody body);


    /**
     * 拒绝发言
     * @param body
     * @return
     */
    @POST(SealClassUrls.SPEECH_REJECT)
    Call<Result<Boolean>> applyReject(@Body RequestBody body);


    /**
     * 转移角色
     * @param body
     * @return
     */
    @POST(SealClassUrls.TRANSFER_ROLE)
    Call<Result<Boolean>> transferRole(@Body RequestBody body);


    /**
     * 要求升级
     * @param body
     * @return
     */
    @POST(SealClassUrls.UPGRADE_INVITE)
    Call<Result<Boolean>> upgradeInvite(@Body RequestBody body);



    /**
     * 接受升级
     * @param body
     * @return
     */
    @POST(SealClassUrls.UPGRADE_APPROVE)
    Call<Result<Boolean>> upgradeApprove(@Body RequestBody body);



    /**
     * 拒绝升级
     * @param body
     * @return
     */
    @POST(SealClassUrls.UPGRADE_REJECT)
    Call<Result<Boolean>> upgradeReject(@Body RequestBody body);


    /**
     * 设置角色， 设置为老师
     * @param body
     * @return
     */
    @POST(SealClassUrls.CHANGE_ROLE)
    Call<Result<Boolean>> changeRole(@Body RequestBody body);


    /**
     * 创建白板
     *
     * @param body
     * @return
     */
    @POST(SealClassUrls.WHITE_BOARD_CREATE)
    Call<Result<String>> createWhiteBoard(@Body RequestBody body);


    /**
     * 删除白板
     *
     * @param body
     * @return
     */
    @POST(SealClassUrls.WHITE_BOARD_DELETE)
    Call<Result<Boolean>> deleteWhiteBoard(@Body RequestBody body);

    /**
     * 获取白板列表
     *
     * @return
     */
    @GET(SealClassUrls.WHITE_BOARD_LIST)
    Call<Result<List<WhiteBoard>>> getWhiteBoardList(@Query("roomId") String roomId);

    /**
     * 切换共享画布区内容显示Id
     * @param body
     * @return
     */
    @POST(SealClassUrls.SWITCH_DISPLAY)
    Call<Result<Boolean>> switchDisplay(@Body RequestBody body);

}
