package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {
    public static void main(String[] args) {
        String str = "\"http://tup.yhdm.tv/?vid=https://gss3.baidu.com/6LZ0ej3k1Qd3ote6lo7D0j9wehsv/tieba-smallvideo/1038_d7bf6c79532db60d58cd4db7e62ca3d9.mp4$mp4\" scrolling=\"no\" allowfullscreen=\"true\" allowtransparency=\"true\" border=\"0\" marginwidth=\"0\" marginheight=\"0\" width=\"100%\" height=\"100%\" frameborder=\"no\"";
        Matcher matcher = Pattern.compile("\"(http://.*\\.mp4[^\"]*)\"").matcher(str);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}
