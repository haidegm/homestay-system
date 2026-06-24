package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    
    // 查询用户的所有心愿单
    List<Wishlist> findByUserIdOrderByCreatedTimeDesc(Long userId);
    
    // 查询用户是否收藏了某个房源
    Optional<Wishlist> findByUserIdAndHouseId(Long userId, Long houseId);
    
    // 检查是否已收藏
    boolean existsByUserIdAndHouseId(Long userId, Long houseId);
    
    // 删除收藏
    void deleteByUserIdAndHouseId(Long userId, Long houseId);
}
