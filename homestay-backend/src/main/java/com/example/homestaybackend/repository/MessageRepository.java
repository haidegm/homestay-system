package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // 查找会话的所有消息，按时间升序
    List<Message> findByConversationIdOrderByCreatedTimeAsc(Long conversationId);
    
    // 标记消息为已读
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.receiverId = :receiverId AND m.isRead = false")
    void markAsRead(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId);
}
