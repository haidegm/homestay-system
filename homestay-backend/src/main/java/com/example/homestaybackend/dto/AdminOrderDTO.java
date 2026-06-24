package com.example.homestaybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminOrderDTO {
    private Long id;
    private String orderNo;
    private String status;
    
    // 用户信息
    private Long userId;
    private String userName;
    private String userAvatar;
    private String userPhone;
    
    // 房源信息
    private Long houseId;
    private String houseTitle;
    private String houseCoverImage;
    private String houseAddress;
    
    // 房东信息
    private Long hostId;
    private String hostName;
    private String hostPhone;
    
    // 订单详情
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private Integer nights;
    private BigDecimal totalPrice;
    private LocalDateTime createdTime;
}
