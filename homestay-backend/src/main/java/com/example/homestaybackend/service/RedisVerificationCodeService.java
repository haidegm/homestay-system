package com.example.homestaybackend.service;

import com.example.homestaybackend.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Redis验证码服务
 * 用于生成、存储和验证各类验证码
 */
@Service
public class RedisVerificationCodeService {

    @Autowired
    private RedisUtil redisUtil;

    // 验证码key前缀
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final String IMAGE_CODE_PREFIX = "image:code:";
    
    // 验证码过期时间（秒）
    private static final long SMS_CODE_EXPIRE = 300; // 5分钟
    private static final long EMAIL_CODE_EXPIRE = 600; // 10分钟
    private static final long IMAGE_CODE_EXPIRE = 120; // 2分钟

    /**
     * 生成随机数字验证码
     * @param length 验证码长度
     * @return 验证码字符串
     */
    private String generateNumericCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 生成并存储短信验证码
     * @param phone 手机号
     * @return 验证码
     */
    public String generateSmsCode(String phone) {
        String code = generateNumericCode(6);
        String key = SMS_CODE_PREFIX + phone;
        redisUtil.set(key, code, SMS_CODE_EXPIRE);
        System.out.println("短信验证码已生成: " + phone + " -> " + code);
        return code;
    }

    /**
     * 验证短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return true表示验证成功
     */
    public boolean verifySmsCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + phone;
        Object storedCode = redisUtil.get(key);
        
        if (storedCode != null && storedCode.toString().equals(code)) {
            // 验证成功后删除验证码（防止重复使用）
            redisUtil.del(key);
            return true;
        }
        return false;
    }

    /**
     * 生成并存储邮箱验证码
     * @param email 邮箱
     * @return 验证码
     */
    public String generateEmailCode(String email) {
        String code = generateNumericCode(6);
        String key = EMAIL_CODE_PREFIX + email;
        redisUtil.set(key, code, EMAIL_CODE_EXPIRE);
        System.out.println("邮箱验证码已生成: " + email + " -> " + code);
        return code;
    }

    /**
     * 验证邮箱验证码
     * @param email 邮箱
     * @param code 验证码
     * @return true表示验证成功
     */
    public boolean verifyEmailCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        Object storedCode = redisUtil.get(key);
        
        if (storedCode != null && storedCode.toString().equals(code)) {
            // 验证成功后删除验证码
            redisUtil.del(key);
            return true;
        }
        return false;
    }

    /**
     * 生成并存储图形验证码
     * @param sessionId 会话ID
     * @param code 验证码
     */
    public void saveImageCode(String sessionId, String code) {
        String key = IMAGE_CODE_PREFIX + sessionId;
        redisUtil.set(key, code.toLowerCase(), IMAGE_CODE_EXPIRE);
    }

    /**
     * 验证图形验证码
     * @param sessionId 会话ID
     * @param code 验证码
     * @return true表示验证成功
     */
    public boolean verifyImageCode(String sessionId, String code) {
        String key = IMAGE_CODE_PREFIX + sessionId;
        Object storedCode = redisUtil.get(key);
        
        if (storedCode != null && storedCode.toString().equalsIgnoreCase(code)) {
            // 验证成功后删除验证码
            redisUtil.del(key);
            return true;
        }
        return false;
    }

    /**
     * 检查验证码是否存在
     * @param type 验证码类型（sms/email/image）
     * @param identifier 标识符（手机号/邮箱/会话ID）
     * @return true表示存在
     */
    public boolean hasCode(String type, String identifier) {
        String key;
        switch (type.toLowerCase()) {
            case "sms":
                key = SMS_CODE_PREFIX + identifier;
                break;
            case "email":
                key = EMAIL_CODE_PREFIX + identifier;
                break;
            case "image":
                key = IMAGE_CODE_PREFIX + identifier;
                break;
            default:
                return false;
        }
        return redisUtil.hasKey(key);
    }

    /**
     * 删除验证码
     * @param type 验证码类型
     * @param identifier 标识符
     */
    public void deleteCode(String type, String identifier) {
        String key;
        switch (type.toLowerCase()) {
            case "sms":
                key = SMS_CODE_PREFIX + identifier;
                break;
            case "email":
                key = EMAIL_CODE_PREFIX + identifier;
                break;
            case "image":
                key = IMAGE_CODE_PREFIX + identifier;
                break;
            default:
                return;
        }
        redisUtil.del(key);
    }
}
