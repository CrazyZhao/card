package com.bright.card.config;

import com.alibaba.fastjson.JSON;
import com.bright.card.common.UserContextHolder;
import com.bright.card.repo.entity.User;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.Arrays;


@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * 密钥
     */
    @Value("${token.secret.key}")
    private String key;

    /**
     * 非拦截路径
     */
    @Value("${token.except.uri}")
    private String exceptUri;

    /**
     * 缓存 token 名称
     */
    @Value("${token.cache.prefix}")
    private String tokenPrefix;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 路径匹配，支持表达式匹配
     */
    private PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI();
        log.info("当前请求路径：" + url);
        //不拦截swagger的请求
        if (url.contains("swagger-") || url.contains("api-docs") || url.contains("favicon")) {
            log.debug("不拦截swagger的请求", url);
            return true;
        }

        if (StringUtils.isNotEmpty(exceptUri)) {
            if (log.isInfoEnabled()) {
                log.info("非拦截api路径有: " + exceptUri);
            }
//			boolean isExceptUri = Arrays.stream(exceptUri.split(",")).anyMatch(exceptUrl -> StringUtils.endsWithIgnoreCase(url, exceptUrl));
            boolean isExceptUri = Arrays.stream(exceptUri.split(","))
                    .anyMatch(exceptUrl -> pathMatcher.match(exceptUrl, url));
            if (isExceptUri) {
                log.info("当前API网关不拦截");
                return true;
            }
        }
        // 获取Token
        String accessToken = null;
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            accessToken = header.substring(7);
        }
        if (checkAccessToken(accessToken, response)) {
            log.info("accessToken 有效");
            return true;
        } else {
            log.warn("accessToken 无效");
            return false;
        }
    }

    /**
     * token 校验
     *
     */
    private boolean checkAccessToken(String accessToken, HttpServletResponse response) {
        if (StringUtils.isEmpty(accessToken)) {
            this.write(response, 100001, "无对应的Token传入");
            return false;
        }
        Jws<Claims> jws = null;
        try {
            jws = Jwts.parser().setSigningKey(getAccessKey()).parseClaimsJws(accessToken);
        } catch (ExpiredJwtException ex) {
            this.write(response, 100002, "Token已超时");
            return false;
        } catch (MalformedJwtException ex) {
            this.write(response, 100003, "Token格式错误");
            return false;
        } catch (SignatureException ex) {
            this.write(response, 100004, "无效的Token签名");
            return false;
        }
        String subject = jws.getBody().getSubject();
        // 获取缓存
        String str = (String) redisTemplate.opsForValue().get(tokenPrefix + "." + subject + "." + accessToken);
        User user = JSON.parseObject(str, User.class);
        String account = null;
        if (user != null) {
            account = user.getOpenId();
        }
        if (StringUtils.isEmpty(account)) {
            this.write(response, 100005, "缓存中不存在用户信息");
            return false;
        }else {
            //保存用户信息到上下文
            UserContextHolder.set(user);//放入请求上下文
            log.info("将当前用户,id:[{}]的信息放入请求上下文中", user.getId());
        }
        return true;
    }

    /**
     * 校验异常返回
     */
    public void write(HttpServletResponse response, int code, String msg) {
        String jsonStr = "{\"code\":" + code + ", \"msg\":\"" + msg + "\"}";
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Key getAccessKey() {
        return new SecretKeySpec(key.getBytes(), SignatureAlgorithm.HS512.getJcaName());
    }
}
