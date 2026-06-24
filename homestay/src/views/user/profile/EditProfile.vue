  <template>
    <div class="edit-page-container">
      <nav class="edit-nav">
        <div class="nav-inner">
          <el-button link @click="router.back()" class="back-btn">
            <el-icon><ArrowLeft /></el-icon> 退出
          </el-button>
        </div>
      </nav>

      <div class="edit-content-wrapper">
        <el-row :gutter="80">
          <el-col :span="8">
            <div class="avatar-fixed-section">
              <div class="avatar-uploader">
                <el-avatar :size="260" :src="fullAvatarUrl" />
                <div class="upload-trigger" @click="triggerFile">
                  <el-icon><Camera /></el-icon> <span>添加</span>
                </div>
                <input type="file" ref="fileInput" hidden @change="uploadAvatar" accept="image/*" />
              </div>
            </div>
          </el-col>

          <el-col :span="16" class="form-scroll-section">
            <section class="form-body">
              <h1 class="main-title">我的个人资料</h1>
              <p class="intro-text">
                你的个人资料对爱彼迎用户可见，帮助建立用户社区的信任。<span class="underline">了解详情</span>
              </p>

              <div class="fields-grid">
                <div class="field-item">
                  <div class="label"><el-icon><Briefcase /></el-icon> 我的职业</div>
                  <el-input v-model="form.profession" placeholder="添加职业" />
                </div>
                <div class="field-item">
                  <div class="label"><el-icon><Compass /></el-icon> 我一直想去的地方</div>
                  <el-input v-model="form.targetLocation" placeholder="添加地点" />
                </div>
                <div class="field-item">
                  <div class="label"><el-icon><ChatDotRound /></el-icon> 关于我的有趣事实</div>
                  <el-input v-model="form.funFact" placeholder="添加有趣事实" />
                </div>
                <div class="field-item">
                  <div class="label"><el-icon><MagicStick /></el-icon> 宠物</div>
                  <el-input v-model="form.pets" placeholder="添加宠物" />
                </div>
                <div class="field-item">
                  <div class="label"><el-icon><Calendar /></el-icon> 我出生的年代</div>
                  <el-input v-model="form.bornYear" placeholder="添加年代" />
                </div>
                <div class="field-item">
                  <div class="label"><el-icon><School /></el-icon> 曾经就读的学校</div>
                  <el-input v-model="form.school" placeholder="添加学校" />
                </div>
                <div class="field-item">
                  <div class="label"><el-icon><Microphone /></el-icon> 我最无用的技能</div>
                  <el-input v-model="form.uselessSkill" placeholder="添加技能" />
                </div>
                <div class="field-item">
                  <div class="label"><el-icon><Clock /></el-icon> 花费我太多时间的事情</div>
                  <el-input v-model="form.wasteTime" placeholder="添加事情" />
                </div>
                <div class="field-item">
                  <div class="label"><el-icon><Headset /></el-icon> 高中时最喜欢的歌曲</div>
                  <el-input v-model="form.favSong" placeholder="添加歌曲" />
                </div>
                <div class="field-item">
                  <div class="label"><el-icon><ChatLineSquare /></el-icon> 我会讲的语言</div>
                  <el-input v-model="form.languages" placeholder="添加语言" />
                </div>
              </div>

              <el-divider />

              <div class="bio-section">
                <h2 class="section-title">关于我</h2>
                <div class="bio-input-card">
                  <el-input 
                    v-model="form.bio" 
                    type="textarea" 
                    :rows="3" 
                    resize="none" 
                    placeholder="写一些有趣又有吸引力的内容..."
                  />
                </div>
              </div>

              <div class="toggle-section-container">
                <div class="toggle-header">
                  <div class="toggle-info">
                    <h2 class="section-title">我去过的地方</h2>
                    <p>选择你希望展示的印章。</p>
                  </div>
                  <el-switch v-model="form.showPlaces" color="#222" />
                </div>

                <el-collapse-transition>
                  <div v-if="form.showPlaces" class="places-grid">
                    <div 
                      v-for="place in availablePlaces" 
                      :key="place.name"
                      class="place-stamp"
                      :class="{ active: selectedPlacesList.includes(place.name) }"
                      @click="togglePlace(place.name)"
                    >
                      <div class="stamp-icon">{{ place.icon }}</div>
                      <div class="stamp-name">{{ place.name }}</div>
                      <div class="check-badge" v-if="selectedPlacesList.includes(place.name)">
                        <el-icon><Check /></el-icon>
                      </div>
                    </div>
                  </div>
                </el-collapse-transition>
              </div>

              <div class="form-footer">
                <el-button class="save-btn" @click="saveAll">完成</el-button>
              </div>
            </section>
          </el-col>
        </el-row>
      </div>
    </div>
  </template>

  <script setup>
  import { ref, reactive, onMounted, computed } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import request from '../../../utils/request'
  import { 
    ArrowLeft, Camera, Briefcase, Compass, ChatDotRound, MagicStick, 
    Calendar, School, Microphone, Clock, Headset, ChatLineSquare,
    Check, Plus 
  } from '@element-plus/icons-vue'
  import { useUserStore } from '../../../store/user'

  const router = useRouter()
  const fileInput = ref(null)
  const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  const userStore = useUserStore()
  // 用户头像信息（存在 User 表）
  const userInfo = reactive({ avatar: '' })

  // 个人资料表单（对应 UserProfile 表）
  const form = reactive({
    profession: '',
    targetLocation: '',
    funFact: '',
    pets: '',
    bornYear: '',
    school: '',
    uselessSkill: '',
    wasteTime: '',
    favSong: '',
    languages: '',
    bio: '',
    showPlaces: true,
    selectedPlaces: '' // 数据库存逗号分隔字符串
  })

  // 计算属性处理头像展示
  const fullAvatarUrl = computed(() => {
    if (!userInfo.avatar) return defaultAvatar
    return userInfo.avatar.startsWith('http') ? userInfo.avatar : `http://localhost:8080${userInfo.avatar}`
  })

  // 处理足迹列表转换
  const selectedPlacesList = computed(() => {
    return form.selectedPlaces ? form.selectedPlaces.split(',') : []
  })

  const availablePlaces = [
    { name: '上海', icon: '🏙️' }, { name: '成都', icon: '🐼' },
    { name: '东京', icon: '🗼' }, { name: '大理', icon: '🏔️' },
    { name: '巴黎', icon: '🥖' }, { name: '清迈', icon: '🐘' }
  ]

  const togglePlace = (name) => {
    let list = [...selectedPlacesList.value]
    const index = list.indexOf(name)
    if (index > -1) list.splice(index, 1)
    else list.push(name)
    form.selectedPlaces = list.join(',')
  }

  // 页面加载获取数据
  const loadData = async () => {
    try {
      const res = await request.get('/api/user/profile/get')
      if (res.user) userInfo.avatar = res.user.avatar
      if (res.profile) Object.assign(form, res.profile)
    } catch (e) {
      console.error('加载失败', e)
    }
  }

  // 头像上传
  const triggerFile = () => fileInput.value.click()
  const uploadAvatar = async (event) => {
    const file = event.target.files[0]
    if (!file) return

    const formData = new FormData()
    formData.append('file', file)

    try {
      const url = await request.post('/api/user/profile/upload/avatar', formData)

      // 更新本页面头像
      userInfo.avatar = url

      userStore.avatar = url

      ElMessage.success('头像上传成功')
    } catch (e) {
      ElMessage.error('上传失败')
    }
  }

  // 保存全部
  const saveAll = async () => {
    try {
      await request.post('/api/user/profile/save', form)
      ElMessage.success('保存成功')
      router.push('/user/profile/info')
    } catch (e) { ElMessage.error('保存失败') }
  }

  onMounted(loadData)
  </script>

  <style scoped>
  /* 样式保持你给出的模板内容，确保布局美观 */
  .edit-page-container { background: #fff; min-height: 100vh; }
  .edit-nav { height: 80px; display: flex; align-items: center; padding: 0 40px; position: sticky; top: 0; background: #fff; z-index: 10; border-bottom: 1px solid #eee; }
  .nav-inner { width: 100%; max-width: 1080px; margin: 0 auto; }
  .back-btn { font-size: 16px; font-weight: 600; color: #222; }
  .edit-content-wrapper { max-width: 1080px; margin: 0 auto; padding: 20px 20px 100px; }
  .avatar-fixed-section { position: sticky; top: 100px; display: flex; justify-content: center; }
  .avatar-uploader { position: relative; border-radius: 50%; }
  .upload-trigger {
    position: absolute; bottom: 20px; right: 20px; background: #fff; border: 1px solid #ddd;
    padding: 6px 15px; border-radius: 20px; cursor: pointer; display: flex; align-items: center;
    gap: 5px; font-weight: 600; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }
  .main-title { font-size: 32px; font-weight: 600; margin-bottom: 10px; }
  .intro-text { color: #717171; line-height: 1.6; margin-bottom: 40px; }
  .fields-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; }
  .field-item { border-bottom: 1px solid #eee; padding-bottom: 5px; }
  .label { display: flex; align-items: center; gap: 10px; font-size: 16px; color: #222; margin-bottom: 10px; }
  :deep(.el-input__wrapper) { box-shadow: none !important; padding: 0; }
  :deep(.el-input__inner) { font-size: 16px; color: #222; }
  .bio-input-card { border: 1px dashed #ccc; border-radius: 12px; padding: 10px; }
  .places-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 16px; margin-top: 20px; }
  .place-stamp {
    position: relative; height: 100px; border: 1px solid #EBEBEB; border-radius: 12px;
    display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer;
  }
  .place-stamp.active { border: 2px solid #222; background: #f9f9f9; }
  .stamp-icon { font-size: 26px; }
  .check-badge { position: absolute; top: -8px; right: -8px; background: #222; color: #fff; width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
  .form-footer { margin-top: 60px; display: flex; justify-content: flex-end; }
  .save-btn { background: #222; color: #fff; padding: 12px 40px; border-radius: 8px; font-weight: 600; font-size: 16px; }
  </style>