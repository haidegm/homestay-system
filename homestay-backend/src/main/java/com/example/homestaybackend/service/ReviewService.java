package com.example.homestaybackend.service;

import com.example.homestaybackend.dto.CreateReviewDTO;
import com.example.homestaybackend.dto.ReviewDTO;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.entity.HouseImage;
import com.example.homestaybackend.entity.Order;
import com.example.homestaybackend.entity.Review;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private com.example.homestaybackend.repository.OrderRepository orderRepository;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseImageRepository houseImageRepository;

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

    /**
     * 创建评论
     */
    @Transactional
    public Review createReview(Long userId, CreateReviewDTO dto) {
        // 1. 验证订单
        Order order = orderRepository.findById(dto.getOrderId())
            .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 2. 验证订单所属用户
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权评论此订单");
        }

        // 3. 验证订单状态（只有已完成的订单才能评论）
        if (!"COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("订单未完成，无法评论");
        }

        // 4. 检查是否已评论
        if (reviewRepository.existsByOrderId(dto.getOrderId())) {
            throw new RuntimeException("该订单已评论");
        }

        // 5. 创建评论
        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setHouseId(order.getHouseId());
        review.setUserId(userId);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false);
        review.setImages(dto.getImages()); // 保存图片URL

        return reviewRepository.save(review);
    }

    /**
     * 获取房源的所有评论
     */
    public List<ReviewDTO> getHouseReviews(Long houseId) {
        List<Review> reviews = reviewRepository.findByHouseIdOrderByCreatedTimeDesc(houseId);
        
        return reviews.stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(review.getId());
            dto.setOrderId(review.getOrderId());
            dto.setHouseId(review.getHouseId());
            dto.setUserId(review.getUserId());
            dto.setRating(review.getRating());
            dto.setComment(review.getComment());
            dto.setReply(review.getReply());
            dto.setIsAnonymous(review.getIsAnonymous());
            dto.setCreatedTime(review.getCreatedTime());

            // 处理匿名显示
            if (review.getIsAnonymous() != null && review.getIsAnonymous()) {
                dto.setUserName("匿名用户");
                dto.setUserAvatar(null);
            } else {
                // 获取用户信息
                User user = userRepository.findById(review.getUserId()).orElse(null);
                if (user != null) {
                    dto.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    dto.setUserAvatar(processImageUrl(user.getAvatar()));
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户的所有评论
     */
    public List<ReviewDTO> getUserReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedTimeDesc(userId);
        
        return reviews.stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(review.getId());
            dto.setOrderId(review.getOrderId());
            dto.setHouseId(review.getHouseId());
            dto.setUserId(review.getUserId());
            dto.setRating(review.getRating());
            dto.setComment(review.getComment());
            dto.setReply(review.getReply());
            dto.setIsAnonymous(review.getIsAnonymous());
            dto.setCreatedTime(review.getCreatedTime());

            // 获取房源信息
            House house = houseRepository.findById(review.getHouseId()).orElse(null);
            if (house != null) {
                dto.setHouseTitle(house.getTitle());
                
                // 获取封面图
                List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
                if (!images.isEmpty()) {
                    dto.setHouseCoverImage(processImageUrl(images.get(0).getImageUrl()));
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 房东回复评论
     */
    @Transactional
    public Review replyReview(Long reviewId, Long hostId, String reply) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 验证房东权限
        House house = houseRepository.findById(review.getHouseId())
            .orElseThrow(() -> new RuntimeException("房源不存在"));

        if (!house.getHostId().equals(hostId)) {
            throw new RuntimeException("无权回复此评论");
        }

        review.setReply(reply);
        return reviewRepository.save(review);
    }

    /**
     * 检查订单是否可以评论
     */
    public boolean canReview(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        // 订单必须是已完成状态
        if (!"COMPLETED".equals(order.getStatus())) {
            return false;
        }

        // 订单不能已经评论过
        return !reviewRepository.existsByOrderId(orderId);
    }

    /**
     * 获取房源的平均评分
     */
    public Double getAverageRating(Long houseId) {
        Double avg = reviewRepository.getAverageRatingByHouseId(houseId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    /**
     * 获取房源的评论数量
     */
    public long getReviewCount(Long houseId) {
        return reviewRepository.countByHouseId(houseId);
    }

    /**
     * 获取房东的所有评论
     */
    public List<ReviewDTO> getHostReviews(Long hostId) {
        // 获取房东的所有房源
        List<House> houses = houseRepository.findByHostId(hostId);
        List<Long> houseIds = houses.stream().map(House::getId).collect(Collectors.toList());

        System.out.println("房东ID: " + hostId + ", 房源数量: " + houses.size());

        if (houseIds.isEmpty()) {
            return List.of();
        }

        // 获取这些房源的所有评论
        List<Review> reviews = reviewRepository.findByHouseIdInOrderByCreatedTimeDesc(houseIds);
        System.out.println("评论数量: " + reviews.size());

        return reviews.stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(review.getId());
            dto.setOrderId(review.getOrderId());
            dto.setHouseId(review.getHouseId());
            dto.setUserId(review.getUserId());
            dto.setRating(review.getRating());
            dto.setComment(review.getComment());
            dto.setReply(review.getReply());
            dto.setIsAnonymous(review.getIsAnonymous());
            dto.setCreatedTime(review.getCreatedTime());

            System.out.println("评论ID: " + review.getId() + ", 用户ID: " + review.getUserId() + ", 是否匿名: " + review.getIsAnonymous());

            // 处理匿名显示
            if (review.getIsAnonymous() != null && review.getIsAnonymous()) {
                dto.setUserName("匿名用户");
                dto.setUserAvatar(null);
                System.out.println("  -> 匿名用户");
            } else {
                // 获取用户信息
                User user = userRepository.findById(review.getUserId()).orElse(null);
                if (user != null) {
                    String userName = user.getNickname() != null ? user.getNickname() : user.getUsername();
                    dto.setUserName(userName);
                    dto.setUserAvatar(processImageUrl(user.getAvatar()));
                    System.out.println("  -> 用户: " + userName + ", 头像: " + user.getAvatar() + " -> " + dto.getUserAvatar());
                } else {
                    System.out.println("  -> 用户不存在");
                }
            }

            // 获取房源信息
            House house = houseRepository.findById(review.getHouseId()).orElse(null);
            if (house != null) {
                dto.setHouseTitle(house.getTitle());
                
                // 获取封面图
                List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());
                if (!images.isEmpty()) {
                    dto.setHouseCoverImage(processImageUrl(images.get(0).getImageUrl()));
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }


    /**
     * 更新评价（用户可以修改评分、评论内容、匿名状态和图片）
     */
    @Transactional
    public Review updateReview(Long reviewId, Long userId, CreateReviewDTO dto) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("评价不存在"));

        // 验证用户权限
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此评价");
        }

        // 更新评价内容
        if (dto.getRating() != null) {
            review.setRating(dto.getRating());
        }
        if (dto.getComment() != null) {
            review.setComment(dto.getComment());
        }
        if (dto.getIsAnonymous() != null) {
            review.setIsAnonymous(dto.getIsAnonymous());
        }
        if (dto.getImages() != null) {
            review.setImages(dto.getImages());
        }

        return reviewRepository.save(review);
    }

    /**
     * 删除评价
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("评价不存在"));

        // 验证用户权限
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评价");
        }

        reviewRepository.delete(review);
    }

    /**
     * 切换匿名状态
     */
    @Transactional
    public Review toggleAnonymous(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("评价不存在"));

        // 验证用户权限
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此评价");
        }

        // 切换匿名状态
        review.setIsAnonymous(!review.getIsAnonymous());
        return reviewRepository.save(review);
    }
}
