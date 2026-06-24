package com.example.homestaybackend.service;

import com.example.homestaybackend.entity.Conversation;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.entity.HouseImage;
import com.example.homestaybackend.entity.Message;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.ConversationRepository;
import com.example.homestaybackend.repository.HouseImageRepository;
import com.example.homestaybackend.repository.HouseRepository;
import com.example.homestaybackend.repository.MessageRepository;
import com.example.homestaybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private HouseRepository houseRepository;
    
    @Autowired
    private HouseImageRepository houseImageRepository;

    /**
     * 发送消息
     */
    @Transactional
    public Map<String, Object> sendMessage(Long senderId, Long receiverId, String content, Long houseId, Long orderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 确定谁是用户，谁是房东
            User sender = userRepository.findById(senderId).orElse(null);
            User receiver = userRepository.findById(receiverId).orElse(null);

            if (sender == null || receiver == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            Long userId = sender.getRole().equals("ROLE_USER") ? senderId : receiverId;
            Long hostId = sender.getRole().equals("ROLE_HOST") ? senderId : receiverId;

            // 查找或创建会话 - 按用户、房东和房源查找
            Conversation conversation = null;
            
            // 如果有houseId，查找特定房源的会话
            if (houseId != null) {
                List<Conversation> conversations = conversationRepository.findByUserIdOrderByLastMessageTimeDesc(userId);
                for (Conversation conv : conversations) {
                    if (conv.getHostId().equals(hostId) && 
                        conv.getHouseId() != null && 
                        conv.getHouseId().equals(houseId)) {
                        conversation = conv;
                        break;
                    }
                }
            } else {
                // 没有houseId，查找任意会话
                conversation = conversationRepository.findByUserIdAndHostId(userId, hostId).orElse(null);
            }
            
            // 如果没找到会话，创建新会话
            if (conversation == null) {
                conversation = new Conversation();
                conversation.setUserId(userId);
                conversation.setHostId(hostId);
                conversation.setHouseId(houseId);
                conversation.setOrderId(orderId);
                conversation.setCreatedTime(LocalDateTime.now());
                conversation.setUpdatedTime(LocalDateTime.now());
                conversation.setUnreadCountUser(0);
                conversation.setUnreadCountHost(0);
            }

            // 更新会话信息
            conversation.setLastMessage(content);
            conversation.setLastMessageTime(LocalDateTime.now());
            conversation.setUpdatedTime(LocalDateTime.now());

            // 更新未读数
            if (senderId.equals(userId)) {
                conversation.setUnreadCountHost(conversation.getUnreadCountHost() + 1);
            } else {
                conversation.setUnreadCountUser(conversation.getUnreadCountUser() + 1);
            }

            conversationRepository.save(conversation);

            // 创建消息
            Message message = new Message();
            message.setConversationId(conversation.getId());
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setContent(content);
            message.setIsRead(false);
            message.setCreatedTime(LocalDateTime.now());

            messageRepository.save(message);

            result.put("success", true);
            result.put("message", "发送成功");
            result.put("data", message);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "发送失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取会话列表
     */
    public List<Map<String, Object>> getConversations(Long userId, String role) {
        System.out.println("获取会话列表 - userId: " + userId + ", role: " + role);
        
        List<Conversation> conversations;

        if ("ROLE_USER".equals(role)) {
            conversations = conversationRepository.findByUserIdOrderByLastMessageTimeDesc(userId);
            System.out.println("用户会话数量: " + conversations.size());
        } else {
            conversations = conversationRepository.findByHostIdOrderByLastMessageTimeDesc(userId);
            System.out.println("房东会话数量: " + conversations.size());
        }

        return conversations.stream().map(conv -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", conv.getId());
            map.put("userId", conv.getUserId());
            map.put("hostId", conv.getHostId());
            map.put("houseId", conv.getHouseId());
            map.put("orderId", conv.getOrderId());
            map.put("lastMessage", conv.getLastMessage());
            map.put("lastMessageTime", conv.getLastMessageTime());

            // 获取对方信息
            Long otherId = "ROLE_USER".equals(role) ? conv.getHostId() : conv.getUserId();
            if (otherId != null && otherId == 0L) {
                map.put("otherName", "系统通知");
                map.put("otherAvatar", null);
            } else if (otherId != null) {
                User other = userRepository.findById(otherId).orElse(null);
                if (other != null) {
                    map.put("otherName", other.getNickname() != null ? other.getNickname() : other.getUsername());
                    map.put("otherAvatar", other.getAvatar());
                }
            }
            
            // 获取房源信息（如果有）
            if (conv.getHouseId() != null) {
                House house = houseRepository.findById(conv.getHouseId()).orElse(null);
                if (house != null) {
                    System.out.println("找到房源: ID=" + house.getId() + ", Title=" + house.getTitle());
                    map.put("houseTitle", house.getTitle());
                    // 获取第一张图片
                    List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(conv.getHouseId());
                    if (!images.isEmpty()) {
                        map.put("houseImage", images.get(0).getImageUrl());
                    }
                } else {
                    System.out.println("未找到房源: ID=" + conv.getHouseId());
                    map.put("houseTitle", "房源ID: " + conv.getHouseId());
                }
            }

            // 未读数
            map.put("unreadCount", "ROLE_USER".equals(role) ? conv.getUnreadCountUser() : conv.getUnreadCountHost());

            System.out.println("会话: " + map);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取会话消息
     */
    @Transactional
    public Map<String, Object> getMessages(Long conversationId, Long currentUserId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null) {
                result.put("success", false);
                result.put("message", "会话不存在");
                return result;
            }

            // 获取消息列表
            List<Message> messages = messageRepository.findByConversationIdOrderByCreatedTimeAsc(conversationId);

            // 标记为已读
            messageRepository.markAsRead(conversationId, currentUserId);

            // 更新会话未读数
            if (currentUserId.equals(conversation.getUserId())) {
                conversation.setUnreadCountUser(0);
            } else {
                conversation.setUnreadCountHost(0);
            }
            conversationRepository.save(conversation);

            // 获取对方信息
            Long otherId = currentUserId.equals(conversation.getUserId()) ? conversation.getHostId() : conversation.getUserId();

            result.put("success", true);
            result.put("messages", messages);
            result.put("conversation", conversation);

            Map<String, Object> otherInfo = new HashMap<>();
            if (otherId != null && otherId == 0L) {
                otherInfo.put("id", 0L);
                otherInfo.put("name", "系统通知");
                otherInfo.put("avatar", null);
            } else if (otherId != null) {
                User other = userRepository.findById(otherId).orElse(null);
                if (other != null) {
                    otherInfo.put("id", other.getId());
                    otherInfo.put("name", other.getNickname() != null ? other.getNickname() : other.getUsername());
                    otherInfo.put("avatar", other.getAvatar());
                }
            }
            result.put("otherUser", otherInfo);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取消息失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取未读消息总数
     */
    public Long getUnreadCount(Long userId, String role) {
        Long count;
        if ("ROLE_USER".equals(role)) {
            count = conversationRepository.countUnreadByUserId(userId);
        } else {
            count = conversationRepository.countUnreadByHostId(userId);
        }
        return count != null ? count : 0L;
    }
    
    /**
     * 发送系统消息给房东
     * @param hostId 房东ID
     * @param content 消息内容
     * @param houseId 相关房源ID（可选）
     */
    @Transactional
    public void sendSystemMessage(Long hostId, String content, Long houseId) {
        try {
            // 系统用户ID设为0
            Long systemUserId = 0L;
            
            // 查找或创建系统会话（系统消息会话的userId也使用hostId，表示这是房东的系统消息）
            Conversation conversation = null;
            List<Conversation> conversations = conversationRepository.findByHostIdOrderByLastMessageTimeDesc(hostId);
            
            // 查找是否存在系统会话（userId为0表示系统会话）
            for (Conversation conv : conversations) {
                if (conv.getUserId() != null && conv.getUserId().equals(0L) && 
                    (houseId == null || (conv.getHouseId() != null && conv.getHouseId().equals(houseId)))) {
                    conversation = conv;
                    break;
                }
            }
            
            // 如果没有找到系统会话，创建新的
            if (conversation == null) {
                conversation = new Conversation();
                conversation.setUserId(0L); // 0表示系统
                conversation.setHostId(hostId);
                conversation.setHouseId(houseId);
                conversation.setCreatedTime(LocalDateTime.now());
                conversation.setUpdatedTime(LocalDateTime.now());
                conversation.setUnreadCountUser(0);
                conversation.setUnreadCountHost(0);
            }
            
            // 更新会话信息
            conversation.setLastMessage(content);
            conversation.setLastMessageTime(LocalDateTime.now());
            conversation.setUpdatedTime(LocalDateTime.now());
            conversation.setUnreadCountHost(conversation.getUnreadCountHost() + 1); // 增加房东未读数
            
            conversationRepository.save(conversation);
            
            // 创建消息
            Message message = new Message();
            message.setConversationId(conversation.getId());
            message.setSenderId(0L); // 系统发送者ID为0
            message.setReceiverId(hostId);
            message.setContent(content);
            message.setIsRead(false);
            message.setCreatedTime(LocalDateTime.now());
            
            messageRepository.save(message);
            
            System.out.println("系统消息发送成功: hostId=" + hostId + ", content=" + content);
            
        } catch (Exception e) {
            System.err.println("发送系统消息失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("发送系统消息失败", e);
        }
    }
}
