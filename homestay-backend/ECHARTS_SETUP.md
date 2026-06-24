# 管理员数据统计可视化模块

## 功能说明

管理员Dashboard现在包含完整的ECharts数据可视化功能，支持自定义日期范围查询。

### 1. 统计卡片
- 用户总数
- 房源总数
- 订单总数
- 交易总额

### 2. 日期范围选择
- 支持按月份范围筛选数据
- 默认显示最近12个月
- 可自定义开始月份和结束月份
- 一键重置到默认范围

### 3. 可视化图表

#### 平台数据增长趋势（柱状图）
- 显示用户数、房源数、房东数的增长趋势
- 支持日期范围筛选
- 三组数据并排对比
- 不同颜色渐变区分

#### 房源状态分布（饼图）
- 待审核（PENDING）
- 已上线（ACTIVE）
- 已下架（INACTIVE）

#### 每月订单数量趋势（柱状图）
- 显示选定时间范围内的订单数量
- 渐变色柱状图
- 鼠标悬停显示具体数量

#### 平台收益趋势（折线图）
- 显示选定时间范围内的平台收益
- 仅统计已完成（COMPLETED）订单
- 面积填充效果

## 后端接口

### 1. 获取统计数据
```
GET /api/admin/stats
```

返回：
```json
{
  "totalUsers": 100,
  "totalHouses": 50,
  "totalOrders": 200,
  "totalRevenue": 50000.00
}
```

### 2. 获取图表数据（支持日期范围）
```
GET /api/admin/stats/charts?startMonth=2025-05&endMonth=2026-04
```

参数：
- `startMonth`（可选）：开始月份，格式 YYYY-MM
- `endMonth`（可选）：结束月份，格式 YYYY-MM
- 如果不传参数，默认返回最近12个月的数据

返回：
```json
{
  "platformGrowth": {
    "months": ["2025-05", "2025-06", ...],
    "users": [10, 15, 20, ...],
    "houses": [5, 8, 12, ...],
    "hosts": [2, 3, 5, ...]
  },
  "houseStatus": {
    "pending": 5,
    "active": 40,
    "inactive": 5
  },
  "monthlyOrders": {
    "months": ["2025-05", "2025-06", ...],
    "counts": [10, 15, 20, ...]
  },
  "monthlyRevenue": {
    "months": ["2025-05", "2025-06", ...],
    "revenues": [5000.00, 7500.00, ...]
  }
}
```

## 数据库修改

### HouseRepository 新增方法
```java
long countByStatus(String status);
```

## 测试数据

### 1. 生成订单测试数据
```bash
mysql -u root -p homestay < insert_test_orders_for_charts.sql
```

### 2. 生成用户和房东测试数据
```bash
mysql -u root -p homestay < insert_test_users_for_growth_chart.sql
```

这些脚本会生成最近12个月的测试数据，用于测试图表显示。

## 前端依赖

已安装 ECharts：
```bash
npm install echarts --save
```

## 使用说明

1. 启动后端服务
2. 启动前端服务
3. 以管理员身份登录（admin/admin123）
4. 访问管理员Dashboard页面
5. 使用日期选择器筛选不同时间范围的数据
6. 点击"重置"按钮恢复到默认的最近12个月

## 功能特点

- 图表会自动适配窗口大小
- 支持自定义日期范围查询
- 收益统计仅包含已完成（COMPLETED）状态的订单
- 月份格式为 yyyy-MM
- 图表数据实时从数据库查询
- 平台增长数据显示累计数量
- 订单和收益数据显示当月数量

## 注意事项

- 日期范围选择器使用月份范围选择模式
- 开始月份不能晚于结束月份
- 默认显示最近12个月的数据
- 所有图表会根据选择的日期范围同步更新

