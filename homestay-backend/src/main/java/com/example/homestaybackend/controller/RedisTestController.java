package com.example.homestaybackend.controller;

import com.example.homestaybackend.service.RedisRateLimiterService;
import com.example.homestaybackend.service.RedisTokenService;
import com.example.homestaybackend.service.RedisVerificationCodeService;
import com.example.homestaybackend.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis测试控制器
 * 用于测试Redis各项功能
 */
@RestController
@RequestMapping("/api/redis")
@CrossOrigin
public class RedisTestController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTokenService tokenService;

    @Autowired
    private RedisRateLimiterService rateLimiterService;

    @Autowired
    private RedisVerificationCodeService verificationCodeService;

    /**
     * 测试基本的Redis操作
     */
    @GetMapping("/test/basic")
    public ResponseEntity<Map<String, Object>> testBasicOperations() {
        Map<String, Object> result = new HashMap<>();

        // 测试String操作
        redisUtil.set("test:key", "Hello Redis!", 60);
        String value = (String) redisUtil.get("test:key");
        result.put("stringTest", value);

        // 测试计数器
        redisUtil.set("test:counter", 0, 60);
        redisUtil.incr("test:counter", 1);
        redisUtil.incr("test:counter", 1);
        Long counter = Long.parseLong(redisUtil.get("test:counter").toString());
        result.put("counterTest", counter);

        // 测试过期时间
        long expireTime = redisUtil.getExpire("test:key");
        result.put("expireTime", expireTime + "秒");

        result.put("status", "success");
        result.put("message", "Redis基本操作测试成功");

        return ResponseEntity.ok(result);
    }

    /**
     * 测试验证码功能
     */
    @PostMapping("/test/verification-code")
    public ResponseEntity<Map<String, Object>> testVerificationCode(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String type = request.get("type"); // sms, email, image
        String identifier = request.get("identifier"); // 手机号、邮箱或sessionId

        String code = "";
        switch (type) {
            case "sms":
                code = verificationCodeService.generateSmsCode(identifier);
                break;
            case "email":
                code = verificationCodeService.generateEmailCode(identifier);
                break;
            case "image":
                code = "ABCD"; // 实际应该生成图形验证码
                verificationCodeService.saveImageCode(identifier, code);
                break;
        }

        result.put("status", "success");
        result.put("message", "验证码已生成");
        result.put("code", code); // 实际生产环境不应该返回验证码
        result.put("identifier", identifier);

        return ResponseEntity.ok(result);
    }

    /**
     * 验证验证码
     */
    @PostMapping("/test/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String type = request.get("type");
        String identifier = request.get("identifier");
        String code = request.get("code");

        boolean isValid = false;
        switch (type) {
            case "sms":
                isValid = verificationCodeService.verifySmsCode(identifier, code);
                break;
            case "email":
                isValid = verificationCodeService.verifyEmailCode(identifier, code);
                break;
            case "image":
                isValid = verificationCodeService.verifyImageCode(identifier, code);
                break;
        }

        result.put("status", isValid ? "success" : "error");
        result.put("message", isValid ? "验证码验证成功" : "验证码错误或已过期");
        result.put("isValid", isValid);

        return ResponseEntity.ok(result);
    }

    /**
     * 测试限流功能
     */
    @GetMapping("/test/rate-limit/{userId}")
    public ResponseEntity<Map<String, Object>> testRateLimit(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 测试搜索限流
        boolean allowSearch = rateLimiterService.allowSearch(userId);
        long remainingSearches = rateLimiterService.getRemainingRequests("search:" + userId, 20);

        result.put("allowSearch", allowSearch);
        result.put("remainingSearches", remainingSearches);

        // 测试预订限流
        boolean allowBooking = rateLimiterService.allowBooking(userId);
        long remainingBookings = rateLimiterService.getRemainingRequests("booking:" + userId, 10);

        result.put("allowBooking", allowBooking);
        result.put("remainingBookings", remainingBookings);

        result.put("status", "success");
        result.put("message", "限流测试完成");

        return ResponseEntity.ok(result);
    }

    /**
     * 测试Token管理
     */
    @PostMapping("/test/token")
    public ResponseEntity<Map<String, Object>> testToken(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String action = request.get("action"); // save, validate, delete
        String token = request.get("token");
        Long userId = request.containsKey("userId") ? Long.parseLong(request.get("userId")) : null;

        switch (action) {
            case "save":
                if (userId != null && token != null) {
                    tokenService.saveToken(token, userId);
                    result.put("message", "Token已保存");
                }
                break;
            case "validate":
                if (token != null) {
                    Long validatedUserId = tokenService.validateToken(token);
                    result.put("userId", validatedUserId);
                    result.put("isValid", validatedUserId != null);
                }
                break;
            case "delete":
                if (token != null) {
                    tokenService.deleteToken(token);
                    result.put("message", "Token已删除");
                }
                break;
            case "refresh":
                if (token != null) {
                    tokenService.refreshToken(token);
                    result.put("message", "Token已刷新");
                }
                break;
        }

        result.put("status", "success");
        return ResponseEntity.ok(result);
    }

    /**
     * 查看Redis连接状态
     */
    @GetMapping("/test/status")
    public ResponseEntity<Map<String, Object>> checkStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 尝试写入一个测试键
            redisUtil.set("test:ping", "pong", 10);
            Object value = redisUtil.get("test:ping");
            
            result.put("status", "success");
            result.put("message", "Redis连接正常");
            result.put("connected", true);
            result.put("testValue", value);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Redis连接失败: " + e.getMessage());
            result.put("connected", false);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 清除测试数据
     */
    @DeleteMapping("/test/clear")
    public ResponseEntity<Map<String, Object>> clearTestData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            redisUtil.del("test:key", "test:counter", "test:ping");
            result.put("status", "success");
            result.put("message", "测试数据已清除");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "清除失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
