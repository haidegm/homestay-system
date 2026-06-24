package com.example.homestaybackend.controller;

import com.example.homestaybackend.dto.RecommendationDTO;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
@CrossOrigin
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 为当前用户推荐房源
     */
    @GetMapping("/for-me")
    public ResponseEntity<?> recommendForMe(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "hybrid") String method,
            Principal principal) {
        
        if (principal == null) {
            // 未登录用户返回热门房源
            List<RecommendationDTO> popular = recommendationService.getPopularHouses(limit);
            return ResponseEntity.ok(popular);
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        // 尝试获取个性化推荐
        List<RecommendationDTO> recommendations = recommendationService.recommendForUser(
                user.getId(), limit, method);
        
        // 如果推荐结果为空或太少，补充热门房源
        if (recommendations == null || recommendations.isEmpty()) {
            System.out.println("用户 " + user.getId() + " 没有推荐结果，返回热门房源");
            recommendations = recommendationService.getPopularHouses(limit);
        } else if (recommendations.size() < limit) {
            System.out.println("用户 " + user.getId() + " 推荐结果不足，补充热门房源");
            // 补充热门房源
            List<RecommendationDTO> popular = recommendationService.getPopularHouses(limit * 2);
            java.util.Set<Long> existingIds = recommendations.stream()
                    .map(RecommendationDTO::getHouseId)
                    .collect(java.util.stream.Collectors.toSet());
            
            for (RecommendationDTO p : popular) {
                if (!existingIds.contains(p.getHouseId())) {
                    recommendations.add(p);
                    if (recommendations.size() >= limit) break;
                }
            }
        }
        
        return ResponseEntity.ok(recommendations);
    }

    /**
     * 为指定用户推荐房源（管理员使用）
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> recommendForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "hybrid") String method) {
        
        List<RecommendationDTO> recommendations = recommendationService.recommendForUser(
                userId, limit, method);
        
        return ResponseEntity.ok(recommendations);
    }

    /**
     * 推荐相似房源
     */
    @GetMapping("/similar/{houseId}")
    public ResponseEntity<?> recommendSimilar(
            @PathVariable Long houseId,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<RecommendationDTO> recommendations = recommendationService.recommendSimilarHouses(
                houseId, limit);
        
        return ResponseEntity.ok(recommendations);
    }

    /**
     * 获取热门房源
     */
    @GetMapping("/popular")
    public ResponseEntity<?> getPopular(
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<RecommendationDTO> popular = recommendationService.getPopularHouses(limit);
        return ResponseEntity.ok(popular);
    }

    /**
     * 手动触发模型训练（管理员使用）
     */
    @PostMapping("/train")
    public ResponseEntity<?> trainModel() {
        boolean success = recommendationService.trainModel();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "模型训练已触发" : "模型训练失败");
        
        return ResponseEntity.ok(response);
    }
}
