package com.itheima.mapper;

import com.itheima.pojo.User;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface UserMapper {

    // 根据用户名查询用户
    @Select("select * from user where username=#{username}")
    User findByUserName(String username);

    // 添加新用户
    @Insert("insert into user(username,password,create_time,update_time)" +
            "values(#{username},#{password},now(),now())")
    void add(String username, String password);

    // 修改用户信息
    @Update("update user set nickname=#{nickname},email=#{email},update_time=#{updateTime} where id=#{id}")
    void update(User user);

    @Update("update user set user_pic=#{avatarUri},update_time=now() where id=#{id}")
    void updateAvatar(String avatarUri,Integer id);

    @Update("update user set password=#{md5String},update_time=now() where id=#{id}")
    void updatePwd(String md5String,Integer id);

    // 查询所有的用户
    List<User> list(String username);

    // 删除指定用户
    @Delete({
            "<script>",
            "DELETE FROM user",
            "WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void deleteUser(@Param("ids") List<Integer> ids);
}