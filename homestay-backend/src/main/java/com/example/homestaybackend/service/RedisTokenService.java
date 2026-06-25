package com.example.homestaybackend.service;

import com.example.homestaybackend.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Redis Token管理服务
 * 用于管理用户登录token和会话
 */
@Service
public class RedisTokenService {

    @Autowired
    private RedisUtil redisUtil;

    // Token缓存key前缀
    private static final String TOKEN_PREFIX = "token:";
    private static final String USER_TOKEN_PREFIX = "user:token:";
    
    // Token过期时间（7天）
    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60;

    /**
     * 保存token到Redis
     * @param token JWT token
     * @param userId 用户ID
     */
    public void saveToken(String token, Long userId) {
        String tokenKey = TOKEN_PREFIX + token;
        String userTokenKey = USER_TOKEN_PREFIX + userId;
        
        // 保存token -> userId的映射
        redisUtil.set(tokenKey, userId, TOKEN_EXPIRE_TIME);
        
        // 保存userId -> token的映射（用于用户登出时删除token）
        redisUtil.set(userTokenKey, token, TOKEN_EXPIRE_TIME);
    }

    /**
     * 验证token是否有效
     * @param token JWT token
     * @return 用户ID，如果token无效返回null
     */
    public Long validateToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userId = redisUtil.get(tokenKey);
        return userId != null ? Long.parseLong(userId.toString()) : null;
    }

    /**
     * 删除token（用户登出）
     * @param token JWT token
     */
    public void deleteToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userId = redisUtil.get(tokenKey);
        
        if (userId != null) {
            // 删除token -> userId的映射
            redisUtil.del(tokenKey);
            
            // 删除userId -> token的映射
            String userTokenKey = USER_TOKEN_PREFIX + userId.toString();
            redisUtil.del(userTokenKey);
        }
    }

    /**
     * 删除用户的所有token（强制下线）
     * @param userId 用户ID
     */
    public void deleteUserTokens(Long userId) {
        String userTokenKey = USER_TOKEN_PREFIX + userId;
        Object token = redisUtil.get(userTokenKey);
        
        if (token != null) {
            // 删除userId -> token的映射
            redisUtil.del(userTokenKey);
            
            // 删除token -> userId的映射
            String tokenKey = TOKEN_PREFIX + token.toString();
            redisUtil.del(tokenKey);
        }
    }

    /**
     * 刷新token过期时间
     * @param token JWT token
     */
    public void refreshToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        Object userId = redisUtil.get(tokenKey);
        
        if (userId != null) {
            // 重新设置过期时间
            redisUtil.expire(tokenKey, TOKEN_EXPIRE_TIME);
            
            String userTokenKey = USER_TOKEN_PREFIX + userId.toString();
            redisUtil.expire(userTokenKey, TOKEN_EXPIRE_TIME);
        }
    }

    /**
     * 检查token是否存在
     * @param token JWT token
     * @return true表示token存在且有效
     */
    public boolean hasToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        return redisUtil.hasKey(tokenKey);
    }
}
