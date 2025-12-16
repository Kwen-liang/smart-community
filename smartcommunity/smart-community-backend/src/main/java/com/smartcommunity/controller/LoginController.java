package com.smartcommunity.controller;

import com.smartcommunity.common.Result;
import com.smartcommunity.entity.SysUser;
import com.smartcommunity.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证与登录控制器
 */
@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.getOrDefault("user", params.get("username"));
        String password = params.getOrDefault("pwd", params.get("password"));

        if (username == null || password == null) {
            return Result.error("用户名或密码不能为空");
        }

        SysUser user = userService.login(username, password);

        if (user != null) {
            // 1. 生成唯一 Token
            String token = "token-" + UUID.randomUUID().toString().replaceAll("-", "");

            // 2. 将 Token 存入 Redis，设置 24 小时过期
            // Key: login:token:{token}, Value: userId
            redisTemplate.opsForValue().set("login:token:" + token, user.getUserId().toString(), 24, TimeUnit.HOURS);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success("登录成功", data);
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    // 可以增加一个退出登录接口
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String token) {
        redisTemplate.delete("login:token:" + token);
        return Result.success("退出成功", null);
    }
}