package com.chatrobot.service;

import com.chatrobot.domain.Goods;

import java.util.List;
import java.util.Map;

/**
 * 商品操作接口
 * Created by hackyo on 2017/4/18.
 */
public interface IGoodsService {

    //添加一件商品
    String addGoods(int userId, String name, String price, String sort, String introduction);

    //查询用户下的商品总数
    int selectGoodsPageCount(int userId);

    //查询用户下的指定行数的商品
    List<Goods> selectGoods(int userId, int page);

    //删除一条商品记录
    void removeGoods(int userId, int id);

    //通过id查询商品
    Goods selectSomeGoods(int id);

    //通过关键字模糊查询商品，列出前10，以热度排名
    List<Map> selectLikeGoods(String keyword, String condition);

    //通过关键字模糊查询商品，列出所有
    List<Map> selectLikeAllGoods(String keyword, String condition);

    //更新商品信息
    String updateGoods(int id, int userId, String name, String price, String sort, String introduction);

    //查询热搜商品
    List<Map> selectHotSearch();

    //查询商户下热度为前7的热搜商品
    List<Map> selectAdminHotSearch(int userId);

    //商品热搜权重增加weights
    void updateGoodsHeat(int weights, int id);

    //智能推荐商品
    List<Goods> selectRecommend(int userId, String userEmail);

}
