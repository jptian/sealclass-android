package cn.rongcloud.sealclass.model;

public class UserInfo extends ClassMember {
    private boolean applySpeeching = false;

    public boolean isApplySpeeching() {
        return applySpeeching;
    }

    public void setApplySpeeching(boolean applySpeeching) {
        this.applySpeeching = applySpeeching;
    }
}
