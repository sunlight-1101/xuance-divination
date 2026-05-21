# 哲玄

哲玄是一套传统易学排盘与分析工具，支持八字、六爻、紫微斗数排盘、知识库检索、AI 结构化分析、历史记录与多人生辰资料保存。

> 说明：本项目用于传统文化学习、排盘辅助与个人记录参考，不作为医疗、投资、法律、婚恋等现实决策依据。

## 功能概览

- 八字排盘：根据出生日期、出生时间、出生地生成四柱，支持真太阳时、十神、藏干、自坐、空亡、纳音、神煞、大运与流年信息。
- 八字分析：结合问题、四柱、大运流年、知识库与典籍片段生成结构化报告。
- 八字合盘：支持保存并选择多人资料，进行关系合盘分析。
- 六爻起卦：按三枚铜钱摇卦流程逐爻生成，本卦、变卦、六亲、六神、世应等自动排出。
- 六爻分析：结合问题、卦象、动爻、日辰月建、空亡和知识库生成断事报告。
- 紫微斗数：支持基础命盘排盘、十二宫展示与紫微分析报告。
- 万年历：首页展示今日干支、农历、宜忌，并支持展开查看日历。
- 知识库：支持八字、六爻、紫微相关规则、案例、典籍资料的管理、搜索与引用。
- 历史记录：用户点击开始分析后会立即生成记录，显示“正在分析中...”，完成后自动更新为报告。
- 用户系统：邮箱注册、登录、忘记密码、修改昵称和密码。
- 移动端适配：以手机使用为优先，支持浏览器访问和添加到主屏幕。

## 技术栈

- 前端：Vue 3、Vite、Element Plus、Pinia、Vue Router
- 后端：Spring Boot 2.7、MyBatis Plus
- 数据库：MySQL 8
- AI：兼容 Chat Completions 格式的模型接口
- 部署：Docker Compose / Nginx / ECS

## 目录结构

```text
.
├── backend/                 # Spring Boot 后端
├── frontend/                # Vue 前端
├── docs/                    # 数据库结构、知识库种子数据和文档
├── scripts/                 # 本地开发脚本
├── docker-compose.prod.yml  # 生产部署编排
├── .env.example             # 环境变量示例
└── README.md
```

## 本地开发

准备环境：

- JDK 8 或 17
- Maven 3.8+
- Node.js 18+
- MySQL 8+

初始化数据库：

```bash
mysql -uroot -p < docs/schema.sql
```

启动后端：

```bash
cd backend
mvn spring-boot:run
```

启动前端：

```bash
cd frontend
npm install
npm run dev
```

访问：

```text
http://localhost:5173
```

也可以使用脚本启动开发环境：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-dev.ps1
```

## Docker 部署

复制环境变量示例：

```bash
cp .env.example .env
```

按需修改 `.env`：

```env
APP_PORT=80
MYSQL_ROOT_PASSWORD=change-this-password

AI_ENABLED=false
AI_BASE_URL=https://api.deepseek.com/chat/completions
AI_API_KEY=
AI_MODEL=deepseek-v4-pro
AI_TEMPERATURE=0.2
AI_MAX_TOKENS=1800
```

启动：

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

查看状态：

```bash
docker ps
docker logs -f xuance-backend
docker logs -f xuance-frontend
```

## 环境变量

常用配置：

- `MYSQL_ROOT_PASSWORD`：MySQL root 密码
- `AI_ENABLED`：是否启用真实 AI
- `AI_BASE_URL`：AI 接口地址
- `AI_API_KEY`：AI API Key
- `AI_MODEL`：模型名称
- `AI_MAX_TOKENS`：最大输出 token
- `MAIL_ENABLED`：是否启用邮箱验证码
- `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD`：SMTP 配置
- `ALMANAC_ENABLED`：是否启用万年历接口
- `ALMANAC_ID` / `ALMANAC_KEY`：万年历接口配置

请不要把真实的 API Key、邮箱授权码、数据库密码提交到 GitHub。

## 数据库说明

主要表包括：

- `user`：用户信息
- `user_profile`：用户保存的出生资料
- `divination_record`：八字、六爻、紫微等分析记录
- `knowledge_rule`：知识库规则
- `classic_book` / `classic_chapter`：典籍与章节内容
- `ai_call_log`：AI 调用日志

`divination_record.status` 用于标记分析状态：

- `PROCESSING`：正在分析中
- `DONE`：已完成
- `FAILED`：分析失败

## 安全建议

- 仓库公开前请确认没有提交 `.env`、API Key、SMTP 授权码、数据库密码和支付私钥。
- 如果密钥曾经公开过，应立即去对应平台重置密钥。
- 生产环境建议开启 HTTPS，并限制数据库端口只允许容器内部访问。
- 网站对外宣传建议使用“传统文化排盘工具、学习参考”等表述，避免承诺预测结果或现实收益。

## 免责声明

本项目输出内容由排盘规则、知识库和 AI 共同生成，仅供传统文化学习、娱乐参考和个人记录使用。请勿将其作为医疗、法律、投资、婚恋、就业等重大事项的唯一决策依据。
