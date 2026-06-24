package com.example.homestaybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.homestaybackend.entity.Order;
import com.example.homestaybackend.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;

    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /**
     * 检查房源在指定日期是否可用
     */
    public boolean isHouseAvailable(Long houseId, LocalDate checkIn, LocalDate checkOut) {
        int count = orderMapper.countConflictingOrders(houseId, checkIn, checkOut);
        return count == 0;
    }

    /**
     * 创建订单
     */
    public Order createOrder(Order order) {
        // 手动生成订单号和创建时间（MyBatis-Plus 不会触发 @PrePersist）
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo("HS" + System.currentTimeMillis() + (int)(Math.random() * 1000));
        }
        if (order.getCreatedTime() == null) {
            order.setCreatedTime(LocalDateTime.now());
        }
        
        System.out.println("生成订单号: " + order.getOrderNo());
        orderMapper.insert(order);
        System.out.println("插入后订单ID: " + order.getId());
        return order;
    }

    /**
     * 查询用户的订单
     */
    public List<Order> getUserOrders(Long userId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("created_time");
        return orderMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取订单
     */
    public Order getOrderById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    /**
     * 更新订单
     */
    public boolean updateOrder(Order order) {
        System.out.println("更新订单: " + order);
        int result = orderMapper.updateById(order);
        System.out.println("MyBatis-Plus updateById 返回值: " + result);
        return result > 0;
    }

    /**
     * 查询房东的订单
     */
    public List<Order> getHostOrders(Long hostId) {
        return orderMapper.findByHostId(hostId);
    }

    /**
     * 查询房东的最近订单
     */
    public List<Order> getRecentHostOrders(Long hostId, int limit) {
        return orderMapper.findRecentByHostId(hostId, limit);
    }

    /**
     * 统计房东数据
     */
    public int countHostOrders(Long hostId) {
        return orderMapper.countByHostId(hostId);
    }

    public Double sumHostIncome(Long hostId) {
        return orderMapper.sumIncomeByHostId(hostId);
    }

    /**
     * 检查房源今天是否有空房
     */
    public boolean isAvailableToday(Long houseId) {
        LocalDate today = LocalDate.now();
        return isHouseAvailable(houseId, today, today.plusDays(1));
    }

    /**
     * 删除订单
     */
    public boolean deleteOrder(Long orderId) {
        System.out.println("删除订单ID: " + orderId);
        int result = orderMapper.deleteById(orderId);
        System.out.println("MyBatis-Plus deleteById 返回值: " + result);
        return result > 0;
    }
}
