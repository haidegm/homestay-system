# 房源可用性检查和地图标记修复

## 问题描述

1. **地图标记动画错误**: 鼠标悬停在房源卡片上时，控制台报错 `TypeError: marker.setAnimation is not a function`
2. **可用性检查错误**: 搜索后所有房源都显示不可用，即使它们没有被预订

## 根本原因

### 1. 地图标记动画问题
- 使用了 `marker.setAnimation('AMAP_ANIMATION_BOUNCE')` 方法
- 该方法在当前版本的高德地图API中可能不支持或语法不正确
- 导致控制台报错

### 2. 可用性检查问题
- `request.js` 的响应拦截器已经返回了 `response.data`
- 后端返回: `{ available: true/false, message: "..." }`
- 前端代码错误地尝试访问 `res.data.available` 或使用了错误的默认值逻辑
- 应该直接访问 `res.available`

## 解决方案

### 1. 修复地图标记动画 (SearchResult.vue)

**之前的代码**:
```javascript
const handleHouseHover = (house) => {
  if (!house.latitude || !house.longitude) return
  const marker = markers.value.find(m => m.getExtData().houseId === house.id)
  if (marker) {
    try {
      marker.setAnimation('AMAP_ANIMATION_BOUNCE')
      hoveredMarker.value = marker
    } catch (error) {
      console.log('Marker animation is not supported', error)
    }
  }
}
```

**修复后的代码**:
```javascript
const handleHouseHover = (house) => {
  if (!house.latitude || !house.longitude || !map.value) return
  const marker = markers.value.find(m => m.getExtData().houseId === house.id)
  if (marker) {
    // 高亮标记：放大图标
    marker.setIcon(new AMap.Icon({
      size: new AMap.Size(40, 40),
      image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
      imageSize: new AMap.Size(40, 40)
    }))
    hoveredMarker.value = marker
  }
}
```

**改进**:
- 不再使用 `setAnimation()` 方法
- 改用 `setIcon()` 方法放大图标来实现高亮效果
- 悬停时图标从 30x30 放大到 40x40
- 移开时恢复到 30x30

### 2. 修复可用性检查 (SearchResult.vue)

**之前的代码**:
```javascript
const res = await request.get(`/api/order/availability/${house.id}`, {
  params: { checkIn: range.checkIn, checkOut: range.checkOut }
})
console.log(`房源${house.id}可用性:`, JSON.stringify(res))
house.available = res.available
```

**修复后的代码**:
```javascript
const res = await request.get(`/api/order/availability/${house.id}`, {
  params: { checkIn: range.checkIn, checkOut: range.checkOut }
})
console.log(`房源${house.id}可用性:`, res)
// request.js的响应拦截器已经返回了response.data
// 所以res就是 { available: true/false, message: "..." }
house.available = res.available !== false
```

**改进**:
- 添加注释说明响应结构
- 使用 `res.available !== false` 确保正确处理布尔值
- 移除 `JSON.stringify()` 以便在控制台查看完整对象

## 后端逻辑确认

### OrderController.java - checkAvailability 方法
```java
@GetMapping("/availability/{houseId}")
public Map<String, Object> checkAvailability(
    @PathVariable Long houseId,
    @RequestParam String checkIn,
    @RequestParam String checkOut
) {
    Map<String, Object> result = new HashMap<>();
    try {
        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);
        boolean isAvailable = orderService.isHouseAvailable(houseId, checkInDate, checkOutDate);
        
        result.put("available", isAvailable);
        if (!isAvailable) {
            result.put("message", "选择的日期已被预订");
        }
    } catch (Exception e) {
        result.put("available", false);
        result.put("message", "检查可用性失败：" + e.getMessage());
    }
    return result;
}
```

### OrderMapper.java - SQL查询
```java
@Select("SELECT COUNT(*) FROM `order` " +
        "WHERE house_id = #{houseId} " +
        "AND status != 'CANCELLED' " +
        "AND NOT (check_out_date <= #{checkIn} OR check_in_date >= #{checkOut})")
int countConflictingOrders(@Param("houseId") Long houseId,
                            @Param("checkIn") LocalDate checkIn,
                            @Param("checkOut") LocalDate checkOut);
```

**逻辑说明**:
- 查询指定房源在指定日期范围内的冲突订单数量
- 排除已取消的订单 (`status != 'CANCELLED'`)
- 检查日期重叠: `NOT (check_out_date <= #{checkIn} OR check_in_date >= #{checkOut})`
- 如果 count = 0，则房源可用

## 测试建议

1. **地图标记测试**:
   - 打开搜索结果页面
   - 鼠标悬停在房源卡片上
   - 确认地图上对应的标记图标放大
   - 确认控制台没有错误

2. **可用性检查测试**:
   - 搜索房源（带日期范围）
   - 查看控制台日志，确认每个房源的可用性检查结果
   - 确认未被预订的房源显示为可用（无灰色水印）
   - 确认已被预订的房源显示为不可用（灰色水印）

3. **日期逻辑测试**:
   - 搜索时选择未来日期
   - 确认水印显示"选定日期不可用"（如果不可用）
   - 搜索时不选择日期
   - 确认水印显示"当日无房源"（如果不可用）

## 修改的文件

- `homestay/src/views/user/SearchResult.vue`
  - 修复 `checkAvailability()` 函数
  - 修复 `handleHouseHover()` 和 `handleHouseLeave()` 函数

## 注意事项

- `HouseDetail.vue` 中的可用性检查已经是正确的，无需修改
- `request.js` 的响应拦截器会自动返回 `response.data`，所以所有API调用都应该直接访问返回对象的属性
- 地图标记的图标URL使用了高德地图的默认图标，可以根据需要替换为自定义图标
