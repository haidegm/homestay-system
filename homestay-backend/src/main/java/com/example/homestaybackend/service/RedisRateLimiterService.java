package com.example.homestaybackend.service;

import com.example.homestaybackend.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Redis限流服务
 * 用于API接口限流和防刷
 */
@Service
public class RedisRateLimiterService {

    @Autowired
    private RedisUtil redisUtil;

    // 限流key前缀
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    
    /**
     * 检查是否超过限流阈值
     * @param key 限流key（通常是用户ID或IP）
     * @param maxRequests 最大请求次数
     * @param timeWindow 时间窗口（秒）
     * @return true表示允许请求，false表示被限流
     */
    public boolean allowRequest(String key, int maxRequests, int timeWindow) {
        String rateLimitKey = RATE_LIMIT_PREFIX + key;
        
        // 获取当前请求次数
        Object count = redisUtil.get(rateLimitKey);
        
        if (count == null) {
            // 第一次请求，设置计数为1
            redisUtil.set(rateLimitKey, 1, timeWindow);
            return true;
        }
        
        long currentCount = Long.parseLong(count.toString());
        
        if (currentCount < maxRequests) {
            // 未超过限制，增加计数
            redisUtil.incr(rateLimitKey, 1);
            return true;
        }
        
        // 超过限制
        return false;
    }

    /**
     * 检查搜索限流（每分钟最多20次搜索）
     * @param userId 用户ID
     * @return true表示允许搜索
     */
    public boolean allowSearch(Long userId) {
        return allowRequest("search:" + userId, 20, 60);
    }

    /**
     * 检查登录限流（每小时最多5次失败登录）
     * @param identifier 标识符（用户名或IP）
     * @return true表示允许登录尝试
     */
    public boolean allowLogin(String identifier) {
        return allowRequest("login:" + identifier, 5, 3600);
    }

    /**
     * 检查预订限流（每天最多10次预订）
     * @param userId 用户ID
     * @return true表示允许预订
     */
    public boolean allowBooking(Long userId) {
        return allowRequest("booking:" + userId, 10, 86400);
    }

    /**
     * 检查发送消息限流（每分钟最多10条消息）
     * @param userId 用户ID
     * @return true表示允许发送消息
     */
    public boolean allowSendMessage(Long userId) {
        return allowRequest("message:" + userId, 10, 60);
    }

    /**
     * 重置限流计数
     * @param key 限流key
     */
    public void resetRateLimit(String key) {
        String rateLimitKey = RATE_LIMIT_PREFIX + key;
        redisUtil.del(rateLimitKey);
    }

    /**
     * 获取剩余可用次数
     * @param key 限流key
     * @param maxRequests 最大请求次数
     * @return 剩余次数
     */
    public long getRemainingRequests(String key, int maxRequests) {
        String rateLimitKey = RATE_LIMIT_PREFIX + key;
        Object count = redisUtil.get(rateLimitKey);
        
        if (count == null) {
            return maxRequests;
        }
        
        long currentCount = Long.parseLong(count.toString());
        return Math.max(0, maxRequests - currentCount);
    }
}
