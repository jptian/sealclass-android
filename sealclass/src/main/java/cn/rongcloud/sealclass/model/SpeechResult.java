package cn.rongcloud.sealclass.model;

/**
 * 申请发言结果
 */
public class SpeechResult {
    String opUserId;
    String opUserName;
    String reqUserId;
    String reqUserName;
    boolean isAccept;   // 是否同意

    public String getOpUserId() {
        return opUserId;
    }

    public void setOpUserId(String opUserId) {
        this.opUserId = opUserId;
    }

    public String getOpUserName() {
        return opUserName;
    }

    public void setOpUserName(String opUserName) {
        this.opUserName = opUserName;
    }

    public String getReqUserId() {
        return reqUserId;
    }

    public void setReqUserId(String reqUserId) {
        this.reqUserId = reqUserId;
    }

    public String getReqUserName() {
        return reqUserName;
    }

    public void setReqUserName(String reqUserName) {
        this.reqUserName = reqUserName;
    }

    public boolean isAccept() {
        return isAccept;
    }

    public void setAccept(boolean accept) {
        isAccept = accept;
    }
}
