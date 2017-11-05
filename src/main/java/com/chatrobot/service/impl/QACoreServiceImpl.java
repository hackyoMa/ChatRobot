package com.chatrobot.service.impl;

import bitoflife.chatterbean.AliceBot;
import bitoflife.chatterbean.AliceBotMother;
import com.chatrobot.service.IQACoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 问答功能实现
 * Created by hackyo on 2017/3/31.
 */
@Service
public class QACoreServiceImpl implements IQACoreService {

    private AliceBotMother mother;

    @Autowired
    public QACoreServiceImpl() {
        mother = new AliceBotMother();
        mother.setUp();
    }

    //获取新的机器
    public AliceBot getBot(int sellerId) {
        try {
            mother.setCorpusFile(sellerId + ".aiml");
            return mother.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //从AIML中查找回复内容
    public String thinkAnswer(String message, AliceBot bot) {
        String answer;
        try {
            answer = bot.respond(message);
        } catch (NullPointerException e) {
            answer = "语料库中无答案";
        }
        return answer;
    }

}
