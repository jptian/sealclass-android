package cn.rongcloud.sealclass.model;

import java.util.List;

public class VideoClassMemberData {
    private List<ClassMember> member;
    private boolean isNoUpdate = false;

    public List<ClassMember> getMember() {
        return member;
    }

    public void setMember(List<ClassMember> member) {
        this.member = member;
    }

    public boolean isNoUpdate() {
        return isNoUpdate;
    }

    public void setNoUpdate(boolean noUpdate) {
        isNoUpdate = noUpdate;
    }
}
