package com.chatrobot.service;

import java.util.List;
import java.util.Map;

/**
 * 访问日志操作接口
 * Created by hackyo on 2017/5/19.
 */
public interface IAccessService {

    //为某个商户添加当日访问记录条目
    void addAccess(int userId);

    //当日店铺访问量加一
    void addAmount(int userId);

    //查询近七天访问量
    List<Map> selectWeekAmount(int userId);

}
