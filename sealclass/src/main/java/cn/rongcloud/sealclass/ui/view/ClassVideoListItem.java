package cn.rongcloud.sealclass.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.rtc.engine.view.RongRTCVideoView;
import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.model.ClassMember;
import cn.rongcloud.sealclass.model.Role;
import cn.rongcloud.sealclass.permission.ClassPermission;

/**
 * 视频列表 item 布局
 */
public class ClassVideoListItem extends RelativeLayout {
    private LinearLayout videoViewContainer;
    private TextView roleText;
    private TextView nameText;
    private TextView contentText;
    private ClassMember data;
    private boolean isHideRole = false;

    public ClassVideoListItem(Context context) {
        super(context);
        initView();
    }

    public ClassVideoListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ClassVideoListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.class_item_video_list, this);
        videoViewContainer = view.findViewById(R.id.class_video_item_videoview_container);
        roleText = view.findViewById(R.id.class_tv_video_item_role);
        nameText = view.findViewById(R.id.class_tv_video_item_name);
        contentText = view.findViewById(R.id.class_tv_video_item_content);
    }

    public void addVideoView(RongRTCVideoView videoView, LinearLayout.LayoutParams params) {
        videoViewContainer.removeAllViews();
        videoViewContainer.addView(videoView, params);
    }

    public void addVideoView(RongRTCVideoView videoView) {
        videoViewContainer.removeAllViews();
        videoViewContainer.addView(videoView);
    }

    public int getVideoViewCount() {
        return videoViewContainer.getChildCount();
    }

    public boolean hasVideoView(RongRTCVideoView videoView) {
        int i = videoViewContainer.indexOfChild(videoView);
        return i < 0 ? false : true;
    }


    public void removeVideoView() {
        videoViewContainer.removeAllViews();
    }

    public void setRole(int strResId) {
        roleText.setText(strResId);
    }

    public void setRole(String role) {
        roleText.setText(role);
    }

    public void setName(int strResId) {
        nameText.setText(strResId);
    }

    public void setName(String name) {
        nameText.setText(name);
    }

    public void contentText(int strResId) {
        contentText.setText(strResId);
    }

    public void contentText(String content) {
        contentText.setText(content);
    }

    public void setRoleVisibility(int visibility) {
        roleText.setVisibility(visibility);
    }

    public void setContentVisibility(int visibility) {
        contentText.setVisibility(visibility);
    }


    public void setVideViewToCenter() {
        videoViewContainer.setGravity(Gravity.CENTER);
    }


    public void setVideViewToCenter(boolean center) {
        if (center) {
            videoViewContainer.setGravity(Gravity.CENTER);
        } else {
            videoViewContainer.setGravity(Gravity.LEFT);
        }
    }

    public void setData(ClassMember item) {
        reset();
        if (item == null) {
            return;
        }
        this.data = item;
        if (!isHideRole) {
            if (item.getRole().hasPermission(ClassPermission.LECTURE)) {
                if (item.getUserId().equals("-1")) {
                    setRoleVisibility(View.GONE);
                } else {
                    setRoleVisibility(View.VISIBLE);
                    setRole(R.string.class_role_lecturer);
                }
                roleText.setBackgroundResource(R.drawable.class_member_list_role_lecturer_bg);
            } else if (item.getRole().hasPermission(ClassPermission.TRANSFER_ROLE)) {
                setRoleVisibility(View.VISIBLE);
                setRole(R.string.class_role_assistant);
                roleText.setBackgroundResource(R.drawable.class_member_list_role_assistant_bg);
            } else {
                setRoleVisibility(View.GONE);
            }
            String userName = item.getUserName();
            userName = TextUtils.isEmpty(userName) ? "" : userName;
            if (userName.length() > 5) {
                userName = userName.substring(0, 5) + "...";
            }
            setName(userName);
        }
    }

    private void reset() {
        setRole("");
        setRoleVisibility(View.GONE);
        setName("");
//        setContentVisibility(View.GONE);
    }

    public ClassMember getData() {
        return data;
    }

    public void setHideRole(boolean isHideRole) {
        this.isHideRole = isHideRole;
    }

    public void clear() {
        reset();
        removeVideoView();
        data = null;
    }

    public RadioRtcVideoView getVideoView() {
        if (videoViewContainer.getChildCount() > 0) {
            return (RadioRtcVideoView) videoViewContainer.getChildAt(0);
        }
        return null;
    }
}
