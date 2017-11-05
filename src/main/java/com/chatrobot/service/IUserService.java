package com.chatrobot.service;

import com.chatrobot.domain.User;

/**
 * 用户操作接口
 * Created by hackyo on 2017/3/22.
 */
public interface IUserService {

    //通过email查询用户信息功能接口
    User selectUser(String email);

    //查询id用户信息功能接口
    User selectUserForId(int id);

    //获取邮件验证码接口
    String getVCode(String vCode, String email);

    //用户登录功能接口
    String userLogin(String email, String password);

    //用户注册功能接口
    String userRegistered(String email, String emailTrue, String vCode, String vCodeTrue, String username, String password, String role, String ip);

}
