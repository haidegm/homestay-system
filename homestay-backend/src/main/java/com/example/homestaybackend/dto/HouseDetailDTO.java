package com.example.homestaybackend.dto;

import java.math.BigDecimal;
import java.util.List;

public class HouseDetailDTO {

    private Long id;
    private String title;
    private String description;
    private String province;
    private String city;
    private String address;

    private Integer maxGuests;
    private Integer area;
    private Integer bedCount;
    private BigDecimal price;

    private String parking;
    private String bathroomDesc;
    private String supplyDesc;

    // ⭐ 新增：经纬度
    private BigDecimal latitude;
    private BigDecimal longitude;

    // ⭐ 新增：房东信息
    private Long hostId;
    private String hostName;
    private String hostAvatar;
    private String hostPhone;

    private List<String> images;      // 图片路径
    private List<Long> amenities;     // 设施ID


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public Integer getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(Integer maxGuests) {
        this.maxGuests = maxGuests;
    }


    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }


    public Integer getBedCount() {
        return bedCount;
    }

    public void setBedCount(Integer bedCount) {
        this.bedCount = bedCount;
    }


    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public String getParking() {
        return parking;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }


    public String getBathroomDesc() {
        return bathroomDesc;
    }

    public void setBathroomDesc(String bathroomDesc) {
        this.bathroomDesc = bathroomDesc;
    }


    public String getSupplyDesc() {
        return supplyDesc;
    }

    public void setSupplyDesc(String supplyDesc) {
        this.supplyDesc = supplyDesc;
    }


    // ===== 新增：latitude =====
    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }


    // ===== 新增：longitude =====
    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }


    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }


    public List<Long> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<Long> amenities) {
        this.amenities = amenities;
    }

    // ===== 新增：房东信息 getter/setter =====
    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostAvatar() {
        return hostAvatar;
    }

    public void setHostAvatar(String hostAvatar) {
        this.hostAvatar = hostAvatar;
    }

    public String getHostPhone() {
        return hostPhone;
    }

    public void setHostPhone(String hostPhone) {
        this.hostPhone = hostPhone;
    }
}