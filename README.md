# 玄策术数分析系统

从零开发的八字、六爻、奇门遁甲分析软件。第一版 MVP 优先跑通六爻：

```text
摇卦生成六爻 -> 知识库检索 -> AI 结构化报告 -> 历史记录
```

## 当前已完成

- Spring Boot 后端骨架
- Vue 3 前端骨架
- MySQL 表结构文档
- 用户注册登录接口
- 知识库规则 CRUD
- 三枚铜钱法摇卦生成六爻
- 自动装卦：本卦、变卦、纳甲地支、五行、六亲、世应、六神
- 六爻分析接口
- 分析报告展示引用规则
- 历史记录保存并还原当次引用规则
- 支持复制报告和导出 Markdown
- 起卦体验：自动填时间、单爻重摇、本卦/变卦图形展示
- 手机优先：三步起卦流程、移动端卡片式摇卦、PWA manifest
- 移动端底部导航
- 游客体验模式
- 起卦日干自动计算，不再需要手填日干
- 八字简要信息可选录入，并随六爻问题一起进入分析
- 用户档案：出生信息可保存，后续起卦自动带入
- 手机端简化：月建、日辰、旬空默认收进高级信息，摇满自动定位到卦象
- 八字分析第一版：手动/半自动四柱输入，结合八字知识库与 AIService 生成报告
- 八字自动四柱实用版：根据出生日期和时间自动填写年柱、月柱、日柱、时柱、日主，临近节气日可手动校正
- 八字知识库扩展：补充五行流通、通根透干、合冲刑害、十神组合、婚恋事业财运、应期和边界规则
- 八字知识库二次扩展：补充十神细分、四柱宫位、格局、神煞辅助、复合正缘、升职辞职、买房合同合作等断事情景
- 六爻与八字知识库深度扩展：补充六爻断事流程、动变细断、反吟伏吟、合冲应期，以及八字细断、场景判断和可检索案例
- 六爻与八字案例库进阶扩展：补充感情、复合、求职、升职、辞职、财运、投资、合同、失物、考试、健康边界、买房、合伙等可检索案例
- 经典断法模型案例：补充六爻用神伏藏、六合六冲、独发多动、入墓、回头生克，以及八字月令提纲、身强身弱、调候、食伤生财、官印相生、杀印相生、伤官见官等经典模型
- 知识库检索增强：分析时若匹配到案例，会优先保留相关案例供 Agent 类比参考
- 历史记录查询与删除
- 本地 mock AIService，占位真实大模型调用

## 启动准备

本机需要安装：

- JDK 8 或 17
- Maven 3.8+
- Node.js 和 npm
- MySQL 8.0

## 数据库

创建数据库并执行：

```bash
docs/schema.sql
```

如 MySQL 用户名密码不是 `root/root`，修改：

```text
backend/src/main/resources/application.yml
```

当前开发机已安装 MySQL 8.4.8，开发数据目录为：

```text
C:/Users/m1913/mysql-data/xuance-mysql-8.4
```

开发配置文件为：

```text
C:/Users/m1913/mysql-data/xuance-my.ini
```

root 密码：`root`

## 后端启动

```bash
cd backend
mvn spring-boot:run
```

后端默认端口：`8080`

## AI 配置

默认使用本地 mock 结果。要启用真实大模型，修改：

```text
backend/src/main/resources/application.yml
```

配置：

```yaml
ai:
  enabled: true
  base-url: https://api.openai.com/v1/chat/completions
  api-key: 你的 API Key
  model: gpt-4o-mini
  temperature: 0.2
```

也可以换成其他兼容 Chat Completions 的服务地址。

## 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端默认端口：`5173`

## 一键启动开发环境

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-dev.ps1
```

启动后访问：

```text
http://localhost:5173
```
