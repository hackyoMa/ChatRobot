package com.chatrobot.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.List;

/**
 * 提取查询关键词
 * Created by hackyo on 2017/4/26.
 */
public final class FindKeyword {

    //使用相应算法查找用户关键词
    public static String findKeywords(String content) {
        List<Term> termList = HanLP.segment(content);
        List<String> keywordList = HanLP.extractKeyword(content, (content.length() / 2) + 1);
        for (Term t : termList) {
            if ((t.nature.toString()).equals("n")) {
                for (String k : keywordList) {
                    if (k.equals(t.word)) {
                        return k;
                    }
                }
            }
        }
        return null;
    }

}
