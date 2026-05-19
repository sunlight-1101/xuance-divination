<template>
  <main class="home-design-page" aria-label="哲玄首页">
    <div class="home-design-frame">
      <picture>
        <source media="(max-width: 700px)" srcset="/images/home-design/home-mobile.png" />
        <img src="/images/home-design/home-desktop.png" alt="哲玄首页" />
      </picture>

      <button class="hotspot desktop-only nav-home" type="button" aria-label="首页" @click="$router.push('/home')" />
      <button class="hotspot desktop-only nav-tools" type="button" aria-label="工具" @click="$router.push('/bazi')" />
      <button class="hotspot desktop-only nav-favorites" type="button" aria-label="收藏" @click="$router.push('/records')" />
      <button class="hotspot desktop-only nav-user" type="button" aria-label="我的" @click="$router.push('/records')" />

      <button class="hotspot desktop-only desktop-bazi" type="button" aria-label="八字排盘" @click="$router.push('/bazi')" />
      <button class="hotspot desktop-only desktop-liuyao" type="button" aria-label="六爻解卦" @click="$router.push('/liuyao')" />
      <button class="hotspot desktop-only desktop-ziwei" type="button" aria-label="紫微斗数" @click="$router.push('/ziwei')" />
      <button class="hotspot desktop-only desktop-support" type="button" aria-label="支持开发" @click="$router.push('/quota')" />
      <button class="hotspot desktop-only desktop-records" type="button" aria-label="最近记录" @click="$router.push('/records')" />

      <button class="hotspot mobile-only mobile-profile" type="button" aria-label="我的" @click="$router.push('/records')" />
      <button class="hotspot mobile-only mobile-bazi" type="button" aria-label="八字排盘" @click="$router.push('/bazi')" />
      <button class="hotspot mobile-only mobile-liuyao" type="button" aria-label="六爻解卦" @click="$router.push('/liuyao')" />
      <button class="hotspot mobile-only mobile-ziwei" type="button" aria-label="紫微斗数" @click="$router.push('/ziwei')" />
      <button class="hotspot mobile-only mobile-support" type="button" aria-label="支持开发" @click="$router.push('/quota')" />
      <button class="hotspot mobile-only mobile-records" type="button" aria-label="最近记录" @click="$router.push('/records')" />
    </div>
  </main>
</template>

<style scoped>
.home-design-page {
  min-height: 100vh;
  background: #f8f1e2;
  overflow-x: hidden;
}

.home-design-frame {
  position: relative;
  width: 100%;
  margin: 0 auto;
  line-height: 0;
}

.home-design-frame img {
  width: 100%;
  height: auto;
  display: block;
  user-select: none;
  -webkit-user-drag: none;
}

.hotspot {
  position: absolute;
  z-index: 3;
  border: 0;
  border-radius: 10px;
  background: transparent;
  cursor: pointer;
  padding: 0;
}

.hotspot:focus-visible {
  outline: 2px solid rgba(244, 215, 145, 0.9);
  outline-offset: 2px;
}

.mobile-only {
  display: none;
}

.desktop-only {
  display: block;
}

.nav-home {
  left: 14.1%;
  top: 1.6%;
  width: 7.3%;
  height: 4.4%;
}

.nav-tools {
  left: 24.1%;
  top: 1.6%;
  width: 7.2%;
  height: 4.4%;
}

.nav-favorites {
  left: 33.6%;
  top: 1.6%;
  width: 7.4%;
  height: 4.4%;
}

.nav-user {
  right: 2.3%;
  top: 1.6%;
  width: 6.6%;
  height: 4.4%;
}

.desktop-bazi {
  left: 3.1%;
  top: 62.1%;
  width: 28.9%;
  height: 16.9%;
}

.desktop-liuyao {
  left: 33%;
  top: 62.1%;
  width: 28.9%;
  height: 16.9%;
}

.desktop-ziwei {
  left: 61.6%;
  top: 62.1%;
  width: 30.8%;
  height: 16.9%;
}

.desktop-support {
  left: 4.1%;
  top: 96.8%;
  width: 56%;
  height: 3.3%;
}

.desktop-records {
  left: 61.8%;
  top: 80.8%;
  width: 30.6%;
  height: 19.4%;
}

@media (max-width: 700px) {
  .home-design-page {
    background: #f6eddb;
  }

  .desktop-only {
    display: none;
  }

  .mobile-only {
    display: block;
  }

  .home-design-frame {
    max-width: 100vw;
  }

  .mobile-profile {
    left: 83%;
    top: 5.2%;
    width: 9%;
    height: 5.2%;
    border-radius: 50%;
  }

  .mobile-bazi {
    left: 13.3%;
    top: 50.7%;
    width: 72.8%;
    height: 8.2%;
  }

  .mobile-liuyao {
    left: 13.3%;
    top: 59.1%;
    width: 72.8%;
    height: 8.2%;
  }

  .mobile-ziwei {
    left: 13.3%;
    top: 67.5%;
    width: 72.8%;
    height: 8.2%;
  }

  .mobile-support {
    left: 14.8%;
    top: 86.5%;
    width: 69.8%;
    height: 2.6%;
  }

  .mobile-records {
    left: 13.3%;
    top: 89.5%;
    width: 72.8%;
    height: 9.5%;
  }
}
</style>
