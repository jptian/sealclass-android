package cn.rongcloud.sealclass.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.rtc.VideoResolution;
import cn.rongcloud.sealclass.ui.dialog.CommonDialog;
import cn.rongcloud.sealclass.ui.dialog.LoadingDialog;
import cn.rongcloud.sealclass.ui.fragment.LoginSettingFragment;
import cn.rongcloud.sealclass.utils.CacheConts;
import cn.rongcloud.sealclass.utils.SessionManager;
import cn.rongcloud.sealclass.utils.TextMatchUtils;
import cn.rongcloud.sealclass.utils.ToastUtils;
import cn.rongcloud.sealclass.utils.update.UpDateApkHelper;
import cn.rongcloud.sealclass.viewmodel.LoginViewModel;

import static cn.rongcloud.rtc.utils.BuildInfo.MANDATORY_PERMISSIONS;


/**
 * 登录界面
 * <p>
 * 通过输入课堂 id 可进入相应的课堂
 * 输入用户姓名可在课堂中让其他人看到
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private final int DEFAULT_VIDEO_RESOLUTION_ID = VideoResolution.RESOLUTION_640_480_15f.getId(); // 默认使用 640 * 480
    private EditText classIdEt;
    private EditText userPhoneEt, schoolIdEt, passwordEt;
    private TextView classIdTipsTv;
    private TextView userPhoneTipsTv, schoolIdTipsTv, passwordTips;
    private CheckBox listenerCb;
    private CheckBox closeCameraCb;
    private TextView loginTv;

    private LoginViewModel loginViewModel;
    private LoginSettingFragment settingFragment;
    private int selectedResolutionId = DEFAULT_VIDEO_RESOLUTION_ID;//选择分辨率
    private boolean isClassNameValid;
    private boolean isUserPhoneValid;
    private boolean isPasswordValid=true;
    private boolean isSchoolValid;

    private static final String[] PERMISSIONS = {
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.READ_PHONE_STATE",
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.BLUETOOTH"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity_main);
        enableKeyboardStateListener(true);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        initView();
        observeModel();

        if(checkPermissions(1)) {
            new UpDateApkHelper(this).diffVersionFromServer();
        }
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
        userPhoneEt = findViewById(R.id.login_et_user_phone);
        userPhoneTipsTv = findViewById(R.id.login_tv_user_phone_tips);
        listenerCb = findViewById(R.id.login_cb_listener);
        closeCameraCb = findViewById(R.id.login_cb_close_camera);

        schoolIdTipsTv = findViewById(R.id.login_tv_class_schoolId_tips);
        schoolIdEt = findViewById(R.id.login_et_school_Id);

        passwordEt = findViewById(R.id.login_et_user_pwd);
        passwordTips = findViewById(R.id.login_tv_user_password_tips);

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
                }else{
                    classIdEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
                }
            }
        });

        classIdEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    userPhoneEt.clearFocus();
                    return true;
                }

                return false;
            }
        });

        // 用户姓名 输入失去监听时判断
        userPhoneEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String input = userPhoneEt.getText().toString();
                    if (TextUtils.isEmpty(input)) {
                        userPhoneEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
                        userPhoneTipsTv.setVisibility(View.INVISIBLE);
                    }

                    if (!input.trim().equals(input)) {
                        userPhoneEt.setText(input.trim());
                        userPhoneEt.setSelection(userPhoneEt.getText().length());
                    }
                }else{
                    userPhoneEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
                }
            }
        });
        userPhoneEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    userPhoneEt.clearFocus();
                    hideInputKeyboard();
                    return true;
                }
                return false;
            }
        });

        // 用户密码 输入失去监听时判断
//        passwordEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(!hasFocus){
//                    String input = passwordEt.getText().toString();
//                    if (TextUtils.isEmpty(input)) {
//                        passwordEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
//                        passwordTips.setVisibility(View.INVISIBLE);
//                    }
//
//                    if (!input.trim().equals(input)) {
//                        passwordEt.setText(input.trim());
//                        passwordEt.setSelection(passwordEt.getText().length());
//                    }
//                }else{
//                    passwordEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
//                }
//            }
//        });
//        passwordEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if(actionId == EditorInfo.IME_ACTION_DONE){
//                    passwordEt.clearFocus();
//                    hideInputKeyboard();
//                    return true;
//                }
//                return false;
//            }
//        });

        // 机构id 输入失去监听时判断
        schoolIdEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String input = schoolIdEt.getText().toString();
                    if (TextUtils.isEmpty(input)) {
                        schoolIdEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
                        schoolIdTipsTv.setVisibility(View.INVISIBLE);
                    }

                    if (!input.trim().equals(input)) {
                        schoolIdEt.setText(input.trim());
                        schoolIdEt.setSelection(schoolIdEt.getText().length());
                    }
                }else{
                    schoolIdEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
                }
            }
        });
        schoolIdEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    schoolIdEt.clearFocus();
                    hideInputKeyboard();
                    return true;
                }
                return false;
            }
        });

        addTextChangedListener();

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
        initSP();
    }

    private void saveSP(String phone, String password, String classId, String schoolId) {
        SessionManager.getInstance().put(CacheConts.SP_PHONE_KEY, phone);
        SessionManager.getInstance().put(CacheConts.SP_PASSWORD_KEY, password);
        SessionManager.getInstance().put(CacheConts.SP_CLASS_ID_KEY, classId);
        SessionManager.getInstance().put(CacheConts.SP_SCHOOL_ID_KEY, schoolId);
    }

    //修改用户输入完之后不点击actionId == EditorInfo.IME_ACTION_DONE  ，而是点击关闭输入法，导致的加入课堂按钮不可用；
    private void addTextChangedListener() {
        schoolIdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkSchoolIsValid();
                checkCanLogin();
            }
        });

//        passwordEt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                checkPasswordIsValid();
//                checkCanLogin();
//            }
//        });

        userPhoneEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkUserPhoneIsValid();
                checkCanLogin();
            }
        });

        classIdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkClassIdIsValid();
                checkCanLogin();
            }
        });
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
     * 检查 手机号码 是否合法
     *
     * @return
     */
    private boolean checkUserPhoneIsValid() {
        String input = userPhoneEt.getText().toString();
        if (!TextUtils.isEmpty(input) && input.length() == 11) {
            userPhoneEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
            userPhoneTipsTv.setVisibility(View.INVISIBLE);
            isUserPhoneValid = true;
            return true;
        } else {
            userPhoneEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text_error));
            userPhoneTipsTv.setVisibility(View.VISIBLE);
            isUserPhoneValid = false;
            return false;
        }
    }

    /**
     * 检查 密码 是否合法
     *
     * @return
     */
    private boolean checkPasswordIsValid() {
        String input = passwordEt.getText().toString();
        if (!TextUtils.isEmpty(input) && input.length() >= 6 && input.length() <= 11) {
            passwordEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
            passwordTips.setVisibility(View.INVISIBLE);
            isPasswordValid = true;
            return true;
        } else {
            passwordEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text_error));
            passwordTips.setVisibility(View.VISIBLE);
            isPasswordValid = false;
            return false;
        }
    }

    /**
     * 检查 机构号 是否合法
     *
     * @return
     */
    private boolean checkSchoolIsValid() {
        String input = schoolIdEt.getText().toString();
        if (!TextUtils.isEmpty(input) && TextMatchUtils.isDigistsLetters(input) && input.length() >= 4 && input.length() <= 8) {
            schoolIdEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text));
            schoolIdTipsTv.setVisibility(View.INVISIBLE);
            isSchoolValid = true;
            return true;
        } else {
            schoolIdEt.setBackground(getResources().getDrawable(R.drawable.login_bg_input_text_error));
            schoolIdTipsTv.setVisibility(View.VISIBLE);
            isSchoolValid = false;
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
        if(!checkPermissions(0)) return;

        if (checkCanLogin()) {
            String classId = classIdEt.getText().toString();
            String userName = userPhoneEt.getText().toString();
            String schoolId = schoolIdEt.getText().toString();
//            String password = passwordEt.getText().toString();
            boolean isListener = listenerCb.isChecked();
            final LoadingDialog loadingDialog = new LoadingDialog();
            loadingDialog.showNow(getSupportFragmentManager(), null);

            saveSP(userName,"",classId,schoolId);

            loginViewModel.login(classId, isListener, userName, schoolId, Role.STUDENT.getValue(), "123456",selectedResolutionId).observe(this, new Observer<RequestState>() {
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
        boolean canLogin = isClassNameValid && isUserPhoneValid && isPasswordValid && isSchoolValid;
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
        if(!isShown){
            classIdEt.clearFocus();
            userPhoneEt.clearFocus();
        }
    }

    /**
     * 判断是否有音视频相关权限
     * @param requestCode 0:login , 1:update
     * @return
     */
    private boolean checkPermissions(int requestCode) {
        List<String> unGrantedPermissions = new ArrayList();
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermissions.add(permission);
            }
        }
        if (unGrantedPermissions.size() == 0) {//已经获得了所有权限
            return true;
        } else {//部分权限未获得，重新请求获取权限
            String[] array = new String[unGrantedPermissions.size()];
            ActivityCompat.requestPermissions(this, unGrantedPermissions.toArray(array), requestCode);
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
            case 1:
                if (grantResults.length > 0) {
                    boolean isSuccess = true;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isSuccess = false;
                            break;
                        }
                    }

                    if (isSuccess) {
                        new UpDateApkHelper(this).diffVersionFromServer();
                    }
                }
                break;
                default:
                    break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // 初始化状态，保证当 Activity 被回收再创建时状态正确
        checkClassIdIsValid();
        checkUserPhoneIsValid();
//        checkPasswordIsValid();
        checkSchoolIsValid();
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


    @Override
    protected void onResume() {
        super.onResume();
        initSP();
    }

    private void initSP() {
        //        老师 15810530010 ~ 15810530019, 123456
//        学生 15810530020 ~ 15810530029, 123456
        //学生1
//        userPhoneEt.setText("15810530020");
        //学生2
//        userPhoneEt.setText("15810530021");
//
//        classIdEt.setText("11223344");
        schoolIdEt.setText("emlZvv");
//        passwordEt.setText("123456");
        //
        if (SessionManager.getInstance().contains(CacheConts.SP_PHONE_KEY)) {
            userPhoneEt.setText(SessionManager.getInstance().getString(CacheConts.SP_PHONE_KEY));
        }
        if (SessionManager.getInstance().contains(CacheConts.SP_PASSWORD_KEY)) {
            passwordEt.setText(SessionManager.getInstance().getString(CacheConts.SP_PASSWORD_KEY));
        }
        if (SessionManager.getInstance().contains(CacheConts.SP_CLASS_ID_KEY)) {
            classIdEt.setText(SessionManager.getInstance().getString(CacheConts.SP_CLASS_ID_KEY));
        }
        if (SessionManager.getInstance().contains(CacheConts.SP_SCHOOL_ID_KEY)) {
            schoolIdEt.setText(SessionManager.getInstance().getString(CacheConts.SP_SCHOOL_ID_KEY));
        }
    }
}
