package cn.rongcloud.sealclass.model;

public class DeviceChange {
    public String userId;
    public DeviceType deviceType;
    public boolean isEnable;
    public DeviceChange(String userId, DeviceType deviceType, boolean isEnable) {
        this.userId = userId;
        this.deviceType = deviceType;
        this.isEnable = isEnable;
    }
}
