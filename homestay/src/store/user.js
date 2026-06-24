import { defineStore } from 'pinia'
import request from '../utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    avatar: localStorage.getItem('avatar') || '',
    userId: localStorage.getItem('userId') || '',
    nickname: ''
  }),

  getters: {
    isLogin: (state) => !!state.token,

    avatarUrl: (state) => {
      if (!state.avatar) return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

      const base = state.avatar.startsWith('http')
        ? state.avatar
        : `http://localhost:8080${state.avatar}`

      return base + '?t=' + Date.now()
    }
  },

  actions: {

    async loadUser() {
      try {
        const res = await request.get('/api/user/profile/get')

        if (res.user) {
          this.avatar = res.user.avatar
          this.nickname = res.user.nickname
          this.userId = res.user.id
        }

        localStorage.setItem('avatar', this.avatar)
        localStorage.setItem('userId', this.userId)

      } catch (e) {
        console.error('加载用户失败')
      }
    },

    setLogin(data) {
      this.token = data.token
      this.role = data.role
      this.avatar = data.avatar || ''
      this.userId = data.userId || data.id || ''

      localStorage.setItem('token', this.token)
      localStorage.setItem('role', this.role)
      localStorage.setItem('avatar', this.avatar)
      localStorage.setItem('userId', this.userId)
      
      // 同时存储完整的user对象，方便其他地方使用
      localStorage.setItem('user', JSON.stringify({
        userId: this.userId,
        id: this.userId,
        role: this.role,
        avatar: this.avatar
      }))
    },

    logout() {
      this.token = ''
      this.role = ''
      this.avatar = ''
      this.nickname = ''
      this.userId = ''

      localStorage.clear()
    }

  }
})