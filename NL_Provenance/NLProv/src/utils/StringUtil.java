package utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: NAVE-FROST
 * Date: 08/02/16
 * Time: 14:15
 */
public class StringUtil {

    public static String getQuoatedString(String str) {
        if (str.startsWith("'") && str.endsWith("'")) {
            return removeSuffixDot(str);
        } else if (str.startsWith("\"") && str.endsWith("\"")) {
            return removeSuffixDot(str);
        } else if (isNumeric(str)) {
            return removeSuffixDot(str);
        } else {
            String[] strSplit = str.split(" ");
            if (strSplit.length <= 3) {
                return removeSuffixDot(str);
            } else {
                return "\"" + removeSuffixDot(str) + "\"";
            }
        }
    }

    public static boolean isNumeric(String str) {
        return StringUtils.isNumeric(str);
    }

    public static String removeSuffixDot(String str) {
        if (str.endsWith(".\"") || str.endsWith(".'")) {
            return str.substring(0, str.length() - 2) + str.charAt(str.length() - 1);
        } else if (str.endsWith(".")) {
            return str.substring(0, str.length() - 1);
        } else {
            return str;
        }
    }
}
