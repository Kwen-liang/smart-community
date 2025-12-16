package com.smartcommunity.config;

import com.alibaba.fastjson2.JSON;
import com.smartcommunity.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 登录鉴权拦截器
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求 (跨域预检)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 1. 获取请求头中的 Token
        String token = request.getHeader("Authorization");

        // 2. 校验 Token 是否存在
        if (!StringUtils.hasText(token)) {
            returnError(response, "未登录，请先登录");
            return false;
        }

        // 3. 校验 Redis 中是否有效
        String userId = redisTemplate.opsForValue().get("login:token:" + token);
        if (userId == null) {
            returnError(response, "会话已过期，请重新登录");
            return false;
        }

        // 4. Token 自动续期 (活跃用户体验更好)
        redisTemplate.expire("login:token:" + token, 24, TimeUnit.HOURS);

        // 放行
        return true;
    }

    private void returnError(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        Result<Void> result = Result.error(msg);
        // 前端通常根据 code=401 跳转登录页，这里为了简单用 500 或自定义 401
        result.setCode(401);
        response.getWriter().write(JSON.toJSONString(result));
    }
}