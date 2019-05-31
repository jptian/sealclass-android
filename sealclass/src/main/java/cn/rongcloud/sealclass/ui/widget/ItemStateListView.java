package cn.rongcloud.sealclass.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import io.rong.imageloader.utils.L;

public class ItemStateListView extends ListView {

    private int oldFirstIndex = 0;
    private int oldEndIndex = 0;
    private OnItemStateListener itemStateListener;

    public ItemStateListView(Context context) {
        super(context);
        init();
    }

    public ItemStateListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ItemStateListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > oldFirstIndex) {
                    itemHide(oldFirstIndex);
                    oldFirstIndex = firstVisibleItem;
                } else if (firstVisibleItem < oldFirstIndex) {
                    oldFirstIndex = firstVisibleItem;
                    itemShow(oldFirstIndex);
                }

                int end = firstVisibleItem + visibleItemCount;
                if (oldEndIndex == 0) {
                    oldEndIndex = end;
                    for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                        itemShow(i);
                    }
                }

                if (oldEndIndex < end) {
                    itemShow(oldEndIndex);
                    oldEndIndex = end;
                } else if (oldEndIndex > end) {
                    oldEndIndex = end;
                    itemHide(oldEndIndex);
                }
            }
        });
    }

    private void itemShow(int position) {
        if (itemStateListener != null) {
            Object item = getAdapter().getItem(position);
            int index = position - getFirstVisiblePosition();
            View childAt = getChildAt(index);
            getFirstVisiblePosition();
            itemStateListener.itemShow(childAt , position, item);
        }
    }

    private void itemHide(int position) {
        if (itemStateListener != null) {
            Object item = getAdapter().getItem(position);
            itemStateListener.itemHide(position, item);
        }
    }

    public void setOnItemStateListener(OnItemStateListener listener) {
        this.itemStateListener = listener;
    }

    public interface OnItemStateListener {
        void itemShow(View view, int position, Object data);
        void itemHide(int position, Object data);
    }
}
