<template>
  <div class="page support-page">
    <div class="page-header support-header">
      <h1 class="page-title">支持开发</h1>
      <p class="page-desc">制作不易，个人开发需要承担服务器、AI 调用和资料整理成本。觉得好用的话，可以随缘打赏支持一下。</p>
    </div>

    <div class="panel support-panel">
      <section class="donate-card">
        <div class="donate-copy">
          <span>哲玄</span>
          <h2>自愿打赏</h2>
          <p>打赏完全自愿，不影响任何功能，也不兑换次数。你的支持会用于服务器、模型调用、古籍资料整理和后续功能维护。</p>
          <small>感谢每一次支持，也感谢你愿意把这个工具继续用下去。</small>
          <div class="contact-box">
            <strong>建议与反馈</strong>
            <span>QQ：1559087584</span>
          </div>
        </div>
        <div class="qr-box">
          <img src="/wechat-pay-qr.jpg" alt="自愿打赏收款码" />
          <strong>微信 / 支付宝收款码</strong>
          <span>请按实际页面显示付款</span>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.support-page {
  max-width: 980px;
  margin: 0 auto;
}

.support-header {
  text-align: center;
}

.support-panel {
  display: grid;
  gap: 16px;
  border-color: rgba(216, 198, 150, 0.72);
  background:
    linear-gradient(180deg, rgba(255, 252, 243, 0.98), rgba(248, 244, 230, 0.98)),
    repeating-linear-gradient(0deg, transparent 0 34px, rgba(91, 112, 95, 0.035) 34px 36px);
}

.donate-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 24px;
  align-items: center;
  padding: 20px;
  border: 1px solid #ead9b8;
  border-radius: 8px;
  background:
    radial-gradient(circle at 8% 0, rgba(47, 111, 94, 0.12), transparent 32%),
    #fffaf0;
}

.donate-copy span,
.donate-copy p,
.donate-copy small,
.qr-box span {
  color: #667085;
}

.donate-copy span {
  font-weight: 700;
  color: #806326;
}

.donate-copy h2 {
  margin: 8px 0 12px;
  color: #173f35;
  font-size: 28px;
}

.donate-copy p {
  margin: 0;
  line-height: 1.8;
}

.donate-copy small {
  display: block;
  margin-top: 14px;
  line-height: 1.6;
}

.contact-box {
  margin-top: 16px;
  display: inline-grid;
  gap: 4px;
  padding: 10px 12px;
  border: 1px solid rgba(176, 138, 60, 0.28);
  border-radius: 8px;
  background: rgba(255, 253, 246, 0.72);
}

.contact-box strong {
  color: #173f35;
  font-size: 14px;
}

.contact-box span {
  color: #806326;
  font-weight: 800;
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
  color: #173f35;
  font-size: 16px;
}

@media (max-width: 700px) {
  .support-page {
    padding-inline: 10px;
  }

  .support-header {
    text-align: left;
  }

  .donate-card {
    grid-template-columns: 1fr;
    padding: 14px;
  }

  .donate-copy h2 {
    font-size: 24px;
  }
}
</style>
