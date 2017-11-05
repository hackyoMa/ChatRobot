package com.chatrobot.service.impl;

import com.chatrobot.dao.ISubDao;
import com.chatrobot.dao.IUserDao;
import com.chatrobot.domain.Subscription;
import com.chatrobot.domain.User;
import com.chatrobot.service.ISubService;
import com.chatrobot.utils.SendEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 收藏操作接口实现
 * Created by hackyo on 2017/5/8.
 */
@Service
public class SubServiceImpl implements ISubService {

    private ISubDao subDao;
    private IUserDao userDao;

    @Autowired
    public SubServiceImpl(ISubDao subDao, IUserDao userDao) {
        this.subDao = subDao;
        this.userDao = userDao;
    }

    //查询用户收藏的数目
    public int selectSubscriptionPageCount(int id) {
        int subscriptionPageCount = subDao.selectSubscriptionCount(id);
        if (subscriptionPageCount > 0) {
            subscriptionPageCount = (subscriptionPageCount - 1) / 10 + 1;
        } else {
            subscriptionPageCount = 0;
        }
        return subscriptionPageCount;
    }

    //查询用户所有收藏
    public List<Subscription> selectAllSub(int userId, int page) {
        return subDao.selectAllSub(userId, (--page) * 10);
    }

    //查询用户是否有某个收藏
    public Subscription selectSub(int userId, String type, String content) {
        return subDao.selectSub(userId, type, content);
    }

    //添加一条收藏
    public String addSub(int userId, String type, String content) {
        if (subDao.selectSub(userId, type, content) == null) {
            Subscription sub = new Subscription();
            sub.setUserId(userId);
            sub.setType(type);
            sub.setContent(content);
            subDao.addSub(sub);
            return "successAdd";
        }
        return "exist";
    }

    //删除一条收藏
    public String removeSub(int userId, String type, String content) {
        subDao.removeSub(userId, type, content);
        return "successRemove";
    }

    //每星期五19:30:00向用户推送收藏
    @Scheduled(cron = "0 30 19 ? * FRI")
    public void pushSub() {
        List<User> allUser = userDao.selectAllUser();
        for (User user : allUser) {
            String regularPhone = "^1(3[0-9]|4[57]|5[0-35-9]|7[0135678]|8[0-9])\\d{8}$";
            if (Pattern.compile(regularPhone).matcher(user.getEmail()).matches()) {
                continue;
            }
            SendEmail.sendSub(user.getEmail(), subDao.selectSubGoods(user.getId()));
        }
    }

}
