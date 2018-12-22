package plasma.blackhole.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A templating engine for replacing bound strings using ${key} syntax.
 */
public final class TemplateEngine {

    public static String bind(String template, String... kwargs) {
        if (kwargs.length % 2 != 0)
            throw new IllegalArgumentException("kwargs must follow the pattern of key, value pairs!");

        Map<String, String> args = new HashMap<>();
        String lastKey = "";
        for (int i = 0; i < kwargs.length; i++) {
            if (i % 2 == 0)
                lastKey = kwargs[i];
            else {
                args.put(lastKey, kwargs[i]);
                lastKey = "";
            }
        }

        //O(n) scanner for finding and replacing ${} tags, note that it makes potentially dangerous assumptions
        StringBuilder sb = new StringBuilder();
        boolean lastWasDollarSign = false;
        boolean isFollowingTag = false;
        StringBuilder tagScan = new StringBuilder();
        for (char c : template.toCharArray()) {
            if (c == '$') {
                lastWasDollarSign = true;
            } else if (lastWasDollarSign) {
                if (c == '{') {
                    lastWasDollarSign = false;
                    isFollowingTag = true;
                } else {
                    sb.append('$').append('{');
                    lastWasDollarSign = false;
                }
            } else if (isFollowingTag) {
                if (c == '}') {
                    sb.append(args.get(tagScan.toString()));
                    tagScan = new StringBuilder();
                    isFollowingTag = false;
                } else {
                    tagScan.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
