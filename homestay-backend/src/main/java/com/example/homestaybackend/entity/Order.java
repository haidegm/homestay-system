package com.example.homestaybackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "`order`")
@TableName("`order`")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false)
    private String orderNo;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "house_id")
    private Long houseId;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        if (orderNo == null) {
            // 生成订单号：HS + 时间戳 + 随机数
            orderNo = "HS" + System.currentTimeMillis() + (int)(Math.random() * 1000);
        }
    }
}