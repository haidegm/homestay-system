# 🏠 智能民宿预订管理系统

<div align="center">

![GitHub](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.5.24-42b883.svg)
![Python](https://img.shields.io/badge/Python-3.8+-3776ab.svg)
![Redis](https://img.shields.io/badge/Redis-7.0+-dc382d.svg)

一个功能完整的智能民宿预订管理系统，集成了 AI 智能推荐、智能客服、多角色管理等功能。

[功能特性](#-功能特性) • [技术栈](#-技术栈) • [快速开始](#-快速开始) • [项目结构](#-项目结构) • [开发文档](#-开发文档)

</div>

---

## 📋 目录

- [功能特性](#-功能特性)
- [技术栈](#-技术栈)
- [系统架构](#-系统架构)
- [快速开始](#-快速开始)
- [项目结构](#-项目结构)
- [核心功能说明](#-核心功能说明)
- [API 接口文档](#-api-接口文档)
- [数据库设计](#-数据库设计)
- [部署说明](#-部署说明)
- [开发文档](#-开发文档)
- [常见问题](#-常见问题)
- [贡献指南](#-贡献指南)
- [许可证](#-许可证)

---

## ✨ 功能特性

### 👤 用户端功能
- 🔐 **用户认证**：注册、登录、JWT Token 认证
- 🔍 **智能搜索**：关键词搜索、地区筛选、价格区间、房型筛选
- 🏡 **房源浏览**：房源列表、详情查看、高清图片、设施展示
- 💡 **智能推荐**：基于协同过滤和内容推荐的混合推荐算法
- 🤖 **AI 客服**：智能问答、24小时在线、基于阿里云通义千问
- 📅 **在线预订**：日期选择、价格计算、订单创建、支付集成
- ⭐ **评价系统**：评分、评论、图片上传、房东回复
- ❤️ **收藏功能**：收藏房源、管理收藏夹
- 📜 **订单管理**：查看订单、订单详情、订单状态跟踪
- 💬 **消息通知**：系统消息、订单通知、房东消息
- 👨‍💼 **个人中心**：个人信息编辑、头像上传、我的行程、我的评价

### 🏨 房东端功能
- 🏠 **房源管理**：发布房源、编辑房源、删除房源、上传图片
- 📍 **地图选点**：高德地图集成、精准定位
- 📊 **数据看板**：订单统计、收入统计、评分统计、ECharts 可视化
- 📝 **订单管理**：查看订单、确认订单、拒绝订单、订单状态管理
- 💰 **收入统计**：月度收入、年度收入、收入趋势图表
- ⭐ **评价管理**：查看评价、回复评价
- 💬 **消息中心**：接收用户消息、回复用户
- 🎯 **推广管理**：房源推广、优惠设置

### 🔧 管理员功能
- 👥 **用户管理**：用户列表、用户详情、用户状态管理、权限管理
- 🏨 **房东管理**：房东审核、房东列表、房东信息管理
- 🏠 **房源管理**：房源审核、房源列表、违规处理
- 📋 **订单管理**：全部订单、订单统计、异常订单处理
- 📊 **数据统计**：用户增长、订单统计、收入统计、平台数据大盘
- 🔔 **系统消息**：发送通知、系统公告

---

## 🛠 技术栈

### 前端技术
- **框架**: Vue 3.5.24 (Composition API)
- **构建工具**: Vite 7.2.5 (Rolldown)
- **路由**: Vue Router 4.6.4
- **状态管理**: Pinia 3.0.4
- **HTTP 客户端**: Axios 1.13.4
- **数据可视化**: ECharts 6.0.0
- **地图服务**: 高德地图 API
- **UI 组件**: 自定义组件库
- **CSS**: 原生 CSS + 响应式设计

### 后端技术
- **框架**: Spring Boot 3.5.10
- **JDK 版本**: Java 17
- **ORM 框架**: 
  - MyBatis-Plus 3.5.7
  - Spring Data JPA
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.0+
- **安全认证**: 
  - Spring Security
  - JWT (jjwt 0.11.5)
- **工具类**: Lombok 1.18.30

### AI 推荐服务
- **语言**: Python 3.8+
- **框架**: Flask
- **机器学习**: 
  - scikit-learn
  - pandas
  - numpy
- **推荐算法**: 
  - 协同过滤 (Collaborative Filtering)
  - 内容推荐 (Content-Based)
  - 混合推荐 (Hybrid Recommendation)

### AI 客服
- **平台**: 阿里云通义千问
- **集成方式**: DashScope SDK

### 数据库与缓存
- **关系数据库**: MySQL 8.0
- **缓存数据库**: Redis 7.0+
- **连接池**: HikariCP
- **数据库迁移**: SQL 脚本

### 开发工具
- **后端 IDE**: IntelliJ IDEA
- **前端 IDE**: VS Code
- **API 测试**: Postman
- **版本控制**: Git
- **包管理**: Maven (后端) / npm (前端)

---

## 🏗 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                         客户端层                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   用户端     │  │   房东端     │  │  管理员端    │     │
│  │  (Vue 3)     │  │  (Vue 3)     │  │  (Vue 3)     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      应用服务层                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │          Spring Boot 后端服务 (Port: 8080)            │  │
│  │  Controller → Service → Repository/Mapper → Entity   │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │       Python 推荐服务 (Port: 5000)                    │  │
│  │  Flask API → 推荐算法引擎 → 数据处理                  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      数据存储层                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │    MySQL     │  │    Redis     │  │  文件存储    │     │
│  │  (关系数据)  │  │   (缓存)     │  │  (图片)      │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      外部服务                                 │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │  阿里云通义   │  │  高德地图    │                        │
│  │  (AI客服)    │  │  (地图服务)  │                        │
│  └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 环境要求

#### 必需环境
- **JDK**: 17+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Maven**: 3.6+
- **Python**: 3.8+
- **Redis**: 7.0+ (可选，用于缓存)

#### 推荐工具
- IntelliJ IDEA 2023+
- VS Code
- Postman
- MySQL Workbench

### 安装步骤

#### 1. 克隆项目
```bash
git clone https://github.com/your-username/homestay-system.git
cd homestay-system
```

#### 2. 数据库配置

**创建数据库**
```sql
CREATE DATABASE homestay_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**导入数据库表结构**
```bash
# 进入后端目录
cd homestay-backend

# 导入所有表结构
mysql -u root -p homestay_db < create_all_tables.sql

# 导入浏览历史表（用于推荐系统）
mysql -u root -p homestay_db < create_browse_history_table.sql
```

#### 3. 配置后端

**修改配置文件**
```bash
cd homestay-backend/src/main/resources
```

编辑 `application.properties`：
```properties
# 数据库配置（修改为你的数据库信息）
spring.datasource.url=jdbc:mysql://localhost:3306/homestay_db
spring.datasource.username=root
spring.datasource.password=your_password

# Redis配置（如需使用缓存）
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**编译运行后端**
```bash
# 返回后端根目录
cd homestay-backend

# 方式1: 使用 Maven 运行
mvn spring-boot:run

# 方式2: 使用 IDE 运行
# 在 IntelliJ IDEA 中打开项目，运行 HomestayBackendApplication

# 后端将在 http://localhost:8080 启动
```

#### 4. 配置前端

**安装依赖**
```bash
cd homestay

# 使用 npm 安装
npm install

# 或使用 yarn
yarn install
```

**运行前端**
```bash
# 开发模式
npm run dev

# 前端将在 http://localhost:5173 启动
```

#### 5. 配置推荐服务（可选）

**安装 Python 依赖**
```bash
cd recommendation-service

# 创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

**配置环境变量**
```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，配置数据库连接
```

**运行推荐服务**
```bash
# Windows:
start.bat

# Linux/Mac:
python app.py

# 推荐服务将在 http://localhost:5000 启动
```

#### 6. 配置 Redis（可选）

**安装 Redis**
- Windows: 下载 https://github.com/tporadowski/redis/releases
- Linux: `sudo apt-get install redis-server`
- Mac: `brew install redis`

**启动 Redis**
```bash
# Windows: 直接运行 redis-server.exe
# Linux/Mac:
redis-server

# 测试连接
redis-cli ping
# 应返回: PONG
```

#### 7. 访问系统

- **前端**: http://localhost:5173
- **后端 API**: http://localhost:8080
- **推荐服务**: http://localhost:5000
- **Redis 测试**: http://localhost:8080/api/redis/test/status

### 测试账号

系统没有预设默认账号，请按照以下步骤创建测试账号：

#### 方式 1：通过注册页面
1. 启动前端和后端服务
2. 访问 http://localhost:5173
3. 点击"注册"按钮
4. 填写用户信息完成注册
5. 注册时可选择角色：普通用户 / 房东

#### 方式 2：直接在数据库插入
```sql
-- 插入管理员账号（密码需要加密）
INSERT INTO users (username, password, email, role, created_at) 
VALUES ('admin', 'encrypted_password', 'admin@example.com', 'ADMIN', NOW());

-- 插入测试房东
INSERT INTO users (username, password, email, role, created_at) 
VALUES ('host01', 'encrypted_password', 'host@example.com', 'HOST', NOW());

-- 插入测试用户
INSERT INTO users (username, password, email, role, created_at) 
VALUES ('user01', 'encrypted_password', 'user@example.com', 'USER', NOW());
```

> **注意**：密码需要使用 BCrypt 加密后存储，不能直接存储明文密码。

---

## 📁 项目结构

### 整体目录结构
```
homestay-system/
├── homestay/                    # 前端项目（Vue 3）
├── homestay-backend/            # 后端项目（Spring Boot）
├── recommendation-service/      # 推荐服务（Python Flask）
├── .gitignore
├── package.json
└── README.md
```

### 前端项目结构
```
homestay/
├── public/                      # 静态资源
├── src/
│   ├── assets/                  # 资源文件
│   │   └── images/             # 图片资源
│   ├── components/             # 公共组件
│   │   ├── AIChatBot.vue       # AI 智能客服
│   │   ├── AMapPicker.vue      # 高德地图选点
│   │   ├── BookingConfirmDialog.vue  # 预订确认
│   │   ├── CategoryTabs.vue    # 分类标签
│   │   ├── CityRecommend.vue   # 城市推荐
│   │   ├── LocationSearch.vue  # 位置搜索
│   │   ├── LoginRegisterDialog.vue  # 登录注册
│   │   ├── MessageDialog.vue   # 消息对话框
│   │   ├── RecentlyViewed.vue  # 最近浏览
│   │   ├── RecommendedHouses.vue # 推荐房源
│   │   ├── RegionList.vue      # 地区列表
│   │   ├── ReviewDialog.vue    # 评价对话框
│   │   ├── ReviewsDialog.vue   # 评价列表
│   │   └── StickySearch.vue    # 粘性搜索框
│   ├── layouts/                # 布局组件
│   │   ├── AdminLayout.vue     # 管理员布局
│   │   ├── HostLayout.vue      # 房东布局
│   │   └── UserLayout.vue      # 用户布局
│   ├── router/                 # 路由配置
│   │   └── index.js
│   ├── store/                  # 状态管理
│   │   └── user.js             # 用户状态
│   ├── utils/                  # 工具类
│   │   └── request.js          # Axios 封装
│   ├── views/                  # 页面组件
│   │   ├── admin/              # 管理员页面
│   │   │   ├── Dashboard.vue
│   │   │   ├── HostManagement.vue
│   │   │   ├── HouseManagement.vue
│   │   │   ├── OrderManagement.vue
│   │   │   └── UserManagement.vue
│   │   ├── host/               # 房东页面
│   │   │   ├── Dashboard.vue
│   │   │   ├── HouseAdd.vue
│   │   │   ├── HouseList.vue
│   │   │   ├── Income.vue
│   │   │   ├── OrderList.vue
│   │   │   ├── Profile.vue
│   │   │   ├── Promotion.vue
│   │   │   ├── Review.vue
│   │   │   └── HostMessage.vue
│   │   └── user/               # 用户页面
│   │       ├── Home.vue
│   │       ├── HouseDetail.vue
│   │       ├── SearchResult.vue
│   │       └── profile/        # 用户个人中心
│   │           ├── EditProfile.vue
│   │           ├── MyReviews.vue
│   │           ├── MyTrips.vue
│   │           ├── ProfileLayout.vue
│   │           ├── UserInfo.vue
│   │           ├── UserMessages.vue
│   │           └── Wishlist.vue
│   ├── App.vue                 # 根组件
│   ├── main.js                 # 入口文件
│   └── style.css               # 全局样式
├── index.html
├── package.json
├── vite.config.js              # Vite 配置
└── README.md
```

### 后端项目结构
```
homestay-backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/homestaybackend/
│   │   │   ├── config/              # 配置类
│   │   │   │   ├── RedisConfig.java # Redis 配置
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/          # 控制器
│   │   │   │   ├── AIChatController.java
│   │   │   │   ├── HostOrderController.java
│   │   │   │   ├── IncomeController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── RedisTestController.java
│   │   │   │   └── WishlistController.java
│   │   │   ├── dto/                 # 数据传输对象
│   │   │   │   ├── AdminOrderDTO.java
│   │   │   │   ├── HouseDetailDTO.java
│   │   │   │   └── OrderCreateDTO.java
│   │   │   ├── entity/              # 实体类
│   │   │   │   ├── House.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── User.java
│   │   │   │   └── Wishlist.java
│   │   │   ├── mapper/              # MyBatis Mapper
│   │   │   │   └── OrderMapper.java
│   │   │   ├── repository/          # JPA Repository
│   │   │   │   ├── HouseRepository.java
│   │   │   │   └── WishlistRepository.java
│   │   │   ├── scheduler/           # 定时任务
│   │   │   │   └── OrderScheduler.java
│   │   │   ├── security/            # 安全相关
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtUtil.java
│   │   │   ├── service/             # 业务服务
│   │   │   │   ├── HouseService.java
│   │   │   │   ├── RecommendationService.java
│   │   │   │   ├── RedisRateLimiterService.java
│   │   │   │   ├── RedisTokenService.java
│   │   │   │   ├── RedisVerificationCodeService.java
│   │   │   │   ├── ReviewService.java
│   │   │   │   └── WishlistService.java
│   │   │   ├── util/                # 工具类
│   │   │   │   └── RedisUtil.java
│   │   │   └── HomestayBackendApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   └── test/
├── create_all_tables.sql         # 数据库表结构
├── create_browse_history_table.sql
├── REDIS_IMPLEMENTATION.md       # Redis 实现文档
├── REDIS_INSTALL_GUIDE.md        # Redis 安装指南
├── pom.xml
└── README.md
```

### 推荐服务结构
```
recommendation-service/
├── app.py                        # Flask 应用入口
├── recommender_hybrid.py         # 混合推荐算法
├── requirements.txt              # Python 依赖
├── .env.example                  # 环境变量模板
├── start.bat                     # Windows 启动脚本
└── README.md
```

---

## 🎯 核心功能说明

### 1. 智能推荐系统

**推荐算法**
- **协同过滤**：基于用户行为的相似度推荐
- **内容推荐**：基于房源属性的相似度推荐
- **混合推荐**：结合协同过滤和内容推荐，权重动态调整

**推荐场景**
- 首页个性化推荐
- 房源详情页相似房源推荐
- 基于浏览历史的推荐

**技术实现**
- Python Flask 提供推荐 API
- scikit-learn 实现推荐算法
- MySQL 存储用户行为数据
- Redis 缓存推荐结果

### 2. AI 智能客服

**功能特性**
- 24小时在线智能问答
- 支持房源咨询、预订帮助、政策解答
- 基于阿里云通义千问大模型
- 上下文对话记忆

**使用场景**
- 用户咨询房源信息
- 预订流程指导
- 常见问题解答
- 平台政策说明

### 3. Redis 缓存系统

**缓存功能**
- 房源详情缓存（1小时）
- 热门房源列表缓存
- 搜索结果缓存
- 用户 Token 缓存（7天）

**限流功能**
- 搜索限流：20次/分钟
- 登录限流：5次/小时
- 预订限流：10次/天
- 消息限流：10条/分钟

**验证码管理**
- 短信验证码（5分钟有效）
- 邮箱验证码（10分钟有效）
- 图形验证码（2分钟有效）

### 4. 订单管理系统

**订单状态流转**
```
待确认 → 已确认 → 已完成
         ↓
      已取消/已拒绝
```

**订单功能**
- 订单创建与价格计算
- 订单状态管理
- 订单超时自动取消（定时任务）
- 订单评价
- 订单退款

**定时任务**
- 每小时检查超时未确认订单
- 自动取消超过24小时未确认的订单
- 订单完成后自动更新房源状态

### 5. 评价系统

**评价功能**
- 星级评分（1-5星）
- 文字评价
- 图片上传（最多9张）
- 房东回复
- 评价展示

**评价统计**
- 平均评分计算
- 评价数量统计
- 评分分布图表

### 6. 地图集成

**高德地图功能**
- 地图选点（发布房源时）
- 地址搜索与定位
- 经纬度获取
- 地理位置展示

### 7. 数据可视化

**ECharts 图表**
- 订单统计图表（折线图、柱状图）
- 收入统计图表（饼图、趋势图）
- 用户增长图表
- 房源分布图表

---

## 📡 API 接口文档

### 用户认证
```http
POST /api/auth/register      # 用户注册
POST /api/auth/login         # 用户登录
POST /api/auth/logout        # 用户登出
GET  /api/auth/info          # 获取当前用户信息
```

### 房源管理
```http
GET    /api/houses                    # 获取房源列表
GET    /api/houses/{id}               # 获取房源详情
POST   /api/houses                    # 创建房源（房东）
PUT    /api/houses/{id}               # 更新房源（房东）
DELETE /api/houses/{id}               # 删除房源（房东）
GET    /api/houses/search             # 搜索房源
GET    /api/houses/recommended        # 获取推荐房源
```

### 订单管理
```http
GET    /api/orders                    # 获取订单列表
GET    /api/orders/{id}               # 获取订单详情
POST   /api/orders                    # 创建订单
PUT    /api/orders/{id}/confirm       # 确认订单（房东）
PUT    /api/orders/{id}/cancel        # 取消订单
PUT    /api/orders/{id}/reject        # 拒绝订单（房东）
```

### 评价管理
```http
GET    /api/reviews/house/{houseId}   # 获取房源评价
POST   /api/reviews                   # 创建评价
PUT    /api/reviews/{id}/reply        # 房东回复评价
DELETE /api/reviews/{id}              # 删除评价
```

### 收藏管理
```http
GET    /api/wishlist                  # 获取收藏列表
POST   /api/wishlist                  # 添加收藏
DELETE /api/wishlist/{id}             # 取消收藏
```

### AI 服务
```http
POST   /api/ai/chat                   # AI 客服对话
GET    /api/recommendation/suggest    # 获取推荐房源
```

### Redis 测试
```http
GET    /api/redis/test/status         # Redis 连接状态
GET    /api/redis/test/basic          # Redis 基本操作测试
POST   /api/redis/test/verification-code  # 验证码测试
GET    /api/redis/test/rate-limit/{userId} # 限流测试
```

### 管理员接口
```http
GET    /api/admin/users               # 用户管理
GET    /api/admin/hosts               # 房东管理
GET    /api/admin/houses              # 房源管理
GET    /api/admin/orders              # 订单管理
GET    /api/admin/statistics          # 数据统计
```

---

## 🗄 数据库设计

### 核心数据表

#### users（用户表）
- `id`: 用户ID（主键）
- `username`: 用户名
- `password`: 密码（加密）
- `email`: 邮箱
- `phone`: 手机号
- `role`: 角色（USER/HOST/ADMIN）
- `avatar`: 头像
- `created_at`: 创建时间

#### houses（房源表）
- `id`: 房源ID（主键）
- `host_id`: 房东ID（外键）
- `title`: 标题
- `description`: 描述
- `price`: 价格
- `address`: 地址
- `city`: 城市
- `latitude`: 纬度
- `longitude`: 经度
- `room_type`: 房型
- `bedrooms`: 卧室数
- `bathrooms`: 浴室数
- `max_guests`: 最大容纳人数
- `status`: 状态
- `created_at`: 创建时间

#### orders（订单表）
- `id`: 订单ID（主键）
- `user_id`: 用户ID（外键）
- `house_id`: 房源ID（外键）
- `check_in_date`: 入住日期
- `check_out_date`: 退房日期
- `guests`: 客人数量
- `total_price`: 总价格
- `status`: 订单状态
- `created_at`: 创建时间

#### reviews（评价表）
- `id`: 评价ID（主键）
- `user_id`: 用户ID（外键）
- `house_id`: 房源ID（外键）
- `order_id`: 订单ID（外键）
- `rating`: 评分（1-5）
- `comment`: 评价内容
- `images`: 评价图片
- `reply`: 房东回复
- `created_at`: 创建时间

#### wishlist（收藏表）
- `id`: 收藏ID（主键）
- `user_id`: 用户ID（外键）
- `house_id`: 房源ID（外键）
- `created_at`: 创建时间

#### browse_history（浏览历史表）
- `id`: 浏览ID（主键）
- `user_id`: 用户ID（外键）
- `house_id`: 房源ID（外键）
- `browse_time`: 浏览时间
- `duration`: 浏览时长

---

## 🚢 部署说明

### Docker 部署（推荐）

#### 1. 创建 Dockerfile（后端）
```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 2. 创建 docker-compose.yml
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: homestay_db
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./homestay-backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/homestay_db
      SPRING_REDIS_HOST: redis

  frontend:
    build: ./homestay
    ports:
      - "80:80"
    depends_on:
      - backend

  recommendation:
    build: ./recommendation-service
    ports:
      - "5000:5000"
    depends_on:
      - mysql

volumes:
  mysql-data:
```

#### 3. 部署命令
```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 服务器部署

#### 准备工作
1. 准备一台服务器（阿里云、腾讯云等）
2. 安装 JDK 17、MySQL 8、Redis、Nginx
3. 配置安全组，开放端口：80、443、8080、3306、6379

#### 后端部署
```bash
# 1. 打包项目
cd homestay-backend
mvn clean package -DskipTests

# 2. 上传 jar 文件到服务器
scp target/homestay-backend.jar user@server:/opt/homestay/

# 3. 创建启动脚本
cat > start.sh << 'EOF'
#!/bin/bash
nohup java -jar homestay-backend.jar \
  --spring.profiles.active=prod \
  > /var/log/homestay/app.log 2>&1 &
EOF

chmod +x start.sh
./start.sh
```

#### 前端部署
```bash
# 1. 构建前端
cd homestay
npm run build

# 2. 上传 dist 文件夹到服务器
scp -r dist/* user@server:/var/www/homestay/

# 3. 配置 Nginx
cat > /etc/nginx/sites-available/homestay << 'EOF'
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /var/www/homestay;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

# 4. 启用站点并重启 Nginx
ln -s /etc/nginx/sites-available/homestay /etc/nginx/sites-enabled/
nginx -t
systemctl restart nginx
```

#### Python 推荐服务部署
```bash
# 1. 安装依赖
cd recommendation-service
pip install -r requirements.txt

# 2. 使用 Gunicorn 部署
pip install gunicorn

# 3. 创建启动脚本
cat > start.sh << 'EOF'
#!/bin/bash
gunicorn -w 4 -b 0.0.0.0:5000 app:app
EOF

chmod +x start.sh
./start.sh
```

---

## 📚 开发文档

### 后端开发文档
- [Redis 集成实现文档](homestay-backend/REDIS_IMPLEMENTATION.md)
- [Redis 安装指南](homestay-backend/REDIS_INSTALL_GUIDE.md)
- [AI 客服集成文档](homestay-backend/AI_CUSTOMER_SERVICE_MANUAL.md)
- [ECharts 图表配置](homestay-backend/ECHARTS_SETUP.md)

### 数据库文档
- [数据库表结构](homestay-backend/create_all_tables.sql)
- [浏览历史表](homestay-backend/create_browse_history_table.sql)

### 推荐系统文档
- [推荐服务 README](recommendation-service/README.md)

### 更新日志
- [房源卡片尺寸修复](homestay/HOUSE_CARD_SIZE_FIX.md)
- [可用性和标记修复](homestay/AVAILABILITY_AND_MARKER_FIX.md)
- [评价对话框指南](homestay/REVIEWS_DIALOG_GUIDE.md)
- [评价可点击更新](homestay/REVIEWS_CLICKABLE_UPDATE.md)

---

## ❓ 常见问题

### 1. 数据库连接失败
**问题**：`Cannot connect to database`

**解决方案**：
- 检查 MySQL 是否启动
- 确认数据库用户名密码正确
- 检查数据库是否已创建
- 确认防火墙未阻止 3306 端口

### 2. Redis 连接失败
**问题**：`Redis connection refused`

**解决方案**：
- 确认 Redis 服务已启动
- 检查 Redis 端口（默认 6379）
- 如果不使用 Redis，注释掉 Redis 相关配置

### 3. 前端请求跨域
**问题**：`CORS policy blocked`

**解决方案**：
- 后端已配置 CORS，检查 `@CrossOrigin` 注解
- 确认前端请求地址正确
- 开发环境可在 vite.config.js 配置代理

### 4. AI 客服无响应
**问题**：AI 客服不返回内容

**解决方案**：
- 检查阿里云 API Key 是否配置
- 确认 API Key 是否有效
- 检查网络连接
- 查看后端日志排查错误

### 5. 推荐服务无法启动
**问题**：推荐服务启动失败

**解决方案**：
- 检查 Python 版本（需要 3.8+）
- 安装所有依赖：`pip install -r requirements.txt`
- 检查 .env 配置文件
- 确认数据库连接配置正确

### 6. 图片上传失败
**问题**：图片上传不成功

**解决方案**：
- 检查上传文件大小限制（默认 10MB）
- 确认文件存储路径存在且有写入权限
- 检查文件类型是否允许
- 查看后端日志排查错误

### 7. JWT Token 过期
**问题**：频繁需要重新登录

**解决方案**：
- 调整 Token 过期时间（application.properties）
- 实现 Token 自动刷新机制
- 使用 Redis 存储 Token，延长有效期

### 8. Maven 构建失败
**问题**：`Maven build failed`

**解决方案**：
- 检查 JDK 版本（需要 17+）
- 清理并重新构建：`mvn clean install`
- 检查网络，确保能下载依赖
- 使用国内镜像加速（阿里云 Maven 镜像）

### 9. Node.js 依赖安装失败
**问题**：`npm install` 失败

**解决方案**：
- 检查 Node.js 版本（需要 16+）
- 清理缓存：`npm cache clean --force`
- 删除 node_modules 和 package-lock.json 重新安装
- 使用淘宝镜像：`npm install --registry=https://registry.npmmirror.com`

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范
- Java：遵循阿里巴巴 Java 开发手册
- JavaScript/Vue：遵循 Vue.js 风格指南
- 提交信息：使用清晰的中文或英文描述


---

## 🎓 学习资源

### 技术文档
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Vue 3 官方文档](https://cn.vuejs.org/)
- [MyBatis-Plus 文档](https://baomidou.com/)
- [Redis 官方文档](https://redis.io/documentation)
- [阿里云通义千问](https://help.aliyun.com/zh/dashscope/)

### 推荐教程
- Spring Boot + Vue 全栈开发
- Redis 缓存实战
- 推荐系统算法实现
- JWT 认证与授权

---

## 📸 项目截图

### 用户端
> 首页、房源列表、房源详情、个人中心

### 房东端
> 房源管理、订单管理、数据统计、收入报表

### 管理员端
> 用户管理、房源管理、订单管理、数据大盘

---

## 🎯 未来规划

- [ ] 移动端适配（响应式设计）
- [ ] 微信小程序版本
- [ ] 实时聊天功能（WebSocket）
- [ ] 支付宝/微信支付集成
- [ ] 多语言支持（国际化）
- [ ] 优惠券系统
- [ ] 积分系统
- [ ] 房源地图展示
- [ ] 高级搜索过滤
- [ ] 更多推荐算法（深度学习）

---



## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [MyBatis-Plus](https://baomidou.com/)
- [Redis](https://redis.io/)
- [ECharts](https://echarts.apache.org/)
- [阿里云通义千问](https://www.aliyun.com/product/dashscope)
- [高德地图](https://lbs.amap.com/)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐ Star！**

Made with ❤️ by [haidegm]

</div>
