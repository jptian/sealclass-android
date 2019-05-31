package cn.rongcloud.sealclass.model;

import android.graphics.Bitmap;

/**
 *  显示资源-用户显示相关
 */
public class UserDisplayResource {
    private ClassMember classMember;
    private Bitmap screenShotBitmap;
    private String screenShotUrl;

    public ClassMember getClassMember() {
        return classMember;
    }

    public void setClassMember(ClassMember classMember) {
        this.classMember = classMember;
    }

    public Bitmap getScreenShotBitmap() {
        return screenShotBitmap;
    }

    public void setScreenShotBitmap(Bitmap screenShotBitmap) {
        this.screenShotBitmap = screenShotBitmap;
    }

    public String getScreenShotUrl() {
        return screenShotUrl;
    }

    public void setScreenShotUrl(String screenShotUrl) {
        this.screenShotUrl = screenShotUrl;
    }
}
