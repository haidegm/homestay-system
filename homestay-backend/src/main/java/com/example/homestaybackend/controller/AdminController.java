package com.example.homestaybackend.controller;

import com.example.homestaybackend.dto.AdminStatsDTO;
import com.example.homestaybackend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 获取统计数据
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // 获取平台增长趋势数据
    @GetMapping("/stats/growth")
    public ResponseEntity<Map<String, Object>> getGrowthData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(adminService.getGrowthData(startDate, endDate));
    }

    // 获取房源状态分布
    @GetMapping("/stats/house-status")
    public ResponseEntity<Map<String, Object>> getHouseStatusData() {
        return ResponseEntity.ok(adminService.getHouseStatusData());
    }

    // 获取订单趋势数据
    @GetMapping("/stats/orders")
    public ResponseEntity<Map<String, Object>> getOrdersData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(adminService.getOrdersData(startDate, endDate));
    }

    // 获取收益趋势数据
    @GetMapping("/stats/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(adminService.getRevenueData(startDate, endDate));
    }

    // 获取房东列表
    @GetMapping("/hosts")
    public ResponseEntity<Map<String, Object>> getHostList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getHostList(keyword, status, page, size));
    }

    // 切换房东状态
    @PutMapping("/hosts/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleHostStatus(@PathVariable Long id) {
        adminService.toggleHostStatus(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "操作成功");
        return ResponseEntity.ok(response);
    }

    // 获取房源列表
    @GetMapping("/houses")
    public ResponseEntity<Map<String, Object>> getHouseList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getHouseList(keyword, type, status, page, size));
    }

    // 切换房源状态
    @PutMapping("/houses/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleHouseStatus(@PathVariable Long id) {
        adminService.toggleHouseStatus(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "操作成功");
        return ResponseEntity.ok(response);
    }

    // 删除房源
    @DeleteMapping("/houses/{id}")
    public ResponseEntity<Map<String, String>> deleteHouse(@PathVariable Long id) {
        adminService.deleteHouse(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }

    // 获取用户列表
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getUserList(keyword, status, page, size));
    }

    // 切换用户状态
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<Map<String, String>> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "操作成功");
        return ResponseEntity.ok(response);
    }

    // 审核房源 - 通过
    @PutMapping("/houses/{id}/approve")
    public ResponseEntity<Map<String, String>> approveHouse(@PathVariable Long id) {
        adminService.approveHouse(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "审核通过");
        return ResponseEntity.ok(response);
    }

    // 审核房源 - 拒绝
    @PutMapping("/houses/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectHouse(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        adminService.rejectHouse(id, reason);
        Map<String, String> response = new HashMap<>();
        response.put("message", "已拒绝");
        return ResponseEntity.ok(response);
    }

    // 获取订单列表
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.getOrderList(keyword, status, page, size));
    }

    // 取消订单
    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable Long id) {
        adminService.cancelOrder(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "订单已取消");
        return ResponseEntity.ok(response);
    }

    // 删除订单
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, String>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        adminService.updateOrderStatus(id, body.get("status"));
        Map<String, String> response = new HashMap<>();
        response.put("message", "订单状态已更新");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Long id) {
        adminService.deleteOrder(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "订单已删除");
        return ResponseEntity.ok(response);
    }
}
