package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // 根据用户ID查询订单
    List<Order> findByUserIdOrderByCreatedTimeDesc(Long userId);
    
    // 根据房源ID查询订单
    List<Order> findByHouseIdOrderByCreatedTimeDesc(Long houseId);
    
    // 根据订单号查询
    Order findByOrderNo(String orderNo);
    
    // 根据状态查询订单（分页）
    Page<Order> findByStatus(String status, Pageable pageable);
    
    // 查询需要自动完成的订单（退房日期+1天中午12点之前的已确认订单）
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' AND o.checkOutDate <= :date")
    List<Order> findOrdersToComplete(@Param("date") LocalDate date);
    
    // 批量更新订单状态为已完成
    @Modifying
    @Query("UPDATE Order o SET o.status = 'COMPLETED' WHERE o.id IN :ids")
    void batchUpdateStatusToCompleted(@Param("ids") List<Long> ids);
}
