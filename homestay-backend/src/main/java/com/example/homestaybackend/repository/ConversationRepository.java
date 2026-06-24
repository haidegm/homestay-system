package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    // 查找用户和房东之间的会话
    Optional<Conversation> findByUserIdAndHostId(Long userId, Long hostId);
    
    // 查找用户的所有会话
    List<Conversation> findByUserIdOrderByLastMessageTimeDesc(Long userId);
    
    // 查找房东的所有会话
    List<Conversation> findByHostIdOrderByLastMessageTimeDesc(Long hostId);
    
    // 统计用户未读消息总数
    @Query("SELECT SUM(c.unreadCountUser) FROM Conversation c WHERE c.userId = :userId")
    Long countUnreadByUserId(@Param("userId") Long userId);
    
    // 统计房东未读消息总数
    @Query("SELECT SUM(c.unreadCountHost) FROM Conversation c WHERE c.hostId = :hostId")
    Long countUnreadByHostId(@Param("hostId") Long hostId);
}
