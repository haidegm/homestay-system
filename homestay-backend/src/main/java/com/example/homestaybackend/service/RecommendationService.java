package com.example.homestaybackend.service;

import com.example.homestaybackend.dto.RecommendationDTO;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.entity.HouseImage;
import com.example.homestaybackend.repository.HouseImageRepository;
import com.example.homestaybackend.repository.HouseRepository;
import com.example.homestaybackend.repository.ReviewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    @Value("${recommendation.service.url:http://localhost:5000}")
    private String recommendationServiceUrl;

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseImageRepository houseImageRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 为用户推荐房源
     */
    public List<RecommendationDTO> recommendForUser(Long userId, Integer limit, String method) {
        try {
            String url = String.format("%s/api/recommend/user/%d?limit=%d&method=%s",
                    recommendationServiceUrl, userId, limit, method);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.get("success").asBoolean()) {
                JsonNode recommendations = root.get("recommendations");
                return buildRecommendationDTOs(recommendations);
            }
        } catch (Exception e) {
            System.err.println("调用推荐服务失败: " + e.getMessage());
            // 如果推荐服务失败，返回热门房源作为降级方案
            return getPopularHouses(limit);
        }

        return new ArrayList<>();
    }

    /**
     * 推荐相似房源
     */
    public List<RecommendationDTO> recommendSimilarHouses(Long houseId, Integer limit) {
        try {
            String url = String.format("%s/api/recommend/similar/%d?limit=%d",
                    recommendationServiceUrl, houseId, limit);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.get("success").asBoolean()) {
                JsonNode similarHouses = root.get("similarHouses");
                return buildRecommendationDTOs(similarHouses);
            }
        } catch (Exception e) {
            System.err.println("调用推荐服务失败: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * 获取热门房源
     */
    public List<RecommendationDTO> getPopularHouses(Integer limit) {
        try {
            String url = String.format("%s/api/recommend/popular?limit=%d",
                    recommendationServiceUrl, limit);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.get("success").asBoolean()) {
                JsonNode popularHouses = root.get("popularHouses");
                return buildRecommendationDTOs(popularHouses);
            }
        } catch (Exception e) {
            System.err.println("调用推荐服务失败: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * 构建推荐DTO列表
     */
    private List<RecommendationDTO> buildRecommendationDTOs(JsonNode recommendations) {
        List<RecommendationDTO> result = new ArrayList<>();

        for (JsonNode rec : recommendations) {
            Long houseId = rec.get("houseId").asLong();
            Double score = rec.has("score") ? rec.get("score").asDouble() : 
                          rec.has("similarity") ? rec.get("similarity").asDouble() :
                          rec.has("popularityScore") ? rec.get("popularityScore").asDouble() : 0.0;

            House house = houseRepository.findById(houseId).orElse(null);
            if (house != null && "ACTIVE".equals(house.getStatus())) {
                RecommendationDTO dto = new RecommendationDTO();
                dto.setHouseId(houseId);
                dto.setScore(score);
                dto.setTitle(house.getTitle());
                dto.setPrice(house.getPrice().doubleValue());
                dto.setCity(house.getCity());

                // 获取封面图片
                List<HouseImage> images = houseImageRepository.findByHouseIdOrderBySortOrderAsc(houseId);
                if (!images.isEmpty()) {
                    dto.setCoverImage(images.get(0).getImageUrl());
                }

                // 获取平均评分
                Double avgRating = reviewRepository.getAverageRatingByHouseId(houseId);
                dto.setRating(avgRating != null ? avgRating : 0.0);

                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 手动触发模型训练
     */
    public boolean trainModel() {
        try {
            String url = String.format("%s/api/recommend/train", recommendationServiceUrl);
            restTemplate.postForObject(url, null, String.class);
            return true;
        } catch (Exception e) {
            System.err.println("触发模型训练失败: " + e.getMessage());
            return false;
        }
    }
}
