# 接口清单

## 用户

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/logout` | 退出 |
| GET | `/api/user/profile` | 当前用户信息 |

## 六爻

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/liuyao/analyze` | 六爻分析 |

## 知识库

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/knowledge/list` | 规则列表 |
| POST | `/api/knowledge/create` | 新增规则 |
| PUT | `/api/knowledge/update` | 修改规则 |
| DELETE | `/api/knowledge/{id}` | 删除规则 |

## 历史记录

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/records` | 查询记录 |
| GET | `/api/records/{id}` | 记录详情 |
| DELETE | `/api/records/{id}` | 删除记录 |

