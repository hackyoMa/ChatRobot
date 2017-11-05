package com.chatrobot.controller;

import com.alibaba.fastjson.JSON;
import com.chatrobot.domain.Goods;
import com.chatrobot.domain.User;
import com.chatrobot.service.IGoodsService;
import com.chatrobot.service.ISubService;
import com.chatrobot.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 顾客操作Controller
 * Created by hackyo on 2017/4/27.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/buyerController", produces = "text/html;charset=UTF-8", method = RequestMethod.POST)
public class buyerController {

    private HttpSession session;
    private IGoodsService gs;
    private IUserService us;
    private ISubService ss;

    @Autowired
    public buyerController(HttpSession session, IGoodsService gs, IUserService us, ISubService ss) {
        this.session = session;
        this.gs = gs;
        this.us = us;
        this.ss = ss;
    }

    //顾客查看商品
    @RequestMapping(value = "/showGoods.do", params = {"goodsId"})
    public String showGoods(@RequestParam String goodsId) {
        Goods goods = gs.selectSomeGoods(Integer.parseInt(goodsId));
        gs.updateGoodsHeat(1, Integer.parseInt(goodsId));
        String sellersName = us.selectUserForId(goods.getUserId()).getUsername();
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("subscription", "false");
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if (ss.selectSub(user.getId(), "goods", goodsId) != null) {
                contentMap.put("subscription", "true");
            }
        }
        contentMap.put("sellersName", sellersName);
        contentMap.put("goods", goods);
        return JSON.toJSONString(contentMap);
    }

    //通过关键词查询商品
    @RequestMapping(value = "/selectLikeGoods.do", params = {"keyword", "sellerId", "province", "lowestPrice", "highestPrice", "selectAll"})
    public String selectLikeGoods(@RequestParam String keyword, @RequestParam String sellerId, @RequestParam String province, @RequestParam String lowestPrice, @RequestParam String highestPrice, @RequestParam String selectAll) {
        Map<String, Object> contentMap = new HashMap<>();
        List<Map> likeGoods;
        String condition = "1 = 1";
        if (!sellerId.equals("")) {
            condition += " AND userId = " + Integer.parseInt(sellerId);
        }
        if (!province.equals("发货地")) {
            if (province.length() == 2 || province.length() == 3) {
                switch (province) {
                    case "江浙沪":
                        condition += " AND (introduction LIKE '%产地：江苏%' OR introduction LIKE '%产地：浙江%' OR introduction LIKE '%产地：上海%')";
                        break;
                    case "珠三角":
                        condition += " AND introduction LIKE '%产地：广东%'";
                        break;
                    case "京津冀":
                        condition += " AND (introduction LIKE '%产地：北京%' OR introduction LIKE '%产地：天津%' OR introduction LIKE '%产地：河北%')";
                        break;
                    case "东三省":
                        condition += " AND (introduction LIKE '%产地：辽宁%' OR introduction LIKE '%产地：吉林%' OR introduction LIKE '%产地：黑龙江%')";
                        break;
                    case "港澳台":
                        condition += " AND (introduction LIKE '%产地：香港%' OR introduction LIKE '%产地：澳门%' OR introduction LIKE '%产地：台湾%')";
                        break;
                    default:
                        condition += " AND introduction LIKE '%产地：" + province + "%'";
                        break;
                }
            }
        }
        if (!lowestPrice.equals("")) {
            condition += " AND price >= " + Double.parseDouble(lowestPrice);
        }
        if (!highestPrice.equals("")) {
            condition += " AND price <= " + Double.parseDouble(highestPrice);
        }
        if (selectAll.equals("false")) {
            likeGoods = gs.selectLikeGoods(keyword, condition);
            contentMap.put("selectAll", "false");
        } else {
            likeGoods = gs.selectLikeAllGoods(keyword, condition);
            contentMap.put("selectAll", "true");
        }
        contentMap.put("goods", likeGoods);
        contentMap.put("subscription", "false");
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if (ss.selectSub(user.getId(), "select", keyword) != null) {
                contentMap.put("subscription", "true");
            }
        }
        return JSON.toJSONString(contentMap);
    }

}
