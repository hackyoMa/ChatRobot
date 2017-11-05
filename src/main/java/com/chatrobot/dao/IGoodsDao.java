package com.chatrobot.dao;

import com.chatrobot.domain.Goods;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 商品表操作接口
 * Created by hackyo on 2017/4/8.
 */
@Mapper
public interface IGoodsDao {

    //查询用户下的商品数目
    @Select("SELECT COUNT(*) FROM goods WHERE userId = #{userId}")
    int selectGoodsCount(int userId);

    //查询用户下的指定行数商品
    @Select("SELECT * FROM goods WHERE userId = #{userId} ORDER BY id LIMIT #{page}, 10")
    List<Goods> selectGoods(@Param("userId") int userId, @Param("page") int page);

    //插入一条商品记录字段
    @Insert("INSERT INTO goods (userId, name, price, sort, introduction) VALUES (#{userId}, #{name}, #{price}, #{sort}, #{introduction})")
    void addGoods(Goods goods);

    //删除一件商品
    @Delete("DELETE FROM goods WHERE userId = #{userId} AND id = #{id}")
    void removeGoods(@Param("userId") int userId, @Param("id") int id);

    //通过id查询商品
    @Select("SELECT * FROM goods WHERE id = #{id}")
    Goods selectSomeGoods(int id);

    //通过关键字模糊查询商品前10条，以热度排名
    @Select("SELECT id, name FROM goods WHERE (name LIKE CONCAT('%', #{keyword}, '%') OR sort LIKE CONCAT('%', #{keyword}, '%')) AND ${condition} ORDER BY heat DESC LIMIT 10")
    List<Map> selectLikeGoods(@Param("keyword") String keyword, @Param("condition") String condition);

    //通过关键字模糊查询商品，列出所有
    @Select("SELECT id, name FROM goods WHERE (name LIKE CONCAT('%', #{keyword}, '%') OR sort LIKE CONCAT('%', #{keyword}, '%')) AND ${condition} ORDER BY heat DESC")
    List<Map> selectLikeAllGoods(@Param("keyword") String keyword, @Param("condition") String condition);

    //更新商品信息
    @Update("UPDATE goods SET name = #{name}, price = #{price}, sort = #{sort}, introduction = #{introduction} WHERE userId = #{userId} AND id = #{id}")
    void updateGoods(Goods goods);

    //查询热度为前100的热搜商品
    @Select("SELECT id, name, price, sort FROM goods ORDER BY heat DESC LIMIT 100")
    List<Map> selectHotSearch();

    //查询商户下热度为前7的热搜商品
    @Select("SELECT name, heat FROM goods WHERE userId = #{userId} ORDER BY heat DESC LIMIT 7")
    List<Map> selectAdminHotSearch(int userId);

    //查询热度前10的某类商品
    @Select("SELECT id, name, price FROM goods WHERE sort = #{sort} ORDER BY heat DESC LIMIT 10")
    List<Goods> selectSortHotSearch(String sort);

    //商品热搜权重增加weights
    @Update("UPDATE goods SET heat = heat + #{weights} WHERE id = #{id}")
    void updateGoodsHeat(@Param("weights") int weights, @Param("id") int id);

}
