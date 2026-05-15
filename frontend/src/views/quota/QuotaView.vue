<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">自愿打赏</h1>
      <p class="page-desc">这个工具由个人持续开发维护，服务器、AI 调用和资料整理都会产生成本。如果它对你有帮助，欢迎随缘打赏支持。</p>
    </div>

    <div class="panel support-panel">
      <div class="quota-summary">
        <div>
          <span>可用次数</span>
          <strong>不限</strong>
        </div>
        <div>
          <span>累计使用</span>
          <strong>{{ quota?.totalUsed ?? '-' }}</strong>
        </div>
        <div>
          <span>免费记录</span>
          <strong>{{ quota?.freeUsed ?? 0 }}/{{ quota?.freeTotal ?? 5 }}</strong>
        </div>
      </div>

      <el-alert
        type="success"
        :closable="false"
        title="分析功能已取消次数限制，打赏完全自愿，不影响使用。"
      />

      <section class="donate-card">
        <div class="donate-copy">
          <span>支持哲玄</span>
          <h2>自愿打赏</h2>
          <p>这个工具由个人持续开发维护，服务器、AI 调用和资料整理都会产生成本。如果它对你有帮助，欢迎随缘打赏支持。</p>
        </div>
        <div class="qr-box">
          <img src="/wechat-pay.jpg" alt="自愿打赏收款码" />
          <strong>微信 / 支付宝收款码</strong>
          <span>请按实际页面显示付款，打赏不兑换次数</span>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getQuota } from '../../api/quota'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const quota = ref(null)

async function loadQuota() {
  if (!userStore.userId) return
  quota.value = await getQuota(userStore.userId)
}

onMounted(loadQuota)
</script>

<style scoped>
.support-panel {
  display: grid;
  gap: 16px;
}

.quota-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.quota-summary div {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 14px;
  background: #fff;
}

.quota-summary span,
.donate-copy span,
.donate-copy p,
.qr-box span {
  color: #667085;
}

.quota-summary strong {
  display: block;
  margin-top: 8px;
  font-size: 26px;
}

.donate-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 18px;
  align-items: center;
  padding: 18px;
  border: 1px solid #ead9b8;
  border-radius: 8px;
  background: #fffaf0;
}

.donate-copy h2 {
  margin: 8px 0;
  font-size: 24px;
}

.donate-copy p {
  margin: 0;
  line-height: 1.7;
}

.qr-box {
  display: grid;
  justify-items: center;
  gap: 8px;
  text-align: center;
}

.qr-box img {
  width: min(260px, 72vw);
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
}

.qr-box strong {
  font-size: 16px;
}

@media (max-width: 700px) {
  .quota-summary,
  .donate-card {
    grid-template-columns: 1fr;
  }

  .donate-card {
    padding: 14px;
  }
}
</style>
