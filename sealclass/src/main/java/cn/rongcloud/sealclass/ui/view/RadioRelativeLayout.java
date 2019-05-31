package cn.rongcloud.sealclass.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 比例布局
 */
public class RadioRelativeLayout extends RelativeLayout {
    private float radio = 1.333f;

    public RadioRelativeLayout(Context context) {
        super(context);
    }

    public RadioRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadioRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        float tmpRadia = (float) width / height;
        if (tmpRadia > 1) {
            if (tmpRadia > radio) {
                // 按高算
                float w = height * radio;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) w, MeasureSpec.EXACTLY);
            } else {
                float h = width / radio;
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) h, MeasureSpec.EXACTLY);
            }
        } else {

            // 高大于宽
            float h = width / radio;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) h, MeasureSpec.EXACTLY);

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
