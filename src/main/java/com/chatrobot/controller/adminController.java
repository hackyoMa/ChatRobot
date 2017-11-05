package com.chatrobot.controller;

import com.alibaba.fastjson.JSON;
import com.chatrobot.domain.Goods;
import com.chatrobot.domain.User;
import com.chatrobot.service.IAccessService;
import com.chatrobot.service.IGoodsService;
import com.chatrobot.utils.CorpusOperat;
import com.chatrobot.utils.FindKeyword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺管理Controller
 * Created by hackyo on 2017/4/18.
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/adminController", produces = "text/html;charset=UTF-8", method = RequestMethod.POST)
public class adminController {

    private HttpSession session;
    private IGoodsService gs;
    private IAccessService as;

    @Autowired
    public adminController(HttpSession session, IGoodsService gs, IAccessService as) {
        this.session = session;
        this.gs = gs;
        this.as = as;
    }

    //商户查看统计信息
    @RequestMapping(value = "/statistics.do")
    public String statistics() {
        Map<String, List<Map>> contentMap = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if (user.getRole().equals("商户")) {
                List<Map> weekAmount = as.selectWeekAmount(user.getId());
                List<Map> hotSearch = gs.selectAdminHotSearch(user.getId());
                contentMap.put("weekAmount", weekAmount);
                contentMap.put("hotSearch", hotSearch);
            }
        }
        return JSON.toJSONString(contentMap);
    }

    //添加一件商品
    @RequestMapping(value = "/addGoods.do", params = {"name", "price", "sort", "introduction"})
    public String addGoods(@RequestParam String name, @RequestParam String price, @RequestParam String sort, @RequestParam String introduction) {
        String content = "noLogin";
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if (user.getRole().equals("商户")) {
                String keyword = FindKeyword.findKeywords(sort);
                if (keyword != null) {
                    content = gs.addGoods(user.getId(), name, price, sort, introduction);
                } else {
                    content = "illegal";
                }
            }
        }
        return content;
    }

    //查询指定页数的商品
    @RequestMapping(value = "/selectGoods.do", params = {"page"})
    public String selectGoods(@RequestParam String page) {
        Map<String, Object> goodsMap = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if (user.getRole().equals("商户")) {
                int goodsPageCount = gs.selectGoodsPageCount(user.getId());
                goodsMap.put("pageNum", goodsPageCount + "");
                int pageInt = Integer.parseInt(page);
                if (pageInt >= 1 && pageInt <= goodsPageCount) {
                    List<Goods> goodsList = gs.selectGoods(user.getId(), pageInt);
                    goodsMap.put("goods", goodsList);
                }
            }
        }
        return JSON.toJSONString(goodsMap);
    }

    //删除一件商品
    @RequestMapping(value = "/removeGoods.do", params = {"id"})
    public String removeGoods(@RequestParam String id) {
        String content = null;
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if (user.getRole().equals("商户")) {
                gs.removeGoods(user.getId(), Integer.parseInt(id));
                content = "success";
            }
        }
        return content;
    }

    //修改商品信息
    @RequestMapping(value = "/updateGoods.do", params = {"id", "name", "price", "sort", "introduction"})
    public String updateGoods(@RequestParam String id, @RequestParam String name, @RequestParam String price, @RequestParam String sort, @RequestParam String introduction) {
        String content = "noLogin";
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if (user.getRole().equals("商户")) {
                String keyword = FindKeyword.findKeywords(sort);
                if (keyword != null) {
                    content = gs.updateGoods(Integer.parseInt(id), user.getId(), name, price, sort, introduction);
                } else {
                    content = "illegal";
                }
            }
        }
        return content;
    }

    //商户查询所有拦截
    @RequestMapping(value = "/selectHeadOff.do")
    public String selectHeadOff() {
        String content = null;
        User user = (User) session.getAttribute("user");
        if (user != null) {
            if (user.getRole().equals("商户")) {
                List<Map> contentList = CorpusOperat.selectCategory(user.getId());
                content = JSON.toJSONString(contentList);
            }
        }
        return content;
    }

    //商户修改拦截
    @RequestMapping(value = "/modifyHeadOff.do", params = {"newStatement", "newReply", "originalStatement"})
    public String modifyHeadOff(@RequestParam String newStatement, @RequestParam String newReply, @RequestParam String originalStatement) {
        String content = "failure";
        User user = (User) session.getAttribute("user");
        if (!newStatement.equals("") && !newReply.equals("") && !originalStatement.equals("")) {
            if (user != null) {
                if (user.getRole().equals("商户")) {
                    String keyword = FindKeyword.findKeywords(newStatement);
                    if (keyword == null) {
                        content = CorpusOperat.modifyCategory(user.getId(), newStatement, newReply, originalStatement);
                        if (content.equals("success")) {
                            session.removeAttribute("bot");
                        }
                    } else {
                        content = "illegal";
                    }
                }
            }
        } else {
            content = "null";
        }
        return content;
    }

    //商户删除拦截
    @RequestMapping(value = "/removeHeadOff.do", params = {"statement"})
    public String removeHeadOff(@RequestParam String statement) {
        String content = "failure";
        User user = (User) session.getAttribute("user");
        if (!statement.equals("")) {
            if (user != null) {
                if (user.getRole().equals("商户")) {
                    content = CorpusOperat.removeCategory(user.getId(), statement);
                    if (content.equals("success")) {
                        session.removeAttribute("bot");
                    }
                }
            }
        }
        return content;
    }

    //商户新增拦截
    @RequestMapping(value = "/addHeadOff.do", params = {"statement", "reply"})
    public String addHeadOff(@RequestParam String statement, @RequestParam String reply) {
        String content = "failure";
        User user = (User) session.getAttribute("user");
        if (!statement.equals("") && !reply.equals("")) {
            if (user != null) {
                if (user.getRole().equals("商户")) {
                    String keyword = FindKeyword.findKeywords(statement);
                    if (keyword == null) {
                        content = CorpusOperat.addCategory(user.getId(), statement, reply);
                        if (content.equals("success")) {
                            session.removeAttribute("bot");
                        }
                    } else {
                        content = "illegal";
                    }
                }
            }
        } else {
            content = "null";
        }
        return content;
    }

}
