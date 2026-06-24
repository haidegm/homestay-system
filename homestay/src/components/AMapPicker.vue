<template>
  <div class="map-wrapper">
    <div id="amap-container"></div>

    <div class="coord-info" v-if="location.latitude">
      当前坐标：
      {{ location.longitude }},
      {{ location.latitude }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue"

const props = defineProps({
  modelValue: Object,
  city: Array  // 新增：接收城市信息
})

const emit = defineEmits(["update:modelValue"])

const location = ref({
  latitude: "",
  longitude: ""
})

let map = null
let marker = null
let geocoder = null

/* 动态加载高德地图 */
const loadAMap = () => {
  return new Promise((resolve) => {
    if (window.AMap) {
      resolve(window.AMap)
      return
    }

    const script = document.createElement("script")
    script.src =
      "https://webapi.amap.com/maps?v=2.0&key=7d8201fc53086dece1a6d92289d0ca66"

    script.onload = () => resolve(window.AMap)

    document.head.appendChild(script)
  })
}

/* 根据城市名称定位 */
const locateCity = (cityName) => {
  if (!map || !geocoder || !cityName) return

  geocoder.getLocation(cityName, (status, result) => {
    if (status === 'complete' && result.geocodes.length) {
      const location = result.geocodes[0].location
      map.setCenter([location.lng, location.lat])
      map.setZoom(12)
    }
  })
}

onMounted(async () => {
  const AMap = await loadAMap()

  map = new AMap.Map("amap-container", {
    zoom: 11,
    center: [116.397428, 39.90923]
  })

  // 初始化地理编码服务
  AMap.plugin('AMap.Geocoder', () => {
    geocoder = new AMap.Geocoder()
  })

  map.on("click", (e) => {
    const lng = e.lnglat.lng
    const lat = e.lnglat.lat

    location.value = {
      longitude: lng,
      latitude: lat
    }

    if (marker) {
      marker.setPosition([lng, lat])
    } else {
      marker = new AMap.Marker({
        position: [lng, lat],
        map: map
      })
    }

    emit("update:modelValue", location.value)
  })

  // 如果有初始坐标，设置标记
  if (props.modelValue?.latitude && props.modelValue?.longitude) {
    const lng = props.modelValue.longitude
    const lat = props.modelValue.latitude
    
    location.value = {
      longitude: lng,
      latitude: lat
    }

    marker = new AMap.Marker({
      position: [lng, lat],
      map: map
    })
    
    map.setCenter([lng, lat])
  }
})

// 监听城市变化，自动定位
watch(() => props.city, (newCity) => {
  if (newCity && newCity.length >= 2) {
    // 使用市级城市名称定位
    locateCity(newCity[1])
  }
}, { deep: true })
</script>

<style scoped>
.map-wrapper {
  width: 100%;
}

#amap-container {
  width: 100%;
  height: 400px;
  border-radius: 8px;
}

.coord-info {
  margin-top: 10px;
  font-size: 13px;
  color: #666;
}
</style>