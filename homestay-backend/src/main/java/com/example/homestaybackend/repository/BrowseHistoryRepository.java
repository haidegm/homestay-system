package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.BrowseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {

    /**
     * 查询用户的浏览记录
     */
    List<BrowseHistory> findByUserIdOrderByBrowseTimeDesc(Long userId);

    /**
     * 查询用户最近的浏览记录
     */
    @Query("SELECT bh FROM BrowseHistory bh WHERE bh.userId = :userId ORDER BY bh.browseTime DESC")
    List<BrowseHistory> findRecentByUserId(@Param("userId") Long userId);

    /**
     * 查询用户在指定时间后的浏览记录
     */
    List<BrowseHistory> findByUserIdAndBrowseTimeAfter(Long userId, LocalDateTime afterTime);

    /**
     * 检查用户是否浏览过某个房源
     */
    boolean existsByUserIdAndHouseId(Long userId, Long houseId);

    /**
     * 删除用户的浏览记录
     */
    void deleteByUserId(Long userId);
}
