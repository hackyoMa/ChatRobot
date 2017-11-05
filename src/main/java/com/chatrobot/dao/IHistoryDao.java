package com.chatrobot.dao;

import com.chatrobot.domain.History;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天记录表操作接口
 * Created by hackyo on 2017/4/8.
 */
@Mapper
public interface IHistoryDao {

    //通过email查询用户聊天记录的数目
    @Select("SELECT COUNT(*) FROM history WHERE sender = #{email} OR receiver = #{email}")
    int selectHistoryCount(String email);

    //通过email查询查询记录
    @Select("SELECT * FROM history WHERE (sender = #{email} OR receiver = #{email}) AND type = 'select' ORDER BY id")
    List<History> selectSelectHistory(String email);

    //通过email查询用户指定行数的聊天记录
    @Select("SELECT * FROM history WHERE sender = #{email} OR receiver = #{email} ORDER BY id LIMIT #{page}, 10")
    List<History> selectHistory(@Param("email") String email, @Param("page") int page);

    //添加一条聊天记录
    @Insert("INSERT INTO history (sender, receiver, content, type, time) VALUES (#{sender}, #{receiver}, #{content}, #{type}, #{time})")
    void addHistory(History history);

}
