# 评价弹窗功能说明

## 功能概述
在房源详情页面，用户可以点击"显示全部评价"按钮，打开一个爱彼迎风格的弹窗，查看所有评价并进行筛选。

## 主要功能

### 1. 评价筛选
- **全部**：显示所有评价
- **好评**：评分 > 4星的评价
- **中评**：评分 > 2星且 ≤ 4星的评价
- **差评**：评分 ≤ 2星的评价

每个筛选按钮会显示对应类型的评价数量。

### 2. 评价展示
每条评价包含：
- 用户头像和昵称
- 评价日期（年月格式）
- 评分（星级 + 数字）
- 评价内容
- 评价图片（如果有）
- 房东回复（如果有）

### 3. 图片预览
- 点击评价中的图片可以放大查看
- 支持左右切换查看多张图片
- 使用Element Plus的ImageViewer组件
- 图片预览层级为3000，确保在弹窗之上

### 4. 房东回复
如果房东对评价进行了回复，会在评价下方显示：
- 灰色背景区域
- 聊天图标 + "房东回复"标题
- 回复内容

## 技术实现

### 组件文件
- **ReviewsDialog.vue** - 评价弹窗组件
- **HouseDetail.vue** - 房源详情页（集成弹窗）

### 数据结构
```javascript
{
  id: 评价ID,
  userName: '用户名',
  avatar: '用户头像URL',
  rating: 4.5, // 评分
  comment: '评价内容',
  createdTime: '2024-01-15T10:30:00',
  images: '/review/1/image1.jpg,/review/1/image2.jpg', // 逗号分隔
  reply: '房东回复内容' // 可选
}
```

### 样式特点
- 圆角16px的弹窗
- 最大高度90vh，内容区域可滚动
- 自定义滚动条样式
- 筛选按钮采用圆角胶囊样式
- 激活状态为黑色背景白色文字
- 图片网格布局，自适应列数
- 图片hover时放大1.05倍

## 使用方法

### 在HouseDetail.vue中集成

1. **导入组件**
```vue
import ReviewsDialog from '../../components/ReviewsDialog.vue'
```

2. **添加响应式变量**
```javascript
const reviewsDialogVisible = ref(false)
```

3. **修改按钮点击事件**
```vue
<el-button 
  v-if="reviews.length > 6"
  class="show-more-btn"
  @click="reviewsDialogVisible = true"
>
  显示全部{{ reviews.length }}条评价
</el-button>
```

4. **添加弹窗组件**
```vue
<ReviewsDialog
  v-model:visible="reviewsDialogVisible"
  :reviews="reviews"
  :average-rating="averageRating"
/>
```

### 确保评价数据完整
在loadReviews函数中，确保包含images和reply字段：
```javascript
reviews.value = res.map(item => ({
  id: item.id,
  userName: item.userName || '匿名用户',
  avatar: item.userAvatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.userId}`,
  rating: item.rating,
  comment: item.comment,
  createdTime: item.createdTime,
  images: item.images || '', // 评价图片
  reply: item.reply || '' // 房东回复
}))
```

## 后端接口

### 获取房源评价
```
GET /api/review/house/{houseId}
```

**响应数据**（ReviewDTO）：
```json
[
  {
    "id": 1,
    "orderId": 10,
    "houseId": 5,
    "userId": 3,
    "userName": "张三",
    "userAvatar": "/uploads/avatar/user3.jpg",
    "rating": 4.5,
    "comment": "房源很不错，位置方便，房东热情",
    "reply": "感谢您的好评！",
    "isAnonymous": false,
    "images": "/review/1/img1.jpg,/review/1/img2.jpg",
    "createdTime": "2024-01-15T10:30:00",
    "houseTitle": "市中心温馨公寓",
    "houseCoverImage": "/house/5/cover.jpg"
  }
]
```

## 测试步骤

### 1. 准备测试数据
确保数据库中有评价数据，包括：
- 不同评分的评价（好评、中评、差评）
- 带图片的评价
- 有房东回复的评价

### 2. 测试筛选功能
1. 打开房源详情页
2. 点击"显示全部X条评价"按钮
3. 弹窗打开，默认显示"全部"
4. 点击"好评"，只显示>4星的评价
5. 点击"中评"，只显示2-4星的评价
6. 点击"差评"，只显示≤2星的评价
7. 验证每个筛选按钮显示的数量是否正确

### 3. 测试图片预览
1. 找到带图片的评价
2. 点击图片
3. 图片应该放大显示
4. 可以左右切换查看多张图片
5. 点击关闭按钮或按ESC键关闭预览

### 4. 测试滚动
1. 如果评价数量较多（超过5-6条）
2. 评价列表应该可以滚动
3. 滚动条样式应该是自定义的细滚动条

### 5. 测试响应式
1. 调整浏览器窗口大小
2. 弹窗应该保持居中
3. 图片网格应该自适应列数
4. 在小屏幕上也能正常显示

## 样式定制

### 修改筛选按钮颜色
```css
.filter-btn.active {
  background: #FF385C; /* 改为爱彼迎红色 */
  border-color: #FF385C;
  color: white;
}
```

### 修改图片网格列数
```css
.review-images {
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); /* 调整最小宽度 */
}
```

### 修改弹窗宽度
```vue
<el-dialog
  width="900px" <!-- 改为更宽 -->
  ...
>
```

## 注意事项

1. **图片路径处理**
   - 评价图片路径可能是相对路径或绝对路径
   - 组件会自动处理，添加baseURL前缀

2. **图片预览层级**
   - ImageViewer的z-index设置为3000
   - 确保在弹窗（z-index通常为2000+）之上

3. **性能优化**
   - 评价列表设置了max-height和overflow-y
   - 避免一次性渲染过多DOM元素

4. **空状态处理**
   - 如果筛选后没有评价，显示空状态提示
   - 使用Element Plus的Empty组件

5. **图片加载失败**
   - 头像加载失败时显示默认头像
   - 评价图片加载失败时可以考虑添加占位图

## 扩展功能建议

### 1. 评价排序
添加排序选项：
- 最新优先
- 评分最高
- 评分最低

### 2. 评价搜索
添加搜索框，支持关键词搜索评价内容

### 3. 评价统计
显示评分分布图表：
- 5星：XX条
- 4星：XX条
- 3星：XX条
- 2星：XX条
- 1星：XX条

### 4. 分页加载
如果评价数量非常多，可以实现分页或无限滚动加载

### 5. 评价点赞
允许用户对有用的评价点赞

## 相关文件
- 前端组件：`homestay/src/components/ReviewsDialog.vue`
- 集成页面：`homestay/src/views/user/HouseDetail.vue`
- 后端控制器：`homestay-backend/src/main/java/com/example/homestaybackend/controller/ReviewController.java`
- 数据传输对象：`homestay-backend/src/main/java/com/example/homestaybackend/dto/ReviewDTO.java`
- 实体类：`homestay-backend/src/main/java/com/example/homestaybackend/entity/Review.java`
