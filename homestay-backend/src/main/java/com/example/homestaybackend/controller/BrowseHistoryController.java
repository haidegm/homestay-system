package com.example.homestaybackend.controller;

import com.example.homestaybackend.entity.BrowseHistory;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.service.BrowseHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/browse")
@CrossOrigin
public class BrowseHistoryController {

    @Autowired
    private BrowseHistoryService browseHistoryService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 记录浏览行为
     */
    @PostMapping("/record/{houseId}")
    public ResponseEntity<?> recordBrowse(
            @PathVariable Long houseId,
            @RequestBody(required = false) Map<String, Object> body) {
        
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            Integer duration = 0;
            if (body != null && body.containsKey("duration")) {
                duration = (Integer) body.get("duration");
            }

            if (duration > 0) {
                browseHistoryService.recordBrowse(user.getId(), houseId, duration);
            } else {
                browseHistoryService.recordBrowse(user.getId(), houseId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "浏览记录已保存");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "记录失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取用户的浏览记录
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyBrowseHistory() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            List<BrowseHistory> history = browseHistoryService.getUserBrowseHistory(user.getId());
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取浏览记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户最近的浏览记录
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentBrowseHistory(
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            List<BrowseHistory> history = browseHistoryService.getRecentBrowseHistory(user.getId(), limit);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取浏览记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近浏览的房源详情（用于推荐展示）
     */
    @GetMapping("/recent-houses")
    public ResponseEntity<?> getRecentBrowsedHouses(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username);
            
            if (user == null) {
                // 未登录返回空列表
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }

            return ResponseEntity.ok(browseHistoryService.getRecentBrowsedHouses(user.getId(), limit));
            
        } catch (Exception e) {
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    /**
     * 清空浏览记录
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearBrowseHistory() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            browseHistoryService.clearUserHistory(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "浏览记录已清空");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清空失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
