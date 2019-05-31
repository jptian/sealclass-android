package cn.rongcloud.sealclass.rtc;

import cn.rongcloud.rtc.RongRTCConfig;

/**
 * 视频分辨率
 */
public enum VideoResolution {
    RESOLUTION_256_144_15f(0, "256x144", RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_144P_15f),
    RESOLUTION_320_240_15f(1, "320x240", RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_240P_15f),
    RESOLUTION_480_360_15f(2, "480x360", RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_15f_1),
    RESOLUTION_640_360_15f(3, "640x360", RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_360P_15f_2),
    RESOLUTION_640_480_15f(4, "640x480", RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_15f_1),
    RESOLUTION_720_480_15f(5, "720x480", RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_480P_15f_2),
    RESOLUTION_1280_720_15f(6, "1280x720", RongRTCConfig.RongRTCVideoProfile.RONGRTC_VIDEO_PROFILE_720P_15f);

    private int id;
    private String resolution;
    private RongRTCConfig.RongRTCVideoProfile profile;

    VideoResolution(int id, String resolution, RongRTCConfig.RongRTCVideoProfile profile) {
        this.id = id;
        this.resolution = resolution;
        this.profile = profile;
    }

    public int getId() {
        return id;
    }

    public String getResolution() {
        return resolution;
    }

    public RongRTCConfig.RongRTCVideoProfile getProfile() {
        return profile;
    }

    public static VideoResolution getById(int id) {
        for (VideoResolution resolution : VideoResolution.values()) {
            if (resolution.id == id) {
                return resolution;
            }
        }

        return RESOLUTION_256_144_15f;
    }
}
