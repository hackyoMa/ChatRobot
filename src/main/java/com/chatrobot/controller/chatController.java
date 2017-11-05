package com.chatrobot.controller;

import bitoflife.chatterbean.AliceBot;
import com.alibaba.fastjson.JSON;
import com.chatrobot.domain.User;
import com.chatrobot.service.IHistoryService;
import com.chatrobot.service.IQACoreService;
import com.chatrobot.utils.FindKeyword;
import com.chatrobot.utils.ImageAnalysis;
import com.chatrobot.utils.NoAnswer;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户聊天Controller
 * Created by hackyo on 2017/3/22.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/chatController", produces = "text/html;charset=UTF-8", method = RequestMethod.POST)
public class chatController {

    private HttpSession session;
    private IQACoreService qa;
    private IHistoryService hs;

    @Autowired
    public chatController(HttpSession session, IQACoreService qa, IHistoryService hs) {
        this.session = session;
        this.qa = qa;
        this.hs = hs;
    }

    //接收并返回信息
    @RequestMapping(value = "/chat.do", params = {"message", "sellerId"})
    public String chat(@RequestParam String message, @RequestParam String sellerId) {
        String type;
        String reply;
        String keyword = FindKeyword.findKeywords(message);
        if (keyword != null) {
            type = "select";
            reply = keyword;
        } else {
            type = "chat";
            AliceBot bot = (AliceBot) session.getAttribute("bot");
            if (bot == null) {
                if (sellerId.equals("")) {
                    bot = qa.getBot(1);
                } else {
                    bot = qa.getBot(Integer.parseInt(sellerId));
                }
                session.setAttribute("bot", bot);
            }
            reply = qa.thinkAnswer(message, bot);
            if (reply.equals("语料库中无答案")) {
                reply = NoAnswer.organizeMessage(message, session.getId());
            }
        }
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("type", type);
        contentMap.put("reply", reply);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            hs.addHistory(user.getEmail(), "bot", message, type);
            if (type.equals("select")) {
                hs.addHistory("bot", user.getEmail(), "为您查询了：\"" + reply + "\"", type);
            } else {
                hs.addHistory("bot", user.getEmail(), reply, type);
            }
        }
        return JSON.toJSONString(contentMap);
    }

    //接收图片并分析
    @RequestMapping(value = "/searchImage.do", params = {"image"})
    public String searchImage(@RequestParam String image) {
        String reply = null;
        if (!image.equals("")) {
            byte[] buffer = Base64.decodeBase64(image.getBytes());
            String keyWords = ImageAnalysis.identify(buffer);
            reply = keyWords != null ? FindKeyword.findKeywords(keyWords) : null;
        }
        User user = (User) session.getAttribute("user");
        String type = "select";
        if (user != null) {
            hs.addHistory(user.getEmail(), "bot", "图片查询", type);
            if (reply != null) {
                hs.addHistory("bot", user.getEmail(), "为您查询了：\"" + reply + "\"", type);
            } else {
                hs.addHistory("bot", user.getEmail(), "小明看不出这是什么哦。", type);
            }
        }
        return reply;
    }

}
