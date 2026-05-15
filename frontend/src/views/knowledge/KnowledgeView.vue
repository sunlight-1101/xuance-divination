<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">{{ isAdmin ? '知识库管理' : '知识库查询' }}</h1>
      <p class="page-desc">{{ isAdmin ? '规则库用于断事调用，典籍库用于按书籍和章节阅读原文资料。' : '普通账号可以查看和搜索知识库，规则维护仅管理员可用。' }}</p>
    </div>

    <div class="toolbar knowledge-toolbar">
      <el-select v-model="query.type" placeholder="术数类型" class="toolbar-control" clearable>
        <el-option label="六爻" value="LIUYAO" />
        <el-option label="八字" value="BAZI" />
        <el-option label="奇门" value="QIMEN" />
      </el-select>
      <el-input v-model="query.category" placeholder="分类" class="toolbar-control" clearable />
      <el-input v-model="query.keyword" placeholder="搜索标题、关键词、内容" class="toolbar-search" clearable />
      <el-button :icon="Search" @click="load">搜索</el-button>
      <el-button v-if="isAdmin" type="primary" :icon="Plus" @click="openCreate">新增规则</el-button>
      <el-button type="warning" :icon="BookOpen" @click="openClassics">典籍库</el-button>
    </div>

    <div class="panel">
      <el-table :data="rules" border>
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="category" label="分类" width="160" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="keywords" label="关键词" min-width="180" />
        <el-table-column prop="ruleContent" label="规则内容" min-width="280" show-overflow-tooltip />
        <el-table-column prop="example" label="示例" min-width="220" show-overflow-tooltip />
        <el-table-column v-if="isAdmin" prop="priority" label="优先级" width="90" />
        <el-table-column v-if="isAdmin" prop="enabled" label="启用" width="80">
          <template #default="{ row }">{{ row.enabled === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-if="isAdmin" v-model="dialogVisible" :title="editing.id ? '编辑规则' : '新增规则'" width="720px">
      <el-form :model="editing" label-position="top">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="类型">
              <el-select v-model="editing.type">
                <el-option label="六爻" value="LIUYAO" />
                <el-option label="八字" value="BAZI" />
                <el-option label="奇门" value="QIMEN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="分类"><el-input v-model="editing.category" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="优先级"><el-input-number v-model="editing.priority" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="标题"><el-input v-model="editing.title" /></el-form-item>
        <el-form-item label="关键词"><el-input v-model="editing.keywords" /></el-form-item>
        <el-form-item label="规则内容"><el-input v-model="editing.ruleContent" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="案例示例"><el-input v-model="editing.example" type="textarea" :rows="3" /></el-form-item>
        <el-checkbox v-model="enabledBool">启用</el-checkbox>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="classicsVisible" title="八字典籍库" size="92%" class="classic-drawer">
      <div class="classic-layout">
        <aside class="classic-list">
          <button
            v-for="book in classicBooks"
            :key="book.id"
            type="button"
            :class="{ active: selectedBook?.id === book.id }"
            @click="selectBook(book)"
          >
            <strong>{{ book.name }}</strong>
            <span>{{ book.focus || '典籍资料' }}</span>
          </button>
        </aside>

        <section class="classic-content">
          <div v-if="selectedBook" class="classic-head">
            <div>
              <h2>{{ selectedBook.name }}</h2>
              <p>{{ selectedBook.dynasty || '年代待考' }} · {{ selectedBook.author || '作者待考' }} · {{ selectedBook.focus }}</p>
            </div>
            <el-button size="small" :icon="NotebookTabs" @click="searchCurrentBook">看规则卡片</el-button>
          </div>

          <div v-if="selectedBook" class="classic-meta">
            <p>{{ selectedBook.description }}</p>
            <div class="classic-tags">
              <el-tag type="warning" effect="plain">{{ selectedBook.copyrightStatus || '来源待确认' }}</el-tag>
              <el-link v-if="selectedBook.sourceUrl" :href="selectedBook.sourceUrl" target="_blank" type="primary">查看来源</el-link>
            </div>
            <small>{{ selectedBook.sourceNote }}</small>
          </div>

          <div class="classic-search">
            <el-input v-model="chapterKeyword" placeholder="在当前典籍章节中搜索" clearable @keyup.enter="loadChapters" />
            <el-button :icon="Search" @click="loadChapters">搜索章节</el-button>
          </div>

          <el-empty v-if="!selectedBook" description="请选择一本典籍" />
          <div v-else-if="classicLoading" class="classic-loading">加载中...</div>
          <div v-else class="classic-rules">
            <article v-for="chapter in chapters" :key="chapter.id" class="classic-card">
              <div class="classic-card-head">
                <div>
                  <span>{{ chapter.volume || '章节' }}</span>
                  <h3>{{ chapter.title }}</h3>
                </div>
                <el-tag size="small" :type="chapter.contentStatus === 'FULL' ? 'success' : 'info'">
                  {{ statusLabel(chapter.contentStatus) }}
                </el-tag>
              </div>
              <p v-if="chapter.originalText" class="classic-original">{{ chapter.originalText }}</p>
              <p v-if="chapter.plainText">{{ chapter.plainText }}</p>
              <blockquote v-if="chapter.notes">{{ chapter.notes }}</blockquote>
              <el-link v-if="chapter.sourceUrl" :href="chapter.sourceUrl" target="_blank" type="primary">本章来源</el-link>
            </article>
          </div>
        </section>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { BookOpen, NotebookTabs, Plus, Search } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createKnowledge, deleteKnowledge, listKnowledge, updateKnowledge } from '../../api/knowledge'
