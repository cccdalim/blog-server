package com.ljlblogserver.mapper;

import com.ljlblogserver.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, created_at FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO users (username, password) VALUES (#{username}, #{password})")
    int insert(User user);

    @Update("UPDATE users SET password = #{password} WHERE username = #{username}")
    int updatePassword(User user);
}
