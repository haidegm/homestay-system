<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emitVisible"
    :show-close="true"
    width="480px"
    center
    class="auth-dialog"
  >
    <template #header>
      <div class="dialog-header">
        <h2>{{ dialogType === 'login' ? '欢迎回来' : '加入我们' }}</h2>
        <p>{{ dialogType === 'login' ? '登录您的账号' : '创建新账号' }}</p>
      </div>
    </template>

    <!-- 登录表单 -->
    <el-form 
      v-if="dialogType === 'login'" 
      :model="loginForm" 
      :rules="loginRules"
      ref="loginFormRef"
      class="auth-form"
      label-position="top"
    >
      <el-form-item label="选择身份" prop="role">
        <el-radio-group v-model="loginForm.role" class="role-group">
          <el-radio-button label="user">
            <el-icon><User /></el-icon>
            <span>用户</span>
          </el-radio-button>
          <el-radio-button label="host">
            <el-icon><House /></el-icon>
            <span>房东</span>
          </el-radio-button>
          <el-radio-button label="admin">
            <el-icon><Setting /></el-icon>
            <span>管理员</span>
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="用户名" prop="username">
        <el-input 
          v-model="loginForm.username" 
          placeholder="请输入用户名"
          size="large"
          :prefix-icon="User"
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input 
          type="password" 
          v-model="loginForm.password"
          placeholder="请输入密码"
          size="large"
          :prefix-icon="Lock"
          show-password
        />
      </el-form-item>

      <el-button 
        type="primary" 
        size="large"
        class="submit-btn"
        @click="handleLogin"
        :loading="loginLoading"
      >
        登录
      </el-button>

      <div class="form-footer">
        <span>还没有账号？</span>
        <el-button text type="primary" @click="switchToRegister">立即注册</el-button>
      </div>
    </el-form>

    <!-- 注册表单 -->
    <el-form 
      v-else 
      :model="registerForm" 
      :rules="registerRules"
      ref="registerFormRef"
      class="auth-form"
      label-position="top"
    >
      <el-form-item label="选择身份" prop="role">
        <el-radio-group v-model="registerForm.role" class="role-group">
          <el-radio-button label="user">
            <el-icon><User /></el-icon>
            <span>用户</span>
          </el-radio-button>
          <el-radio-button label="host">
            <el-icon><House /></el-icon>
            <span>房东</span>
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="用户名" prop="username">
        <el-input 
          v-model="registerForm.username"
          placeholder="请输入用户名"
          size="large"
          :prefix-icon="User"
        />
      </el-form-item>

      <el-form-item label="昵称" prop="nickname">
        <el-input 
          v-model="registerForm.nickname"
          placeholder="请输入昵称"
          size="large"
        />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input 
          v-model="registerForm.phone"
          placeholder="请输入手机号"
          size="large"
          :prefix-icon="Phone"
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input 
          type="password" 
          v-model="registerForm.password"
          placeholder="请输入密码（至少6位）"
          size="large"
          :prefix-icon="Lock"
          show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input 
          type="password" 
          v-model="registerForm.confirmPassword"
          placeholder="请再次输入密码"
          size="large"
          :prefix-icon="Lock"
          show-password
        />
      </el-form-item>

      <el-button 
        type="primary" 
        size="large"
        class="submit-btn"
        @click="handleRegister"
        :loading="registerLoading"
      >
        注册
      </el-button>

      <div class="form-footer">
        <span>已有账号？</span>
        <el-button text type="primary" @click="switchToLogin">立即登录</el-button>
      </div>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { User, House, Setting, Lock, Phone } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const props = defineProps({
  visible: Boolean,
  dialogType: String
})

const emit = defineEmits(['update:visible', 'update:dialogType'])

const emitVisible = (val) => emit('update:visible', val)

const loginFormRef = ref()
const registerFormRef = ref()
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  role: 'user'
})

const registerForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'user'
})

