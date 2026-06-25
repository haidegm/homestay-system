# Redis 安装指南（Windows）

## 方法 1：下载 Redis for Windows（推荐）

### 步骤 1：下载 Redis
1. 访问：https://github.com/tporadowski/redis/releases
2. 下载最新版本的 `Redis-x64-*.zip`（例如：Redis-x64-5.0.14.1.zip）
3. 解压到一个固定位置，例如：`D:\Redis`

### 步骤 2：启动 Redis 服务器

#### 方式 A：直接运行（临时）
1. 打开解压的文件夹（例如：`D:\Redis`）
2. 双击 `redis-server.exe`
3. 看到以下信息说明启动成功：
   ```
   [####] Server started, Redis version 5.0.14.1
   [####] The server is now ready to accept connections on port 6379
   ```

#### 方式 B：安装为 Windows 服务（推荐，开机自启）
1. 以**管理员身份**打开 PowerShell
2. 进入 Redis 目录：
   ```powershell
   cd D:\Redis
   ```
3. 安装服务：
   ```powershell
   .\redis-server.exe --service-install redis.windows.conf --loglevel verbose
   ```
4. 启动服务：
   ```powershell
   .\redis-server.exe --service-start
   ```
5. 查看服务状态：
   ```powershell
   .\redis-server.exe --service-status
   ```

其他服务命令：
- 停止服务：`.\redis-server.exe --service-stop`
- 卸载服务：`.\redis-server.exe --service-uninstall`

### 步骤 3：测试 Redis 连接

1. 在 Redis 目录打开新的命令行窗口
2. 运行 Redis 客户端：
   ```powershell
   .\redis-cli.exe
   ```
3. 测试连接：
   ```
   127.0.0.1:6379> ping
   PONG
   ```
4. 设置和获取值：
   ```
   127.0.0.1:6379> set test "Hello Redis"
   OK
   127.0.0.1:6379> get test
   "Hello Redis"
   ```
5. 退出：
   ```
   127.0.0.1:6379> exit
   ```

---

## 方法 2：使用 Memurai（原生 Windows，更稳定）

### 步骤 1：下载 Memurai
1. 访问：https://www.memurai.com/get-memurai
2. 点击 "Download Memurai Developer" (免费版)
3. 填写简单信息后下载安装程序

### 步骤 2：安装
1. 运行下载的 `.msi` 安装程序
2. 按照向导完成安装
3. 安装完成后会自动作为 Windows 服务运行

### 步骤 3：验证
1. 打开服务管理器（Win+R，输入 `services.msc`）
2. 找到 "Memurai" 服务，确认状态为"正在运行"
3. 或使用命令行测试：
   ```powershell
   memurai-cli ping
   ```

---

## 方法 3：使用 Docker（如果你有 Docker）

```bash
# 拉取 Redis 镜像
docker pull redis:latest

# 运行 Redis 容器
docker run -d --name redis -p 6379:6379 redis:latest

# 测试连接
docker exec -it redis redis-cli ping
```

---

## 验证 Redis 是否安装成功

### 1. 检查端口是否被占用
```powershell
netstat -ano | findstr :6379
```
应该看到类似：
```
TCP    0.0.0.0:6379           0.0.0.0:0              LISTENING       12345
```

### 2. 测试 Spring Boot 连接
启动你的 Spring Boot 项目，访问：
```
http://localhost:8080/api/redis/test/status
```

应该返回：
```json
{
  "status": "success",
  "message": "Redis连接正常",
  "connected": true,
  "testValue": "pong"
}
```

---

## 常见问题

### Q1: Redis 启动后立即关闭
**解决方案：** 检查端口是否被占用，或查看 Redis 日志文件

### Q2: 连接被拒绝（Connection refused）
**解决方案：**
1. 确认 Redis 服务正在运行
2. 检查防火墙设置
3. 确认端口号（默认 6379）

### Q3: 找不到 redis-server.exe
**解决方案：** 确保 Redis 解压路径正确，或将 Redis 目录添加到系统 PATH

### Q4: 访问拒绝（需要密码）
**解决方案：** 在 `application.properties` 中配置密码：
```properties
spring.data.redis.password=your_password
```

---

## Redis 配置文件（可选）

编辑 `redis.windows.conf` 或 `redis.conf`：

```conf
# 绑定地址（允许远程访问改为 0.0.0.0）
bind 127.0.0.1

# 端口
port 6379

# 设置密码（可选）
# requirepass your_password

# 最大内存（例如：100MB）
maxmemory 100mb

# 内存淘汰策略
maxmemory-policy allkeys-lru

# 持久化配置
save 900 1
save 300 10
save 60 10000
```

---

## 推荐配置

对于开发环境，我推荐：
- **简单快速**：方法 1 - 直接运行 redis-server.exe
- **稳定长期**：方法 2 - Memurai（原生 Windows 支持）
- **有 Docker**：方法 3 - Docker 容器

---

## 下一步

1. 安装并启动 Redis
2. 访问测试接口：`http://localhost:8080/api/redis/test/status`
3. 查看 `REDIS_IMPLEMENTATION.md` 了解更多功能
4. 开始在业务代码中使用 Redis！

祝你使用愉快！🎉
