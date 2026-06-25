# Redis 集成实现文档

## 概述
本项目已成功集成 Redis，用于缓存、会话管理、限流和验证码管理等功能。

## 已实现的功能

### 1. Redis 基础配置

#### 依赖配置 (pom.xml)
- `spring-boot-starter-data-redis`: Spring Boot Redis 支持
- `commons-pool2`: Redis 连接池
- `jackson-datatype-jsr310`: JSON 序列化支持

#### 配置文件 (application.properties)
```properties
# Redis配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
spring.data.redis.timeout=5000

# Redis连接池配置
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
spring.data.redis.lettuce.pool.max-wait=-1ms
```

### 2. 核心组件

#### RedisConfig.java
- 配置 RedisTemplate，使用 Jackson 序列化
- 配置 CacheManager，支持 Spring Cache 注解
- 默认缓存过期时间：1小时

#### RedisUtil.java
Redis 工具类，封装了常用操作：
- **String 操作**: set, get, incr, decr
- **Hash 操作**: hset, hget, hmset, hmget, hdel
- **Set 操作**: sSet, sGet, setRemove
- **List 操作**: lSet, lGet, lRemove
- **通用操作**: expire, hasKey, del, getExpire

### 3. 业务服务

#### RedisTokenService.java - Token 管理
功能：
- `saveToken(token, userId)`: 保存用户登录 token
- `validateToken(token)`: 验证 token 有效性
- `deleteToken(token)`: 用户登出，删除 token
- `deleteUserTokens(userId)`: 强制用户下线
- `refreshToken(token)`: 刷新 token 过期时间

使用场景：
- 用户登录后保存 JWT token
- API 请求时验证 token
- 用户登出时删除 token
- 实现单点登录（踢出其他设备）

#### RedisRateLimiterService.java - 限流服务
功能：
- `allowRequest(key, maxRequests, timeWindow)`: 通用限流检查
- `allowSearch(userId)`: 搜索限流（20次/分钟）
- `allowLogin(identifier)`: 登录限流（5次/小时）
- `allowBooking(userId)`: 预订限流（10次/天）
- `allowSendMessage(userId)`: 消息限流（10条/分钟）
- `getRemainingRequests(key, maxRequests)`: 获取剩余次数

使用场景：
- 防止恶意搜索
- 防止暴力破解登录
- 防止恶意下单
- 防止消息轰炸

#### RedisVerificationCodeService.java - 验证码服务
功能：
- `generateSmsCode(phone)`: 生成短信验证码（6位数字，5分钟有效）
- `verifySmsCode(phone, code)`: 验证短信验证码
- `generateEmailCode(email)`: 生成邮箱验证码（6位数字，10分钟有效）
- `verifyEmailCode(email, code)`: 验证邮箱验证码
- `saveImageCode(sessionId, code)`: 保存图形验证码（2分钟有效）
- `verifyImageCode(sessionId, code)`: 验证图形验证码

使用场景：
- 用户注册时发送验证码
- 忘记密码时发送验证码
- 登录时的图形验证码
- 敏感操作二次验证

#### HouseService.java - 房源缓存
已增强的功能：
- `getHouseById(houseId)`: 从 Redis 缓存获取房源详情
- `getHouseViewCount(houseId)`: 获取房源浏览次数
- `getHotHouses(limit)`: 获取热门房源列表
- `deleteHouse(...)`: 删除房源时清除缓存

使用场景：
- 热门房源访问加速
- 减少数据库查询压力
- 统计房源浏览数据

### 4. 测试接口 (RedisTestController.java)

提供了完整的测试接口：

#### 基本操作测试
```
GET /api/redis/test/basic
```

#### 验证码测试
```
POST /api/redis/test/verification-code
Body: {
  "type": "sms",  // sms, email, image
  "identifier": "13800138000"
}

POST /api/redis/test/verify-code
Body: {
  "type": "sms",
  "identifier": "13800138000",
  "code": "123456"
}
```

#### 限流测试
```
GET /api/redis/test/rate-limit/{userId}
```

#### Token 管理测试
```
POST /api/redis/test/token
Body: {
  "action": "save",  // save, validate, delete, refresh
  "token": "your-jwt-token",
  "userId": "1"
}
```

