<template>
  <div class="auth-page">
    <div class="auth-panel">
      <img class="auth-logo" src="/icons/zhexuan-logo.png" alt="哲玄" />
      <h1>哲玄</h1>
      <p>登录后进入六爻工作台，也可以直接游客体验。</p>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin">登录</el-button>
        <el-button size="large" @click="guestLogin">游客体验</el-button>
        <el-button link @click="$router.push('/register')">注册新账号</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  loading.value = true
  try {
    const user = await login(form)
    userStore.setUser(user)
    ElMessage.success('登录成功')
    router.push(user.role === 'ADMIN' ? '/dashboard' : '/bazi')
  } finally {
    loading.value = false
  }
}

function guestLogin() {
  userStore.setUser({ id: null, username: 'guest', nickname: '游客', role: 'GUEST' })
  router.push('/bazi')
}
</script>

<style scoped src="./auth.css"></style>
