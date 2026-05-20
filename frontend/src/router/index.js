import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import MainLayout from '../components/MainLayout.vue'
import HomeView from '../views/home/HomeView.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import BaziAnalyzeView from '../views/bazi/BaziAnalyzeView.vue'
import LiuyaoAnalyzeView from '../views/liuyao/LiuyaoAnalyzeView.vue'
import ZiweiChartView from '../views/ziwei/ZiweiChartView.vue'
import KnowledgeView from '../views/knowledge/KnowledgeView.vue'
import RecordListView from '../views/record/RecordListView.vue'
import QuotaView from '../views/quota/QuotaView.vue'
import AccountView from '../views/account/AccountView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    {
      path: '/',
      component: MainLayout,
      redirect: () => {
        const userStore = useUserStore()
        return userStore.isAdmin ? '/dashboard' : '/home'
      },
      children: [
        { path: 'home', component: HomeView },
        { path: 'dashboard', component: DashboardView, meta: { adminOnly: true } },
        { path: 'bazi', component: BaziAnalyzeView },
        { path: 'liuyao', component: LiuyaoAnalyzeView },
        { path: 'ziwei', component: ZiweiChartView },
        { path: 'quota', component: QuotaView },
        { path: 'knowledge', component: KnowledgeView },
        { path: 'records', component: RecordListView },
        { path: 'account', component: AccountView }
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
