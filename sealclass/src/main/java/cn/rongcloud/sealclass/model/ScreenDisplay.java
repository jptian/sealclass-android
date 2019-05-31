package cn.rongcloud.sealclass.model;

import android.text.TextUtils;

import cn.rongcloud.sealclass.utils.log.SLog;

/**
 * 屏幕显示
 */
public class ScreenDisplay {
    private final static String DISPLAY_PREFIX = "display://";
    private final static String DISPLAY_TYPE = "type";
    private final static String DISPLAY_URI = "uri";
    private final static String DISPLAY_USER_ID = "userId";

    private String userId;
    private String whiteBoardUri;
    private Display type;
    private ClassMember classMember;

    public static ScreenDisplay createScreenDisplay(String display) {
        ScreenDisplay screenDisplay = new ScreenDisplay();
        if(TextUtils.isEmpty(display) || !display.startsWith(DISPLAY_PREFIX)){
            return screenDisplay;
        }
        String params = display.substring(DISPLAY_PREFIX.length());
        try {
            String[] paramArray = params.split("\\?");
            for (String param : paramArray) {
                String[] keyAndValue = param.split("=");

                // 当没有 key 和 value 两个值时不进行处理
                if(keyAndValue.length < 2){
                    continue;
                }

                String key = keyAndValue[0];
                String value = keyAndValue[1];

                if (DISPLAY_TYPE.equals(key)) { // 显示类型
                    screenDisplay.setType(Display.getDisplay(Integer.valueOf(value)));
                } else if (DISPLAY_URI.equals(key)) {   // 白板地址
                    screenDisplay.setWhiteBoardUri(value);
                } else if (DISPLAY_USER_ID.equals(key)) {   // 若显示为用户类型时，用户 id
                    screenDisplay.setUserId(value);
                } else if(screenDisplay.getWhiteBoardUri() != null){ //若已存在白板信息，则后续参数为白板的参数
                    String oriUrl = screenDisplay.getWhiteBoardUri();
                    screenDisplay.setWhiteBoardUri(oriUrl + "?" + param);
                }
            }
        } catch (Exception e) {
            SLog.e(SLog.TAG_DATA, "Display format error:" + display);
        }

        return screenDisplay;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWhiteBoardUri() {
        return whiteBoardUri;
    }

    public void setWhiteBoardUri(String whiteBoardUri) {
        this.whiteBoardUri = whiteBoardUri;
    }

    public Display getType() {
        return type;
    }

    public void setType(Display type) {
        this.type = type;
    }

    public ClassMember getClassMember() {
        return classMember;
    }

    public void setClassMember(ClassMember classMember) {
        this.classMember = classMember;
    }

    public enum Display {
        ASSISTANT(0),   //0 助教
        LECTURER(1),    //1 讲师
        WHITEBOARD(2),  //2 白板
        SCREEN(3),      //3 屏幕
        NONE(4);        //4 清空 display

        private int type;

        Display(int type) {
            this.type = type;
        }

        public static Display getDisplay(int type) {
            Display[] values = Display.values();
            for (Display display : values) {
                if (display.type == type) {
                    return display;
                }
            }

            return NONE;
        }

        public int getType() {
            return type;
        }
    }

    @Override
    public String toString() {
        return "ScreenDisplay{" +
                "userId='" + userId + '\'' +
                ", whiteBoardUri='" + whiteBoardUri + '\'' +
                ", type=" + type +
                ", classMember=" + classMember +
                '}';
    }
}
