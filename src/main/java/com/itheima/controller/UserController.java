package com.itheima.controller;

import com.itheima.pojo.PageBean;
import com.itheima.pojo.Result;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.JwtUtil;
import com.itheima.utils.Md5Util;
import com.itheima.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> params) {

        // 1. 获取前端传递的用户名和密码
        String username = params.get("username");
        String password = params.get("password");

        // 2. 校验参数是否为空
        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }

        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 3. 校验用户名和密码格式
        if (!username.matches("^\\S{5,16}$")) {
            return Result.error("用户名必须为5-16位非空白字符");
        }

        if (!password.matches("^\\S{5,16}$")) {
            return Result.error("密码必须为5-16位非空白字符");
        }

        // 4. 查询用户名是否已存在
        User user = userService.findByUserName(username);

        if (user != null) {
            return Result.error("该用户名已经被占用");
        }

        // 5. 注册用户
        userService.register(username, password);

        return Result.success();
    }

    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username, @Pattern(regexp = "^\\S{5,16}$") String password) {
        // 根据用户名查询用户
        User loginUser = userService.findByUserName(username);
        // 判断用户是否存在
        if(loginUser==null) {
            return Result.error("用户名错误");
        }
        //判断密码是否正确
        if (Md5Util.getMD5String(password).equals(loginUser.getPassword())){
            // 登录成功
            Map<String,Object> claims = new HashMap<>();
            claims.put("id",loginUser.getId());
            claims.put("username",loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            // 把token存储到redis
            ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
            operations.set(token,token,1, TimeUnit.HOURS);
            return Result.success(token);
        }

        return Result.error("密码错误");
    }

    @GetMapping("/userInfo")
    public Result<User> userInfo() {
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User user = userService.findByUserName(username);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user) {
        userService.update(user);
        return Result.success("用户信息更新成功");
    }

    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUri){
        userService.updateAvatar(avatarUri);
        return Result.success();
    }

    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params,@RequestHeader("Authorization") String token) {
    // 1、校验参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)) {
            return Result.error("缺少必要的参数");
        }

        // 原密码是否正确
        // 调用userService根据用户名拿到原密码，再和old_pwd比对
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User loginUser = userService.findByUserName(username);
        if (!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码不正确");
        }

        // newPwd和rePwd是否一样
        if (!rePwd.equals(newPwd)) {
            return Result.error("两次填写的新密码不一样");
        }

        //2、调用service完成密码更新
        userService.updatePwd(newPwd);
        // 删除redis中对应的token
        ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
        stringRedisTemplate.delete(token);
        return Result.successMessage("密码修改成功！");
    }

    @GetMapping("/list")
    public Result<PageBean<User>> list(
            Integer pageNum,
            Integer pageSize,
            @RequestParam(required = false) String username
    ){
        PageBean<User> pb = userService.list(pageNum,pageSize,username);
        return Result.success(pb);
    }

//    删除单个指定用户
//    @DeleteMapping("/delete")
//    public Result deleteUser(@RequestParam Integer id) {
//        userService.deleteUser(id);
//        return Result.successMessage("用户删除成功");
//    }

    // 删除单个和多个用户
    @DeleteMapping("/delete")
    public Result deleteUser(@RequestBody List<Integer> ids) {
        userService.deleteUser(ids);
        return Result.success();
    }
    }



