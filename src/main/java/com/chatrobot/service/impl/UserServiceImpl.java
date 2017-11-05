package com.chatrobot.service.impl;

import com.chatrobot.dao.IUserDao;
import com.chatrobot.domain.User;
import com.chatrobot.service.IUserService;
import com.chatrobot.utils.SendEmail;
import com.chatrobot.utils.SendSms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * 用户操作接口实现
 * Created by hackyo on 2017/3/22.
 */
@Service
public class UserServiceImpl implements IUserService {

    private IUserDao userDao;

    @Autowired
    public UserServiceImpl(IUserDao userDao) {
        this.userDao = userDao;
    }

    //通过Email查询用户信息
    public User selectUser(String email) {
        return this.userDao.selectUser(email);
    }

    //通过id查询用户信息
    public User selectUserForId(int id) {
        return this.userDao.selectUserForId(id);
    }

    //获取邮件验证码
    public String getVCode(String vCode, String email) {
        int accountType = 0;
        String regularEmail = "[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]+$";
        String regularPhone = "^1(3[0-9]|4[57]|5[0-35-9]|7[0135678]|8[0-9])\\d{8}$";
        if (Pattern.compile(regularEmail).matcher(email).matches()) {
            accountType = 1;
        }
        if (Pattern.compile(regularPhone).matcher(email).matches()) {
            accountType = 2;
        }
        if (accountType != 0) {
            if (userDao.selectUser(email) == null) {
                boolean confirm = false;
                if (accountType == 1) {
                    confirm = SendEmail.sendVCode(email, vCode);
                }
                if (accountType == 2) {
                    confirm = SendSms.sendVCode(email, vCode);
                }
                if (confirm) {
                    return "success";
                } else {
                    return "emailNotAvailable";
                }
            } else {
                return "mailboxAlreadyRegistered";
            }
        } else {
            return "emailFormatIncorrect";
        }
    }

    //用户登录
    public String userLogin(String email, String password) {
        String regularEmail = "[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]+$";
        String regularPhone = "^1(3[0-9]|4[57]|5[0-35-9]|7[0135678]|8[0-9])\\d{8}$";
        if (Pattern.compile(regularEmail).matcher(email).matches() || Pattern.compile(regularPhone).matcher(email).matches()) {
            User user = userDao.selectUser(email);
            if (user != null) {
                String regular1 = "[\\w]{6,32}$";
                if (Pattern.compile(regular1).matcher(password).matches()) {
                    if (password.equals(user.getPassword())) {
                        return "success";
                    } else {
                        return "passwordError";
                    }
                } else {
                    return "passwordError";
                }
            } else {
                return "emailNotExist";
            }
        } else {
            return "emailError";
        }
    }

    //用户注册
    public String userRegistered(String email, String emailTrue, String vCode, String vCodeTrue, String username, String password, String role, String ip) {
        if (emailTrue != null && vCodeTrue != null) {
            if (emailTrue.equals(email) && vCodeTrue.equals(vCode.toUpperCase())) {
                String regular = "[\\u4E00-\\u9FA5\\w]{1,16}$";
                if (Pattern.compile(regular).matcher(username).matches()) {
                    String regular1 = "[\\w]{6,32}$";
                    if (Pattern.compile(regular1).matcher(password).matches()) {
                        if (role.equals("商户") || role.equals("顾客")) {
                            User user = new User();
                            user.setEmail(email);
                            user.setPassword(password);
                            user.setUsername(username);
                            user.setRole(role);
                            user.setStatus(0);
                            user.setRegTime(new Date());
                            user.setRegIp(ip);
                            userDao.addUser(user);
                            return "success";
                        } else {
                            return "identityNotCorrect";
                        }
                    } else {
                        return "passwordFormatIncorrect";
                    }
                } else {
                    return "usernameFormatIncorrect";
                }
            } else {
                return "incorrectVerificationCode";
            }
        } else {
            return "notGetVCode";
        }
    }

}
