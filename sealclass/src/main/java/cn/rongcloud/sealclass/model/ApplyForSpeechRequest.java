package cn.rongcloud.sealclass.model;

/**
 * 旁听者请求发言成为课堂成员
 */
public class ApplyForSpeechRequest {
    private String reqUserId; // 请求人用户id
    private String reqUserName; // 请求人用户名
    private String ticket; // 请求凭证

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

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
