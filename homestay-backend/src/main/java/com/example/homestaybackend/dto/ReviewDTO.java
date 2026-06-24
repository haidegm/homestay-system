package com.example.homestaybackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long orderId;
    private Long houseId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Double rating;
    private String comment;
    private String reply;
    private Boolean isAnonymous; // 是否匿名
    private String images; // 评价图片，多张用逗号分隔
    private LocalDateTime createdTime;
    private String houseTitle; // 房源标题
    private String houseCoverImage; // 房源封面
}
