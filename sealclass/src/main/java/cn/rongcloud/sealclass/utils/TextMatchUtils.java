package cn.rongcloud.sealclass.utils;

import android.text.TextUtils;

public class TextMatchUtils {
    /**
     * 适配汉字、字母、数字
     */
    private static final String REGEX_HANZI_DIGIST_LETTER = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$";
    /**
     * 适配字母、数字
     */
    private static final String REGEX_DIGIST_LETTER = "^[a-zA-Z0-9]+$";

    /**
     * 判断是否是汉字，数字，字母
     *
     * @param content
     * @return
     */
    public static boolean isHanziDigistsLetters(String content) {
        if (TextUtils.isEmpty(content)) return false;

        return content.matches(REGEX_HANZI_DIGIST_LETTER);
    }

    /**
     * 判断是否是数字，字母
     *
     * @param content
     * @return
     */
    public static boolean isDigistsLetters(String content) {
        if (TextUtils.isEmpty(content)) return false;

        return content.matches(REGEX_DIGIST_LETTER);
    }
}
