<template>
  <div class="auth-page">
    <div class="auth-panel">
      <h1>创建账号</h1>
      <p>使用邮箱验证码注册，后续登录也使用邮箱和密码。</p>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" size="large" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="form.emailCode" size="large" placeholder="6位验证码" />
            <el-button :disabled="codeCountdown > 0" :loading="codeLoading" @click="sendCode">
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister">注册并登录</el-button>
        <el-button link @click="$router.push('/login')">返回登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, sendEmailCode } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const codeLoading = ref(false)
const codeCountdown = ref(0)
const form = reactive({ email: '', emailCode: '', nickname: '', password: '' })

async function sendCode() {
  if (!form.email.trim()) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  codeLoading.value = true
  try {
    await sendEmailCode({ email: form.email })
    ElMessage.success('验证码已发送，请查收邮箱')
    codeCountdown.value = 60
    const timer = setInterval(() => {
      codeCountdown.value -= 1
      if (codeCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } finally {
    codeLoading.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    const user = await register({ ...form, username: form.email })
    userStore.setUser(user)
    ElMessage.success('注册成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped src="./auth.css"></style>
