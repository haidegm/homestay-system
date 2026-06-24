<template>
  <div class="inspiration-section">
    <div class="section-header">
      <h2 class="section-title">灵感推荐</h2>
      <p class="section-subtitle">发现更多精彩住宿体验</p>
    </div>

    <div class="category-grid">
      <div
        v-for="category in categories"
        :key="category.name"
        :class="['category-card', { active: selectedCategory === category.name }]"
        @click="selectCategory(category.name)"
      >
        <div class="category-image-wrapper">
          <img :src="category.image" :alt="category.name" class="category-image" />
          <div class="category-overlay"></div>
        </div>
        <div class="category-info">
          <h3 class="category-name">{{ category.name }}</h3>
          <p class="category-desc">{{ category.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { defineEmits } from 'vue'

const emit = defineEmits(['categorySelected'])

const categories = [
  {
    name: '热门',
    description: '最受欢迎的住宿选择',
    image: 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=400&h=300&fit=crop'
  },
  {
    name: '艺术与文化',
    description: '体验当地艺术氛围',
    image: 'https://images.unsplash.com/photo-1513694203232-719a280e022f?w=400&h=300&fit=crop'
  },
  {
    name: '近沙滩',
    description: '享受海滨度假时光',
    image: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400&h=300&fit=crop'
  },
  {
    name: '山区',
    description: '探索山间秘境',
    image: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop'
  },
  {
    name: '户外',
    description: '亲近大自然',
    image: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=400&h=300&fit=crop'
  },
  {
    name: '森林',
    description: '沉浸在绿意之中',
    image: 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop'
  },
  {
    name: '湖畔',
    description: '静享湖光山色',
    image: 'https://images.unsplash.com/photo-1439066615861-d1af74d74000?w=400&h=300&fit=crop'
  },
  {
    name: '乡村',
    description: '体验田园生活',
    image: 'https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400&h=300&fit=crop'
  }
]

const selectedCategory = ref('热门')

const selectCategory = (category) => {
  selectedCategory.value = category
  emit('categorySelected', category)
}
</script>

<style scoped>
.inspiration-section {
  max-width: 1280px;
  margin: 0 auto;
  padding: 80px 40px;
  background: #fff;
}

.section-header {
  margin-bottom: 48px;
  text-align: left;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  color: #222;
  margin: 0 0 8px 0;
}

.section-subtitle {
  font-size: 16px;
  color: #717171;
  margin: 0;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.category-card {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  background: #fff;
  border: 1px solid transparent;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.category-card.active {
  border-color: #222;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.category-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.category-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.category-card:hover .category-image {
  transform: scale(1.05);
}

.category-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.1) 100%);
  pointer-events: none;
}

.category-info {
  padding: 16px;
}

.category-name {
  font-size: 18px;
  font-weight: 600;
  color: #222;
  margin: 0 0 4px 0;
}

.category-desc {
  font-size: 14px;
  color: #717171;
  margin: 0;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .category-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .inspiration-section {
    padding: 60px 24px;
  }

  .section-title {
    font-size: 28px;
  }

  .category-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }

  .category-image-wrapper {
    height: 160px;
  }
}

@media (max-width: 480px) {
  .inspiration-section {
    padding: 40px 16px;
  }

  .section-title {
    font-size: 24px;
  }

  .category-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .category-image-wrapper {
    height: 180px;
  }
}
</style>
