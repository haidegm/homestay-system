<template>
  <div class="region-section" v-if="selectedCategory">
    <div class="region-container">
      <div class="region-grid">
        <div 
          class="region-card" 
          v-for="(region, index) in regionData[selectedCategory]" 
          :key="index"
          @click="handleRegionClick(region)"
        >
          <div class="region-image-wrapper">
            <img :src="region.image" :alt="region.name" class="region-image" />
          </div>
          <div class="region-info">
            <h4 class="region-name">{{ region.name }}</h4>
            <p class="region-distance">{{ region.distance }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  selectedCategory: String
})

const regionData = {
  '热门': [
    { name: '北京', distance: '距离您 120 公里', image: 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=400&h=300&fit=crop' },
    { name: '上海', distance: '距离您 280 公里', image: 'https://images.unsplash.com/photo-1545893835-abaa50cbe628?w=400&h=300&fit=crop' },
    { name: '杭州', distance: '距离您 180 公里', image: 'https://images.unsplash.com/photo-1559564484-e48bf5f6c69b?w=400&h=300&fit=crop' },
    { name: '成都', distance: '距离您 1800 公里', image: 'https://images.unsplash.com/photo-1590859808308-3d2d9c515b1a?w=400&h=300&fit=crop' },
    { name: '西安', distance: '距离您 1100 公里', image: 'https://images.unsplash.com/photo-1547981609-4b6bfe67ca0b?w=400&h=300&fit=crop' },
    { name: '厦门', distance: '距离您 1500 公里', image: 'https://images.unsplash.com/photo-1583952812866-8d5e9c3e9f9d?w=400&h=300&fit=crop' }
  ],
  '艺术与文化': [
    { name: '北京故宫', distance: '历史文化遗产', image: 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=400&h=300&fit=crop' },
    { name: '西安古城', distance: '千年古都', image: 'https://images.unsplash.com/photo-1547981609-4b6bfe67ca0b?w=400&h=300&fit=crop' },
    { name: '苏州园林', distance: '江南水乡', image: 'https://images.unsplash.com/photo-1559564484-e48bf5f6c69b?w=400&h=300&fit=crop' },
    { name: '丽江古城', distance: '纳西文化', image: 'https://images.unsplash.com/photo-1590859808308-3d2d9c515b1a?w=400&h=300&fit=crop' },
    { name: '平遥古城', distance: '明清建筑', image: 'https://images.unsplash.com/photo-1548919973-5cef591cdbc9?w=400&h=300&fit=crop' },
    { name: '凤凰古城', distance: '湘西风情', image: 'https://images.unsplash.com/photo-1583952812866-8d5e9c3e9f9d?w=400&h=300&fit=crop' }
  ],
  '近沙滩': [
    { name: '三亚', distance: '热带海滨', image: 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=400&h=300&fit=crop' },
    { name: '青岛', distance: '黄海明珠', image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop' },
    { name: '厦门鼓浪屿', distance: '海上花园', image: 'https://images.unsplash.com/photo-1583952812866-8d5e9c3e9f9d?w=400&h=300&fit=crop' },
    { name: '大连', distance: '浪漫之都', image: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400&h=300&fit=crop' },
    { name: '北海', distance: '银滩之美', image: 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=400&h=300&fit=crop' },
    { name: '威海', distance: '海滨城市', image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop' }
  ],
  '山区': [
    { name: '黄山', distance: '天下第一奇山', image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop' },
    { name: '张家界', distance: '奇峰异石', image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop' },
    { name: '九寨沟', distance: '人间仙境', image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop' },
    { name: '峨眉山', distance: '佛教名山', image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop' },
    { name: '泰山', distance: '五岳之首', image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop' },
    { name: '华山', distance: '险峻奇绝', image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop' }
  ],
  '户外': [
    { name: '呼伦贝尔', distance: '大草原', image: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=400&h=300&fit=crop' },
    { name: '稻城亚丁', distance: '最后的香格里拉', image: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=400&h=300&fit=crop' },
    { name: '喀纳斯', distance: '神的后花园', image: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=400&h=300&fit=crop' },
    { name: '香格里拉', distance: '世外桃源', image: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=400&h=300&fit=crop' },
    { name: '西藏', distance: '雪域高原', image: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=400&h=300&fit=crop' },
    { name: '新疆', distance: '大美新疆', image: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=400&h=300&fit=crop' }
  ],
  '森林': [
    { name: '长白山', distance: '原始森林', image: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop' },
    { name: '神农架', distance: '野人传说', image: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop' },
    { name: '西双版纳', distance: '热带雨林', image: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop' },
    { name: '大兴安岭', distance: '林海雪原', image: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop' },
    { name: '武夷山', distance: '丹霞地貌', image: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop' },
    { name: '张家界', distance: '森林公园', image: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop' }
  ],
  '湖畔': [
    { name: '西湖', distance: '人间天堂', image: 'https://images.unsplash.com/photo-1439066615861-d1af74d74000?w=400&h=300&fit=crop' },
    { name: '千岛湖', distance: '碧波荡漾', image: 'https://images.unsplash.com/photo-1439066615861-d1af74d74000?w=400&h=300&fit=crop' },
    { name: '洱海', distance: '高原明珠', image: 'https://images.unsplash.com/photo-1439066615861-d1af74d74000?w=400&h=300&fit=crop' },
    { name: '泸沽湖', distance: '女儿国', image: 'https://images.unsplash.com/photo-1439066615861-d1af74d74000?w=400&h=300&fit=crop' },
    { name: '青海湖', distance: '中国最大咸水湖', image: 'https://images.unsplash.com/photo-1439066615861-d1af74d74000?w=400&h=300&fit=crop' },
    { name: '太湖', distance: '江南水乡', image: 'https://images.unsplash.com/photo-1439066615861-d1af74d74000?w=400&h=300&fit=crop' }
  ],
  '乡村': [
    { name: '婺源', distance: '中国最美乡村', image: 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400&h=300&fit=crop' },
    { name: '宏村', distance: '徽派建筑', image: 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400&h=300&fit=crop' },
    { name: '西递', distance: '古村落', image: 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400&h=300&fit=crop' },
    { name: '周庄', distance: '水乡古镇', image: 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400&h=300&fit=crop' },
    { name: '乌镇', distance: '江南水乡', image: 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400&h=300&fit=crop' },
    { name: '同里', distance: '古镇风情', image: 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400&h=300&fit=crop' }
  ]
}

const handleRegionClick = (region) => {
  console.log('点击了地区:', region.name)
  // 可以跳转到搜索页面，传入地区名称作为搜索条件
  // router.push({
  //   path: '/search',
  //   query: { location: region.name }
  // })
}
</script>

<style scoped>
.region-section {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 40px 80px 40px;
  background: #fff;
}

.region-container {
  width: 100%;
}

.region-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
}

.region-card {
  cursor: pointer;
  transition: transform 0.2s ease;
}

.region-card:hover {
  transform: translateY(-4px);
}

.region-image-wrapper {
  position: relative;
  width: 100%;
  height: 240px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
}

.region-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.region-card:hover .region-image {
  transform: scale(1.05);
}

.region-info {
  padding: 0 4px;
}

.region-name {
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin: 0 0 4px 0;
}

.region-distance {
  font-size: 14px;
  color: #717171;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .region-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
  }

  .region-image-wrapper {
    height: 200px;
  }
}

@media (max-width: 768px) {
  .region-section {
    padding: 0 24px 60px 24px;
  }

  .region-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }

  .region-image-wrapper {
    height: 180px;
  }
}

@media (max-width: 480px) {
  .region-section {
    padding: 0 16px 40px 16px;
  }

  .region-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .region-image-wrapper {
    height: 160px;
  }

  .region-name {
    font-size: 14px;
  }

  .region-distance {
    font-size: 12px;
  }
}
</style>
