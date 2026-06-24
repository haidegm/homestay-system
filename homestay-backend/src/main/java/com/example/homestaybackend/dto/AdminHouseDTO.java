package com.example.homestaybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminHouseDTO {
    private Long id;
    private String title;
    private String address;
    private String type;
    private String coverImage;
    private String hostName;
    private String hostAvatar;
    private BigDecimal price;
    private Integer guests;
    private Integer bedrooms;
    private Integer beds;
    private Integer bathrooms;
    private BigDecimal rating;
    private Long orderCount;
    private String status;
    private String rejectReason;
    private String description;
    private LocalDateTime createdAt;
}
