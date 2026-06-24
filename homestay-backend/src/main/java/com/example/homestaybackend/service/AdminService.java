package com.example.homestaybackend.service;

import com.example.homestaybackend.dto.AdminHostDTO;
import com.example.homestaybackend.dto.AdminHouseDTO;
import com.example.homestaybackend.dto.AdminOrderDTO;
import com.example.homestaybackend.dto.AdminStatsDTO;
import com.example.homestaybackend.dto.AdminUserDTO;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.entity.Order;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.HouseImageRepository;
import com.example.homestaybackend.repository.HouseRepository;
import com.example.homestaybackend.repository.OrderRepository;
import com.example.homestaybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseImageRepository houseImageRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MessageService messageService;

    @PersistenceContext
    private EntityManager entityManager;

    // 辅助方法：处理图片URL
    private String processImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        // 如果已经是完整URL，直接返回
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        // 否则添加服务器前缀
        return "http://localhost:8080" + (url.startsWith("/") ? url : "/" + url);
    }

    // 获取统计数据
    public AdminStatsDTO getStats() {
        AdminStatsDTO stats = new AdminStatsDTO();
        
        // 统计房东数量
        stats.setTotalHosts(userRepository.countByRole("ROLE_HOST"));
        
        // 统计房源数量
        stats.setTotalHouses(houseRepository.count());
        
        // 统计用户数量
        stats.setTotalUsers(userRepository.countByRole("ROLE_USER"));
        
        // 统计订单数量
        String orderCountSql = "SELECT COUNT(*) FROM `order`";
        Query orderCountQuery = entityManager.createNativeQuery(orderCountSql);
        Long orderCount = ((Number) orderCountQuery.getSingleResult()).longValue();
        stats.setTotalOrders(orderCount);
        
        // 统计交易总额（已完成订单）
        String revenueSql = "SELECT COALESCE(SUM(total_price), 0) FROM `order` WHERE status = 'COMPLETED'";
        Query revenueQuery = entityManager.createNativeQuery(revenueSql);
        BigDecimal totalRevenue = (BigDecimal) revenueQuery.getSingleResult();
        stats.setTotalRevenue(totalRevenue.doubleValue());
        
        return stats;
    }

    // 获取房东列表
    public Map<String, Object> getHostList(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        
        Page<User> userPage;
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findByRoleAndUsernameContainingOrNicknameContaining(
                "ROLE_HOST", keyword, keyword, pageable);
        } else if (status != null && !status.isEmpty()) {
            userPage = userRepository.findByRoleAndStatus("ROLE_HOST", status, pageable);
        } else {
            userPage = userRepository.findByRole("ROLE_HOST", pageable);
        }
        
        List<AdminHostDTO> hostList = userPage.getContent().stream().map(user -> {
            AdminHostDTO dto = new AdminHostDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setEmail(user.getUsername() + "@example.com");
            dto.setPhone(user.getPhone());
            dto.setAvatar(processImageUrl(user.getAvatar()));
            dto.setStatus(user.getStatus() != null ? user.getStatus() : "ACTIVE");
            dto.setCreatedAt(user.getCreatedTime());
            
            // 统计房源数量
            Long houseCount = houseRepository.countByHostId(user.getId());
            dto.setHouseCount(houseCount);
            
            // 统计订单数量和总收入
            String sql = "SELECT COUNT(*), COALESCE(SUM(total_price), 0) FROM `order` o " +
                        "JOIN house h ON o.house_id = h.id WHERE h.host_id = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, user.getId());
            Object[] result = (Object[]) query.getSingleResult();
            dto.setOrderCount(((Number) result[0]).longValue());
            dto.setTotalIncome((BigDecimal) result[1]);
            
            return dto;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", hostList);
        response.put("total", userPage.getTotalElements());
        return response;
    }

    // 获取房源列表
    public Map<String, Object> getHouseList(String keyword, String type, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        
        Page<House> housePage;
        if (keyword != null && !keyword.isEmpty()) {
            housePage = houseRepository.findByTitleContainingOrAddressContaining(keyword, keyword, pageable);
        } else if (status != null && !status.isEmpty()) {
            housePage = houseRepository.findByStatus(status, pageable);
        } else {
            housePage = houseRepository.findAll(pageable);
        }
        
        List<AdminHouseDTO> houseList = housePage.getContent().stream().map(house -> {
            AdminHouseDTO dto = new AdminHouseDTO();
            dto.setId(house.getId());
            dto.setTitle(house.getTitle());
            dto.setAddress(house.getAddress());
            dto.setType("entire"); // 默认类型
            dto.setPrice(house.getPrice());
            dto.setGuests(house.getMaxGuests());
            dto.setBedrooms(1); // 默认值
            dto.setBeds(house.getBedCount());
            dto.setBathrooms(1); // 默认值
            
            // 计算真实评分
            String ratingSql = "SELECT AVG(rating) FROM review WHERE house_id = ?";
            Query ratingQuery = entityManager.createNativeQuery(ratingSql);
            ratingQuery.setParameter(1, house.getId());
            Object avgRating = ratingQuery.getSingleResult();
            if (avgRating != null) {
                dto.setRating(new BigDecimal(avgRating.toString()));
            } else {
                dto.setRating(null); // 没有评分时设为null，前端显示"新房源"
            }
            
            dto.setStatus(house.getStatus());
            dto.setRejectReason(house.getRejectReason());
            dto.setDescription(house.getDescription());
            dto.setCreatedAt(house.getCreatedTime());
            
            // 获取封面图片
            var images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
            if (!images.isEmpty()) {
                dto.setCoverImage(processImageUrl(images.get(0).getImageUrl()));
            }
            
            // 获取房东信息
            User host = userRepository.findById(house.getHostId()).orElse(null);
            if (host != null) {
                dto.setHostName(host.getNickname() != null ? host.getNickname() : host.getUsername());
                dto.setHostAvatar(processImageUrl(host.getAvatar()));
            }
            
            // 统计订单数量
            String sql = "SELECT COUNT(*) FROM `order` WHERE house_id = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, house.getId());
            Long orderCount = ((Number) query.getSingleResult()).longValue();
            dto.setOrderCount(orderCount);
            
            return dto;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", houseList);
        response.put("total", housePage.getTotalElements());
        return response;
    }

    // 切换房东状态
    public void toggleHostStatus(Long hostId) {
        User user = userRepository.findById(hostId)
            .orElseThrow(() -> new RuntimeException("房东不存在"));
        
        String newStatus = "ACTIVE".equals(user.getStatus()) ? "DISABLED" : "ACTIVE";
        user.setStatus(newStatus);
        userRepository.save(user);
    }

    // 切换房源状态
    public void toggleHouseStatus(Long houseId) {
        House house = houseRepository.findById(houseId)
            .orElseThrow(() -> new RuntimeException("房源不存在"));
        
        String newStatus = "ACTIVE".equals(house.getStatus()) ? "INACTIVE" : "ACTIVE";
        house.setStatus(newStatus);
        houseRepository.save(house);
    }

    // 删除房源
    public void deleteHouse(Long houseId) {
        houseRepository.deleteById(houseId);
    }

    // 获取用户列表
    public Map<String, Object> getUserList(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        
        Page<User> userPage;
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findByRoleAndUsernameContainingOrNicknameContaining(
                "ROLE_USER", keyword, keyword, pageable);
        } else if (status != null && !status.isEmpty()) {
            userPage = userRepository.findByRoleAndStatus("ROLE_USER", status, pageable);
        } else {
            userPage = userRepository.findByRole("ROLE_USER", pageable);
        }
        
        List<AdminUserDTO> userList = userPage.getContent().stream().map(user -> {
            AdminUserDTO dto = new AdminUserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setEmail(user.getUsername() + "@example.com");
            dto.setPhone(user.getPhone());
            dto.setAvatar(processImageUrl(user.getAvatar()));
            dto.setStatus(user.getStatus() != null ? user.getStatus() : "ACTIVE");
            dto.setCreatedAt(user.getCreatedTime());
            
            // 统计订单数量
            String orderSql = "SELECT COUNT(*) FROM `order` WHERE user_id = ?";
            Query orderQuery = entityManager.createNativeQuery(orderSql);
            orderQuery.setParameter(1, user.getId());
            Long orderCount = ((Number) orderQuery.getSingleResult()).longValue();
            dto.setOrderCount(orderCount);
            
            // 统计收藏数量（假设有收藏表，这里先设为0）
            dto.setWishlistCount(0L);
            
            return dto;
        }).collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", userList);
        response.put("total", userPage.getTotalElements());
        return response;
    }

    // 切换用户状态
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String newStatus = "ACTIVE".equals(user.getStatus()) ? "DISABLED" : "ACTIVE";
        user.setStatus(newStatus);
        userRepository.save(user);
    }

    // 审核房源（通过）
    public void approveHouse(Long houseId) {
        House house = houseRepository.findById(houseId)
            .orElseThrow(() -> new RuntimeException("房源不存在"));
        
        house.setStatus("ACTIVE");
        houseRepository.save(house);
    }

    // 审核房源（拒绝）
    public void rejectHouse(Long houseId, String reason) {
        House house = houseRepository.findById(houseId)
            .orElseThrow(() -> new RuntimeException("房源不存在"));

        house.setStatus("REJECTED");
        house.setRejectReason(reason);
        houseRepository.save(house);

        // 发送系统消息通知房东
        if (house.getHostId() != null) {
            String msg = "您的房源「" + house.getTitle() + "」审核未通过";
            if (reason != null && !reason.isEmpty()) {
                msg += "，原因：" + reason;
            }
            try {
                messageService.sendSystemMessage(house.getHostId(), msg, houseId);
            } catch (Exception e) {
                System.err.println("发送系统消息失败: " + e.getMessage());
            }
        }
    }

    // 获取订单列表
    public Map<String, Object> getOrderList(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        
        Page<Order> orderPage;
        if (status != null && !status.isEmpty()) {
            orderPage = orderRepository.findByStatus(status, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }
        
        List<AdminOrderDTO> orderList = orderPage.getContent().stream().map(order -> {
            AdminOrderDTO dto = new AdminOrderDTO();
            dto.setId(order.getId());
            dto.setOrderNo(order.getOrderNo());
            dto.setStatus(order.getStatus());
            dto.setCheckInDate(order.getCheckInDate());
            dto.setCheckOutDate(order.getCheckOutDate());
            dto.setGuestCount(order.getGuestCount());
            dto.setTotalPrice(order.getTotalPrice());
            dto.setCreatedTime(order.getCreatedTime());
            
            // 计算入住天数
            if (order.getCheckInDate() != null && order.getCheckOutDate() != null) {
                dto.setNights((int) java.time.temporal.ChronoUnit.DAYS.between(
                    order.getCheckInDate(), order.getCheckOutDate()));
            }
            
            // 获取用户信息
            User user = userRepository.findById(order.getUserId()).orElse(null);
            if (user != null) {
                dto.setUserId(user.getId());
                dto.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                dto.setUserAvatar(processImageUrl(user.getAvatar()));
                dto.setUserPhone(user.getPhone());
            }
            
            // 获取房源信息
            House house = houseRepository.findById(order.getHouseId()).orElse(null);
            if (house != null) {
                dto.setHouseId(house.getId());
                dto.setHouseTitle(house.getTitle());
                dto.setHouseAddress(house.getAddress());
                
                // 获取房源封面图
                var images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
                if (!images.isEmpty()) {
                    dto.setHouseCoverImage(processImageUrl(images.get(0).getImageUrl()));
                }
                
                // 获取房东信息
                User host = userRepository.findById(house.getHostId()).orElse(null);
                if (host != null) {
                    dto.setHostId(host.getId());
                    dto.setHostName(host.getNickname() != null ? host.getNickname() : host.getUsername());
                    dto.setHostPhone(host.getPhone());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        // 如果有关键字搜索，过滤结果
        if (keyword != null && !keyword.isEmpty()) {
            orderList = orderList.stream()
                .filter(dto -> 
                    (dto.getOrderNo() != null && dto.getOrderNo().contains(keyword)) ||
                    (dto.getUserName() != null && dto.getUserName().contains(keyword)) ||
                    (dto.getHouseTitle() != null && dto.getHouseTitle().contains(keyword)) ||
                    (dto.getHostName() != null && dto.getHostName().contains(keyword))
                )
                .collect(Collectors.toList());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("list", orderList);
        response.put("total", keyword != null && !keyword.isEmpty() ? orderList.size() : orderPage.getTotalElements());
        return response;
    }
    
    // 取消订单
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    public void updateOrderStatus(Long orderId, String status) {
        if (status == null || !List.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED").contains(status)) {
            throw new RuntimeException("Invalid order status");
        }

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        orderRepository.save(order);
    }
    
    // 删除订单
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    // 获取平台增长趋势数据
    public Map<String, Object> getGrowthData(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算日期范围
        java.time.LocalDate start;
        java.time.LocalDate end;
        
        if (startDate != null && endDate != null) {
            start = java.time.LocalDate.parse(startDate);
            end = java.time.LocalDate.parse(endDate);
        } else {
            // 默认最近30天
            end = java.time.LocalDate.now();
            start = end.minusDays(29);
        }
        
        // 生成日期列表
        List<String> dates = new ArrayList<>();
        java.time.LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            dates.add(currentDate.toString());
            currentDate = currentDate.plusDays(1);
        }
        
        // 统计每天的累计数据
        List<Long> userCounts = new ArrayList<>();
        List<Long> houseCounts = new ArrayList<>();
        List<Long> hostCounts = new ArrayList<>();
        
        for (String dateStr : dates) {
            // 累计用户数
            String userSql = "SELECT COUNT(*) FROM user WHERE role = 'ROLE_USER' AND DATE(created_time) <= ?";
            Query userQuery = entityManager.createNativeQuery(userSql);
            userQuery.setParameter(1, dateStr);
            userCounts.add(((Number) userQuery.getSingleResult()).longValue());
            
            // 累计房源数
            String houseSql = "SELECT COUNT(*) FROM house WHERE DATE(created_time) <= ?";
            Query houseQuery = entityManager.createNativeQuery(houseSql);
            houseQuery.setParameter(1, dateStr);
            houseCounts.add(((Number) houseQuery.getSingleResult()).longValue());
            
            // 累计房东数
            String hostSql = "SELECT COUNT(*) FROM user WHERE role = 'ROLE_HOST' AND DATE(created_time) <= ?";
            Query hostQuery = entityManager.createNativeQuery(hostSql);
            hostQuery.setParameter(1, dateStr);
            hostCounts.add(((Number) hostQuery.getSingleResult()).longValue());
        }
        
        result.put("dates", dates);
        result.put("users", userCounts);
        result.put("houses", houseCounts);
        result.put("hosts", hostCounts);
        
        return result;
    }
    
    // 获取房源状态分布
    public Map<String, Object> getHouseStatusData() {
        Map<String, Object> result = new HashMap<>();
        result.put("pending", houseRepository.countByStatus("PENDING"));
        result.put("active", houseRepository.countByStatus("ACTIVE"));
        result.put("inactive", houseRepository.countByStatus("INACTIVE"));
        return result;
    }
    
    // 获取订单趋势数据
    public Map<String, Object> getOrdersData(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算日期范围
        java.time.LocalDate start;
        java.time.LocalDate end;
        
        if (startDate != null && endDate != null) {
            start = java.time.LocalDate.parse(startDate);
            end = java.time.LocalDate.parse(endDate);
        } else {
            // 默认最近30天
            end = java.time.LocalDate.now();
            start = end.minusDays(29);
        }
        
        // 生成日期列表
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        java.time.LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            String dateStr = currentDate.toString();
            dates.add(dateStr);
            
            // 统计当天订单数
            String sql = "SELECT COUNT(*) FROM `order` WHERE DATE(created_time) = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, dateStr);
            counts.add(((Number) query.getSingleResult()).longValue());
            
            currentDate = currentDate.plusDays(1);
        }
        
        result.put("dates", dates);
        result.put("counts", counts);
        
        return result;
    }
    
    // 获取收益趋势数据
    public Map<String, Object> getRevenueData(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算日期范围
        java.time.LocalDate start;
        java.time.LocalDate end;
        
        if (startDate != null && endDate != null) {
            start = java.time.LocalDate.parse(startDate);
            end = java.time.LocalDate.parse(endDate);
        } else {
            // 默认最近30天
            end = java.time.LocalDate.now();
            start = end.minusDays(29);
        }
        
        // 生成日期列表
        List<String> dates = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();
        
        java.time.LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            String dateStr = currentDate.toString();
            dates.add(dateStr);
            
            // 统计当天收益（已完成订单）
            String sql = "SELECT COALESCE(SUM(total_price), 0) FROM `order` " +
                        "WHERE status = 'COMPLETED' AND DATE(created_time) = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, dateStr);
            BigDecimal revenue = (BigDecimal) query.getSingleResult();
            revenues.add(revenue.doubleValue());
            
            currentDate = currentDate.plusDays(1);
        }
        
        result.put("dates", dates);
        result.put("revenues", revenues);
        
        return result;
    }
}
