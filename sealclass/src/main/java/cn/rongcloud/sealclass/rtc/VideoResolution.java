package cn.rongcloud.sealclass.rtc;

import cn.rongcloud.rtc.base.RCRTCParamsType.RCRTCVideoResolution;

/**
 * 视频分辨率
 */
public enum VideoResolution {
    RESOLUTION_256_144_15f(0, "256x144", RCRTCVideoResolution.RESOLUTION_144_256),
    RESOLUTION_320_240_15f(1, "320x240", RCRTCVideoResolution.RESOLUTION_240_320),
    RESOLUTION_480_360_15f(2, "480x360", RCRTCVideoResolution.RESOLUTION_360_480),
    RESOLUTION_640_360_15f(3, "640x360", RCRTCVideoResolution.RESOLUTION_360_640),
    RESOLUTION_640_480_15f(4, "640x480", RCRTCVideoResolution.RESOLUTION_480_640),
    RESOLUTION_720_480_15f(5, "720x480", RCRTCVideoResolution.RESOLUTION_480_720),
    RESOLUTION_1280_720_15f(6, "1280x720", RCRTCVideoResolution.RESOLUTION_720_1280);

    private int id;
    private String resolution;
    private RCRTCVideoResolution profile;

    VideoResolution(int id, String resolution, RCRTCVideoResolution profile) {
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

    public RCRTCVideoResolution getProfile() {
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
