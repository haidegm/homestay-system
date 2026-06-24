package com.example.homestaybackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminUserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Long orderCount;
    private Long wishlistCount;
    private String status;
    private LocalDateTime createdAt;
}
