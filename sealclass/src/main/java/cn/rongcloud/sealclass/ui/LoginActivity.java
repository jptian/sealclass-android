package cn.rongcloud.sealclass.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.common.ErrorCode;
import cn.rongcloud.sealclass.model.LoginResult;
import cn.rongcloud.sealclass.model.RequestState;
import cn.rongcloud.sealclass.rtc.VideoResolution;
import cn.rongcloud.sealclass.ui.dialog.CommonDialog;
import cn.rongcloud.sealclass.ui.dialog.LoadingDialog;
import cn.rongcloud.sealclass.ui.fragment.LoginSettingFragment;
import cn.rongcloud.sealclass.utils.TextMatchUtils;
import cn.rongcloud.sealclass.utils.ToastUtils;
import cn.rongcloud.sealclass.viewmodel.LoginViewModel;

import static cn.rongcloud.rtc.core.voiceengine.BuildInfo.MANDATORY_PERMISSIONS;

/**
 * 登录界面
 * <p>
 * 通过输入课堂 id 可进入相应的课堂
 * 输入用户姓名可在课堂中让其他人看到
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private final int DEFAULT_VIDEO_RESOLUTION_ID = VideoResolution.RESOLUTION_640_480_15f.getId(); // 默认使用 640 * 480
    private EditText classIdEt;
    private EditText userNameEt;
    private TextView classIdTipsTv;
    private TextView userNameTipsTv;
    private CheckBox listenerCb;
    private CheckBox closeCameraCb;
    private TextView loginTv;

    private LoginViewModel loginViewModel;
    private LoginSettingFragment settingFragment;
    private int selectedResolutionId = DEFAULT_VIDEO_RESOLUTION_ID;//选择分辨率
    private boolean isClassNameValid;
    private boolean isUserNameValid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideNavigationBar();
        setContentView(R.layout.login_activity_main);
        enableKeyboardStateListener(true);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        initView();
        observeModel();
    }

    private void initView() {
        View main = findViewById(R.id.login_rl_main);
        main.setOnClickListener(this);

        loginTv = findViewById(R.id.login_tv_enter);
        loginTv.setOnClickListener(this);
        ImageView settingIv = findViewById(R.id.login_iv_setting);
        settingIv.setOnClickListener(this);

        classIdEt = findViewById(R.id.login_et_class_id);
        classIdTipsTv = findViewById(R.id.login_tv_class_id_tips);
        userNameEt = findViewById(R.id.login_et_user_name);
        userNameTipsTv = findViewById(R.id.login_tv_user_name_tips);
        listenerCb = findViewById(R.id.login_cb_listener);
        closeCameraCb = findViewById(R.id.login_cb_close_camera);

        // 课堂 id 失去监听时判断
        classIdEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String input = classIdEt.getText().toString();
                    if (TextUtils.isEmpty(input)) {
                        classIdEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
                        classIdTipsTv.setVisibility(View.INVISIBLE);
                    }

                    if (!input.trim().equals(input)) {
                        classIdEt.setText(input.trim());
                        classIdEt.setSelection(classIdEt.getText().length());
                    }

                    checkClassIdIsValid();
                    checkCanLogin();
                }else{
                    classIdEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
                }
            }
        });

        classIdEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    userNameEt.clearFocus();
                    return true;
                }

                return false;
            }
        });

        // 用户姓名 输入失去监听时判断
        userNameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String input = userNameEt.getText().toString();
                    if (TextUtils.isEmpty(input)) {
                        userNameEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
                        userNameTipsTv.setVisibility(View.INVISIBLE);
                    }

                    if (!input.trim().equals(input)) {
                        userNameEt.setText(input.trim());
                        userNameEt.setSelection(userNameEt.getText().length());
                    }

                    checkUserNameIsValid();
                    checkCanLogin();
                }else{
                    userNameEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
                }
            }
        });
        userNameEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    userNameEt.clearFocus();
                    hideInputKeyboard();
                    return true;
                }
                return false;
            }
        });

        listenerCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    closeCameraCb.setChecked(false);
                    closeCameraCb.setEnabled(false);
                } else {
                    closeCameraCb.setEnabled(true);
                }
            }
        });

        // 创建显示设置分辨率 Fragment
        settingFragment = new LoginSettingFragment.Builder()
                .setResolutionId(selectedResolutionId)
                .build();

        // 设置分辨率选择监听
        settingFragment.setOnSettingSelectedListener(new LoginSettingFragment.OnSettingSelectedListener() {
            @Override
            public void onResolutionSelected(VideoResolution videoResolution) {
                selectedResolutionId = videoResolution.getId();

                // 选中时取消对话框
                showSetting(false);
            }

            @Override
            public void onTouchOutSide() {
                showSetting(false);
            }
        });

        // 添加设置分辨率 Fragment 并隐藏
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        Fragment existSettingFragment = supportFragmentManager.findFragmentById(R.id.login_fl_setting_container);
        if(existSettingFragment != null){
            fragmentTransaction.remove(existSettingFragment);
        }
        fragmentTransaction.add(R.id.login_fl_setting_container,settingFragment);
        fragmentTransaction.hide(settingFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void observeModel() {
        LiveData<LoginResult> loginResult = loginViewModel.getLoginResult();
        // 监听登录结果
        loginResult.observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(LoginResult loginResult) {
                if (loginResult != null) {
                    boolean isCloseCamera = closeCameraCb.isChecked();

                    Intent intent = new Intent(LoginActivity.this, ClassActivity.class);
                    intent.putExtra(ClassActivity.EXTRA_LOGIN_RESULT, loginResult);
                    intent.putExtra(ClassActivity.EXTRA_CLOSE_CAMERA, isCloseCamera);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 检查 教室id 是否合法
     *
     * @return
     */
    private boolean checkClassIdIsValid() {
        String input = classIdEt.getText().toString();
        if (TextMatchUtils.isDigistsLetters(input)) {
            classIdEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
            classIdTipsTv.setVisibility(View.INVISIBLE);
            isClassNameValid = true;
            return true;
        } else {
            if(!TextUtils.isEmpty(input)) {
                classIdEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text_error));
                classIdTipsTv.setVisibility(View.VISIBLE);
            }
            isClassNameValid = false;
            return false;
        }
    }

    /**
     * 检查 用户姓名 是否合法
     *
     * @return
     */
    private boolean checkUserNameIsValid() {
        String input = userNameEt.getText().toString();
        if (TextMatchUtils.isHanziDigistsLetters(input)) {
            userNameEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
            userNameTipsTv.setVisibility(View.INVISIBLE);
            isUserNameValid = true;
            return true;
        } else {
            if(!TextUtils.isEmpty(input)) {
                userNameEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text_error));
                userNameTipsTv.setVisibility(View.VISIBLE);
            }
            isUserNameValid = false;
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        hideInputKeyboard();

        switch (v.getId()) {
            case R.id.login_rl_main:
                break;
            case R.id.login_tv_enter:
                requestLogin();
                break;
            case R.id.login_iv_setting:
                showSetting(true);
                break;
        }
    }

    /**
     * 请求进入课堂
     */
    private void requestLogin() {
        // 判断是否音视频相关权限并开启
        if(!checkPermissions()) return;

        if (checkCanLogin()) {
            String classId = classIdEt.getText().toString();
            String userName = userNameEt.getText().toString();
            boolean isListener = listenerCb.isChecked();
            final LoadingDialog loadingDialog = new LoadingDialog();
            loadingDialog.showNow(getSupportFragmentManager(), null);

            // 设置分辨率
            loginViewModel.setVideoResolution(VideoResolution.getById(selectedResolutionId));

            loginViewModel.login(classId, isListener, userName).observe(this, new Observer<RequestState>() {
                @Override
                public void onChanged(RequestState requestState) {
                    RequestState.State state = requestState.getState();
                    if (state != RequestState.State.LOADING) {
                        loadingDialog.dismiss();
                    }

                    if (state == RequestState.State.FAILED) {
                        // 当非旁听身份已满时提示，引导使用旁听身份登录
                        if (requestState.getErrorCode() == ErrorCode.API_ERR_OVER_MAX_COUNT) {
                            CommonDialog dialog = new CommonDialog.Builder()
                                    .setContentMessage(getString(R.string.login_dialog_not_listener_over_max))
                                    .setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
                                        @Override
                                        public void onPositiveClick(View v, Bundle bundle) {
                                            // 勾选旁听选项，再次登录
                                            listenerCb.setChecked(true);
                                            requestLogin();
                                        }

                                        @Override
                                        public void onNegativeClick(View v, Bundle bundle) {

                                        }
                                    }).build();
                            dialog.show(getSupportFragmentManager(), null);
                        } else {
                            ToastUtils.showToast(requestState.getErrorCode().getMessageResId());
                        }
                    }
                }
            });
        }
    }

    /**
     * 判断是否可以登录
     *
     * @return
     */
    private boolean checkCanLogin(){
        boolean canLogin = isClassNameValid && isUserNameValid;
        if(canLogin){
            loginTv.setEnabled(true);
            loginTv.setAlpha(1);
        } else {
            loginTv.setEnabled(false);
            loginTv.setAlpha(0.4f);
        }
        return canLogin;
    }

    /**
     * 显示设置菜单
     */
    private void showSetting(boolean isShow) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        if(isShow){
            fragmentTransaction.show(settingFragment);
        }else{
            fragmentTransaction.hide(settingFragment);
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    /**
     * 设置菜单是否已显示
     */
    private boolean isSettingShow(){
        Fragment settingFragment = getSupportFragmentManager().findFragmentById(R.id.login_fl_setting_container);
        return settingFragment != null && !settingFragment.isHidden();
    }

    @Override
    public void onKeyboardStateChanged(boolean isShown, int height) {
        hideNavigationBar();
        if(!isShown){
            classIdEt.clearFocus();
            userNameEt.clearFocus();
        }
    }

    /**
     * 判断是否有音视频相关权限
     *
     * @return
     */
    private boolean checkPermissions() {
        List<String> unGrantedPermissions = new ArrayList();
        for (String permission : MANDATORY_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }
        if (unGrantedPermissions.size() == 0) {//已经获得了所有权限
            return true;
        } else {//部分权限未获得，重新请求获取权限
            String[] array = new String[unGrantedPermissions.size()];
            ActivityCompat.requestPermissions(this, unGrantedPermissions.toArray(array), 0);
            ToastUtils.showToast(R.string.toast_error_need_app_permission, Toast.LENGTH_LONG);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){//
            case 0://如果申请权限回调的参数
                if (grantResults.length > 0) {
                    boolean isSuccess = true;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isSuccess = false;
                            break;
                        }
                    }

                    if (isSuccess) {
                        requestLogin();
                    }
                }
                break;

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        hideNavigationBar();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // 初始化状态，保证当 Activity 被回收再创建时状态正确
        checkClassIdIsValid();
        checkUserNameIsValid();
        checkCanLogin();
    }

    @Override
    public void onBackPressed() {
        if(isSettingShow()){
            showSetting(false);
        }else {
            super.onBackPressed();
        }
    }
}
