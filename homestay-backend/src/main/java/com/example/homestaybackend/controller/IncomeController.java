package com.example.homestaybackend.controller;

import com.example.homestaybackend.entity.Order;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/host/income")
@CrossOrigin
public class IncomeController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public IncomeController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return user.getId();
    }

    @GetMapping
    public Map<String, Object> getIncomeStats(@RequestParam(defaultValue = "30") int range, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long hostId = getCurrentUserId(principal);
            
            // 获取房东的所有订单
            List<Order> orders = orderService.getHostOrders(hostId);
            
            // 计算日期范围
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(range);
            
            // 过滤指定日期范围内的订单
            List<Order> rangeOrders = orders.stream()
                    .filter(order -> {
                        LocalDate orderDate = order.getCreatedTime().toLocalDate();
                        return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());
            
            // 计算总收入（已确认和已完成的订单）
            double totalIncome = rangeOrders.stream()
                    .filter(order -> "CONFIRMED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus()))
                    .mapToDouble(order -> order.getTotalPrice().doubleValue())
                    .sum();
            
            // 计算待结算（已确认但未完成）
            double pending = rangeOrders.stream()
                    .filter(order -> "CONFIRMED".equals(order.getStatus()))
                    .mapToDouble(order -> order.getTotalPrice().doubleValue())
                    .sum();
            
            // 计算已结算（已完成）
            double settled = rangeOrders.stream()
                    .filter(order -> "COMPLETED".equals(order.getStatus()))
                    .mapToDouble(order -> order.getTotalPrice().doubleValue())
                    .sum();
            
            // 订单数
            int orderCount = rangeOrders.size();
            
            // 构建收入趋势数据
            List<Map<String, Object>> trend = buildTrendData(rangeOrders, startDate, endDate);
            
            // 构建收入来源占比数据
            List<Map<String, Object>> source = buildSourceData(rangeOrders);
            
            // 计算经营指标
            Map<String, String> metrics = calculateMetrics(orders, rangeOrders);
            
            // 组装返回数据
            result.put("success", true);
            Map<String, Object> data = new HashMap<>();
            data.put("totalIncome", Math.round(totalIncome * 100.0) / 100.0);
            data.put("pending", Math.round(pending * 100.0) / 100.0);
            data.put("settled", Math.round(settled * 100.0) / 100.0);
            data.put("orders", orderCount);
            data.put("trend", trend);
            data.put("source", source);
            data.put("metrics", metrics);
            
            result.put("data", data);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取收入统计失败：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 构建收入趋势数据
     */
    private List<Map<String, Object>> buildTrendData(List<Order> orders, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        // 按日期分组统计
        Map<LocalDate, Double> dailyIncome = new TreeMap<>();
        
        // 初始化所有日期为0
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyIncome.put(current, 0.0);
            current = current.plusDays(1);
        }
        
        // 累加每天的收入
        for (Order order : orders) {
            if ("CONFIRMED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) {
                LocalDate orderDate = order.getCreatedTime().toLocalDate();
                if (!orderDate.isBefore(startDate) && !orderDate.isAfter(endDate)) {
                    dailyIncome.merge(orderDate, order.getTotalPrice().doubleValue(), Double::sum);
                }
            }
        }
        
        // 转换为前端需要的格式
        for (Map.Entry<LocalDate, Double> entry : dailyIncome.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey().format(formatter));
            item.put("income", Math.round(entry.getValue() * 100.0) / 100.0);
            trend.add(item);
        }
        
        return trend;
    }
    
    /**
     * 构建收入来源占比数据
     */
    private List<Map<String, Object>> buildSourceData(List<Order> orders) {
        List<Map<String, Object>> source = new ArrayList<>();
        
        // 按状态分组统计
        Map<String, Double> statusIncome = new HashMap<>();
        statusIncome.put("已完成", 0.0);
        statusIncome.put("已确认", 0.0);
        statusIncome.put("待支付", 0.0);
        
        for (Order order : orders) {
            double amount = order.getTotalPrice().doubleValue();
            switch (order.getStatus()) {
                case "COMPLETED":
                    statusIncome.merge("已完成", amount, Double::sum);
                    break;
                case "CONFIRMED":
                    statusIncome.merge("已确认", amount, Double::sum);
                    break;
                case "PENDING":
                    statusIncome.merge("待支付", amount, Double::sum);
                    break;
            }
        }
        
        // 转换为前端需要的格式
        for (Map.Entry<String, Double> entry : statusIncome.entrySet()) {
            if (entry.getValue() > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", entry.getKey());
                item.put("value", Math.round(entry.getValue() * 100.0) / 100.0);
                source.add(item);
            }
        }
        
        return source;
    }
    
    /**
     * 计算经营指标
     */
    private Map<String, String> calculateMetrics(List<Order> allOrders, List<Order> rangeOrders) {
        Map<String, String> metrics = new HashMap<>();
        
        // 入住率（简化计算：已完成订单数 / 总订单数）
        long completedCount = rangeOrders.stream()
                .filter(order -> "COMPLETED".equals(order.getStatus()))
                .count();
        double occupancyRate = rangeOrders.isEmpty() ? 0 : (completedCount * 100.0 / rangeOrders.size());
        metrics.put("occupancyRate", String.format("%.1f%%", occupancyRate));
        
        // 平均房价
        double avgPrice = rangeOrders.stream()
                .filter(order -> "CONFIRMED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus()))
                .mapToDouble(order -> order.getTotalPrice().doubleValue())
                .average()
                .orElse(0.0);
        metrics.put("averagePrice", "¥ " + Math.round(avgPrice));
        
        // 取消率
        long cancelledCount = rangeOrders.stream()
                .filter(order -> "CANCELLED".equals(order.getStatus()))
                .count();
        double cancellationRate = rangeOrders.isEmpty() ? 0 : (cancelledCount * 100.0 / rangeOrders.size());
        metrics.put("cancellationRate", String.format("%.1f%%", cancellationRate));
        
        return metrics;
    }
}