import { listClassicBooks, listClassicChapters } from '../../api/classics'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const rules = ref([])
const dialogVisible = ref(false)
const classicsVisible = ref(false)
const classicLoading = ref(false)
const classicBooks = ref([])
const selectedBook = ref(null)
const chapters = ref([])
const chapterKeyword = ref('')
const query = reactive({ type: 'LIUYAO', category: '', keyword: '' })
const editing = reactive(emptyRule())
const enabledBool = computed({
  get: () => editing.enabled === 1,
  set: value => { editing.enabled = value ? 1 : 0 }
})

function emptyRule() {
  return { type: 'LIUYAO', category: '', title: '', keywords: '', ruleContent: '', example: '', priority: 0, enabled: 1 }
}

async function load() {
  rules.value = await listKnowledge(query)
}

function assignEditing(rule) {
  Object.keys(editing).forEach(key => delete editing[key])
  Object.assign(editing, rule)
}

function openCreate() {
  if (!isAdmin.value) return
  assignEditing(emptyRule())
  dialogVisible.value = true
}

function openEdit(row) {
  if (!isAdmin.value) return
  assignEditing({ ...row })
  dialogVisible.value = true
}

async function save() {
  if (!isAdmin.value) return
  if (editing.id) {
    await updateKnowledge(editing)
  } else {
    await createKnowledge(editing)
  }
  ElMessage.success('已保存')
  dialogVisible.value = false
  load()
}

async function remove(row) {
  if (!isAdmin.value) return
  await ElMessageBox.confirm(`确定删除《${row.title}》吗？`, '删除确认')
  await deleteKnowledge(row.id)
  ElMessage.success('已删除')
  load()
}

async function openClassics() {
  classicsVisible.value = true
  if (!classicBooks.value.length) {
    classicBooks.value = await listClassicBooks()
  }
  if (!selectedBook.value && classicBooks.value.length) {
    await selectBook(classicBooks.value[0])
  }
}

async function selectBook(book) {
  selectedBook.value = book
  chapterKeyword.value = ''
  await loadChapters()
}

async function loadChapters() {
  if (!selectedBook.value) return
  classicLoading.value = true
  try {
    chapters.value = await listClassicChapters({
      bookId: selectedBook.value.id,
      keyword: chapterKeyword.value
    })
  } finally {
    classicLoading.value = false
  }
}

async function searchCurrentBook() {
  if (!selectedBook.value) return
  query.type = 'BAZI'
  query.category = `典籍-${selectedBook.value.name}`
  query.keyword = ''
  classicsVisible.value = false
  await load()
}

function statusLabel(status) {
  if (status === 'FULL') return '全文'
  if (status === 'PARTIAL') return '节选'
  return '待校对'
}

onMounted(load)
</script>

<style scoped>
.knowledge-toolbar {
  align-items: center;
}

.toolbar-control {
  width: 160px;
}

.toolbar-search {
  width: 260px;
}

.classic-layout {
  display: grid;
  grid-template-columns: 230px 1fr;
  gap: 18px;
  min-height: 72vh;
}

.classic-list {
  display: grid;
  align-content: start;
  gap: 8px;
  border-right: 1px solid #e5e7eb;
  padding-right: 14px;
}

.classic-list button {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
}

.classic-list button.active {
  border-color: #c59a42;
  background: #fff8e8;
}

.classic-list strong,
.classic-list span {
  display: block;
}

.classic-list span {
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
}

.classic-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 12px;
}

.classic-head h2 {
  margin: 0 0 6px;
  font-size: 22px;
}

.classic-head p,
.classic-meta p {
  margin: 0;
  color: #667085;
  line-height: 1.6;
}

.classic-meta {
  border: 1px solid #ece2d0;
  background: #fffaf0;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.classic-tags {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 10px;
}

.classic-meta small {
  display: block;
  margin-top: 8px;
  color: #8a6b2d;
  line-height: 1.5;
}

.classic-search {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  margin-bottom: 12px;
}

.classic-rules {
  display: grid;
  gap: 12px;
}

.classic-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 14px;
  background: #fff;
}

.classic-card-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.classic-card-head span {
  color: #8a6b2d;
  font-size: 13px;
}

.classic-card h3 {
  margin: 3px 0 0;
  font-size: 16px;
}

.classic-card p {
  margin: 10px 0 0;
  line-height: 1.8;
  white-space: pre-wrap;
}

.classic-original {
  color: #2f3542;
  font-family: "Noto Serif SC", "Songti SC", serif;
}

.classic-card blockquote {
  margin: 12px 0 0;
  padding: 10px 12px;
  border-left: 3px solid #c59a42;
  background: #fffaf0;
  color: #5f4b20;
  line-height: 1.7;
}

.classic-loading {
  padding: 40px;
  text-align: center;
  color: #667085;
}

@media (max-width: 700px) {
  .knowledge-toolbar {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .toolbar-control,
  .toolbar-search {
    width: 100%;
  }

  .toolbar-search {
    grid-column: 1 / -1;
  }

  .knowledge-toolbar .el-button {
    width: 100%;
  }

  .classic-layout {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .classic-list {
    display: flex;
    overflow-x: auto;
    border-right: 0;
    border-bottom: 1px solid #e5e7eb;
    padding: 0 0 10px;
  }

  .classic-list button {
    min-width: 132px;
  }

  .classic-head,
  .classic-search {
    display: grid;
    grid-template-columns: 1fr;
  }

  .classic-card {
    padding: 12px;
  }
}
</style>
