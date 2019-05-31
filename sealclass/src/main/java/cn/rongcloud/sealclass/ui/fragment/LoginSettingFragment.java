package cn.rongcloud.sealclass.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import cn.rongcloud.sealclass.R;
import cn.rongcloud.sealclass.rtc.VideoResolution;
import cn.rongcloud.sealclass.ui.adapter.SimpleCheckTextAdapter;

/**
 * 登录界面 设置
 */
public class LoginSettingFragment extends BaseFragment {
    private static final String BUNDLE_KEY_SETTING_LISTENER = "item_listener";
    private static final String BUNDLE_KEY_RESOLUTION_ID = "resolution_id";

    private GridView resolutionGridView;
    private SimpleCheckTextAdapter resolutionAdapter;
    private OnSettingSelectedListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        int resolutionId = 0;
        if (bundle != null) {
            Serializable serializable = bundle.getSerializable(BUNDLE_KEY_SETTING_LISTENER);
            if (serializable != null && serializable instanceof OnSettingSelectedListener) {
                listener = (OnSettingSelectedListener) bundle.getSerializable(BUNDLE_KEY_SETTING_LISTENER);
            }
            resolutionId = bundle.getInt(BUNDLE_KEY_RESOLUTION_ID);
        }

        View parentView = inflater.inflate(R.layout.login_fragment_setting, container,false);
        // 点击对话框以外区域时取消对话框
        parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onTouchOutSide();
                }
            }
        });

        // 添加点击事件防止点击设置区域时对话框消失
        parentView.findViewById(R.id.login_setting_ll_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        resolutionGridView = parentView.findViewById(R.id.login_setting_gv_resolution);
        resolutionAdapter = new SimpleCheckTextAdapter();
        resolutionGridView.setAdapter(resolutionAdapter);
        resolutionAdapter.setDataList(getResolutionList());

        // 设置分辨率选中项
        resolutionGridView.setItemChecked(getResolutionPositionById(resolutionId), true);

        resolutionGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    VideoResolution item = resolutionAdapter.getItem(position);
                    listener.onResolutionSelected(item);
                }
            }
        });

        return parentView;
    }

    @Override
    protected int getLayoutResId() {
        return 0;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
    }

    private List<VideoResolution> getResolutionList() {
        return Arrays.asList(VideoResolution.values());
    }

    private int getResolutionPositionById(int id) {
        int position = 0;
        VideoResolution[] resolutions = VideoResolution.values();
        int len = resolutions.length;
        for (int i = 0; i < len; i++) {
            if (resolutions[i].getId() == id) {
                position = i;
                break;
            }
        }

        return position;
    }

    /**
     * 设置点击事件
     *
     * @param listener
     */
    public void setOnSettingSelectedListener(OnSettingSelectedListener listener) {
        this.listener = listener;
    }

    public static class Builder {
        private int resolutionId;

        /**
         * 选中的分辨率id
         *
         * @param resolutionId
         */
        public Builder setResolutionId(int resolutionId) {
            this.resolutionId = resolutionId;
            return this;
        }

        public LoginSettingFragment build() {
            LoginSettingFragment dialog = new LoginSettingFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(BUNDLE_KEY_RESOLUTION_ID, resolutionId);
            dialog.setArguments(bundle);
            return dialog;
        }
    }

    /**
     * 设置选择监听
     */
    public interface OnSettingSelectedListener extends Serializable {
        /**
         * 当分辨率选择时回调
         */
        void onResolutionSelected(VideoResolution videoResolution);

        /**
         * 当点击设置以外的区域
         */
        void onTouchOutSide();
    }
}
