package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // 根据订单ID查询评论
    Optional<Review> findByOrderId(Long orderId);
    
    // 根据房源ID查询所有评论
    List<Review> findByHouseIdOrderByCreatedTimeDesc(Long houseId);
    
    // 根据用户ID查询所有评论
    List<Review> findByUserIdOrderByCreatedTimeDesc(Long userId);
    
    // 根据多个房源ID查询所有评论（用于房东查看所有房源的评论）
    List<Review> findByHouseIdInOrderByCreatedTimeDesc(List<Long> houseIds);
    
    // 检查订单是否已评论
    boolean existsByOrderId(Long orderId);
    
    // 统计房源的评论数量
    long countByHouseId(Long houseId);
    
    // 计算房源的平均评分
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.houseId = ?1")
    Double getAverageRatingByHouseId(Long houseId);
}
