package cn.rongcloud.sealclass.model;

import java.io.Serializable;

public class WhiteBoard implements Serializable {
    private int curPg;
    private String name;
    private String whiteboardId;

    public int getCurPg() {
        return curPg;
    }

    public void setCurPg(int curPg) {
        this.curPg = curPg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWhiteboardId() {
        return whiteboardId;
    }

    public void setWhiteboardId(String whiteboardId) {
        this.whiteboardId = whiteboardId;
    }
}
