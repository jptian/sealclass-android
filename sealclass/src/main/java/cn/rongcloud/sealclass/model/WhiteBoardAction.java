package cn.rongcloud.sealclass.model;

/**
 * 白板操作行为
 */
public enum  WhiteBoardAction {
    CREATE(1),//创建白板
    DELETE(2),//删除白板
    UNKNOWN(-999);

    private int value;

    WhiteBoardAction(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public static WhiteBoardAction getAction(int value){
        WhiteBoardAction[] values = WhiteBoardAction.values();
        for (WhiteBoardAction action : values) {
            if (action.value == value) {
                return action;
            }
        }

        return UNKNOWN;
    }
}
