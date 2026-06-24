package com.example.homestaybackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminHostDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Long houseCount;
    private Long orderCount;
    private BigDecimal totalIncome;
    private String status;
    private LocalDateTime createdAt;
}
