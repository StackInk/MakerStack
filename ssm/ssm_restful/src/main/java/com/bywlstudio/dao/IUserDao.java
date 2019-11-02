package com.bywlstudio.dao;

import com.bywlstudio.domain.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository("userDao")
public interface IUserDao {
    @Select("select * from user")
    List<User> findAll();

    @Select("select * from user where uid = #{id}")
    User findUserById(@Param("id") Integer id);

    @Delete("delete from user where uid = #{id}")
    void delUser(@Param("id") Integer id);

    @Update("update user set uname = #{uname} , usex = #{usex} , uphone = #{uphone} where uid = #{uid} ")
    void updateUser(User user);

    @Insert("insert user(uname,usex,uphone) values(#{uname},#{usex},#{uphone})")
    @Options(useGeneratedKeys = true , keyProperty = "uid" , keyColumn = "uid" )
    void insertUser(User user);
}
