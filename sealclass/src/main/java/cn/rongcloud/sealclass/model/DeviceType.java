package cn.rongcloud.sealclass.model;

/**
 * 设备类型
 */
public enum DeviceType {
    Microphone(0),
    Camera(1),
    UNKNOWN(-999);

    private int type;

    DeviceType(int type) {
        this.type = type;
    }

    public static DeviceType getDeviceType(int type) {
        DeviceType[] values = DeviceType.values();
        for (DeviceType deviceType : values) {
            if (deviceType.type == type) {
                return deviceType;
            }
        }

        return UNKNOWN;
    }
}
