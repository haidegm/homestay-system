package com.example.homestaybackend.dto;

import lombok.Data;

@Data
public class AdminStatsDTO {
    private Long totalHosts;
    private Long totalHouses;
    private Long totalUsers;
    private Long totalOrders;
    private Double totalRevenue;
}
