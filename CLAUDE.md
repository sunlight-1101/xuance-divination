# 项目说明

这是一个八字、六爻、紫微斗数排盘与 AI 分析系统。

## 技术栈

- frontend: Vue 3 + Vite
- backend: Spring Boot + MyBatis Plus
- database: MySQL 8
- deploy: Docker Compose

## 重要命令

本地前端：
npm run dev

后端测试：
mvn -f backend/pom.xml test

生产部署：
docker compose -f docker-compose.prod.yml up -d --build

## 注意事项

- 不要提交 .env、API Key、SMTP 授权码、数据库密码、支付宝私钥。
- 修改前端后需要构建 frontend。
- 修改后端后需要测试并重新部署 backend。
- 数据库容器名是 zhexuan-mysql。
- 后端容器名是 zhexuan-backend。
- 前端容器名是 zhexuan-frontend。
