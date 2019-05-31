package cn.rongcloud.sealclass.ui;

/**
 * 内部显示 toast 信息的控件
 */
public interface ToastBySelfComponent {
    /**
     * 设定显示时间的 toast 消息
     *
     * @param content  显示内容
     * @param duration 显示时长，单位毫秒
     */
    void showToast(String content, long duration);

    /**
     * 使用默认时长的 toast 消息
     *
     * @param content
     */
    void showToast(String content);

    /**
     * 设定显示时间的 toast 消息
     *
     * @param resId    显示内容的 String Resource id
     * @param duration 显示时长，单位毫秒
     */
    void showToast(int resId, long duration);

    /**
     * 使用默认时长的 toast 消息
     */
    void showToast(int resId);
}
