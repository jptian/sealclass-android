package cn.rongcloud.sealclass.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.permission.ClassPermission;
import cn.rongcloud.sealclass.ui.dialog.CreateWhiteBoardDialog;
import cn.rongcloud.sealclass.ui.widget.OnOperateItemListener;
import cn.rongcloud.sealclass.ui.widget.OperateButtonGroupView;
import cn.rongcloud.sealclass.ui.widget.OperateItem;
import cn.rongcloud.sealclass.utils.DisplayUtils;
import cn.rongcloud.sealclass.viewmodel.ClassViewModel;

public class ClassScreenControlFragment extends BaseFragment {

    private OperateButtonGroupView operateView;

    private ScreenControlButtonListener listener;
    private HashMap<ControlOperateType, Boolean> buttonEnableStatus;
    private ControlOperateType currentCheckedButton = ControlOperateType.VIDEO_LIST;

    // button 的资源
    private int[] btnBgResIds = new int[]{
            R.drawable.class_fragment_screen_control_cb_withe_board_selector,
            R.drawable.class_fragment_screen_control_cb_res_library_selector,
            R.drawable.class_fragment_screen_control_cb_menber_list_selector,
            R.drawable.class_fragment_screen_control_cb_video_list_selector,
            R.drawable.class_fragment_screen_control_cb_im_selector,
            R.drawable.class_fragment_screen_control_cb_has_message_selector
    };

    /**
     * 操作类型
     */
    public enum ControlOperateType {
        /**
         * 白板
         */
        WHITE_BOARD(0),
        /**
         * 资源库
         */
        RES_LIBRARY(1),
        /**
         * 成员列表
         */
        MEMBER_LIST(2),
        /**
         * 视频列表
         */
        VIDEO_LIST(3),
        /**
         * IM
         */
        IM(4),
        /**
         * 有消息
         */
        HAS_MESSAGE(5);

        private int value;

        ControlOperateType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ControlOperateType getType(int value) {
            for (ControlOperateType type : ControlOperateType.values()) {
                if (value == type.getValue()) {
                    return type;
                }
            }
            return VIDEO_LIST;
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.class_fragment_screen_control;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        List<OperateItem> items = new ArrayList<>();
        int value = DisplayUtils.dip2px(getContext(), 24);
        for (ControlOperateType type : ControlOperateType.values()) {
            OperateItem item = new OperateItem();
            item.id = type.getValue();
            item.bgResId = btnBgResIds[type.getValue()];
            item.width = value;
            item.height = value;
            item.top = value;
            if (type == ControlOperateType.HAS_MESSAGE) {
                item.type = OperateItem.Type.BUTTON;
            } else {
                item.type = OperateItem.Type.CHECKBOX;
            }
            items.add(item);
        }

        operateView = findView(R.id.class_operate_view);
        operateView.initView(items, new OnOperateItemListener() {
            @Override
            public void onItemClicked(View v, OperateItem item) {
                if (ControlOperateType.getType(item.id) == ControlOperateType.HAS_MESSAGE) {
                    operateView.setItemVisibility(ControlOperateType.HAS_MESSAGE.getValue(), View.GONE);
                    operateView.setItemVisibility(ControlOperateType.IM.getValue(), View.VISIBLE);
                    CheckBox box = operateView.getView(ControlOperateType.IM.getValue());
                    box.setChecked(true);
                }
            }

            @Override
            public void onCheckedChanged(View v, OperateItem item, boolean isChecked) {
                ControlOperateType type = ControlOperateType.getType(item.id);
                if (isChecked) {
                    if (type == ControlOperateType.WHITE_BOARD) {
                        showCreateWhiteBoardDialog();
                    }

                    if (listener != null) {
                        listener.onCheckedChanged(type, isChecked);
                    }
                    checkButton(item.id);
                } else {
                    if (listener != null) {
                        listener.onCheckedChanged(type, isChecked);
                    }

                    // 如果是但按钮取消选择， 则直接选择视频按钮
                    if (currentCheckedButton == ControlOperateType.getType(item.id)) {
                        clearChecked();
                    }
                }
            }
        });
        operateView.setGravity(Gravity.CENTER);
        operateView.setOrientation(LinearLayout.VERTICAL);
        operateView.setItemVisibility(ControlOperateType.HAS_MESSAGE.getValue(), View.GONE);
        setButtonEnableStatus(buttonEnableStatus);
        setCheckedButton(currentCheckedButton);

    }

