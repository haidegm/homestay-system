package com.example.homestaybackend.controller;

import com.example.homestaybackend.dto.CreateReviewDTO;
import com.example.homestaybackend.dto.ReviewDTO;
import com.example.homestaybackend.entity.Review;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return user.getId();
    }

    /**
     * 创建评论
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(
            @RequestBody CreateReviewDTO dto,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getCurrentUserId(principal);
            Review review = reviewService.createReview(userId, dto);
            response.put("success", true);
            response.put("message", "评论成功");
            response.put("data", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取房源的所有评论
     */
    @GetMapping("/house/{houseId}")
    public ResponseEntity<List<ReviewDTO>> getHouseReviews(@PathVariable Long houseId) {
        return ResponseEntity.ok(reviewService.getHouseReviews(houseId));
    }

    /**
     * 获取我的评论
     */
    @GetMapping("/my")
    public ResponseEntity<List<ReviewDTO>> getMyReviews(Principal principal) {
        Long userId = getCurrentUserId(principal);
        return ResponseEntity.ok(reviewService.getUserReviews(userId));
    }

    /**
     * 检查订单是否可以评论
     */
    @GetMapping("/can-review/{orderId}")
    public ResponseEntity<Map<String, Boolean>> canReview(@PathVariable Long orderId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("canReview", reviewService.canReview(orderId));
        return ResponseEntity.ok(response);
    }

    /**
     * 房东回复评论
     */
    @PutMapping("/{reviewId}/reply")
    public ResponseEntity<Map<String, Object>> replyReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> body,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long hostId = getCurrentUserId(principal);
            String reply = body.get("reply");
            Review review = reviewService.replyReview(reviewId, hostId, reply);
            response.put("success", true);
            response.put("message", "回复成功");
            response.put("data", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取房源的平均评分和评论数
     */
    @GetMapping("/stats/{houseId}")
    public ResponseEntity<Map<String, Object>> getReviewStats(@PathVariable Long houseId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", reviewService.getAverageRating(houseId));
        stats.put("reviewCount", reviewService.getReviewCount(houseId));
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取房东的所有评论（用于房东端评价管理）
     */
    @GetMapping("/host/reviews")
    public ResponseEntity<List<ReviewDTO>> getHostReviews(Principal principal) {
        Long hostId = getCurrentUserId(principal);
        return ResponseEntity.ok(reviewService.getHostReviews(hostId));
    }


    /**
     * 更新评价
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody CreateReviewDTO dto,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getCurrentUserId(principal);
            Review review = reviewService.updateReview(reviewId, userId, dto);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", review);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable Long reviewId,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getCurrentUserId(principal);
            reviewService.deleteReview(reviewId, userId);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 切换匿名状态
     */
    @PutMapping("/{reviewId}/toggle-anonymous")
    public ResponseEntity<Map<String, Object>> toggleAnonymous(
            @PathVariable Long reviewId,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getCurrentUserId(principal);
            Review review = reviewService.toggleAnonymous(reviewId, userId);
            response.put("success", true);
            response.put("message", "已切换匿名状态");
            response.put("isAnonymous", review.getIsAnonymous());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
