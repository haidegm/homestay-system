package com.example.homestaybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.homestaybackend.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询指定日期范围内的订单数量（用于检查房源可用性）
     */
    @Select("SELECT COUNT(*) FROM `order` " +
            "WHERE house_id = #{houseId} " +
            "AND status != 'CANCELLED' " +
            "AND NOT (check_out_date <= #{checkIn} OR check_in_date >= #{checkOut})")
    int countConflictingOrders(@Param("houseId") Long houseId,
                                @Param("checkIn") LocalDate checkIn,
                                @Param("checkOut") LocalDate checkOut);

    /**
     * 查询房东的所有订单
     */
    @Select("SELECT o.* FROM `order` o " +
            "INNER JOIN house h ON o.house_id = h.id " +
            "WHERE h.host_id = #{hostId} " +
            "ORDER BY o.created_time DESC")
    List<Order> findByHostId(@Param("hostId") Long hostId);

    /**
     * 查询房东的最近订单
     */
    @Select("SELECT o.* FROM `order` o " +
            "INNER JOIN house h ON o.house_id = h.id " +
            "WHERE h.host_id = #{hostId} " +
            "ORDER BY o.created_time DESC " +
            "LIMIT #{limit}")
    List<Order> findRecentByHostId(@Param("hostId") Long hostId, @Param("limit") int limit);

    /**
     * 统计房东的订单总数
     */
    @Select("SELECT COUNT(*) FROM `order` o " +
            "INNER JOIN house h ON o.house_id = h.id " +
            "WHERE h.host_id = #{hostId}")
    int countByHostId(@Param("hostId") Long hostId);

    /**
     * 统计房东的总收入
     */
    @Select("SELECT COALESCE(SUM(o.total_price), 0) FROM `order` o " +
            "INNER JOIN house h ON o.house_id = h.id " +
            "WHERE h.host_id = #{hostId} " +
            "AND o.status IN ('CONFIRMED', 'COMPLETED')")
    Double sumIncomeByHostId(@Param("hostId") Long hostId);
}
