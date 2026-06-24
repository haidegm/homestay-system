package com.example.homestaybackend.controller;

import com.example.homestaybackend.dto.WishlistDTO;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.entity.Wishlist;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return user.getId();
    }

    /**
     * 添加到心愿单
     */
    @PostMapping("/add/{houseId}")
    public ResponseEntity<Map<String, Object>> addToWishlist(
            @PathVariable Long houseId,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getCurrentUserId(principal);
            Wishlist wishlist = wishlistService.addToWishlist(userId, houseId);
            response.put("success", true);
            response.put("message", "已添加到心愿单");
            response.put("data", wishlist);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 从心愿单移除
     */
    @DeleteMapping("/remove/{houseId}")
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @PathVariable Long houseId,
            Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getCurrentUserId(principal);
            wishlistService.removeFromWishlist(userId, houseId);
            response.put("success", true);
            response.put("message", "已从心愿单移除");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查是否在心愿单中
     */
    @GetMapping("/check/{houseId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(
            @PathVariable Long houseId,
            Principal principal) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            if (principal == null) {
                response.put("isInWishlist", false);
                return ResponseEntity.ok(response);
            }
            
            Long userId = getCurrentUserId(principal);
            boolean isInWishlist = wishlistService.isInWishlist(userId, houseId);
            response.put("isInWishlist", isInWishlist);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("检查心愿单状态失败: " + e.getMessage());
            response.put("isInWishlist", false);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取用户的心愿单
     */
    @GetMapping("/my")
    public ResponseEntity<List<WishlistDTO>> getMyWishlist(Principal principal) {
        try {
            if (principal == null) {
                System.out.println("Principal is null, 用户未登录");
                return ResponseEntity.ok(List.of()); // 返回空列表
            }
            
            Long userId = getCurrentUserId(principal);
            System.out.println("获取心愿单，用户ID: " + userId);
            List<WishlistDTO> wishlist = wishlistService.getUserWishlist(userId);
            System.out.println("心愿单数量: " + wishlist.size());
            return ResponseEntity.ok(wishlist);
        } catch (Exception e) {
            System.err.println("获取心愿单失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // 返回空列表而不是报错
        }
    }
}
