package com.example.homestaybackend.entity;

import java.io.Serializable;
import java.util.Objects;

public class HouseAmenityId implements Serializable {

    private Long houseId;
    private Long amenityId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HouseAmenityId)) return false;
        HouseAmenityId that = (HouseAmenityId) o;
        return Objects.equals(houseId, that.houseId)
                && Objects.equals(amenityId, that.amenityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseId, amenityId);
    }
}
