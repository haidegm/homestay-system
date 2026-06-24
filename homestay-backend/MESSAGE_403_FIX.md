# 消息未读数403错误修复说明

## 问题描述
房东端访问 `/api/messages/unread-count` 接口时返回403错误。

## 问题原因分析
1. JWT Token可能未正确发送到后端
2. 用户认证状态可能未正确设置
3. 缺少详细的错误日志，难以定位问题

## 修复内容

### 1. 前端改进 (HostLayout.vue)
- **增强日志输出**：添加详细的console.log，追踪token状态和请求过程
- **角色验证**：在加载未读消息数前检查用户角色是否为HOST
- **异步处理**：确保loadUnreadCount在onMounted中正确等待
- **错误详情**：打印完整的错误响应信息

```javascript
// 改进后的loadUnreadCount方法
const loadUnreadCount = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      console.log('未找到token，跳过加载未读消息数')
      return
    }
    
    console.log('正在加载未读消息数，token存在:', !!token)
    const res = await request.get('/api/messages/unread-count')
    unreadCount.value = res.unreadCount || 0
    console.log('未读消息数加载成功:', unreadCount.value)
  } catch (error) {
    console.error('获取未读消息数失败:', error)
    console.error('错误详情:', error.response?.data)
  }
}
```

### 2. 后端改进 (MessageController.java)
- **增强错误处理**：添加try-catch包裹整个方法
- **详细日志**：打印每一步的执行状态
- **认证检查**：明确检查Authentication对象是否存在和有效
- **用户验证**：检查用户是否存在于数据库
- **友好错误信息**：返回具体的错误原因

```java
@GetMapping("/unread-count")
public Map<String, Object> getUnreadCount() {
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("未读消息数接口：用户未认证");
            return Map.of("unreadCount", 0, "msg", "用户未认证");
        }
        
        String username = authentication.getName();
        System.out.println("未读消息数接口：当前用户 = " + username);
        
        // ... 更多验证和日志
        
        return Map.of("unreadCount", count);
    } catch (Exception e) {
        System.err.println("获取未读消息数失败: " + e.getMessage());
        e.printStackTrace();
        return Map.of("unreadCount", 0, "msg", "获取失败: " + e.getMessage());
    }
}
```

### 3. JWT过滤器优化 (JwtFilter.java)
- **减少日志噪音**：只对API请求打印日志，静态资源不打印
- **简化日志输出**：Authorization Header存在时只显示"存在"而不是完整内容
- **保持功能**：所有认证逻辑保持不变

## 测试步骤

### 1. 重启后端服务
```bash
# 在 homestay-backend 目录下
mvn spring-boot:run
```

### 2. 重启前端服务
```bash
# 在 homestay 目录下
npm run dev
```

### 3. 测试流程
1. 使用房东账号登录系统
2. 进入房东后台（/host/dashboard）
3. 打开浏览器开发者工具（F12）
4. 查看Console标签页，应该看到：
   ```
   正在加载未读消息数，token存在: true
   未读消息数加载成功: 0
   ```
5. 查看Network标签页，找到 `unread-count` 请求：
   - 状态码应该是 200
   - 响应应该是 `{"unreadCount": 0}` 或具体数字

### 4. 查看后端日志
后端控制台应该显示：
```
JwtFilter 处理请求: GET /api/messages/unread-count
Authorization Header: 存在
解析出的用户名: xxx, 角色: HOST
格式化后的角色: ROLE_HOST
认证成功，已设置到 SecurityContext
未读消息数接口：当前用户 = xxx
未读消息数接口：用户 xxx 的未读消息数 = 0
```

## 可能的问题和解决方案

### 问题1：仍然返回403
**原因**：Token已过期或无效
**解决**：
1. 退出登录
2. 清除浏览器localStorage（F12 -> Application -> Local Storage -> 清除）
3. 重新登录

### 问题2：Token不存在
**原因**：登录流程未正确保存token
**解决**：
1. 检查登录接口是否正确返回token
2. 检查LoginRegisterDialog.vue中是否正确调用userStore.setLogin()
3. 检查localStorage中是否有token

### 问题3：用户角色不匹配
**原因**：用户角色不是HOST
**解决**：
1. 检查数据库中用户的role字段是否为'HOST'
2. 确保使用房东账号登录，而不是普通用户账号

### 问题4：CORS错误
**原因**：跨域配置问题
**解决**：
1. 确保SecurityConfig中的CorsFilter配置正确
2. 确保前端baseURL配置正确（http://localhost:8080）

## 验证成功标志
- ✅ 浏览器Console无错误
- ✅ Network请求返回200状态码
- ✅ 后端日志显示认证成功
- ✅ 消息中心菜单项显示未读数徽章（如果有未读消息）

## 注意事项
1. 确保数据库中有messages表和conversations表
2. 确保用户角色正确（HOST）
3. 确保JWT密钥配置正确（application.properties中的jwt.secret）
4. 如果修改了Java代码，需要重启后端服务
5. 如果修改了Vue代码，Vite会自动热重载

## 相关文件
- 前端：`homestay/src/layouts/HostLayout.vue`
- 后端控制器：`homestay-backend/src/main/java/com/example/homestaybackend/controller/MessageController.java`
- JWT过滤器：`homestay-backend/src/main/java/com/example/homestaybackend/security/JwtFilter.java`
- 安全配置：`homestay-backend/src/main/java/com/example/homestaybackend/security/SecurityConfig.java`
- 请求工具：`homestay/src/utils/request.js`
