package cn.rongcloud.sealclass.ui.widget;

import android.view.View;

/**
 * 动态添加操作按钮布局事件监听
 */
public interface OnOperateItemListener {
    /**
     * 点击监听
     * @param v
     * @param item
     */
    void onItemClicked(View v, OperateItem item);

    /**
     * 选择状态监听
     * @param v
     * @param item
     * @param isChecked
     */
    void onCheckedChanged(View  v, OperateItem item, boolean isChecked);
}
