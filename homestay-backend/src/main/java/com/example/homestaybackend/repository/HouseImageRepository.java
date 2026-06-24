package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.HouseImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseImageRepository extends JpaRepository<HouseImage, Long> {

    List<HouseImage> findByHouseIdOrderBySortOrderAsc(Long houseId);
    void deleteByHouseId(Long houseId);
}
