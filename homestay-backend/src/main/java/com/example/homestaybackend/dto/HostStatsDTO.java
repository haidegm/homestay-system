package com.example.homestaybackend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class HostStatsDTO {
    private Long totalHouses;      // 房源总数
    private Long totalOrders;      // 订单总数
    private BigDecimal totalIncome; // 总收入
    private Double avgRating;      // 平均评分
}
