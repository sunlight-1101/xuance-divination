# 玄策术数分析系统开发说明

## MVP 目标

第一版先做“摇卦生成六爻 + 知识库规则 + AI 结构化解读 + 历史记录”。八字和奇门遁甲暂不做自动排盘算法。

## 第一阶段范围

1. 后端 Spring Boot 2.7 项目骨架
2. 前端 Vue 3 + Element Plus 项目骨架
3. 用户注册、登录、退出
4. 三枚铜钱法摇卦与自动装卦
5. 六爻分析接口
5. 知识库规则 CRUD
6. 测算历史记录查询与删除
7. AI 调用服务占位，后续接入真实大模型 API

## 目录

```text
backend/   Spring Boot 后端
frontend/  Vue 3 前端
docs/      数据库、接口、Prompt 文档
```

## 推荐开发顺序

1. 安装 JDK 8 或 17、Maven、Node.js 和 npm
2. 创建 MySQL 数据库并执行 `docs/schema.sql`
3. 配置 `backend/src/main/resources/application.yml`
4. 启动后端
5. 安装前端依赖并启动前端
6. 配置真实大模型 API，替换本地 mock 报告
7. 先跑通六爻模块，再扩展八字和奇门
