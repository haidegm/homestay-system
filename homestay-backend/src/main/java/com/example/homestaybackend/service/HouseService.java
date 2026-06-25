package com.example.homestaybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.homestaybackend.entity.House;
import com.example.homestaybackend.repository.HouseAmenityRepository;
import com.example.homestaybackend.repository.HouseImageRepository;
import com.example.homestaybackend.repository.HouseRepository;
import com.example.homestaybackend.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
public class HouseService {

    private final HouseRepository houseRepository;
    private final HouseImageRepository houseImageRepository;
    private final HouseAmenityRepository houseAmenityRepository;
    
    @Autowired
    private RedisUtil redisUtil;
    
    // Redis缓存key前缀
    private static final String HOUSE_CACHE_PREFIX = "house:";
    private static final String HOT_HOUSES_KEY = "hot:houses";
    private static final int CACHE_EXPIRE_TIME = 3600; // 1小时

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
    @CacheEvict(value = "house", key = "#houseId")
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
            
            // 6. 清除Redis缓存
            String cacheKey = HOUSE_CACHE_PREFIX + houseId;
            redisUtil.del(cacheKey);
            // 清除热门房源缓存
            redisUtil.del(HOT_HOUSES_KEY);
            
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
     * 获取房源详情（带Redis缓存）
     */
    @Cacheable(value = "house", key = "#houseId", unless = "#result == null")
    public House getHouseById(Long houseId) {
        System.out.println("从数据库查询房源: " + houseId);
        
        // 先尝试从Redis获取
        String cacheKey = HOUSE_CACHE_PREFIX + houseId;
        Object cachedHouse = redisUtil.get(cacheKey);
        
        if (cachedHouse != null) {
            System.out.println("从Redis缓存获取房源: " + houseId);
            return (House) cachedHouse;
        }
        
        // 从数据库查询
        House house = houseRepository.findById(houseId).orElse(null);
        
        // 存入Redis缓存
        if (house != null) {
            redisUtil.set(cacheKey, house, CACHE_EXPIRE_TIME);
            System.out.println("房源已缓存到Redis: " + houseId);
            
            // 增加浏览计数
            String viewCountKey = "house:view:" + houseId;
            redisUtil.incr(viewCountKey, 1);
        }
        
        return house;
    }
    
    /**
     * 获取房源浏览次数
     */
    public Long getHouseViewCount(Long houseId) {
        String viewCountKey = "house:view:" + houseId;
        Object count = redisUtil.get(viewCountKey);
        return count != null ? Long.parseLong(count.toString()) : 0L;
    }
    
    /**
     * 获取热门房源（基于浏览次数）
     */
    @SuppressWarnings("unchecked")
    public List<House> getHotHouses(int limit) {
        // 先尝试从Redis获取
        Object cachedHouses = redisUtil.get(HOT_HOUSES_KEY);
        if (cachedHouses != null) {
            System.out.println("从Redis缓存获取热门房源");
            return (List<House>) cachedHouses;
        }
        
        // 从数据库查询（这里简化处理，实际可以根据浏览次数排序）
        List<House> houses = houseRepository.findAll();
        if (houses.size() > limit) {
            houses = houses.subList(0, limit);
        }
        
        // 存入Redis缓存
        redisUtil.set(HOT_HOUSES_KEY, houses, CACHE_EXPIRE_TIME);
        System.out.println("热门房源已缓存到Redis");
        
        return houses;
    }

    /**
     * 验证房源所有权
     */
    public boolean verifyOwnership(Long houseId, Long hostId) {
        House house = houseRepository.findById(houseId).orElse(null);
        return house != null && house.getHostId().equals(hostId);
    }
}