// 登录表单验证规则（不限制密码长度，兼容旧账号）
const loginRules = {
  role: [{ required: true, message: '请选择身份', trigger: 'change' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 注册表单验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  role: [{ required: true, message: '请选择身份', trigger: 'change' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loginLoading.value = true
    try {
      const res = await request.post('/api/auth/login', loginForm)

      if (res && res.token) {
        // 验证角色是否匹配
        const expectedRole = loginForm.role === 'user' 
          ? 'ROLE_USER' 
          : loginForm.role === 'host' 
          ? 'ROLE_HOST' 
          : 'ROLE_ADMIN'

        if (res.role !== expectedRole) {
          ElMessage.error(`该账号不是${loginForm.role === 'user' ? '用户' : loginForm.role === 'host' ? '房东' : '管理员'}账号`)
          return
        }

        userStore.setLogin(res)
        ElMessage.success('登录成功')
        emitVisible(false)

        // 登录后加载用户信息
        await userStore.loadUser()

        // 根据角色跳转到对应页面
        if (res.role === 'ROLE_ADMIN') {
          router.push('/admin/dashboard')
        } else if (res.role === 'ROLE_HOST') {
          router.push('/host/dashboard')
        } else {
          router.push('/user')
        }
        
        // 刷新页面
        setTimeout(() => {
          window.location.reload()
        }, 100)

      } else {
        ElMessage.error(res.msg || '登录失败')
      }
    } catch (error) {
      console.error('登录请求异常', error)
      ElMessage.error('登录失败，请检查用户名和密码')
    } finally {
      loginLoading.value = false
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    registerLoading.value = true
    try {
      const roleMap = {
        'user': 'ROLE_USER',
        'host': 'ROLE_HOST'
      }

      const payload = {
        username: registerForm.username,
        nickname: registerForm.nickname,
        phone: registerForm.phone,
        password: registerForm.password,
        role: roleMap[registerForm.role]
      }

      const res = await request.post('/api/auth/register', payload)
      
      if (res.msg === '注册成功' || res.success) {
        ElMessage.success('注册成功！')
        // 清空注册表单
        registerForm.username = ''
        registerForm.nickname = ''
        registerForm.phone = ''
        registerForm.password = ''
        registerForm.confirmPassword = ''
        registerForm.role = 'user'
        // 切换到登录页面
        setTimeout(() => {
          switchToLogin()
        }, 500)
      } else {
        ElMessage.error(res.msg || '注册失败')
      }
    } catch (error) {
      console.error('注册请求异常', error)
      ElMessage.error('注册失败: ' + (error.message || '未知错误'))
    } finally {
      registerLoading.value = false
    }
  })
}

const switchToRegister = () => {
  emit('update:dialogType', 'register')
  // 清空登录表单
  loginForm.username = ''
  loginForm.password = ''
  loginForm.role = 'user'
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
}

const switchToLogin = () => {
  emit('update:dialogType', 'login')
  // 清空注册表单
  registerForm.username = ''
  registerForm.nickname = ''
  registerForm.phone = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.role = 'user'
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
}
</script>

<style scoped>
.auth-dialog :deep(.el-dialog) {
  border-radius: 16px;
  padding: 0;
}

.auth-dialog :deep(.el-dialog__header) {
  padding: 32px 32px 0;
  margin: 0;
}

.auth-dialog :deep(.el-dialog__body) {
  padding: 24px 32px 32px;
}

.dialog-header {
  text-align: center;
}

.dialog-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #222;
}

.dialog-header p {
  font-size: 14px;
  color: #717171;
  margin: 0;
}

.auth-form {
  margin-top: 24px;
}

.role-group {
  width: 100%;
  display: flex;
  gap: 8px;
}

.role-group :deep(.el-radio-button) {
  flex: 1;
}

.role-group :deep(.el-radio-button__inner) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 20px;
  border-radius: 8px;
  border: 1px solid #DDDDDD;
  background: white;
  transition: all 0.2s;
}

.role-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #222;
  border-color: #222;
  color: white;
  box-shadow: none;
}

.role-group :deep(.el-radio-button__inner:hover) {
  color: #222;
}

.auth-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #222;
  margin-bottom: 8px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #DDDDDD inset;
  padding: 12px 16px;
}

.auth-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #222 inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #222 inset;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: linear-gradient(to right, #E61E4D, #E31C5F, #D70466);
  border: none;
  margin-top: 8px;
}

.submit-btn:hover {
  background: linear-gradient(to right, #D01346, #CA1A5B, #C10366);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #717171;
}

.form-footer .el-button {
  font-weight: 600;
  padding: 0 4px;
}
</style>