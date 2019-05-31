package cn.rongcloud.sealclass.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class WrapContentGridView extends GridView {
    public WrapContentGridView(Context context) {
        super(context);
    }

    public WrapContentGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapContentGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec;
        if(getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
            heightSpec = MeasureSpec.makeMeasureSpec(
                    Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        } else{
            heightSpec = heightMeasureSpec;
        }

        super.onMeasure(widthMeasureSpec, heightSpec);
    }
}
