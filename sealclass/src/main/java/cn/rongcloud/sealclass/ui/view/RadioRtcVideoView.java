package cn.rongcloud.sealclass.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.core.EglRenderer;
import cn.rongcloud.rtc.core.RendererCommon;

/**
 * 音视频显示的 VideoView 控件
 */
public class RadioRtcVideoView extends RCRTCVideoView {
    public RadioRtcVideoView(Context context) {
        super(context);
        init();
    }

    public RadioRtcVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
    }

    private float radio = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (radio > 0) {
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
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private boolean isBindVideo = false;
    private boolean isSubscribeAudio = false;

    public void setBindVideo(boolean bind) {
        isBindVideo = bind;
    }

    public boolean isBindVideo() {
        return isBindVideo;
    }

    public boolean isSubscribeAudio() {
        return isSubscribeAudio;
    }

    public void setSubscribeAudio(boolean subscribe) {
        isSubscribeAudio = subscribe;
    }

    public boolean isHasParent() {
        return getParent() != null;
    }

    public void getFrame(final OnFrameCallback callback) {
        addFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(final Bitmap bitmap) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFrame(bitmap);
                        }
                    }
                });

            }
        }, 1.0f);
    }

    public void setRadio(float radio) {
        this.radio = radio;
        postInvalidate();

    }

    public interface OnFrameCallback {
        void onFrame(Bitmap bitmap);
    }

}
