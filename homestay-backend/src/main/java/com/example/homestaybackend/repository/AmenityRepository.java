package com.example.homestaybackend.repository;

import com.example.homestaybackend.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    @Query("""
        select a.name
        from Amenity a
        join HouseAmenity ha on a.id = ha.amenityId
        where ha.houseId = :houseId
    """)
    List<String> findAmenityNamesByHouseId(@Param("houseId") Long houseId);
}
