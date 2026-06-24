package com.example.homestaybackend.controller;

import com.example.homestaybackend.scheduler.OrderScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/scheduler")
@CrossOrigin(origins = "*")
public class OrderSchedulerController {

    @Autowired
    private OrderScheduler orderScheduler;

    /**
     * 手动触发订单状态检查
     * 管理员可以随时调用此接口来更新过期订单的状态
     */
    @PostMapping("/check-orders")
    public Map<String, Object> checkOrders() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("\n=== 手动触发：检查过期订单 ===");
            orderScheduler.completeExpiredOrders();
            
            result.put("success", true);
            result.put("message", "订单状态检查完成");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "检查失败：" + e.getMessage());
        }
        
        return result;
    }
}
