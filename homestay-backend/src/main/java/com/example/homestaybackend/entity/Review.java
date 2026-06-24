package com.example.homestaybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "house_id")
    private Long houseId;

    @Column(name = "user_id")
    private Long userId;

    private Double rating; // 评分 1-5，支持0.5分

    @Column(columnDefinition = "TEXT")
    private String comment; // 评论内容

    @Column(columnDefinition = "TEXT")
    private String reply; // 房东回复

    @Column(name = "is_anonymous")
    private Boolean isAnonymous; // 是否匿名

    @Column(columnDefinition = "TEXT")
    private String images; // 评价图片，多张用逗号分隔

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
