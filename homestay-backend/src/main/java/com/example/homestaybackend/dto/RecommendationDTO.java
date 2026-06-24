package com.example.homestaybackend.dto;

import lombok.Data;

@Data
public class RecommendationDTO {
    private Long houseId;
    private Double score;
    private String title;
    private String coverImage;
    private Double price;
    private String city;
    private Double rating;
}
