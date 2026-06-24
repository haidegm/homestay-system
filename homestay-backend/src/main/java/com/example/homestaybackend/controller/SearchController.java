package com.example.homestaybackend.controller;

import com.example.homestaybackend.dto.HouseSearchDTO;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.entity.HouseImage;
import com.example.homestaybackend.repository.HouseImageRepository;
import com.example.homestaybackend.repository.HouseRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/house")
@CrossOrigin
public class SearchController {

    private final HouseRepository houseRepository;
    private final HouseImageRepository houseImageRepository;
    private final com.example.homestaybackend.service.OrderService orderService;
    private final com.example.homestaybackend.service.RecommendationService recommendationService;
    private final com.example.homestaybackend.repository.UserRepository userRepository;
    private final com.example.homestaybackend.repository.ReviewRepository reviewRepository;

    public SearchController(HouseRepository houseRepository, 
                           HouseImageRepository houseImageRepository,
                           com.example.homestaybackend.service.OrderService orderService,
                           com.example.homestaybackend.service.RecommendationService recommendationService,
                           com.example.homestaybackend.repository.UserRepository userRepository,
                           com.example.homestaybackend.repository.ReviewRepository reviewRepository) {
        this.houseRepository = houseRepository;
        this.houseImageRepository = houseImageRepository;
        this.orderService = orderService;
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/search")
    public List<HouseSearchDTO> searchHouses(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double radius,
            @RequestParam(required = false) Integer maxGuests
    ) {
        // 简单实现：获取所有房源，后续可以优化为地理位置查询
        List<House> houses = houseRepository.findAll();
        List<HouseSearchDTO> result = new ArrayList<>();

        for (House house : houses) {
            // 只显示已上架的房源
            if (!"ACTIVE".equals(house.getStatus())) {
                continue;
            }
            
            // 如果房源没有坐标，跳过
            if (house.getLatitude() == null || house.getLongitude() == null) {
                continue;
            }

            // 计算距离（简单的欧几里得距离，实际应该用球面距离）
            double distance = calculateDistance(latitude, longitude, 
                house.getLatitude().doubleValue(), house.getLongitude().doubleValue());

            // 如果超出搜索半径，跳过
            if (distance > radius) {
                continue;
            }

            // 如果指定了人数要求，检查是否满足
            if (maxGuests != null && house.getMaxGuests() < maxGuests) {
                continue;
            }

            HouseSearchDTO dto = new HouseSearchDTO();
            dto.setId(house.getId());
            dto.setTitle(house.getTitle());
            dto.setCity(house.getCity());
            dto.setAddress(house.getAddress());
            dto.setPrice(house.getPrice());
            dto.setMaxGuests(house.getMaxGuests());
            dto.setBedCount(house.getBedCount());
            dto.setLatitude(house.getLatitude());
            dto.setLongitude(house.getLongitude());
            dto.setDistance(Math.round(distance * 100.0) / 100.0); // 保留两位小数

            // 检查今日是否可用
            dto.setAvailableToday(orderService.isAvailableToday(house.getId()));

            Double avgRating = reviewRepository.getAverageRatingByHouseId(house.getId());
            if (avgRating != null) {
                dto.setRating(avgRating);
            }

            // 获取封面图片
            List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
            if (!images.isEmpty()) {
                dto.setCoverImage(images.get(0).getImageUrl());
            }

            result.add(dto);
        }

        // 按距离排序
        result.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));

