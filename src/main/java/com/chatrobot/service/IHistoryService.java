package com.chatrobot.service;

import com.chatrobot.domain.History;

import java.util.List;

/**
 * 历史记录操作接口
 * Created by hackyo on 2017/4/27.
 */
public interface IHistoryService {

    //查询用户聊天记录数目的页码总数
    int selectHistoryPageCount(String email);

    //通过email查询用户指定行数的聊天记录
    List<History> selectHistory(String email, int page);

    //添加聊天记录
    void addHistory(String sender, String receiver, String content, String type);

}
