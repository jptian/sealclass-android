package cn.rongcloud.sealclass.ui.widget;

/**
 * 动态设置按钮的信息对象
 */
public class OperateItem {

    public int id;
    public int bgResId;
    public int tag;
    public String text;
    public int width;
    public int height;
    public int top;
    public int left;
    public int bottom;
    public int right;

    public Type type;

    public enum Type{
        BUTTON,
        CHECKBOX,
        RADIOBUTTON
    }
}
