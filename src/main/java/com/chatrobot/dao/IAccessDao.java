package com.chatrobot.dao;

import com.chatrobot.domain.Access;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 访问日志操作接口
 * Created by hackyo on 2017/5/19.
 */
@Mapper
public interface IAccessDao {

    //添加一条访问日志
    @Insert("INSERT INTO access (userId, noteDate, amount) VALUES (#{userId}, #{noteDate}, #{amount})")
    void addAccess(Access access);

    //当日店铺访问量加一
    @Update("UPDATE access SET amount = amount + 1 WHERE userId = #{userId} AND noteDate = #{noteDate}")
    void addAmount(@Param("userId") int userId, @Param("noteDate") String noteDate);

    //查询近七天访问量
    @Select("SELECT noteDate, amount FROM access WHERE userId = #{userId} ORDER BY noteDate DESC LIMIT 7")
    List<Map> selectWeekAmount(int userId);

}
