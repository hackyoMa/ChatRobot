package com.chatrobot.service;

import com.chatrobot.domain.Subscription;

import java.util.List;

/**
 * 收藏操作接口
 * Created by hackyo on 2017/5/8.
 */
public interface ISubService {

    //查询用户收藏的数目
    int selectSubscriptionPageCount(int id);

    //查询用户所有收藏
    List<Subscription> selectAllSub(int userId, int page);

    //查询用户是否有某个收藏
    Subscription selectSub(int userId, String type, String content);

    //添加一条收藏
    String addSub(int userId, String type, String content);

    //删除一条收藏
    String removeSub(int userId, String type, String content);

}
