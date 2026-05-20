<template>
  <div class="account-page">
    <section class="account-hero">
      <div>
        <p>账号中心</p>
        <h1>资料与安全</h1>
        <span>{{ userStore.user?.email || userStore.user?.username || '未登录' }}</span>
      </div>
    </section>

    <section class="account-grid">
      <el-card class="account-card" shadow="never">
        <template #header>
          <div class="card-title">修改昵称</div>
        </template>
        <el-form label-position="top" @submit.prevent>
          <el-form-item label="当前昵称">
            <el-input v-model="nicknameForm.nickname" maxlength="30" show-word-limit placeholder="请输入昵称" />
          </el-form-item>
          <el-button type="primary" :loading="nicknameLoading" @click="submitNickname">保存昵称</el-button>
        </el-form>
      </el-card>

      <el-card class="account-card" shadow="never">
        <template #header>
          <div class="card-title">修改密码</div>
        </template>
        <el-form label-position="top" @submit.prevent>
          <el-form-item label="原密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password autocomplete="current-password" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-button type="primary" :loading="passwordLoading" @click="submitPassword">修改密码</el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, watchEffect } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword, updateNickname } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const nicknameLoading = ref(false)
const passwordLoading = ref(false)
const nicknameForm = reactive({ nickname: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

watchEffect(() => {
  nicknameForm.nickname = userStore.user?.nickname || ''
})

async function submitNickname() {
  if (!userStore.userId) {
    ElMessage.warning('请先登录账号')
    return
  }
  if (!nicknameForm.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  nicknameLoading.value = true
  try {
    const user = await updateNickname({ userId: userStore.userId, nickname: nicknameForm.nickname.trim() })
    userStore.setUser(user)
    ElMessage.success('昵称已更新')
  } finally {
    nicknameLoading.value = false
  }
}

async function submitPassword() {
  if (!userStore.userId) {
    ElMessage.warning('请先登录账号')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少需要 6 位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  passwordLoading.value = true
  try {
    await changePassword({
      userId: userStore.userId,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    ElMessage.success('密码已修改，下次登录请使用新密码')
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped>
.account-page {
  width: min(980px, 100%);
  margin: 0 auto;
}

.account-hero {
  min-height: 150px;
  display: flex;
  align-items: end;
  padding: 28px;
  border-radius: 8px;
  color: #f8ead2;
  background: linear-gradient(110deg, #05302b, #0b4e42);
  border: 1px solid rgba(205, 166, 94, 0.35);
}

.account-hero p {
  margin: 0 0 8px;
  color: #d6b978;
}

.account-hero h1 {
  margin: 0 0 8px;
  font-size: 32px;
  letter-spacing: 0;
}

.account-hero span {
  color: rgba(248, 234, 210, 0.82);
}

.account-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.account-card {
  border-radius: 8px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #143f38;
}

.account-card .el-button {
  width: 100%;
}

@media (max-width: 760px) {
  .account-page {
    padding: 0 10px 86px;
  }

  .account-hero {
    min-height: 126px;
    padding: 22px 18px;
  }

  .account-hero h1 {
    font-size: 26px;
  }

  .account-grid {
    grid-template-columns: 1fr;
  }
}
</style>
