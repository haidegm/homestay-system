package com.example.homestaybackend.service;

import com.example.homestaybackend.entity.BrowseHistory;
import com.example.homestaybackend.repository.BrowseHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BrowseHistoryService {

    @Autowired
    private BrowseHistoryRepository browseHistoryRepository;

    @Autowired
    private com.example.homestaybackend.repository.HouseRepository houseRepository;

    @Autowired
    private com.example.homestaybackend.repository.HouseImageRepository houseImageRepository;

    @Autowired
    private com.example.homestaybackend.repository.ReviewRepository reviewRepository;

    /**
     * 记录浏览行为
     */
    @Transactional
    public void recordBrowse(Long userId, Long houseId) {
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setHouseId(houseId);
        history.setBrowseTime(LocalDateTime.now());
        history.setBrowseDuration(0);
        
        browseHistoryRepository.save(history);
    }

    /**
     * 记录浏览行为（带时长）
     */
    @Transactional
    public void recordBrowse(Long userId, Long houseId, Integer duration) {
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setHouseId(houseId);
        history.setBrowseTime(LocalDateTime.now());
        history.setBrowseDuration(duration);
        
        browseHistoryRepository.save(history);
    }

    /**
     * 获取用户的浏览记录
     */
    public List<BrowseHistory> getUserBrowseHistory(Long userId) {
        return browseHistoryRepository.findByUserIdOrderByBrowseTimeDesc(userId);
    }

    /**
     * 获取用户最近的浏览记录（限制数量）
     */
    public List<BrowseHistory> getRecentBrowseHistory(Long userId, int limit) {
        List<BrowseHistory> all = browseHistoryRepository.findRecentByUserId(userId);
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    /**
     * 获取用户最近N天的浏览记录
     */
    public List<BrowseHistory> getRecentDaysBrowseHistory(Long userId, int days) {
        LocalDateTime afterTime = LocalDateTime.now().minusDays(days);
        return browseHistoryRepository.findByUserIdAndBrowseTimeAfter(userId, afterTime);
    }

    /**
     * 检查用户是否浏览过某个房源
     */
    public boolean hasBrowsed(Long userId, Long houseId) {
        return browseHistoryRepository.existsByUserIdAndHouseId(userId, houseId);
    }

    /**
     * 清空用户的浏览记录
     */
    @Transactional
    public void clearUserHistory(Long userId) {
        browseHistoryRepository.deleteByUserId(userId);
    }

    /**
     * 获取最近浏览的房源详情（用于展示）
     */
    public List<java.util.Map<String, Object>> getRecentBrowsedHouses(Long userId, int limit) {
        List<BrowseHistory> history = getRecentBrowseHistory(userId, limit * 2); // 多取一些，去重后可能不够
        
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        java.util.Set<Long> addedHouseIds = new java.util.HashSet<>();
        
        for (BrowseHistory browse : history) {
            if (result.size() >= limit) {
                break;
            }
            
            Long houseId = browse.getHouseId();
            
            // 避免重复
            if (addedHouseIds.contains(houseId)) {
                continue;
            }
            
            com.example.homestaybackend.entity.House house = houseRepository.findById(houseId).orElse(null);
            if (house != null && "ACTIVE".equals(house.getStatus())) {
                java.util.Map<String, Object> houseData = new java.util.HashMap<>();
                houseData.put("houseId", house.getId());
                houseData.put("title", house.getTitle());
                houseData.put("price", house.getPrice());
                houseData.put("city", house.getCity());
                
                // 获取封面图片
                List<com.example.homestaybackend.entity.HouseImage> images = 
                    houseImageRepository.findByHouseIdOrderBySortOrderAsc(houseId);
                if (!images.isEmpty()) {
                    houseData.put("coverImage", images.get(0).getImageUrl());
                }
                
                // 获取平均评分
                Double avgRating = reviewRepository.getAverageRatingByHouseId(houseId);
                houseData.put("rating", avgRating != null ? avgRating : 0.0);
                
                result.add(houseData);
                addedHouseIds.add(houseId);
            }
        }
        
        return result;
    }
}