#### Redis 状态检查
```
GET /api/redis/test/status
```

#### 清除测试数据
```
DELETE /api/redis/test/clear
```

## 使用示例

### 1. 在 Controller 中使用限流
```java
@GetMapping("/search")
public ResponseEntity<?> search(@RequestParam Long userId) {
    if (!rateLimiterService.allowSearch(userId)) {
        return ResponseEntity.status(429).body("搜索过于频繁，请稍后再试");
    }
    // 执行搜索逻辑
    return ResponseEntity.ok(result);
}
```

### 2. 在登录时保存 Token
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // 验证用户名密码
    String token = jwtUtil.generateToken(user);
    
    // 保存token到Redis
    tokenService.saveToken(token, user.getId());
    
    return ResponseEntity.ok(token);
}
```

### 3. 验证码发送
```java
@PostMapping("/send-code")
public ResponseEntity<?> sendCode(@RequestParam String phone) {
    String code = verificationCodeService.generateSmsCode(phone);
    
    // 调用短信服务发送验证码
    smsService.send(phone, code);
    
    return ResponseEntity.ok("验证码已发送");
}
```

### 4. 缓存房源详情
```java
// HouseService 中已经实现，使用 @Cacheable 注解
@Cacheable(value = "house", key = "#houseId")
public House getHouseById(Long houseId) {
    // 自动从Redis缓存获取，如果没有则查询数据库并缓存
    return houseRepository.findById(houseId).orElse(null);
}
```

## 安装和启动

### 1. 安装 Redis

#### Windows
1. 下载 Redis for Windows: https://github.com/microsoftarchive/redis/releases
2. 解压并运行 `redis-server.exe`
3. 默认端口：6379

#### Linux/Mac
```bash
# 使用包管理器安装
# Ubuntu/Debian
sudo apt-get install redis-server

# Mac
brew install redis

# 启动Redis
redis-server
```

### 2. 启动项目
```bash
# 确保Redis正在运行
redis-cli ping  # 应该返回 PONG

# 启动Spring Boot项目
mvn spring-boot:run
```

### 3. 验证Redis连接
访问：`http://localhost:8080/api/redis/test/status`

应该返回：
```json
{
  "status": "success",
  "message": "Redis连接正常",
  "connected": true,
  "testValue": "pong"
}
```

## 性能优化建议

### 1. 合理设置缓存过期时间
- 热门数据：1-3小时
- 用户会话：7天
- 验证码：2-10分钟

### 2. 使用 Redis 连接池
项目已配置连接池，建议根据并发量调整：
```properties
spring.data.redis.lettuce.pool.max-active=20  # 最大连接数
spring.data.redis.lettuce.pool.max-idle=10    # 最大空闲连接
```

### 3. 批量操作
使用 Pipeline 进行批量操作，减少网络往返。

### 4. 监控 Redis
```bash
# 监控Redis命令
redis-cli monitor

# 查看Redis信息
redis-cli info

# 查看内存使用
redis-cli info memory
```

## 常见问题

### 1. 连接失败
- 检查 Redis 是否启动：`redis-cli ping`
- 检查端口是否正确：默认 6379
- 检查防火墙设置

### 2. 序列化错误
- 确保实体类实现 Serializable
- 检查 Jackson 配置

### 3. 缓存不生效
- 检查 `@EnableCaching` 注解
- 确认方法不是 private
- 避免在同一个类内部调用（绕过代理）

## 下一步优化

1. **Redis Cluster**: 生产环境考虑使用 Redis 集群
2. **Redis Sentinel**: 实现高可用
3. **缓存预热**: 系统启动时预加载热门数据
4. **缓存穿透/击穿/雪崩**: 实现布隆过滤器、互斥锁等防护机制
5. **数据持久化**: 配置 RDB 和 AOF
6. **监控告警**: 集成 Redis 监控工具

## 相关文档

- [Spring Data Redis 官方文档](https://spring.io/projects/spring-data-redis)
- [Redis 官方文档](https://redis.io/documentation)
- [Lettuce Redis Client](https://lettuce.io/)
