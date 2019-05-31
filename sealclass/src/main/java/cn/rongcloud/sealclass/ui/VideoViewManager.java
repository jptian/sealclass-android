package cn.rongcloud.sealclass.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.rongcloud.sealclass.ui.view.RadioRtcVideoView;
import cn.rongcloud.sealclass.utils.log.SLog;

/**
 * VideoView 的管理类
 */
public class VideoViewManager {
    private static VideoViewManager manager = new VideoViewManager();
    private VideoViewManager() {

    }

    public static VideoViewManager getInstance() {
        return manager;
    }

    private ConcurrentHashMap<String, RadioRtcVideoView> videoViews = new ConcurrentHashMap<>();
    private RadioRtcVideoView shareScreenVideoView;

    public boolean containsKey(String userId) {
        return videoViews.containsKey(userId);

    }

    public RadioRtcVideoView get(String userId) {
        if (videoViews.containsKey(userId)) {
            RadioRtcVideoView videoView = videoViews.get(userId);
            SLog.d("video_manager", "get, userId =  " + userId + ", videoView == " + videoView);
            return videoView;
        }
        return null;
    }

    public void put(String userId, RadioRtcVideoView videoView) {
        if (!videoViews.containsKey(userId)) {
            SLog.d("video_manager", "create, userId =  " + userId + ", videoView == " + videoView);
            videoViews.put(userId, videoView);
        }
    }

    public RadioRtcVideoView remove(String userId) {
        if (videoViews.containsKey(userId)) {
            return videoViews.remove(userId);
        }
        return null;
    }

    public void getFrame(String userId) {
        if (videoViews.containsKey(userId)) {
            videoViews.get(userId).getDrawingCache(false);
        }
    }

    public RadioRtcVideoView getShareScreenVideoView() {
        return shareScreenVideoView;
    }


    public void setShareScreenVideoView(RadioRtcVideoView videoView) {
        shareScreenVideoView = videoView;
    }

    public void clear() {
        if (videoViews != null) {
            for (Map.Entry<String,RadioRtcVideoView>   entry : videoViews.entrySet()) {
                RadioRtcVideoView value = entry.getValue();
                if (value != null) {
                    value.setBindVideo(false);
                    value.setSubscribeAudio(false);
                    value.clearScreen();
                }
            }
            videoViews.clear();
        }
        if (shareScreenVideoView != null) {
            shareScreenVideoView.setBindVideo(false);
            shareScreenVideoView.clearScreen();
            shareScreenVideoView = null;
        }
    }

//    public Bitmap getVideoViewFrame(String userId) {
//        if (videoViews.containsKey(userId)) {
//            RadioRtcVideoView videoView = videoViews.get(userId);
//            videoView.getWindowVisibleDisplayFrame();
//            videoView.buildDrawingCache();
//            Bitmap bitmap = videoView.getDrawingCache();
//            return bitmap;
//        }
//        return null;
//    }
}