    @Override
    protected void onInitViewModel() {
        ClassViewModel classViewModel = ViewModelProviders.of(getActivity()).get(ClassViewModel.class);
        classViewModel.getUserInfo().observe(this, new Observer<UserInfo>() {
            @Override
            public void onChanged(UserInfo userInfo) {
                Role role = userInfo.getRole();
                boolean createWhiteBoardPermission = role.hasPermission(ClassPermission.CREATE_WHITE_BOARD);
                boolean resLibraryPermission = role.hasPermission(ClassPermission.RESOURCE_LIBARAY);
                boolean videoListPermission = role.hasPermission(ClassPermission.LOOK_MEMBER_VIDEO_LIST);
                boolean memberListPermission = role.hasPermission(ClassPermission.LOOK_MEMBER_LIST);
                boolean imChatPermission = role.hasPermission(ClassPermission.IM_CHAT);

                if (createWhiteBoardPermission) {
                    operateView.setItemEnabled(ControlOperateType.WHITE_BOARD.getValue(), true);
                } else {
                    operateView.setItemEnabled(ControlOperateType.WHITE_BOARD.getValue(), false);
                }

                if (resLibraryPermission) {
                    operateView.setItemEnabled(ControlOperateType.RES_LIBRARY.getValue(), true);
                } else {
                    operateView.setItemEnabled(ControlOperateType.RES_LIBRARY.getValue(), false);
                }

                if (videoListPermission) {
                    operateView.setItemEnabled(ControlOperateType.VIDEO_LIST.getValue(), true);
                } else {
                    operateView.setItemEnabled(ControlOperateType.VIDEO_LIST.getValue(), false);
                }

                if (memberListPermission) {
                    operateView.setItemEnabled(ControlOperateType.MEMBER_LIST.getValue(), true);
                } else {
                    operateView.setItemEnabled(ControlOperateType.MEMBER_LIST.getValue(), false);
                }

                if (imChatPermission) {
                    operateView.setItemEnabled(ControlOperateType.IM.getValue(), true);
                } else {
                    operateView.setItemEnabled(ControlOperateType.IM.getValue(), false);
                }
            }
        });

        classViewModel.getUnReadMessage().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer > 0) {
                    showHasMessageButton();
                }
            }
        });

    }

    /**
     * 设置 Button 选择监听
     * @param listener
     */
    public void setScreenControlButtonCheckListener(ScreenControlButtonListener listener) {
        this.listener = listener;
    }

    /**
     * 设置选择的 button 项
     * @param type
     */
    public void setCheckedButton(ControlOperateType type) {
        currentCheckedButton = type;
        if (type == null) {
            return;
        }

        if (operateView != null) {
            CheckBox box = operateView.getView(type.getValue());
            box.setChecked(true);
        }
    }

    /**
     * 设置 Button 的可用状态
     * @param values
     */
    public void setButtonEnableStatus(HashMap<ControlOperateType, Boolean> values) {

        this.buttonEnableStatus = values;
        if (values == null) {
            return;
        }

        if (operateView != null) {
            for (ControlOperateType type : ControlOperateType.values()) {
                boolean enableStatus = true;
                if (values.containsKey(type)) {
                    enableStatus = values.get(type);
                }
                operateView.setItemEnabled(type.getValue(), enableStatus);
            }
        }
    }

    /**
     * 清除当前所选择的状态，默认选择视频列表选项
     */
    public void clearChecked() {
        if (operateView != null && currentCheckedButton != ControlOperateType.IM) {
           CheckBox box = operateView.getView(ControlOperateType.VIDEO_LIST.getValue());
           box.setChecked(true);
        }
    }

    // 检测 button 的选择状态
    private void checkButton(int id) {
        currentCheckedButton = ControlOperateType.getType(id);
        if (currentCheckedButton == ControlOperateType.VIDEO_LIST) {
            operateView.setItemEnabled(id, false);
        } else {
            operateView.setItemEnabled(ControlOperateType.VIDEO_LIST.getValue(), true);
        }

        for (ControlOperateType type : ControlOperateType.values()) {
            if (type != ControlOperateType.HAS_MESSAGE && type.getValue() != id) {
                CheckBox box = operateView.getView(type.getValue());
                box.setChecked(false);
            }
        }
    }

    /**
     * 当有消息时， 但是没有在聊天界面， 就会显示有消息的按钮提醒
     */
    private void showHasMessageButton() {
        CheckBox imCheckBox = operateView.getView(ControlOperateType.IM.getValue());
        if (!imCheckBox.isChecked()) {
            operateView.setItemVisibility(ControlOperateType.HAS_MESSAGE.getValue(), View.VISIBLE);
            operateView.setItemVisibility(ControlOperateType.IM.getValue(), View.GONE);
        }
    }

    /**
     * 显示创建白板对话框
     */
    private void showCreateWhiteBoardDialog() {
        CreateWhiteBoardDialog dialog = new CreateWhiteBoardDialog.Builder()
                .setOnItemClickedListener(new CreateWhiteBoardDialog.OnItemClickedListener() {
                    @Override
                    public void onCreateWhiteBoardClicked() {
                        CheckBox box = operateView.getView(ControlOperateType.WHITE_BOARD.getValue());
                        box.setChecked(false);

                        if (listener != null) {
                            listener.onCreateWhiteBoard();
                        }
                    }
                }).setOnCancelListener(new CreateWhiteBoardDialog.OnCancelListener() {
                    @Override
                    public void onCancel() {
                        clearChecked();
                    }
                }).create();

        dialog.showToRight(operateView.getView(ControlOperateType.WHITE_BOARD.value), getFragmentManager());
    }

    /**
     *  Button 选择监听
     */
    public interface ScreenControlButtonListener {

        /**
         * 选择监听
         * @param type
         * @param isChecked
         */
        void onCheckedChanged(ControlOperateType type, boolean isChecked);

        /**
         * 白板创建
         */
        void onCreateWhiteBoard();
    }
}
