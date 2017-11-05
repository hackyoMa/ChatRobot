package com.chatrobot.service.impl;

import com.chatrobot.dao.IGoodsDao;
import com.chatrobot.dao.IHistoryDao;
import com.chatrobot.dao.ISubDao;
import com.chatrobot.domain.Goods;
import com.chatrobot.domain.History;
import com.chatrobot.domain.Subscription;
import com.chatrobot.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 商品操作接口实现
 * Created by hackyo on 2017/4/18.
 */
@Service
public class GoodsServiceImpl implements IGoodsService {

    private IGoodsDao goodsDao;
    private ISubDao subDao;
    private IHistoryDao historyDao;

    @Autowired
    public GoodsServiceImpl(IGoodsDao goodsDao, ISubDao subDao, IHistoryDao historyDao) {
        this.goodsDao = goodsDao;
        this.subDao = subDao;
        this.historyDao = historyDao;
    }

    //添加一件商品
    public String addGoods(int userId, String name, String price, String sort, String introduction) {
        if (!name.equals("") && !sort.equals("") && !price.equals("") && !introduction.equals("")) {
            String regular = "(^[1-9]([0-9]+)?(\\.[0-9]{1,2})?$)|(^(0)$)|(^[0-9]\\.[0-9]([0-9])?$)";
            if (Pattern.compile(regular).matcher(price).matches()) {
                Goods goods = new Goods();
                goods.setUserId(userId);
                goods.setName(name);
                goods.setPrice(Double.parseDouble(price));
                goods.setSort(sort);
                goods.setIntroduction(introduction);
                goodsDao.addGoods(goods);
                return "success";
            } else {
                return "priceError";
            }
        } else {
            return "incompleteInformation";
        }
    }

    //查询用户下的商品总数
    public int selectGoodsPageCount(int userId) {
        int goodsPageCount = goodsDao.selectGoodsCount(userId);
        if (goodsPageCount > 0) {
            goodsPageCount = (goodsPageCount - 1) / 10 + 1;
        } else {
            goodsPageCount = 0;
        }
        return goodsPageCount;
    }

    //查询用户下的指定行数的商品
    public List<Goods> selectGoods(int userId, int page) {
        return goodsDao.selectGoods(userId, (--page) * 10);
    }

    //删除一条商品记录
    public void removeGoods(int userId, int id) {
        goodsDao.removeGoods(userId, id);
    }

    //通过id查询商品
    public Goods selectSomeGoods(int id) {
        return goodsDao.selectSomeGoods(id);
    }

    //通过关键字模糊查询商品，列出前10，以热度排名
    public List<Map> selectLikeGoods(String keyword, String condition) {
        return goodsDao.selectLikeGoods(keyword, condition);
    }

    //通过关键字模糊查询商品，列出所有
    public List<Map> selectLikeAllGoods(String keyword, String condition) {
        return goodsDao.selectLikeAllGoods(keyword, condition);
    }

    //更新商品信息
    public String updateGoods(int id, int userId, String name, String price, String sort, String introduction) {
        if (!name.equals("") && !sort.equals("") && !price.equals("") && !introduction.equals("")) {
            String regular = "(^[1-9]([0-9]+)?(\\.[0-9]{1,2})?$)|(^(0)$)|(^[0-9]\\.[0-9]([0-9])?$)";
            if (Pattern.compile(regular).matcher(price).matches()) {
                Goods goods = new Goods();
                goods.setId(id);
                goods.setUserId(userId);
                goods.setName(name);
                goods.setPrice(Double.parseDouble(price));
                goods.setSort(sort);
                goods.setIntroduction(introduction);
                goodsDao.updateGoods(goods);
                return "success";
            } else {
                return "priceError";
            }
        } else {
            return "incompleteInformation";
        }
    }

    //查询热搜商品
    public List<Map> selectHotSearch() {
        List<Map> topGoods = goodsDao.selectHotSearch();
        List<Map> randomGoods = new ArrayList<>();
        if (topGoods.size() <= 10) {
            randomGoods = topGoods;
        } else {
            Random random = new Random();
            int randomInt;
            while (randomGoods.size() < 10) {
                randomInt = random.nextInt(topGoods.size());
                randomGoods.add(topGoods.get(randomInt));
                topGoods.remove(randomInt);
            }
        }
        return randomGoods;
    }

    //查询商户下热度为前7的热搜商品
    public List<Map> selectAdminHotSearch(int userId) {
        return goodsDao.selectAdminHotSearch(userId);
    }

    //商品热搜权重增加weights
    public void updateGoodsHeat(int weights, int id) {
        goodsDao.updateGoodsHeat(weights, id);
    }

    //智能推荐商品
    public List<Goods> selectRecommend(int userId, String userEmail) {
        List<Subscription> userSub = subDao.selectOwnSub(userId);
        List<History> userHistory = historyDao.selectSelectHistory(userEmail);
        List<String> sort = new ArrayList<>();
        for (Subscription s : userSub) {
            if (s.getType().equals("select")) {
                if (sort.indexOf(s.getContent()) == -1) {
                    sort.add(s.getContent());
                }
            } else {
                if (sort.indexOf(s.getGoodsSort()) == -1) {
                    sort.add(s.getGoodsSort());
                }
            }
        }
        for (History h : userHistory) {
            if (h.getSender().equals("bot")) {
                String historySort = h.getContent().substring(7, h.getContent().length() - 1);
                if (sort.indexOf(historySort) == -1) {
                    sort.add(historySort);
                }
            }
        }
        Random random = new Random();
        List<Goods> recommendGoods = new ArrayList<>();
        if (sort.size() == 0) {
            recommendGoods = null;
        } else if (sort.size() >= 5) {
            while (recommendGoods.size() < 5) {
                String sortStr = sort.get(random.nextInt(sort.size()));
                List<Goods> goods = goodsDao.selectSortHotSearch(sortStr);
                if (goods.size() != 0) {
                    recommendGoods.add(goods.get(random.nextInt(goods.size())));
                    sort.remove(sortStr);
                }
            }
        } else {
            while (recommendGoods.size() < 5) {
                String sortStr = sort.get(random.nextInt(sort.size()));
                List<Goods> goodsList = goodsDao.selectSortHotSearch(sortStr);
                if (goodsList.size() != 0) {
                    Goods goods = goodsList.get(random.nextInt(goodsList.size()));
                    if (recommendGoods.indexOf(goods) == -1) {
                        recommendGoods.add(goods);
                    }
                }
            }
        }
        return recommendGoods;
    }

}
