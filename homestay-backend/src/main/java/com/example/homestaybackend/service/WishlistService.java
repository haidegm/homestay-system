package com.example.homestaybackend.service;

import com.example.homestaybackend.dto.WishlistDTO;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.entity.HouseImage;
import com.example.homestaybackend.entity.Wishlist;
import com.example.homestaybackend.repository.HouseImageRepository;
import com.example.homestaybackend.repository.HouseRepository;
import com.example.homestaybackend.repository.ReviewRepository;
import com.example.homestaybackend.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseImageRepository houseImageRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // 辅助方法：处理图片URL
    private String processImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return "http://localhost:8080" + (url.startsWith("/") ? url : "/" + url);
    }

    /**
     * 添加到心愿单
     */
    @Transactional
    public Wishlist addToWishlist(Long userId, Long houseId) {
        // 检查是否已存在
        if (wishlistRepository.existsByUserIdAndHouseId(userId, houseId)) {
            throw new RuntimeException("已在心愿单中");
        }

        // 检查房源是否存在
        if (!houseRepository.existsById(houseId)) {
            throw new RuntimeException("房源不存在");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setHouseId(houseId);
        return wishlistRepository.save(wishlist);
    }

    /**
     * 从心愿单移除
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long houseId) {
        wishlistRepository.deleteByUserIdAndHouseId(userId, houseId);
    }

    /**
     * 检查是否在心愿单中
     */
    public boolean isInWishlist(Long userId, Long houseId) {
        return wishlistRepository.existsByUserIdAndHouseId(userId, houseId);
    }

    /**
     * 获取用户的心愿单
     */
    public List<WishlistDTO> getUserWishlist(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByCreatedTimeDesc(userId);

        return wishlists.stream().map(wishlist -> {
            WishlistDTO dto = new WishlistDTO();
            dto.setId(wishlist.getId());
            dto.setHouseId(wishlist.getHouseId());
            dto.setCreatedTime(wishlist.getCreatedTime());

            // 获取房源信息
            House house = houseRepository.findById(wishlist.getHouseId()).orElse(null);
            if (house != null) {
                dto.setHouseTitle(house.getTitle());
                dto.setPrice(house.getPrice());
                dto.setCity(house.getCity());
                dto.setProvince(house.getProvince());

                // 获取封面图
                List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
                if (!images.isEmpty()) {
                    dto.setHouseCoverImage(processImageUrl(images.get(0).getImageUrl()));
                }

                // 获取评分
                Double avgRating = reviewRepository.getAverageRatingByHouseId(house.getId());
                dto.setRating(avgRating);

                // 获取评论数
                long reviewCount = reviewRepository.countByHouseId(house.getId());
                dto.setReviewCount((int) reviewCount);
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
