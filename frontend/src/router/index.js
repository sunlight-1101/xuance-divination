import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const MainLayout = () => import('../components/MainLayout.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/auth/LoginView.vue') },
    { path: '/register', component: () => import('../views/auth/RegisterView.vue') },
    {
      path: '/',
      component: MainLayout,
      redirect: () => {
        const userStore = useUserStore()
        return userStore.isAdmin ? '/dashboard' : '/home'
      },
      children: [
        { path: 'home', component: () => import('../views/home/HomeView.vue') },
        { path: 'dashboard', component: () => import('../views/dashboard/DashboardView.vue'), meta: { adminOnly: true } },
        { path: 'bazi', component: () => import('../views/bazi/BaziAnalyzeView.vue') },
        { path: 'hecan', component: () => import('../views/bazi/HeCanView.vue') },
        { path: 'liuyao', component: () => import('../views/liuyao/LiuyaoAnalyzeView.vue') },
        { path: 'ziwei', component: () => import('../views/ziwei/ZiweiChartView.vue') },
        { path: 'quota', component: () => import('../views/quota/QuotaView.vue') },
        { path: 'knowledge', component: () => import('../views/knowledge/KnowledgeView.vue') },
        { path: 'records', component: () => import('../views/record/RecordListView.vue') },
        { path: 'account', component: () => import('../views/account/AccountView.vue') },
        { path: 'admin/users', component: () => import('../views/dashboard/UserListView.vue'), meta: { adminOnly: true } },
        { path: 'admin/records', component: () => import('../views/dashboard/AdminRecordListView.vue'), meta: { adminOnly: true } }
      ]
    }
  ]
})

router.beforeEach(to => {
  const userStore = useUserStore()
  if (!userStore.isLogin && !['/login', '/register'].includes(to.path)) {
    return '/login'
  }
  if (to.meta.adminOnly && !userStore.isAdmin) {
    return '/home'
  }
})

export default router
