package com.example.homestaybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.repository.HouseAmenityRepository;
import com.example.homestaybackend.repository.HouseImageRepository;
import com.example.homestaybackend.repository.HouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
public class HouseService {

    private final HouseRepository houseRepository;
    private final HouseImageRepository houseImageRepository;
    private final HouseAmenityRepository houseAmenityRepository;

    public HouseService(HouseRepository houseRepository,
                       HouseImageRepository houseImageRepository,
                       HouseAmenityRepository houseAmenityRepository) {
        this.houseRepository = houseRepository;
        this.houseImageRepository = houseImageRepository;
        this.houseAmenityRepository = houseAmenityRepository;
    }

    /**
     * 删除房源（级联删除相关数据）
     */
    @Transactional
    public boolean deleteHouse(Long houseId, Long hostId) {
        try {
            System.out.println("开始删除房源，ID: " + houseId + ", 房东ID: " + hostId);
            
            // 1. 验证房源是否存在
            House house = houseRepository.findById(houseId)
                    .orElseThrow(() -> new RuntimeException("房源不存在"));
            
            System.out.println("找到房源: " + house.getTitle());
            
            // 2. 验证权限
            if (!house.getHostId().equals(hostId)) {
                throw new RuntimeException("无权删除此房源");
            }
            
            // 3. 删除房源图片记录
            System.out.println("删除房源图片记录...");
            houseImageRepository.deleteByHouseId(houseId);
            
            // 4. 删除房源设施关联记录
            System.out.println("删除房源设施记录...");
            houseAmenityRepository.deleteByHouseId(houseId);
            
            // 5. 删除房源本身
            System.out.println("删除房源记录...");
            houseRepository.deleteById(houseId);
            
            System.out.println("房源删除成功");
            return true;
            
        } catch (Exception e) {
            System.err.println("删除房源失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("删除房源失败：" + e.getMessage());
        }
    }

    /**
     * 获取房东的所有房源
     */
    public List<House> getHostHouses(Long hostId) {
        return houseRepository.findByHostId(hostId);
    }

    /**
     * 获取房源详情
     */
    public House getHouseById(Long houseId) {
        return houseRepository.findById(houseId).orElse(null);
    }

    /**
     * 验证房源所有权
     */
    public boolean verifyOwnership(Long houseId, Long hostId) {
        House house = houseRepository.findById(houseId).orElse(null);
        return house != null && house.getHostId().equals(hostId);
    }
}
