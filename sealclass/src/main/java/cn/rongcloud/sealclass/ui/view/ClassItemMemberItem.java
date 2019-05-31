package cn.rongcloud.sealclass.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.UserInfo;
import cn.rongcloud.sealclass.permission.ClassExecutedPermission;
import cn.rongcloud.sealclass.permission.ClassPermission;
import cn.rongcloud.sealclass.utils.DisplayUtils;
import cn.rongcloud.sealclass.ui.widget.ButtonOperateView;
import cn.rongcloud.sealclass.ui.widget.OnOperateItemListener;
import cn.rongcloud.sealclass.ui.widget.OperateItem;

/**
 * 成员列表 item 布局
 */
public class ClassItemMemberItem extends RelativeLayout {

    public ClassItemMemberItem(Context context) {
        super(context);
        initView();
    }

    public ClassItemMemberItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ClassItemMemberItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private LinearLayout llMemberItem;
    private TextView tvPortrait;
    private TextView tvName;
    private TextView tvRole;
    private TextView tvApply;
    private OnItemMemberClickListener listener;
    private ClassMember member;

    private int[] btnBgResIds = new int[]{
            R.drawable.class_item_member_list_transfer_selector,
            R.drawable.class_item_member_list_lacturer_selector,
            R.drawable.class_item_member_list_close_mic_selector,
            R.drawable.class_item_member_list_mic_selector,
            R.drawable.class_item_member_list_close_camera_selector,
            R.drawable.class_item_member_list_camera_selector,
            R.drawable.class_item_member_list_upgrade_selector,
            R.drawable.class_item_member_list_downgrade_selector,
            R.drawable.class_item_member_list_del_selector
    };

    public enum OperateType {
        /**
         * 转移角色
         */
        TRANSFER_ROLE(0),
        /**
         * 升级为讲师
         */
        UPGRAGE_TO_LECTURER(1),
        /**
         * 请求打开麦克风
         */
        APPLY_OPEN_MIC(2),
        /**
         * 关闭麦克风
         */
        CLOSE_MIC(3),
        /**
         * 请求打开摄像头
         */
        APPLY_OPEN_CAMERA(4),
        /**
         * 关闭摄像头
         */
        CLOSE_CAMERA(5),
        /**
         * 升级
         */
        UPGRADE(6),
        /**
         * 降级
         */
        DOWNGRADE(7),
        /**
         * 踢人
         */
        KICK_OFF(8),
        /**
         * 申请发言
         */
        APPLY_SPEECH(9);
        private int value;

        OperateType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static OperateType getOperateType(int value) {
            for (OperateType type : OperateType.values()) {
                if (value == type.getValue()) {
                    return type;
                }
            }

            return null;
        }

    }


    private ButtonOperateView operateView;

    //初始化布局
    private void initView() {
        View view = View.inflate(getContext(), R.layout.class_item_member_list, this);
        llMemberItem = view.findViewById(R.id.class_ll_menber_item);

        tvPortrait = view.findViewById(R.id.class_tv_member_portrait);
        tvName = view.findViewById(R.id.class_tv_member_name);
        tvRole = view.findViewById(R.id.class_tv_member_role);
        tvApply = view.findViewById(R.id.class_tv_member_apply);
        operateView = view.findViewById(R.id.class_item_member_operation);

        List<OperateItem> items = new ArrayList<>();
        int value = DisplayUtils.dip2px(getContext(), 22);
        int marginLeft = DisplayUtils.dip2px(getContext(), 15);
        for (OperateType type : OperateType.values()) {
            if (type != OperateType.APPLY_SPEECH) {
                OperateItem item = new OperateItem();
                item.id = type.getValue();
                item.bgResId = btnBgResIds[type.getValue()];
                item.left = marginLeft;
                item.width = value;
                item.height = value;
                items.add(item);
            }

        }

        operateView.initView(items, new OnOperateItemListener() {
            @Override
            public void onItemClicked(View v, OperateItem item) {
                if (listener != null) {
                    OperateType operateType = OperateType.getOperateType(item.id);
                    listener.onClick(operateType, member);
                }
            }

            @Override
            public void onCheckedChanged(View v, OperateItem item, boolean isChecked) {

            }
        });
        operateView.setOrientation(LinearLayout.HORIZONTAL);
        operateView.setGravity(Gravity.CENTER);
        operateView.setVisibility(View.GONE);

    }

