<template>
  <div class="auth-page">
    <div class="auth-panel">
      <img class="auth-logo" src="/icons/zhexuan-logo.png" alt="哲玄" />
      <h1>哲玄</h1>
      <p>登录后进入术数分析系统，也可以直接游客体验。</p>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="邮箱">
          <el-input v-model="form.username" size="large" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" size="large" show-password autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin">登录</el-button>
        <el-button size="large" @click="guestLogin">游客体验</el-button>
        <div class="auth-links">
          <el-button link @click="$router.push('/register')">注册新账号</el-button>
          <el-button link @click="resetDialogVisible = true">忘记密码</el-button>
        </div>
      </el-form>
    </div>

    <el-dialog v-model="resetDialogVisible" title="重置密码" width="420px" class="reset-dialog">
      <el-form :model="resetForm" label-position="top" @submit.prevent>
        <el-form-item label="注册邮箱">
          <el-input v-model="resetForm.email" size="large" placeholder="请输入注册邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="resetForm.emailCode" size="large" placeholder="6位验证码" />
            <el-button :disabled="resetCountdown > 0" :loading="resetCodeLoading" @click="sendResetCode">
              {{ resetCountdown > 0 ? `${resetCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" type="password" size="large" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="resetForm.confirmPassword" type="password" size="large" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, resetPassword, sendResetPasswordCode } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const resetDialogVisible = ref(false)
const resetLoading = ref(false)
const resetCodeLoading = ref(false)
const resetCountdown = ref(0)
const form = reactive({ username: '', password: '' })
const resetForm = reactive({ email: '', emailCode: '', newPassword: '', confirmPassword: '' })

async function handleLogin() {
  loading.value = true
  try {
    const user = await login(form)
    userStore.setUser(user)
    ElMessage.success('登录成功')
    router.push(user.role === 'ADMIN' ? '/dashboard' : '/home')
  } finally {
    loading.value = false
  }
}

function guestLogin() {
  userStore.setUser({ id: null, username: 'guest', nickname: '游客', role: 'GUEST' })
  router.push('/home')
}

async function sendResetCode() {
  if (!resetForm.email.trim()) {
    ElMessage.warning('请先输入注册邮箱')
    return
  }
  resetCodeLoading.value = true
  try {
    await sendResetPasswordCode({ email: resetForm.email.trim() })
    ElMessage.success('验证码已发送，请查收邮箱')
    resetCountdown.value = 60
    const timer = setInterval(() => {
      resetCountdown.value -= 1
      if (resetCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } finally {
    resetCodeLoading.value = false
  }
}

async function handleResetPassword() {
  if (resetForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少需要 6 位')
    return
  }
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  resetLoading.value = true
  try {
    await resetPassword({
      email: resetForm.email.trim(),
      emailCode: resetForm.emailCode.trim(),
      newPassword: resetForm.newPassword
    })
    ElMessage.success('密码已重置，请使用新密码登录')
    resetDialogVisible.value = false
    form.username = resetForm.email.trim()
    resetForm.emailCode = ''
    resetForm.newPassword = ''
    resetForm.confirmPassword = ''
  } finally {
    resetLoading.value = false
  }
}
</script>

<style scoped src="./auth.css"></style>
<style scoped>
.auth-links {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.auth-links .el-button {
  width: auto;
}

@media (max-width: 700px) {
  .reset-dialog :deep(.el-dialog) {
    width: calc(100vw - 24px) !important;
  }
}
</style>
