package cn.rongcloud.sealclass.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态设置按钮的布局
 */
public abstract class OperateView extends LinearLayout {
    public OperateView(Context context) {
        super(context);
    }

    public OperateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OperateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private HashMap<Integer, View> views;
    private OnOperateItemListener listener;

    public void initView(List<OperateItem> items, OnOperateItemListener listener) {
        if (items == null || items.size() <= 0) {
            return;
        }

        for (final OperateItem item : items) {
            if (views == null) {
                views = new HashMap<>();
            }
            View view = createView(item, listener);
            views.put(item.id, view);
            addView(view);
        }
    }


//    public void setOnOperateItemListener (OnOperateItemListener listener) {
//        this.listener = listener;
//    }

    public <T extends View> T getView(OperateItem item) {
        return getView(item.id);
    }

    public <T extends View> T getView(int id) {
        if (views == null) {
            return null;
        }
        View view = views.get(id);
        if (view == null) {
            return null;
        }

        return (T) view;
    }


    public void setItemEnabled(OperateItem item, boolean enabled) {
        setItemEnabled(item.id, enabled);
    }

    public void setItemEnabled(int id, boolean enabled) {
        if (views == null) {
            return;
        }
        View view = views.get(id);
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
    }

    public void setItemVisibility(OperateItem item, int visibile) {
        setItemVisibility(item.id, visibile);
    }

    public void setItemVisibility(int id, int visibile) {
        if (views == null) {
            return;
        }
        View view = views.get(id);
        if (view == null) {
            return;
        }
        view.setVisibility(visibile);
    }


    public void setEnabled(boolean enabled) {
        if (views == null) {
            return;
        }

        for (Map.Entry<Integer, View> entry : views.entrySet()) {
            entry.getValue().setEnabled(enabled);
        }
    }

    protected abstract View createView(OperateItem item, OnOperateItemListener listener);


}
