package com.example.homestaybackend.controller;

import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.entity.HouseAmenity;
import com.example.homestaybackend.entity.HouseImage;
import com.example.homestaybackend.entity.User;
import com.example.homestaybackend.repository.*;
import com.example.homestaybackend.dto.HouseListDTO;
import com.example.homestaybackend.dto.HouseDetailDTO;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/host/house")
public class HouseController {

    private final HouseRepository houseRepository;
    private final HouseImageRepository houseImageRepository;
    private final HouseAmenityRepository houseAmenityRepository;
    private final AmenityRepository amenityRepository;
    private final UserRepository userRepository;
    private final com.example.homestaybackend.service.HouseService houseService;

    public HouseController(
            HouseRepository houseRepository,
            HouseImageRepository houseImageRepository,
            HouseAmenityRepository houseAmenityRepository,
            AmenityRepository amenityRepository,
            UserRepository userRepository,
            com.example.homestaybackend.service.HouseService houseService
    ) {
        this.houseRepository = houseRepository;
        this.houseImageRepository = houseImageRepository;
        this.houseAmenityRepository = houseAmenityRepository;
        this.amenityRepository = amenityRepository;
        this.userRepository = userRepository;
        this.houseService = houseService;
    }

    // 获取当前登录房东ID
    private Long getCurrentHostId(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("用户不存在");
        return user.getId();
    }

    /**
     * 1 新增房源
     */
    @PostMapping
    public House addHouse(@RequestBody House house, Principal principal) {

        house.setHostId(getCurrentHostId(principal));
        house.setStatus("PENDING"); // 新增房源默认为待审核状态

        return houseRepository.save(house);
    }

    /**
     * 2 我的房源列表
     */
    @GetMapping("/list")
    public List<House> list(Principal principal) {

        return houseRepository.findByHostId(getCurrentHostId(principal));
    }

    /**
     * 3 上传图片
     */
    @PostMapping("/image")
    public String uploadHouseImages(
            @RequestParam Long houseId,
            @RequestParam("files") MultipartFile[] files
    ) throws Exception {

        if (files == null || files.length == 0) {
            throw new RuntimeException("请上传图片");
        }

        String uploadDir = "D:/wlh/homestay_upload/house/" + houseId + "/";

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        List<HouseImage> images = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {

            MultipartFile file = files[i];
            if (file.isEmpty()) continue;

            String fileName = UUID.randomUUID() + ".jpg";

            file.transferTo(new File(uploadDir + fileName));

            HouseImage img = new HouseImage();
            img.setHouseId(houseId);
            img.setImageUrl("/house/" + houseId + "/" + fileName);
            img.setSortOrder(i + 1);

            images.add(img);
        }

        houseImageRepository.saveAll(images);

        return "ok";
    }

    /**
     * 4 保存设施
     */
    @PostMapping("/amenity")
    public String saveAmenities(
            @RequestParam Long houseId,
            @RequestBody List<Long> amenityIds
    ) {

        houseAmenityRepository.deleteByHouseId(houseId);

        List<HouseAmenity> list = new ArrayList<>();

        for (Long amenityId : amenityIds) {

            HouseAmenity ha = new HouseAmenity();
            ha.setHouseId(houseId);
            ha.setAmenityId(amenityId);

            list.add(ha);
        }

        houseAmenityRepository.saveAll(list);

        return "ok";
    }

    /**
     * 5 房源列表视图（修复：加入经纬度）
     */
    @GetMapping("/list/view")
    public List<HouseListDTO> listForView(Principal principal) {

        Long hostId = getCurrentHostId(principal);

        List<House> houses = houseRepository.findByHostId(hostId);

        List<HouseListDTO> result = new ArrayList<>();

        for (House house : houses) {

            HouseListDTO dto = new HouseListDTO();

            dto.setId(house.getId());
            dto.setTitle(house.getTitle());
            dto.setCity(house.getCity());
            dto.setPrice(house.getPrice());
            dto.setParking(house.getParking());
            dto.setStatus(house.getStatus()); // 设置状态

            // ⭐ 新增：返回经纬度
            dto.setLatitude(house.getLatitude());
            dto.setLongitude(house.getLongitude());

            // 封面图
            List<HouseImage> images =
                    houseImageRepository.findByHouseIdOrderBySortOrderAsc(house.getId());

            if (!images.isEmpty()) {
                dto.setCoverImage(images.get(0).getImageUrl());
            }

            // 设施
            List<String> amenityNames =
                    amenityRepository.findAmenityNamesByHouseId(house.getId());

            dto.setAmenities(amenityNames);

            result.add(dto);
        }

        return result;
    }

