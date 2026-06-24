package com.example.homestaybackend.dto;

import lombok.Data;

@Data
public class CreateReviewDTO {
    private Long orderId;
    private Double rating; // 1-5星，支持0.5分
    private String comment; // 评论内容
    private Boolean isAnonymous; // 是否匿名，默认false
    private String images; // 评价图片URL，多张用逗号分隔
}
