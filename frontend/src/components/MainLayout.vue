<template>
  <el-container class="layout" :class="{ 'home-layout': $route.path === '/home' }">
    <el-aside width="228px" class="aside">
      <div class="brand">
        <img class="brand-logo" src="/icons/zhexuan-logo.png" alt="哲玄" />
        <div>
          <div class="brand-name">哲玄</div>
          <div class="brand-sub">术数分析系统</div>
        </div>
      </div>
      <el-menu router :default-active="$route.path" class="menu">
        <el-menu-item v-if="userStore.isAdmin" index="/dashboard"><LayoutDashboard :size="18" />管理首页</el-menu-item>
        <el-menu-item index="/home"><Landmark :size="18" />首页</el-menu-item>
        <el-menu-item index="/bazi"><CalendarDays :size="18" />八字</el-menu-item>
        <el-menu-item index="/liuyao"><ScrollText :size="18" />六爻</el-menu-item>
        <el-menu-item index="/ziwei"><Sparkles :size="18" />紫微</el-menu-item>
        <el-menu-item index="/quota"><CreditCard :size="18" />支持开发</el-menu-item>
        <el-menu-item index="/knowledge"><BookOpen :size="18" />知识库</el-menu-item>
        <el-menu-item index="/records"><Clock3 :size="18" />历史</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="mobile-title">
          <img class="mobile-logo" src="/icons/zhexuan-logo.png" alt="哲玄" />
          <span>哲玄</span>
        </div>
        <nav class="home-top-nav" aria-label="首页导航">
          <router-link to="/home">首页</router-link>
          <router-link to="/bazi">工具</router-link>
          <router-link to="/records">收藏</router-link>
        </nav>
        <el-dropdown>
          <span class="user">
            <UserRound :size="18" />
            {{ userStore.user?.nickname || userStore.user?.username }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/account')">账号设置</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <nav class="bottom-nav">
      <router-link to="/home" class="bottom-item">
        <Landmark :size="20" />
        <span>首页</span>
      </router-link>
      <router-link to="/bazi" class="bottom-item">
        <CalendarDays :size="20" />
        <span>八字</span>
      </router-link>
      <router-link to="/liuyao" class="bottom-item">
        <ScrollText :size="20" />
        <span>六爻</span>
      </router-link>
      <router-link to="/ziwei" class="bottom-item">
        <Sparkles :size="20" />
        <span>紫微</span>
      </router-link>
      <router-link to="/records" class="bottom-item">
        <Clock3 :size="20" />
        <span>历史</span>
      </router-link>
      <router-link to="/quota" class="bottom-item">
        <CreditCard :size="20" />
        <span>打赏</span>
      </router-link>
      <router-link to="/knowledge" class="bottom-item">
        <BookOpen :size="20" />
        <span>知识</span>
      </router-link>
    </nav>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { BookOpen, CalendarDays, Clock3, CreditCard, Landmark, LayoutDashboard, ScrollText, Sparkles, UserRound } from 'lucide-vue-next'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.clearUser()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  background: #151a22;
  color: #fff;
}

.brand {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: block;
  object-fit: cover;
  background: #f7f0e5;
}

.brand-name {
  font-size: 18px;
  font-weight: 800;
}

.brand-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #aeb7c2;
}

.menu {
  border-right: 0;
  background: transparent;
}

.menu :deep(.el-menu-item) {
  color: #d1d5db;
  gap: 10px;
}

.menu :deep(.el-menu-item.is-active) {
  color: #d6a954;
  background: rgba(214, 169, 84, 0.12);
}

.header {
  height: 58px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.mobile-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
}

.mobile-logo {
  width: 28px;
  height: 28px;
  border-radius: 7px;
  object-fit: cover;
}

.user {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  cursor: pointer;
}

.main {
  padding: 0;
}

.home-top-nav {
  display: none;
}

.bottom-nav {
  display: none;
}

@media (min-width: 701px) {
  .layout.home-layout {
    display: block;
    background: #f8f1e2;
  }

  .layout.home-layout .aside {
    display: none;
  }

  .layout.home-layout .header {
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    z-index: 8;
    height: 74px;
    padding: 0 46px;
    border-bottom: 1px solid rgba(232, 214, 165, 0.12);
    background: rgba(0, 45, 39, 0.88);
    backdrop-filter: blur(10px);
  }

  .layout.home-layout .mobile-title {
    color: #fff8dd;
    font-size: 22px;
    gap: 12px;
  }

  .layout.home-layout .mobile-logo {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    border: 1px solid #d6b66d;
    background: #0b3b32;
  }

  .layout.home-layout .home-top-nav {
    display: flex;
    align-items: center;
    gap: 18px;
    margin-right: auto;
    margin-left: 44px;
  }

  .layout.home-layout .home-top-nav a {
    min-width: 86px;
    height: 40px;
    display: grid;
    place-items: center;
    border: 1px solid transparent;
    border-radius: 999px;
    color: #e9d39c;
    text-decoration: none;
    font-weight: 800;
  }

  .layout.home-layout .home-top-nav a.router-link-active,
  .layout.home-layout .home-top-nav a:hover {
    border-color: rgba(232, 214, 165, 0.26);
    background: rgba(255, 255, 255, 0.08);
    color: #fff4ca;
  }

  .layout.home-layout .user {
    color: #e9d39c;
  }
}

@media (max-width: 700px) {
  .layout {
    display: block;
    min-height: 100dvh;
    padding-bottom: calc(60px + env(safe-area-inset-bottom));
    overflow-x: hidden;
  }

  .aside {
    display: none;
  }

  .header {
    height: 48px;
    padding: 0 10px;
    position: sticky;
    top: 0;
    z-index: 20;
  }

  .layout.home-layout .header {
    position: absolute;
    background: transparent;
    border-bottom: 0;
  }

  .layout.home-layout .mobile-title {
    color: #fff8dd;
  }

  .layout.home-layout .mobile-logo {
    border-radius: 50%;
    border: 1px solid #d6b66d;
    background: #0b3b32;
  }

  .layout.home-layout .user {
    color: #e9d39c;
  }

  .user {
    max-width: 48vw;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .bottom-nav {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 30;
    min-height: 58px;
    height: calc(58px + env(safe-area-inset-bottom));
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    background: #fff;
    border-top: 1px solid #e5e7eb;
    padding-bottom: env(safe-area-inset-bottom);
  }

  .bottom-item {
    display: grid;
    place-items: center;
    align-content: center;
    gap: 3px;
    color: #667085;
    text-decoration: none;
    font-size: 10px;
    line-height: 1.1;
    min-width: 0;
  }

  .bottom-item svg {
    width: 19px;
    height: 19px;
  }

  .bottom-item span {
    white-space: nowrap;
  }

  .bottom-item.router-link-active {
    color: #a36f12;
  }
}
</style>