        return result;
    }

    @GetMapping("/recommend")
    public List<HouseSearchDTO> recommendHouses(
            @RequestParam(required = false) String group,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        List<House> houses = new ArrayList<>();
        
        // 尝试获取当前用户
        Long userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String username = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication().getName();
                if (!"anonymousUser".equals(username)) {
                    com.example.homestaybackend.entity.User user = userRepository.findByUsername(username);
                    if (user != null) {
                        userId = user.getId();
                    }
                }
            } catch (Exception e) {
                System.err.println("获取用户信息失败: " + e.getMessage());
            }
        }
        
        // 获取所有房源
        List<House> allHouses = houseRepository.findAll();
        
        // 按城市分组
        java.util.Map<String, List<House>> housesByCity = new java.util.HashMap<>();
        for (House house : allHouses) {
            if ("ACTIVE".equals(house.getStatus())) {
                String city = house.getCity();
                housesByCity.computeIfAbsent(city, k -> new ArrayList<>()).add(house);
            }
        }
        
        // 处理杭州的房源（随机展示，不使用推荐算法）
        List<House> hangzhouHouses = housesByCity.getOrDefault("杭州市", new ArrayList<>());
        if (hangzhouHouses.isEmpty()) {
            // 尝试其他可能的杭州命名
            hangzhouHouses = housesByCity.getOrDefault("杭州", new ArrayList<>());
        }
        
        if (!hangzhouHouses.isEmpty()) {
            // 随机打乱杭州房源
            java.util.Collections.shuffle(hangzhouHouses);
            // 限制最多20个
            int hangzhouLimit = Math.min(20, hangzhouHouses.size());
            houses.addAll(hangzhouHouses.subList(0, hangzhouLimit));
            System.out.println("杭州使用随机展示，从 " + hangzhouHouses.size() + " 个房源中随机选择了 " + hangzhouLimit + " 个");
        }
        
        // 处理其他城市的房源（随机展示）
        List<String> otherCities = new ArrayList<>(housesByCity.keySet());
        otherCities.remove("杭州市");
        otherCities.remove("杭州");
        java.util.Collections.shuffle(otherCities);
        
        // 最多添加5个其他城市（加上杭州总共6个）
        int citiesAdded = 0;
        for (String city : otherCities) {
            if (citiesAdded >= 5) break;
            
            List<House> cityHouses = housesByCity.get(city);
            java.util.Collections.shuffle(cityHouses);
            // 每个其他城市也限制最多10个
            int cityLimit = Math.min(10, cityHouses.size());
            houses.addAll(cityHouses.subList(0, cityLimit));
            citiesAdded++;
        }
        
        System.out.println("返回房源：杭州（随机）+ " + citiesAdded + " 个其他城市（随机）");
        
        // 构建返回结果
        List<HouseSearchDTO> result = new ArrayList<>();

        for (House house : houses) {
            HouseSearchDTO dto = new HouseSearchDTO();
            dto.setId(house.getId());
            dto.setTitle(house.getTitle());
            dto.setCity(convertCityName(house.getCity()));
            dto.setAddress(house.getAddress());
            dto.setPrice(house.getPrice());
            dto.setMaxGuests(house.getMaxGuests());
            dto.setBedCount(house.getBedCount());

            // 检查今日是否可用
            dto.setAvailableToday(orderService.isAvailableToday(house.getId()));

            // 获取封面图片
            List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
            
            if (!images.isEmpty()) {
                dto.setCoverImage(images.get(0).getImageUrl());
            } else {
                dto.setCoverImage("https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400&h=300&fit=crop");
            }
            
            // 获取平均评分
            Double avgRating = reviewRepository.getAverageRatingByHouseId(house.getId());
            if (avgRating != null) {
                dto.setRating(avgRating);
            }

            result.add(dto);
        }

        System.out.println("最终返回 " + result.size() + " 个已上架房源");
        return result;
    }
    
    // 城市名称转换
    private String convertCityName(String cityPinyin) {
        if (cityPinyin == null) return "未知";
        
        switch (cityPinyin.toLowerCase()) {
            case "hangzhou": return "杭州";
            case "chengdu": return "成都";
            case "zhejiang": return "浙江";
            case "sichuan": return "四川";
            case "beijing": return "北京";
            case "shanghai": return "上海";
            case "guangzhou": return "广州";
            case "shenzhen": return "深圳";
            case "nanjing": return "南京";
            case "suzhou": return "苏州";
            case "wuhan": return "武汉";
            case "xian": return "西安";
            case "qingdao": return "青岛";
            case "dalian": return "大连";
            case "tianjin": return "天津";
            case "chongqing": return "重庆";
            default: return cityPinyin; // 如果已经是中文，直接返回
        }
    }

    // 计算两点间距离（公里）
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
