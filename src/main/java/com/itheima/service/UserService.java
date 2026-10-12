package com.itheima.service;

import com.itheima.pojo.PageBean;
import com.itheima.pojo.User;

import java.util.List;

public interface UserService {
    User findByUserName(String username);

    void register(String username, String password);

    // 更新用户信息
    void update(User user);

    // 更新用户图像
    void updateAvatar(String avatarUri);

    // 更新密码
    void updatePwd(String newPwd);

    // 查询所有注册的用户
    PageBean<User> list(Integer pageNum, Integer pageSize, String username);

    // 删除用户
    void deleteUser(List<Integer> ids);
}
