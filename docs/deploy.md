# 部署到手机可访问

## 方案一：同 Wi-Fi 临时访问

适合开发测试，不需要上传服务器。

1. 电脑启动项目：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-dev.ps1
```

2. 查看电脑局域网 IP：

```powershell
ipconfig
```

找无线网卡或以太网里的 IPv4，例如 `192.168.1.23`。

3. 手机和电脑连同一个 Wi-Fi，手机访问：

```text
http://电脑IP:5173
```

例如：

```text
http://192.168.1.23:5173
```

如果手机打不开，通常是 Windows 防火墙拦截了 `5173` 或 `8080` 端口。

## 方案二：正式服务器部署

适合真正用手机长期访问。推荐买一台 Linux 云服务器，装 Docker 和 Docker Compose，然后上传整个项目。

服务器目录示例：

```text
/opt/zhexuan-divination
```

### 1. 上传项目

把整个 `六爻断事` 文件夹上传到服务器，比如：

```bash
scp -r ./六爻断事 root@服务器IP:/opt/zhexuan-divination
```

也可以用宝塔、FinalShell、Xftp、1Panel 的文件管理上传。

### 2. 配置环境变量

在服务器项目目录执行：

```bash
cp .env.example .env
```

然后编辑 `.env`：

```bash
nano .env
```

至少改：

```text
MYSQL_ROOT_PASSWORD=一个强密码
```

如果要接真实 AI，再改：

```text
AI_ENABLED=true
AI_API_KEY=你的APIKey
AI_MODEL=你的模型
```

### 3. 启动

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

启动后手机访问：

```text
http://服务器IP
```

有域名时，把域名 A 记录解析到服务器 IP，然后访问：

```text
http://你的域名
```

### 4. 查看日志

```bash
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f frontend
docker compose -f docker-compose.prod.yml logs -f mysql
```

### 5. 更新代码

上传新代码后执行：

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

### 6. 数据备份

备份 MySQL：

```bash
docker exec zhexuan-mysql mysqldump -uroot -p zhexuan_divination > zhexuan_backup.sql
```

恢复时：

```bash
docker exec -i zhexuan-mysql mysql -uroot -p zhexuan_divination < zhexuan_backup.sql
```

## 手机安装到桌面

当前前端已经有 PWA 基础配置。手机浏览器打开网站后：

- iPhone Safari：分享按钮 -> 添加到主屏幕
- Android Chrome：菜单 -> 添加到主屏幕

这样会像 App 一样从桌面打开。
