import { createRouter, createWebHistory } from 'vue-router'
import UserLayout from '../layouts/UserLayout.vue'
import Home from '../views/user/Home.vue'
import HostLayout from '../layouts/HostLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'

const routes = [
  {
    path: '/user',
    component: UserLayout,
    children: [
      {
        path: '',
        name: 'UserHome',
        component: Home
      },
      // --- 重构后的个人中心：嵌套路由结构 ---
      {
        path: 'profile',
        component: () => import('../views/user/profile/ProfileLayout.vue'),
        meta: { requiresAuth: true },
        children: [
          {
            path: '', // 默认重定向到“关于我”
            redirect: '/user/profile/info'
          },
          {
            path: 'info',
            name: 'UserProfileInfo',
            component: () => import('../views/user/profile/UserInfo.vue')
          },
          {
            path: 'trips',
            name: 'UserTrips',
            component: () => import('../views/user/profile/MyTrips.vue')
          },
          {
            path: 'wishlist',
            name: 'UserWishlist',
            component: () => import('../views/user/profile/Wishlist.vue')
          },
          {
            path: 'reviews',
            name: 'UserReviews',
            component: () => import('../views/user/profile/MyReviews.vue')
          },
          {
            path: 'messages',
            name: 'UserMessages',
            component: () => import('../views/user/profile/UserMessages.vue')
          }
        ]
      },
      {
        path: '/user/profile/edit',
        name: 'UserProfileEdit',
        component: () => import('../views/user/profile/EditProfile.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  {
    path: '/search',
    name: 'SearchResult',
    component: () => import('../views/user/SearchResult.vue')
  },
  {
    path: '/house/:id',
    name: 'HouseDetail',
    component: () => import('../views/user/HouseDetail.vue')
  },
  {
    path: '/',
    redirect: '/user'
  },
  {
    path: '/host',
    component: HostLayout,
    redirect: '/host/dashboard',
    meta: { requiresAuth: true, role: 'ROLE_HOST' }, 
    children: [
      { path: 'dashboard', component: () => import('../views/host/Dashboard.vue') },
      { path: 'house/list', component: () => import('../views/host/HouseList.vue') },
      { path: 'house/add', component: () => import('../views/host/HouseAdd.vue') },
      { path: 'order', component: () => import('../views/host/OrderList.vue') },
      { path: 'promotion', component: () => import('../views/host/Promotion.vue') },
      { path: 'income', component: () => import('../views/host/Income.vue') },
      { path: 'review', component: () => import('../views/host/Review.vue') },
      { path: 'profile', component: () => import('../views/host/Profile.vue') },
      { path: 'hostmessage', component: () => import('../views/host/HostMessage.vue') },
      { path: 'house/edit/:id', component: () => import('../views/host/HouseAdd.vue')}
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, role: 'ROLE_ADMIN' },
    children: [
      { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'hosts', component: () => import('../views/admin/HostManagement.vue') },
      { path: 'houses', component: () => import('../views/admin/HouseManagement.vue') },
      { path: 'users', component: () => import('../views/admin/UserManagement.vue') },
      { path: 'orders', component: () => import('../views/admin/OrderManagement.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// --- 核心：全局路由守卫（已优化以支持通用的 requiresAuth） ---
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 1. 检查是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      alert('请先登录！')
      next('/user')
      return // 拦截
    }
    
    // 2. 如果是去往房东页面，额外检查角色
    if (to.path.startsWith('/host') && role !== 'ROLE_HOST') {
      alert('您没有房东权限！')
      next('/user')
      return // 拦截
    }

    // 3. 如果是去往管理员页面，额外检查角色
    if (to.path.startsWith('/admin') && role !== 'ROLE_ADMIN') {
      alert('您没有管理员权限！')
      next('/user')
      return // 拦截
    }
  }

  next() // 满足条件，放行
})

export default router