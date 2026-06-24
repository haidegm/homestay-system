package com.example.homestaybackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "house_amenity")
@IdClass(HouseAmenityId.class)
public class HouseAmenity {

    @Id
    @Column(name = "house_id")
    private Long houseId;

    @Id
    @Column(name = "amenity_id")
    private Long amenityId;
}
