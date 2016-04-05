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
            return str;
        } else if (str.startsWith("\"") && str.endsWith("\"")) {
            return str;
        } else if (isNumeric(str)) {
            return str;
        } else {
            return "\"" + str + "\"";
        }
    }

    public static boolean isNumeric(String str) {
        return StringUtils.isNumeric(str);
    }
}
