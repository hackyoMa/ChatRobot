package com.chatrobot.controller;

import com.alibaba.fastjson.JSON;
import com.chatrobot.domain.Goods;
import com.chatrobot.domain.History;
import com.chatrobot.domain.Subscription;
import com.chatrobot.domain.User;
import com.chatrobot.service.IGoodsService;
import com.chatrobot.service.IHistoryService;
import com.chatrobot.service.ISubService;
import com.chatrobot.utils.CorpusOperat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能列表Controller
 * Created by hackyo on 2017/4/27.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/featuresController", produces = "text/html;charset=UTF-8", method = RequestMethod.POST)
public class featuresController {

    private HttpSession session;
    private IHistoryService hs;
    private ISubService ss;
    private IGoodsService gs;

    @Autowired
    public featuresController(HttpSession session, IHistoryService hs, ISubService ss, IGoodsService gs) {
        this.session = session;
        this.hs = hs;
        this.ss = ss;
        this.gs = gs;
    }

    //查询指定页码数中的聊天记录
    @RequestMapping(value = "/selectHistory.do", params = {"page"})
    public String selectHistory(@RequestParam String page) {
        Map<String, Object> historyMap = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            int historyPageCount = hs.selectHistoryPageCount(user.getEmail());
            historyMap.put("pageNum", historyPageCount + "");
            int pageInt = Integer.parseInt(page);
            if (pageInt >= 1 && pageInt <= historyPageCount) {
                List<History> historyList = hs.selectHistory(user.getEmail(), pageInt);
                historyMap.put("history", historyList);
            }
        }
        return JSON.toJSONString(historyMap);
    }

    //查询指定页码数中的收藏内容
    @RequestMapping(value = "/selectSubscription.do", params = {"page"})
    public String selectSubscription(@RequestParam String page) {
        Map<String, Object> subscriptionMap = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            int subPageCount = ss.selectSubscriptionPageCount(user.getId());
            subscriptionMap.put("pageNum", subPageCount + "");
            int pageInt = Integer.parseInt(page);
            if (pageInt >= 1 && pageInt <= subPageCount) {
                List<Subscription> subscriptionList = ss.selectAllSub(user.getId(), pageInt);
                subscriptionMap.put("subscription", subscriptionList);
            }
        }
        return JSON.toJSONString(subscriptionMap);
    }

    //添加或删除收藏
    @RequestMapping(value = "/subscription.do", params = {"opera", "type", "content"})
    public String subscription(@RequestParam String opera, @RequestParam String type, @RequestParam String content) {
        String response = null;
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if (opera.equals("add")) {
                response = ss.addSub(user.getId(), type, content);
                if (type.equals("goods")) {
                    gs.updateGoodsHeat(2, Integer.parseInt(content));
                }
            }
            if (opera.equals("remove")) {
                response = ss.removeSub(user.getId(), type, content);
                if (type.equals("goods")) {
                    gs.updateGoodsHeat(-2, Integer.parseInt(content));
                }
            }
        }
        return response;
    }

    //用户查询热搜
    @RequestMapping(value = "/hotSearch.do", params = {"sellerId"})
    public String hotSearch(@RequestParam String sellerId) {
        Map<String, List> contentMap = new HashMap<>();
        List<Map> hotGoods = gs.selectHotSearch();
        if (!sellerId.equals("")) {
            List<Map> headOff = CorpusOperat.selectCategory(Integer.parseInt(sellerId));
            contentMap.put("headOff", headOff);
        }
        contentMap.put("hotGoods", hotGoods);
        return JSON.toJSONString(contentMap);
    }

    //返回推荐内容
    @RequestMapping(value = "/recommend.do")
    public String recommend() {
        User user = (User) session.getAttribute("user");
        Map<String, Object> contentMap = new HashMap<>();
        String status = "offline";
        if (user != null) {
            List<Goods> recommendGoods = gs.selectRecommend(user.getId(), user.getEmail());
            if (recommendGoods != null) {
                status = "success";
                contentMap.put("goods", recommendGoods);
            } else {
                status = "null";
            }
        }
        contentMap.put("status", status);
        return JSON.toJSONString(contentMap);
    }

}