    /**
     * 6 房源详情（修复：加入经纬度和房东信息）
     */
    @GetMapping("/detail/{id}")
    public HouseDetailDTO detail(@PathVariable Long id) {

        House house =
                houseRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("房源不存在"));

        HouseDetailDTO dto = new HouseDetailDTO();

        dto.setId(house.getId());
        dto.setTitle(house.getTitle());
        dto.setDescription(house.getDescription());
        dto.setProvince(house.getProvince());
        dto.setCity(house.getCity());
        dto.setAddress(house.getAddress());
        dto.setMaxGuests(house.getMaxGuests());
        dto.setArea(house.getArea());
        dto.setBedCount(house.getBedCount());
        dto.setPrice(house.getPrice());
        dto.setBathroomDesc(house.getBathroomDesc());
        dto.setSupplyDesc(house.getSupplyDesc());
        dto.setParking(house.getParking());
        dto.setLatitude(house.getLatitude());
        dto.setLongitude(house.getLongitude());

        // ⭐ 新增：房东信息
        User host = userRepository.findById(house.getHostId()).orElse(null);
        if (host != null) {
            dto.setHostId(host.getId());
            dto.setHostName(host.getNickname() != null ? host.getNickname() : host.getUsername());
            dto.setHostAvatar(host.getAvatar());
            dto.setHostPhone(host.getPhone());
        }

        // 图片
        List<HouseImage> images =
                houseImageRepository.findByHouseIdOrderBySortOrderAsc(id);

        dto.setImages(
                images.stream()
                        .map(HouseImage::getImageUrl)
                        .collect(Collectors.toList())
        );

        // 设施
        List<HouseAmenity> has = houseAmenityRepository.findByHouseId(id);

        dto.setAmenities(
                has.stream()
                        .map(HouseAmenity::getAmenityId)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    /**
     * 7 更新房源（修复：更新经纬度）
     */
    @PutMapping("/{id}")
    public House updateHouse(
            @PathVariable Long id,
            @RequestBody House house
    ) {
        System.out.println("=== 更新房源 ID: " + id + " ===");
        System.out.println("接收到的 description: " + house.getDescription());

        House dbHouse =
                houseRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("房源不存在"));

        System.out.println("数据库中原有的 description: " + dbHouse.getDescription());

        dbHouse.setTitle(house.getTitle());
        dbHouse.setDescription(house.getDescription());
        dbHouse.setProvince(house.getProvince());
        dbHouse.setCity(house.getCity());
        dbHouse.setAddress(house.getAddress());
        dbHouse.setMaxGuests(house.getMaxGuests());
        dbHouse.setArea(house.getArea());
        dbHouse.setBedCount(house.getBedCount());
        dbHouse.setPrice(house.getPrice());
        dbHouse.setBathroomDesc(house.getBathroomDesc());
        dbHouse.setSupplyDesc(house.getSupplyDesc());
        dbHouse.setParking(house.getParking());
        dbHouse.setLatitude(house.getLatitude());
        dbHouse.setLongitude(house.getLongitude());

        House saved = houseRepository.save(dbHouse);
        System.out.println("保存后的 description: " + saved.getDescription());
        
        return saved;
    }

    /**
     * 8 删除房源
     */
    @DeleteMapping("/{id}")
    public java.util.Map<String, Object> deleteHouse(@PathVariable Long id, Principal principal) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            Long currentHostId = getCurrentHostId(principal);
            System.out.println("收到删除房源请求，房源ID: " + id + ", 房东ID: " + currentHostId);
            
            // 使用 HouseService 删除房源
            boolean deleted = houseService.deleteHouse(id, currentHostId);
            
            result.put("success", deleted);
            result.put("message", deleted ? "删除成功" : "删除失败");
            
        } catch (Exception e) {
            System.err.println("删除房源失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        
        return result;
    }
}