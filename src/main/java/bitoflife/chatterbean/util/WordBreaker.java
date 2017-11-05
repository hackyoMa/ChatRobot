package bitoflife.chatterbean.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

public class WordBreaker {

    public static String participle(String str) {
        if (str.getBytes().length == str.length()) {
            return str;
        }
        String newStr = "";
        for (Term t : HanLP.segment(str)) {
            newStr += t.word + " ";
        }
        return newStr;
    }

}
