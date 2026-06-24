package com.example.homestaybackend.controller;

import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.UserRepository;
import com.example.homestaybackend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> request) {
        Long receiverId = Long.valueOf(request.get("receiverId").toString());
        String content = request.get("content").toString();
        Long houseId = request.get("houseId") != null ? Long.valueOf(request.get("houseId").toString()) : null;
        Long orderId = request.get("orderId") != null ? Long.valueOf(request.get("orderId").toString()) : null;

        // 从SecurityContext获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);
        
        if (currentUser == null) {
            return Map.of("success", false, "message", "用户未登录");
        }

        Long senderId = currentUser.getId();

        return messageService.sendMessage(senderId, receiverId, content, houseId, orderId);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public List<Map<String, Object>> getConversations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);
        
        if (currentUser == null) {
            return List.of();
        }

        Long userId = currentUser.getId();
        String role = currentUser.getRole();
        
        // 确保role格式正确（添加ROLE_前缀）
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return messageService.getConversations(userId, role);
    }

    /**
     * 获取会话消息
     */
    @GetMapping("/conversation/{conversationId}")
    public Map<String, Object> getMessages(@PathVariable Long conversationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);
        
        if (currentUser == null) {
            return Map.of("success", false, "message", "用户未登录");
        }

        Long currentUserId = currentUser.getId();
        return messageService.getMessages(conversationId, currentUserId);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("未读消息数接口：用户未认证");
                return Map.of("unreadCount", 0, "msg", "用户未认证");
            }
            
            String username = authentication.getName();
            System.out.println("未读消息数接口：当前用户 = " + username);
            
            if (username == null || "anonymousUser".equals(username)) {
                System.out.println("未读消息数接口：匿名用户");
                return Map.of("unreadCount", 0, "msg", "匿名用户");
            }
            
            User currentUser = userRepository.findByUsername(username);
            
            if (currentUser == null) {
                System.out.println("未读消息数接口：用户不存在 - " + username);
                return Map.of("unreadCount", 0, "msg", "用户不存在");
            }

            Long userId = currentUser.getId();
            String role = currentUser.getRole();
            
            // 确保role格式正确（添加ROLE_前缀）
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }

            Long count = messageService.getUnreadCount(userId, role);
            System.out.println("未读消息数接口：用户 " + username + " 的未读消息数 = " + count);

            return Map.of("unreadCount", count);
        } catch (Exception e) {
            System.err.println("获取未读消息数失败: " + e.getMessage());
            e.printStackTrace();
            return Map.of("unreadCount", 0, "msg", "获取失败: " + e.getMessage());
        }
    }
}
