<template>
  <div class="page">
    <div class="page-header">
      <div class="page-header-top">
        <h1 class="page-title">用户管理</h1>
        <el-button @click="$router.push('/admin')">← 返回首页</el-button>
      </div>
      <p class="page-desc">查看所有注册用户信息</p>
    </div>

    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索邮箱或昵称" style="width: 260px" clearable @keyup.enter="search" />
      <el-button @click="search">搜索</el-button>
      <el-button v-if="query.keyword" @click="clearSearch">清空</el-button>
    </div>

    <div class="desktop-records">
      <div class="table-wrap">
        <el-table :data="users" stripe border v-loading="loading" empty-text="暂无用户数据">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="email" label="邮箱" min-width="180" />
          <el-table-column prop="nickname" label="昵称" width="120" />
          <el-table-column prop="role" label="角色" width="80">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
                {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="70">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="gender" label="性别" width="70">
            <template #default="{ row }">
              {{ row.gender === 'MALE' ? '男' : row.gender === 'FEMALE' ? '女' : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="birthDate" label="出生日期" width="110" />
          <el-table-column prop="birthPlace" label="出生地" width="130" />
          <el-table-column prop="createTime" label="注册时间" width="170" />
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <div class="mobile-records">
      <el-empty v-if="!users.length" description="暂无用户数据" />
      <div v-for="row in users" :key="row.id" class="record-card">
        <div class="record-card-head">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
          </el-tag>
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
          <span>{{ formatTime(row.createTime) }}</span>
        </div>
        <strong>{{ row.email }}</strong>
        <div class="card-meta">
          <span>昵称：{{ row.nickname || '-' }}</span>
          <span>性别：{{ row.gender === 'MALE' ? '男' : row.gender === 'FEMALE' ? '女' : '-' }}</span>
          <span>{{ row.birthDate || '未设置出生日期' }}</span>
          <span>{{ row.birthPlace || '' }}</span>
        </div>
      </div>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :total="total"
          :page-size="pageSize"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const users = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const windowWidth = ref(window.innerWidth)
const query = reactive({ keyword: '' })

async function load() {
  if (!userStore.userId) return
  loading.value = true
  try {
    const result = await listUsers({
      keyword: query.keyword || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    users.value = result.list || []
    total.value = result.total || 0
  } catch {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

function search() {
  currentPage.value = 1
  load()
}

function clearSearch() {
  query.keyword = ''
  currentPage.value = 1
  load()
}

function handlePageChange(page) {
  currentPage.value = page
  load()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  load()
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function handleResize() { windowWidth.value = window.innerWidth }

onMounted(() => {
  window.addEventListener('resize', handleResize)
  load()
})

onBeforeUnmount(() => window.removeEventListener('resize', handleResize))
</script>

<style scoped>
.page-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.table-wrap {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.mobile-records {
  display: none;
}

@media (max-width: 700px) {
  .toolbar {
    flex-direction: column;
  }

  .toolbar .el-input,
  .toolbar .el-button {
    width: 100% !important;
  }

  .desktop-records {
    display: none;
  }

  .mobile-records {
    display: grid;
    gap: 10px;
  }

  .record-card {
    min-width: 0;
    padding: 12px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
  }

  .record-card-head {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    color: #667085;
    font-size: 12px;
    flex-wrap: wrap;
  }

  .record-card-head span {
    margin-left: auto;
  }

  .record-card strong {
    display: block;
    color: #101828;
    font-size: 14px;
    line-height: 1.5;
    overflow-wrap: anywhere;
    word-break: break-all;
  }

  .card-meta {
    margin-top: 8px;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 4px 12px;
    color: #667085;
    font-size: 12px;
  }
}
</style>
