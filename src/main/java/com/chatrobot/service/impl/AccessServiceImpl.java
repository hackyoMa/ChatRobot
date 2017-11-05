package com.chatrobot.service.impl;

import com.chatrobot.dao.IAccessDao;
import com.chatrobot.dao.IUserDao;
import com.chatrobot.domain.Access;
import com.chatrobot.service.IAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 访问日志操作实现
 * Created by hackyo on 2017/5/19.
 */
@Service
public class AccessServiceImpl implements IAccessService {

    private IAccessDao accessDao;
    private IUserDao userDao;

    @Autowired
    public AccessServiceImpl(IAccessDao accessDao, IUserDao userDao) {
        this.accessDao = accessDao;
        this.userDao = userDao;
    }

    //每天00:00:00为商户创建当天访问记录条目
    @Scheduled(cron = "0 0 0 * * ?")
    public void addAllAccess() {
        List<Integer> adminUserId = userDao.selectAllAdminUser();
        for (int userId : adminUserId) {
            addAccess(userId);
        }
    }

    //为某个商户添加当日访问记录条目
    public void addAccess(int userId) {
        Access access = new Access();
        access.setUserId(userId);
        access.setNoteDate(new Date());
        access.setAmount(0);
        accessDao.addAccess(access);
    }

    //当日店铺访问量加一
    public void addAmount(int userId) {
        SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd");
        String noteDate = dateFm.format(new Date());
        accessDao.addAmount(userId, noteDate);
    }

    //查询近七天访问量
    public List<Map> selectWeekAmount(int userId) {
        return accessDao.selectWeekAmount(userId);
    }

}
