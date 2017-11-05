package com.chatrobot.dao;

import com.chatrobot.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户表操作接口
 * Created by hackyo on 2017/3/22.
 */
@Mapper
public interface IUserDao {

    //查询出所有用户
    @Select("SELECT * FROM user WHERE email != 'bot'")
    List<User> selectAllUser();

    //查询出所有商户ID
    @Select("SELECT id FROM user WHERE role = '商户'")
    List<Integer> selectAllAdminUser();

    //通过Emial查询用户信息
    @Select("SELECT * FROM user WHERE email = #{email}")
    User selectUser(String email);

    //通过id查询用户信息
    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectUserForId(int id);

    //添加一位用户
    @Insert("INSERT INTO user (email, password, username, role, status, regTime, regIp) VALUES (#{email}, #{password}, #{username}, #{role}, #{status}, #{regTime}, #{regIp})")
    void addUser(User user);

}
