# Vercel 部署说明

## 当前线上地址

- 生产地址：https://xuance-divination.vercel.app
- 本次部署地址：https://xuance-divination-d149pu8i6-sunlights-projects-40eadcaf.vercel.app
- Vercel 项目：sunlights-projects-40eadcaf/xuance-divination

## 当前部署范围

这次部署的是 Vue 前端。Vercel 可以长期托管这个前端页面，所以手机访问不需要再启动本地电脑。

当前 Spring Boot 后端和 MySQL 仍然是本地/服务器部署形态，不能直接作为常驻服务放在 Vercel 上运行。要让六爻、八字分析和历史记录在手机上完整可用，还需要把后端与数据库也部署到云端。

## 推荐后续方案

最少改动方案：

1. 后端和 MySQL 用 `docker-compose.prod.yml` 部署到一台云服务器。
2. 给后端配置域名或公网 IP，例如 `https://api.example.com/api`。
3. 在 Vercel 项目里添加环境变量：

```text
VITE_API_BASE_URL=https://api.example.com/api
```

4. 重新部署 Vercel 前端。

本地开发不需要改环境变量，默认仍然请求 `/api`，由 Vite 代理到 `http://localhost:8080`。

## Vercel 本地命令

```powershell
npx vercel link --yes --project xuance-divination
npx vercel deploy --yes
```

如需重新发布生产版本：

```powershell
npx vercel deploy --prod
```
