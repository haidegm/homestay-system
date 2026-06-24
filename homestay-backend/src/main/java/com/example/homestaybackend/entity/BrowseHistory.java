package com.example.homestaybackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "browse_history")
public class BrowseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Column(name = "browse_time", nullable = false)
    private LocalDateTime browseTime;

    @Column(name = "browse_duration")
    private Integer browseDuration;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
        if (browseTime == null) {
            browseTime = LocalDateTime.now();
        }
        if (browseDuration == null) {
            browseDuration = 0;
        }
    }
}
