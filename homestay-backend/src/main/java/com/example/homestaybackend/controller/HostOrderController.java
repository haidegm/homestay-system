package com.example.homestaybackend.controller;

import com.example.homestaybackend.dto.HostOrderDTO;
import com.example.homestaybackend.dto.HostStatsDTO;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.entity.HouseImage;
import com.example.homestaybackend.entity.Order;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.HouseImageRepository;
import com.example.homestaybackend.repository.HouseRepository;
import com.example.homestaybackend.repository.ReviewRepository;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/host")
@CrossOrigin
public class HostOrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final HouseRepository houseRepository;
    private final HouseImageRepository houseImageRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public HostOrderController(OrderService orderService, 
                               UserRepository userRepository,
                               HouseRepository houseRepository,
                               HouseImageRepository houseImageRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.houseRepository = houseRepository;
        this.houseImageRepository = houseImageRepository;
    }

    // 获取当前房东ID
    private Long getCurrentHostId(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return user.getId();
    }

    /**
     * 获取房东的所有订单
     */
    @GetMapping("/orders")
    public List<HostOrderDTO> getHostOrders(Principal principal) {
        Long hostId = getCurrentHostId(principal);
        return orderService.getHostOrders(hostId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取房东的最近订单（用于订单列表页面）
     */
    @GetMapping("/orders/list")
    public List<HostOrderDTO> getRecentOrders(
            @RequestParam(defaultValue = "5") int limit,
            Principal principal
    ) {
        Long hostId = getCurrentHostId(principal);
        return orderService.getRecentHostOrders(hostId, limit)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取房东统计数据（用于仪表盘）
     */
    @GetMapping("/stats")
    public ResponseEntity<HostStatsDTO> getStats(Principal principal) {
        Long hostId = getCurrentHostId(principal);
        HostStatsDTO stats = new HostStatsDTO();

        // 1. 统计房源数量
        Long houseCount = houseRepository.countByHostId(hostId);
        stats.setTotalHouses(houseCount);

        // 2. 统计订单数量和总收入
        String sql = "SELECT COUNT(*), COALESCE(SUM(total_price), 0) FROM `order` o " +
                    "JOIN house h ON o.house_id = h.id WHERE h.host_id = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, hostId);
        Object[] result = (Object[]) query.getSingleResult();
        stats.setTotalOrders(((Number) result[0]).longValue());
        stats.setTotalIncome((BigDecimal) result[1]);

        // 3. 计算平均评分
        List<House> houses = houseRepository.findByHostId(hostId);
        if (!houses.isEmpty()) {
            double totalRating = 0;
            int ratingCount = 0;
            for (House house : houses) {
                Double avgRating = reviewRepository.getAverageRatingByHouseId(house.getId());
                if (avgRating != null && avgRating > 0) {
                    totalRating += avgRating;
                    ratingCount++;
                }
            }
            if (ratingCount > 0) {
                stats.setAvgRating(Math.round(totalRating / ratingCount * 10.0) / 10.0);
            } else {
                stats.setAvgRating(0.0);
            }
        } else {
            stats.setAvgRating(0.0);
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取最近订单（用于仪表盘）
     */
    @GetMapping("/orders/recent")
    public ResponseEntity<List<HostOrderDTO>> getDashboardRecentOrders(
            @RequestParam(defaultValue = "5") int limit,
            Principal principal) {
        Long hostId = getCurrentHostId(principal);

        // 查询最近的订单
        String sql = "SELECT o.id, o.order_no, o.user_id, o.house_id, o.check_in_date, " +
                    "o.check_out_date, o.guest_count, o.total_price, o.status, o.created_time, " +
                    "h.title as house_title, u.nickname as guest_name, u.username as guest_username " +
                    "FROM `order` o " +
                    "JOIN house h ON o.house_id = h.id " +
                    "LEFT JOIN user u ON o.user_id = u.id " +
                    "WHERE h.host_id = ? " +
                    "ORDER BY o.created_time DESC " +
                    "LIMIT ?";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, hostId);
        query.setParameter(2, limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<HostOrderDTO> orders = new ArrayList<>();
        for (Object[] row : results) {
            HostOrderDTO dto = new HostOrderDTO();
            dto.setId(((Number) row[0]).longValue());
            dto.setOrderNo((String) row[1]);
            dto.setUserId(((Number) row[2]).longValue());
            dto.setHouseId(((Number) row[3]).longValue());
            
            // 处理日期
            if (row[4] != null) {
                if (row[4] instanceof LocalDate) {
                    dto.setCheckInDate((LocalDate) row[4]);
                } else {
                    dto.setCheckInDate(LocalDate.parse(row[4].toString()));
                }
            }
            if (row[5] != null) {
                if (row[5] instanceof LocalDate) {
                    dto.setCheckOutDate((LocalDate) row[5]);
                } else {
                    dto.setCheckOutDate(LocalDate.parse(row[5].toString()));
                }
            }
            
            dto.setGuestCount(((Number) row[6]).intValue());
            dto.setTotalPrice((BigDecimal) row[7]);
            dto.setStatus((String) row[8]);
            dto.setHouseTitle((String) row[10]);
            
            // 设置房客名称
            String guestName = (String) row[11];
            String guestUsername = (String) row[12];
            dto.setGuestName(guestName != null ? guestName : guestUsername);

            orders.add(dto);
        }

        return ResponseEntity.ok(orders);
    }

    private HostOrderDTO toDto(Order order) {
        HostOrderDTO dto = new HostOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setHouseId(order.getHouseId());
        dto.setCheckInDate(order.getCheckInDate());
        dto.setCheckOutDate(order.getCheckOutDate());
        dto.setGuestCount(order.getGuestCount());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setCreatedTime(order.getCreatedTime());

        House house = houseRepository.findById(order.getHouseId()).orElse(null);
        if (house != null) {
            dto.setHouseName(house.getTitle());
            List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
            if (!images.isEmpty()) {
                dto.setCoverImage(images.get(0).getImageUrl());
            }
        }

        User guest = userRepository.findById(order.getUserId()).orElse(null);
        if (guest != null) {
            dto.setGuestName(guest.getNickname() != null && !guest.getNickname().isEmpty()
                    ? guest.getNickname()
                    : guest.getUsername());
        }

        return dto;
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/chart-data")
    public ResponseEntity<?> getChartData(Principal principal) {
        Long hostId = getCurrentHostId(principal);
        
        Map<String, Object> chartData = new HashMap<>();
        
        try {
            // 1. 收入趋势数据（最近6个月）
            Map<String, Object> incomeData = new HashMap<>();
            List<String> dates = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();
            
            LocalDate now = LocalDate.now();
            for (int i = 5; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                dates.add(month.getMonthValue() + "月");
                
                // 查询该月的收入
                String sql = "SELECT COALESCE(SUM(o.total_price), 0) FROM `order` o " +
                           "JOIN house h ON o.house_id = h.id " +
                           "WHERE h.host_id = :hostId " +
                           "AND o.status IN ('CONFIRMED', 'COMPLETED') " +
                           "AND YEAR(o.check_in_date) = :year " +
                           "AND MONTH(o.check_in_date) = :month";
                
                Query query = entityManager.createNativeQuery(sql);
                query.setParameter("hostId", hostId);
                query.setParameter("year", month.getYear());
                query.setParameter("month", month.getMonthValue());
                
                BigDecimal income = (BigDecimal) query.getSingleResult();
                values.add(income);
            }
            
            incomeData.put("dates", dates);
            incomeData.put("values", values);
            chartData.put("incomeData", incomeData);
            
            // 2. 订单状态分布数据
            String statusSql = "SELECT o.status, COUNT(*) as count FROM `order` o " +
                             "JOIN house h ON o.house_id = h.id " +
                             "WHERE h.host_id = :hostId " +
                             "GROUP BY o.status";
            
            Query statusQuery = entityManager.createNativeQuery(statusSql);
            statusQuery.setParameter("hostId", hostId);
            
            List<Object[]> statusResults = statusQuery.getResultList();
            List<Map<String, Object>> orderStatusData = new ArrayList<>();
            
            Map<String, String> statusNameMap = new HashMap<>();
            statusNameMap.put("PENDING", "待确认");
            statusNameMap.put("CONFIRMED", "已确认");
            statusNameMap.put("COMPLETED", "已完成");
            statusNameMap.put("CANCELLED", "已取消");
            
            Map<String, String> statusColorMap = new HashMap<>();
            statusColorMap.put("PENDING", "#E6A23C");
            statusColorMap.put("CONFIRMED", "#67C23A");
            statusColorMap.put("COMPLETED", "#909399");
            statusColorMap.put("CANCELLED", "#F56C6C");
            
            for (Object[] result : statusResults) {
                String status = (String) result[0];
                Long count = ((Number) result[1]).longValue();
                
                Map<String, Object> statusItem = new HashMap<>();
                statusItem.put("name", statusNameMap.getOrDefault(status, status));
                statusItem.put("value", count);
                
                Map<String, String> itemStyle = new HashMap<>();
                itemStyle.put("color", statusColorMap.getOrDefault(status, "#909399"));
                statusItem.put("itemStyle", itemStyle);
                
                orderStatusData.add(statusItem);
            }
            
            chartData.put("orderStatusData", orderStatusData);
            
            return ResponseEntity.ok(chartData);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(chartData); // 返回空数据，前端会使用模拟数据
        }
    }
}
