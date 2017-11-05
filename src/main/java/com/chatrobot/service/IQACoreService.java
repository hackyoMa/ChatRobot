package com.chatrobot.service;

import bitoflife.chatterbean.AliceBot;

/**
 * 问答功能接口
 * Created by hackyo on 2017/3/31.
 */
public interface IQACoreService {

    //获取机器人
    AliceBot getBot(int sellerId);

    //查找回复内容
    String thinkAnswer(String message, AliceBot bot);

}
