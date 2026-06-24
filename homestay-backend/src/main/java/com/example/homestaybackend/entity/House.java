package com.example.homestaybackend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "house")
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_id")
    private Long hostId;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String province;
    private String city;
    private String address;

    @Column(name = "max_guests")
    private Integer maxGuests;

    private Integer area;

    @Column(name = "bed_count")
    private Integer bedCount;

    private BigDecimal price;

    @Column(name = "bathroom_desc")
    private String bathroomDesc;

    @Column(name = "supply_desc")
    private String supplyDesc;

    private String status;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    private String parking;

//    private BigDecimal latitude;
//
//    private BigDecimal longitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

}
