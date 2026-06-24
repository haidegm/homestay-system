package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.House;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseRepository extends JpaRepository<House, Long> {

    List<House> findByHostId(Long hostId);
    
    long countByHostId(Long hostId);
    
    // 管理员功能：按状态查询
    Page<House> findByStatus(String status, Pageable pageable);
    
    // 管理员功能：按关键词查询
    Page<House> findByTitleContainingOrAddressContaining(
        String title, String address, Pageable pageable);
    
    // 统计功能：按状态统计数量
    long countByStatus(String status);

    // AI客服：按城市和状态查询房源
    List<House> findByCityAndStatus(String city, String status);

    // AI客服：按状态查询最新房源（限制条数）
    List<House> findTop8ByStatusOrderByCreatedTimeDesc(String status);
}
