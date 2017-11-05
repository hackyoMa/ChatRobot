package com.chatrobot.dao;

import com.chatrobot.domain.Subscription;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订阅操作表接口
 * Created by hackyo on 2017/5/8.
 */
@Mapper
public interface ISubDao {

    //查询用户收藏的数目
    @Select("SELECT COUNT(*) FROM subscription WHERE userId = #{id}")
    int selectSubscriptionCount(int id);

    //查询用户所有收藏，分页
    @Select("SELECT *, IF (subscription.type = 'goods', (SELECT goods.name FROM goods WHERE goods.id = subscription.content), NULL) AS goodsName FROM subscription WHERE subscription.userId = #{userId} ORDER BY id LIMIT #{page}, 10")
    List<Subscription> selectAllSub(@Param("userId") int userId, @Param("page") int page);

    //查询用户所有收藏，以及关键字
    @Select("SELECT *, IF (subscription.type = 'goods', (SELECT goods.sort FROM goods WHERE goods.id = subscription.content), NULL) AS goodsSort FROM subscription WHERE subscription.userId = #{userId} ORDER BY id")
    List<Subscription> selectOwnSub(int userId);

    //查询用户所有收藏商品的名称和价格
    @Select("SELECT goods.name, goods.price FROM subscription, goods WHERE subscription.type = 'goods' AND goods.id = subscription.content AND subscription.userId = #{userId} ORDER BY subscription.id")
    List<Map> selectSubGoods(int userId);

    //查询用户单条收藏
    @Select("SELECT * FROM subscription WHERE userId = #{userId} AND type = #{type} AND content = #{content}")
    Subscription selectSub(@Param("userId") int userId, @Param("type") String type, @Param("content") String content);

    //添加一条收藏
    @Select("INSERT INTO subscription (userId, type, content) VALUES (#{userId}, #{type}, #{content})")
    void addSub(Subscription sub);

    //删除一条收藏
    @Delete("DELETE FROM subscription WHERE userId = #{userId} AND type = #{type} AND content = #{content}")
    void removeSub(@Param("userId") int userId, @Param("type") String type, @Param("content") String content);

}
