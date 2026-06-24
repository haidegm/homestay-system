package com.example.homestaybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WishlistDTO {
    private Long id;
    private Long houseId;
    private String houseTitle;
    private String houseCoverImage;
    private BigDecimal price;
    private String city;
    private String province;
    private Double rating;
    private Integer reviewCount;
    private LocalDateTime createdTime;
}
