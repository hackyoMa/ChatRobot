package com.chatrobot.service.impl;

import com.chatrobot.dao.IHistoryDao;
import com.chatrobot.domain.History;
import com.chatrobot.service.IHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 历史记录查询实现
 * Created by hackyo on 2017/4/27.
 */
@Service
public class HistoryServiceImpl implements IHistoryService {

    private IHistoryDao historyDao;

    @Autowired
    public HistoryServiceImpl(IHistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    //查询用户聊天记录数目的页码总数
    public int selectHistoryPageCount(String email) {
        int historyPageCount = historyDao.selectHistoryCount(email);
        if (historyPageCount > 0) {
            historyPageCount = (historyPageCount - 1) / 10 + 1;
        } else {
            historyPageCount = 0;
        }
        return historyPageCount;
    }

    //通过email查询用户指定行数的聊天记录
    public List<History> selectHistory(String email, int page) {
        return historyDao.selectHistory(email, (--page) * 10);
    }

    //添加聊天记录
    public void addHistory(String sender, String receiver, String content, String type) {
        History history = new History();
        history.setSender(sender);
        history.setReceiver(receiver);
        history.setContent(content);
        history.setType(type);
        history.setTime(new Date());
        historyDao.addHistory(history);
    }

}
