package com.itheima.service.Impl;

import com.itheima.mapper.UserMapper;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.Md5Util;
import com.itheima.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import com.itheima.pojo.PageBean;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUserName(String username) {
        User u = userMapper.findByUserName(username);
        return u;
    }

    @Override
    public void register(String username, String password) {
        // 加密
        String md5String = Md5Util.getMD5String(password);

        // 添加
        userMapper.add(username, md5String);
    }

    @Override
    public void update(User user) {
        // 设置更新时间
        user.setUpdateTime(LocalDateTime.now());
        // 修改用户
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatUri){
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updateAvatar(avatUri,id);
    }

    @Override
    public void updatePwd(String newPwd) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);
    }

    @Override
    public PageBean<User> list(Integer pageNum, Integer pageSize, String username) {
        // 1. 创建 PageBean 对象
        PageBean<User> pb = new PageBean<>();

        // 2. 开启分页查询 PageHelper
        PageHelper.startPage(pageNum, pageSize);

        // 3. 调用 mapper 查询
        List<User> userList = userMapper.list(username);

        // 4. PageHelper 会把查询结果封装到 Page 对象中，可以获取总数
        Page<User> p = (Page<User>) userList;

        // 5. 把数据填充到 PageBean 中
        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());

        return pb;
    }

    @Override
    public void deleteUser(List<Integer> ids) {
        userMapper.deleteUser(ids);
    }


}