package cn.rongcloud.sealclass.model;

import java.io.Serializable;
import java.util.Objects;

public class ClassMember implements Serializable {
    private boolean camera;
    private long joinTime;
    private boolean microphone;
    private int role;
    private String userId;
    private String userName;

    public boolean isCamera() {
        return camera;
    }

    public void setCamera(boolean camera) {
        this.camera = camera;
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public boolean isMicrophone() {
        return microphone;
    }

    public void setMicrophone(boolean microphone) {
        this.microphone = microphone;
    }

    public Role getRole() {
        return Role.createRole(role);
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassMember member = (ClassMember) o;
        return Objects.equals(userId, member.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "ClassMember{" +
                "camera=" + camera +
                ", joinTime=" + joinTime +
                ", microphone=" + microphone +
                ", role=" + role +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