    public void setData(final UserInfo currentUser, final ClassMember member, boolean expand) {
        operateView.setVisibility(View.GONE);
        tvApply.setText(R.string.class_tv_member_apply_text);
        if (currentUser == null || TextUtils.isEmpty(currentUser.getUserId()) || member == null || TextUtils.isEmpty(member.getUserId())) {
            return;
        }
        this.member = member;
        operateView.setEnabled(true);
        operateView.setVisibility(expand ? VISIBLE : GONE);

        String name = member.getUserName() == null ? "" : member.getUserName();

        int nameLen = name.length();
        String portraitName = nameLen > 0 ? name.substring(nameLen - 1, nameLen) : "";
        tvPortrait.setText(portraitName);

        if (name.length() > 3) {
            name = name.substring(0, 3) + "...";
        }
        tvName.setText(name);


        // 有没有转移身份的权限和被转移身份的权限
        boolean transferRole = currentUser.getRole().hasPermission(ClassPermission.TRANSFER_ROLE);
        boolean controlMemberCamera = currentUser.getRole().hasPermission(ClassPermission.CONTROL_MEMBER_CAMERA);
        boolean controlMemberMic = currentUser.getRole().hasPermission(ClassPermission.CONTROL_MEMBER_MIC);
        boolean kickOffMember = currentUser.getRole().hasPermission(ClassPermission.KICK_OFF_MEMBER);
        boolean upgradeMember = currentUser.getRole().hasPermission(ClassPermission.UPGRADE_MEMBER);
        boolean downgradeMember = currentUser.getRole().hasPermission(ClassPermission.DOWNGRADE_MEMBER);

        boolean memberExcutedAcceptTransferRole = member.getRole().hasExecutedPermission(ClassExecutedPermission.ACCEPT_TRANSFER_ROLE);
        boolean memberExcutedControlCamera = member.getRole().hasExecutedPermission(ClassExecutedPermission.CONTROL_VIDEO);
        boolean memberExcutedControlMic = member.getRole().hasExecutedPermission(ClassExecutedPermission.CONTROL_MIC);
        boolean memberExcutedKickOff = member.getRole().hasExecutedPermission(ClassExecutedPermission.KICK_OFF);
        boolean memberExcutedDowngrade = member.getRole().hasExecutedPermission(ClassExecutedPermission.DOWNGRADE);
        boolean memberExcutedUpgrade = member.getRole().hasExecutedPermission(ClassExecutedPermission.UPGRADE);
        boolean memberLecturePermission = member.getRole().hasPermission(ClassPermission.LECTURE);
        boolean memberApplySpeechPermission = member.getRole().hasPermission(ClassPermission.APPLY_SPEECH);
        boolean memberUpgradeMember = member.getRole().hasPermission(ClassPermission.UPGRADE_MEMBER);


        if (memberUpgradeMember) {
            tvRole.setVisibility(View.VISIBLE);
            tvRole.setText(R.string.class_role_assistant);
            tvRole.setBackgroundResource(R.drawable.class_member_list_role_assistant_bg);
            tvPortrait.setBackgroundResource(R.drawable.class_portrait_assistant);
        } else if (memberLecturePermission) {
            tvRole.setVisibility(View.VISIBLE);
            tvRole.setText(R.string.class_role_lecturer);
            tvRole.setBackgroundResource(R.drawable.class_member_list_role_lecturer_bg);
            tvPortrait.setBackgroundResource(R.drawable.class_portrait_lecturer);

        } else if (memberApplySpeechPermission) {
            tvRole.setVisibility(View.GONE);
            tvPortrait.setBackgroundResource(R.drawable.class_portrait_listener);
        } else {
            tvRole.setVisibility(View.GONE);
            tvPortrait.setBackgroundResource(R.drawable.class_portrait_student);
        }

        if (memberApplySpeechPermission && currentUser.getUserId().equals(member.getUserId())) {
            tvApply.setVisibility(View.VISIBLE);
            tvApply.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(OperateType.APPLY_SPEECH, member);
                    }
                }
            });

            if (currentUser.isApplySpeeching()) {
                tvApply.setText(R.string.class_item_member_applying);
                tvApply.setEnabled(false);
            } else {
                tvApply.setText(R.string.class_tv_member_apply_text);
                tvApply.setEnabled(true);
            }
        } else {
            tvApply.setVisibility(View.GONE);
        }

        // 转移角色
        if (transferRole && memberExcutedAcceptTransferRole) {
            operateView.setItemEnabled(OperateType.TRANSFER_ROLE.getValue(), true);
        } else {
            operateView.setItemEnabled(OperateType.TRANSFER_ROLE.getValue(), false);
        }

        //摄像头
        if (controlMemberCamera && memberExcutedControlCamera) {
            boolean isOpened = member.isCamera();
            if (isOpened) {
                operateView.setItemVisibility(OperateType.CLOSE_CAMERA.getValue(), View.VISIBLE);
                operateView.setItemVisibility(OperateType.APPLY_OPEN_CAMERA.getValue(), View.GONE);
            } else {
                operateView.setItemVisibility(OperateType.CLOSE_CAMERA.getValue(), View.GONE);
                operateView.setItemVisibility(OperateType.APPLY_OPEN_CAMERA.getValue(), View.VISIBLE);
            }

        } else {
            operateView.setItemEnabled(OperateType.CLOSE_CAMERA.getValue(), false);
            operateView.setItemVisibility(OperateType.CLOSE_CAMERA.getValue(), View.VISIBLE);
            operateView.setItemVisibility(OperateType.APPLY_OPEN_CAMERA.getValue(), View.GONE);
        }

        //麦克风
        if (controlMemberMic && memberExcutedControlMic) {
            boolean isOpened = member.isMicrophone();
            if (isOpened) {
                operateView.setItemVisibility(OperateType.CLOSE_MIC.getValue(), View.VISIBLE);
                operateView.setItemVisibility(OperateType.APPLY_OPEN_MIC.getValue(), View.GONE);
            } else {
                operateView.setItemVisibility(OperateType.CLOSE_MIC.getValue(), View.GONE);
                operateView.setItemVisibility(OperateType.APPLY_OPEN_MIC.getValue(), View.VISIBLE);
            }

        } else {
            operateView.setItemEnabled(OperateType.CLOSE_MIC.getValue(), false);
            operateView.setItemVisibility(OperateType.CLOSE_MIC.getValue(), View.VISIBLE);
            operateView.setItemVisibility(OperateType.APPLY_OPEN_MIC.getValue(), View.GONE);
        }

        // 踢人
        if (kickOffMember && memberExcutedKickOff) {
            operateView.setItemEnabled(OperateType.KICK_OFF.getValue(), true);
        } else {
            operateView.setItemEnabled(OperateType.KICK_OFF.getValue(), false);
        }

        //降级，
        if (downgradeMember && memberExcutedDowngrade) {
            operateView.setItemEnabled(OperateType.DOWNGRADE.getValue(), true);
            operateView.setItemVisibility(OperateType.DOWNGRADE.getValue(), View.VISIBLE);
            operateView.setItemVisibility(OperateType.UPGRADE.getValue(), View.GONE);

        } else {
            operateView.setItemEnabled(OperateType.DOWNGRADE.getValue(), false);
            operateView.setItemVisibility(OperateType.DOWNGRADE.getValue(), View.GONE);
        }

        // 升级
        if (upgradeMember && memberExcutedUpgrade) { // 可执行升级， 成员也可被执行升级

            if (memberLecturePermission) { // 有讲师的权限讲师权限
                operateView.setItemEnabled(OperateType.UPGRAGE_TO_LECTURER.getValue(), false);
                operateView.setItemVisibility(OperateType.UPGRADE.getValue(), View.GONE);
            } else {
                operateView.setItemEnabled(OperateType.UPGRAGE_TO_LECTURER.getValue(), true);
                operateView.setItemVisibility(OperateType.UPGRADE.getValue(), View.VISIBLE);
            }

            if (memberApplySpeechPermission) {
                operateView.setItemEnabled(OperateType.UPGRAGE_TO_LECTURER.getValue(), false);
                operateView.setItemVisibility(OperateType.UPGRADE.getValue(), View.VISIBLE);
                operateView.setItemEnabled(OperateType.UPGRADE.getValue(), true);
            } else {
                operateView.setItemVisibility(OperateType.UPGRADE.getValue(), View.GONE);
                operateView.setItemEnabled(OperateType.UPGRADE.getValue(), false);
                operateView.setItemEnabled(OperateType.UPGRAGE_TO_LECTURER.getValue(), true);
            }

        } else {
            if (downgradeMember && memberExcutedDowngrade) {
                operateView.setItemVisibility(OperateType.UPGRADE.getValue(), View.GONE);
            } else {
                operateView.setItemVisibility(OperateType.UPGRADE.getValue(), View.VISIBLE);
            }
            operateView.setItemVisibility(OperateType.UPGRAGE_TO_LECTURER.getValue(), View.VISIBLE);
            operateView.setItemEnabled(OperateType.UPGRADE.getValue(), false);
            operateView.setItemEnabled(OperateType.UPGRAGE_TO_LECTURER.getValue(), false);
        }

        llMemberItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // 自己则不显示
                if (currentUser.getUserId().equals(member.getUserId())) {
                    operateView.setVisibility(View.GONE);
                    return;
                }

                int visibility = operateView.getVisibility();
                if (visibility == View.VISIBLE) {
                    operateView.setVisibility(View.GONE);
                    if (listener != null) {
                        listener.onExpandViewStatus(member, false);
                    }
                } else {
                    operateView.setVisibility(View.VISIBLE);
                    if (listener != null) {
                        listener.onExpandViewStatus(member, true);
                    }
                }

            }
        });

        // 自己则不显示
        if (currentUser.getUserId().equals(member.getUserId())) {
            operateView.setVisibility(View.GONE);
        }
    }

    public void setApplyBtnStatus(boolean isApplying) {
        if (isApplying) {
            tvApply.setText(R.string.class_item_member_applying);
            tvApply.setEnabled(false);
        } else {
            tvApply.setText(R.string.class_tv_member_apply_text);
            tvApply.setEnabled(true);
        }
    }

    public void setExpandViewVisibility(int visibility) {
        if (operateView != null) {
            operateView.setVisibility(visibility);
        }

    }

    public void setOnItemMemberClickListener(OnItemMemberClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemMemberClickListener {
        public void onClick(OperateType type, ClassMember member);

        public void onExpandViewStatus(ClassMember member, boolean expand);
    }
}
