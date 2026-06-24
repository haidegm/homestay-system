package com.example.homestaybackend.controller;

import com.example.homestaybackend.dto.OrderCreateDTO;
import com.example.homestaybackend.dto.UserTripDTO;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.entity.HouseImage;
import com.example.homestaybackend.entity.Order;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.HouseImageRepository;
import com.example.homestaybackend.repository.HouseRepository;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final HouseRepository houseRepository;
    private final HouseImageRepository houseImageRepository;

    public OrderController(OrderService orderService,
                           UserRepository userRepository,
                           HouseRepository houseRepository,
                           HouseImageRepository houseImageRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.houseRepository = houseRepository;
        this.houseImageRepository = houseImageRepository;
    }

    // 获取当前用户ID
    private Long getCurrentUserId(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return user.getId();
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody OrderCreateDTO dto, Principal principal) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查日期冲突
            boolean isAvailable = orderService.isHouseAvailable(
                dto.getHouseId(), 
                dto.getCheckInDate(), 
                dto.getCheckOutDate()
            );

            if (!isAvailable) {
                result.put("success", false);
                result.put("message", "选择的日期已被预订，请选择其他日期");
                return result;
            }

            // 创建订单
            Order order = new Order();
            order.setUserId(getCurrentUserId(principal));
            order.setHouseId(dto.getHouseId());
            order.setCheckInDate(dto.getCheckInDate());
            order.setCheckOutDate(dto.getCheckOutDate());
            order.setGuestCount(dto.getGuestCount());
            order.setTotalPrice(dto.getTotalPrice());
            order.setStatus("PENDING");

            Order savedOrder = orderService.createOrder(order);
            System.out.println("保存的订单: " + savedOrder);
            System.out.println("订单编号: " + savedOrder.getOrderNo());

            result.put("success", true);
            result.put("message", "订单创建成功");
            result.put("orderId", savedOrder.getId());
            result.put("orderNo", savedOrder.getOrderNo());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建订单失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取我的订单
     */
    @GetMapping("/my")
    public List<UserTripDTO> getMyOrders(Principal principal) {
        Long userId = getCurrentUserId(principal);
        return orderService.getUserOrders(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 支付订单（模拟）
     */
    @PostMapping("/pay/{orderId}")
    public Map<String, Object> payOrder(@PathVariable Long orderId, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        Long userId = getCurrentUserId(principal);

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            result.put("success", false);
            result.put("message", "订单不存在");
            return result;
        }
        if (!userId.equals(order.getUserId())) {
            result.put("success", false);
            result.put("message", "无权限操作该订单");
            return result;
        }

        order.setStatus("CONFIRMED");
        boolean updated = orderService.updateOrder(order);
        result.put("success", updated);
        result.put("message", updated ? "支付成功" : "支付失败");
        return result;
    }

    private UserTripDTO toDto(Order order) {
        UserTripDTO dto = new UserTripDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setHouseId(order.getHouseId());
        dto.setCheckInDate(order.getCheckInDate());
        dto.setCheckOutDate(order.getCheckOutDate());
        dto.setGuestCount(order.getGuestCount());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setCreatedTime(order.getCreatedTime());

        House house = houseRepository.findById(order.getHouseId()).orElse(null);
        if (house != null) {
            dto.setHouseTitle(house.getTitle());
            List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
            if (!images.isEmpty()) {
                dto.setCoverImage(images.get(0).getImageUrl());
            }
        }
        return dto;
    }

    /**
     * 检查房源可用性
     */
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

    /**
     * 检查房源今天是否有空房
     */
    @GetMapping("/available-today/{houseId}")
    public Map<String, Object> checkAvailableToday(@PathVariable Long houseId) {
        Map<String, Object> result = new HashMap<>();
        boolean available = orderService.isAvailableToday(houseId);
        result.put("available", available);
        return result;
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    public Map<String, Object> cancelOrder(@PathVariable Long orderId, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("收到取消订单请求，订单ID: " + orderId);
            
            Long userId = getCurrentUserId(principal);
            System.out.println("当前用户ID: " + userId);
            
            Order order = orderService.getOrderById(orderId);
            System.out.println("查询到的订单: " + order);
            
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            if (!userId.equals(order.getUserId())) {
                result.put("success", false);
                result.put("message", "无权限操作该订单");
                return result;
            }
            
            if ("COMPLETED".equals(order.getStatus())) {
                result.put("success", false);
                result.put("message", "订单已完成，无法取消");
                return result;
            }
            
            if ("CANCELLED".equals(order.getStatus())) {
                result.put("success", false);
                result.put("message", "订单已取消");
                return result;
            }
            
            order.setStatus("CANCELLED");
            boolean updated = orderService.updateOrder(order);
            System.out.println("更新结果: " + updated);
            
            result.put("success", updated);
            result.put("message", updated ? "取消成功" : "取消失败");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "取消订单失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{orderId}")
    public Map<String, Object> deleteOrder(@PathVariable Long orderId, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("收到删除订单请求，订单ID: " + orderId);
            
            Long userId = getCurrentUserId(principal);
            System.out.println("当前用户ID: " + userId);
            
            Order order = orderService.getOrderById(orderId);
            System.out.println("查询到的订单: " + order);
            
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            if (!userId.equals(order.getUserId())) {
                result.put("success", false);
                result.put("message", "无权限操作该订单");
                return result;
            }
            
            // 只能删除待支付或已取消的订单
            if (!"PENDING".equals(order.getStatus()) && !"CANCELLED".equals(order.getStatus())) {
                result.put("success", false);
                result.put("message", "只能删除待支付或已取消的订单");
                return result;
            }
            
            boolean deleted = orderService.deleteOrder(orderId);
            System.out.println("删除结果: " + deleted);
            
            result.put("success", deleted);
            result.put("message", deleted ? "删除成功" : "删除失败");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除订单失败：" + e.getMessage());
        }
        
        return result;
    }
}
