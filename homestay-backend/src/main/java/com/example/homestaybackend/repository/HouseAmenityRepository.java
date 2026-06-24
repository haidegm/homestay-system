package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.HouseAmenity;
import com.example.homestaybackend.entity.HouseAmenityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HouseAmenityRepository
        extends JpaRepository<HouseAmenity, HouseAmenityId> {

    /**
     * 根据房源 ID 删除所有关联设施
     */
    @Transactional
    @Modifying
    void deleteByHouseId(Long houseId);

    /**
     * 根据房源 ID 查询所有设施关联
     */
    List<HouseAmenity> findByHouseId(Long houseId);
}
