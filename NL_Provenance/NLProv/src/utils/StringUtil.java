package utils;

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
        } else {
            return "\"" + str + "\"";
        }
    }
}
