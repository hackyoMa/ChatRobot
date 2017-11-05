package com.chatrobot.controller;

import com.alibaba.fastjson.JSON;
import com.chatrobot.domain.User;
import com.chatrobot.service.IAccessService;
import com.chatrobot.service.IUserService;
import com.chatrobot.utils.CorpusOperat;
import com.chatrobot.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用户管理Controller
 * Created by hackyo on 2017/3/22.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/userController", produces = "text/html;charset=UTF-8", method = RequestMethod.POST)
public class UserController {

    private HttpServletRequest request;
    private HttpSession session;
    private IUserService us;
    private IAccessService as;

    @Autowired
    public UserController(HttpServletRequest request, HttpSession session, IUserService us, IAccessService as) {
        this.request = request;
        this.session = session;
        this.us = us;
        this.as = as;
    }

    //初始化页面信息
    @RequestMapping(value = "/initPage.do", params = {"sellerId"})
    public String initPage(@RequestParam String sellerId) {
        session.removeAttribute("bot");
        User user = (User) session.getAttribute("user");
        Map<String, String> contentMap = new HashMap<>();
        if (user != null) {
            contentMap.put("userStatus", "online");
            contentMap.put("username", user.getUsername());
            contentMap.put("userId", user.getId() + "");
            contentMap.put("userRole", user.getRole());
        } else {
            contentMap.put("userStatus", "offline");
        }
        contentMap.put("accessSeller", "false");
        if (sellerId != null) {
            String regular1 = "^\\d+$";
            if (Pattern.compile(regular1).matcher(sellerId).matches()) {
                User sellerUser = us.selectUserForId(Integer.parseInt(sellerId));
                if (sellerUser != null) {
                    if (sellerUser.getRole().equals("商户")) {
                        as.addAmount(Integer.parseInt(sellerId));
                        contentMap.put("sellerName", sellerUser.getUsername());
                        contentMap.put("accessSeller", "true");
                    }
                }
            }
        }
        return JSON.toJSONString(contentMap);
    }

    //获取验证码
    @RequestMapping(value = "/getVCode.do", params = {"email"})
    public String getVCode(@RequestParam String email) {
        Map<String, String> content = new HashMap<>();
        String operatingState = "hasLanded";
        String timeLeft = "60";
        User user = (User) session.getAttribute("user");
        if (user == null) {
            long nowTime = new Date().getTime();
            long againGetVCode = session.getAttribute("againGetVCode") == null ? 0 : (long) session.getAttribute("againGetVCode");
            if (nowTime > againGetVCode) {
                String vCode = Util.getRandomString();
                operatingState = us.getVCode(vCode, email);
                if (operatingState.equals("success")) {
                    session.setAttribute("vCode", vCode);
                    session.setAttribute("email", email);
                    session.setAttribute("againGetVCode", nowTime + 60000);
                }
            } else {
                operatingState = "repeatOperation";
                timeLeft = (int) ((againGetVCode - nowTime) / 1000) + "";
            }
        }
        content.put("status", operatingState);
        content.put("timeLeft", timeLeft);
        return JSON.toJSONString(content);
    }

    //用户登录
    @RequestMapping(value = "/userLogin.do", params = {"email", "password"})
    public String userLogin(@RequestParam String email, @RequestParam String password) {
        String operatingState = us.userLogin(email, password);
        if (operatingState.equals("success")) {
            session.setAttribute("user", us.selectUser(email));
        }
        return operatingState;
    }

    //用户注册
    @RequestMapping(value = "/userRegistered.do", params = {"email", "vCode", "username", "password", "role"})
    public String userRegistered(@RequestParam String email, @RequestParam String vCode, @RequestParam String username, @RequestParam String password, @RequestParam String role) {
        String operatingState = "hasLanded";
        User user = (User) session.getAttribute("user");
        if (user == null) {
            String emailTrue = (String) session.getAttribute("email");
            String vCodeTrue = (String) session.getAttribute("vCode");
            String ip = Util.getUserIp(request);
            operatingState = us.userRegistered(email, emailTrue, vCode, vCodeTrue, username, password, role, ip);
            if (operatingState.equals("success")) {
                User regUser = us.selectUser(email);
                session.setAttribute("user", regUser);
                session.removeAttribute("vCode");
                session.removeAttribute("email");
                session.removeAttribute("againGetVCode");
                if (role.equals("商户")) {
                    as.addAccess(regUser.getId());
                    CorpusOperat.addCorpus(regUser.getId());
                }
            }
        }
        return operatingState;
    }

    //用户注销
    @RequestMapping(value = "/userSignOut.do")
    public String userSignOut() {
        String operatingState = "NoLogin";
        User user = (User) session.getAttribute("user");
        if (user != null) {
            session.removeAttribute("user");
            operatingState = "success";
        }
        return operatingState;
    }

}
