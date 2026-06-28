# LJL Blog 部署指南

> 项目架构、本地开发、日常维护见 [HANDBOOK.md](./HANDBOOK.md)。

本文档覆盖 **前端静态资源 + Spring Boot 后端 + MySQL + Nginx** 的生产部署流程。

## 架构概览

```
用户浏览器
    │
    ▼
Nginx (443/80)
    ├── /          → 前端 dist/ 静态文件
    ├── /api/      → 反代 Spring Boot :8080
    └── /uploads/  → 直接托管 data/uploads/（或由 Spring Boot 提供）
```

## 1. 服务器准备

- Linux 服务器（推荐 Ubuntu 22.04+）
- Java 17+
- MySQL 8+
- Nginx
- Node.js 18+（仅构建前端时需要）

## 2. 数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE ljl_blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ljl_blog'@'localhost' IDENTIFIED BY '你的强密码';
GRANT ALL PRIVILEGES ON ljl_blog.* TO 'ljl_blog'@'localhost';
FLUSH PRIVILEGES;
```

导入表结构：

```bash
mysql -u ljl_blog -p ljl_blog < src/main/resources/schema.sql
```

### 从旧版数据库升级

若你已在运行早期版本（仅有 `articles` 无 `article_type` / 无 `projects` 表），请执行增量脚本，**不要**重新导入完整 `schema.sql`：

```bash
mysql -u ljl_blog -p ljl_blog < src/main/resources/schema-migration-v2.sql
```

脚本会依次：

| 步骤 | 内容 | 已执行时的表现 |
|------|------|----------------|
| 1 | `articles` 增加 `article_type` 列（默认 `blog`） | 报错 `Duplicate column`，可忽略 |
| 2 | 创建 `projects` 表及索引 | `CREATE TABLE IF NOT EXISTS`，可重复执行 |
| 3 | `slug` 改为 `UNIQUE(article_type, slug)` | 报错 `Unknown key 'slug'` 或重复索引，可忽略 |

**升级前建议备份：**

```bash
mysqldump -u ljl_blog -p ljl_blog > backup-before-v2.sql
```

升级后重启 Spring Boot，后台即可使用项目管理；Blog 与 Docs 可各自使用相同 slug（如 `intro`）。

## 3. 后端配置

```bash
cd ljl-blog-server
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

编辑 `application-local.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ljl_blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ljl_blog
    password: 你的强密码

app:
  jwt:
    secret: 至少32位的随机字符串-生产环境务必修改
  admin:
    username: admin
    password: 你的管理员密码
  storage:
    root: /opt/ljl-blog/data
```

**重要：** 生产环境务必修改 JWT secret 和管理员密码。

### 启动后端

```bash
# 打包
./mvnw -DskipTests package

# 运行（建议使用 systemd 托管）
java -jar target/ljl-blog-server-*.jar
```

确保 `data/` 目录可写，用于存放 Markdown 正文和上传图片。

## 4. 前端构建

```bash
cd ljl-blog-web
cp .env.example .env.production.local
```

编辑 `.env.production.local`：

```env
VITE_SITE_URL=https://your-domain.com
VITE_API_BASE_URL=/api
VITE_USE_MOCK=false
VITE_BASE_PATH=/
```

```bash
npm ci
npm run build
```

构建产物在 `dist/` 目录。

## 5. Nginx 配置

参考 `docs/nginx-site.conf`，将路径替换为你的实际路径：

- 前端：`/opt/ljl-blog/web/dist`
- 上传目录：`/opt/ljl-blog/data/uploads`

```bash
sudo cp docs/nginx-site.conf /etc/nginx/sites-available/ljl-blog
sudo ln -s /etc/nginx/sites-available/ljl-blog /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

### HTTPS（推荐）

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

## 6. 部署后检查清单

- [ ] 首页可访问，Blog / Album 列表有数据
- [ ] `/admin/login` 可登录
- [ ] 后台可新建文章、上传封面和正文图片
- [ ] 图片 URL `/uploads/images/xxx` 可正常访问
- [ ] 修改默认管理员密码（后台「账号设置」或首次登录后立即修改）
- [ ] MySQL 仅允许 localhost 连接
- [ ] `application-local.yml` 不提交到 Git

## 7. 日常维护

| 操作 | 说明 |
|------|------|
| 备份数据库 | `mysqldump ljl_blog > backup.sql` |
| 备份文件 | 复制 `data/content/` 和 `data/uploads/` |
| 更新前端 | `npm run build` 后替换 dist |
| 更新后端 | 重新打包 jar 并重启服务 |

## 8. systemd 服务示例（可选）

创建 `/etc/systemd/system/ljl-blog.service`：

```ini
[Unit]
Description=LJL Blog Server
After=network.target mysql.service

[Service]
User=www-data
WorkingDirectory=/opt/ljl-blog/server
ExecStart=/usr/bin/java -jar /opt/ljl-blog/server/ljl-blog-server.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable ljl-blog
sudo systemctl start ljl-blog
```

## 9. 图片上传成功但不显示（排查）

上传接口返回的 URL 形如 `/uploads/images/xxx.jpg`，浏览器会向**当前域名**请求该路径。本地开发时 Vite 会把 `/uploads` 代理到 Spring Boot，所以正常；生产环境需要 Nginx 或 Spring Boot 能访问到磁盘上的文件。

### 快速诊断

1. 上传一张图，在浏览器 Network 里看 `/api/upload/image` 响应，记下 `data.url`（如 `/uploads/images/abc.jpg`）
2. 新标签页直接打开 `https://你的域名/uploads/images/abc.jpg`
3. 根据结果判断：

| 现象 | 原因 | 处理 |
|------|------|------|
| 404 Not Found | Nginx 未配置 `/uploads/`，或 alias 路径不对 | 见下方「方案 A/B」 |
| 200 但内容是 HTML | 被 SPA 的 `try_files ... /index.html` 拦截 | 给 `/uploads/` 加 `^~` 前缀 |
| 403 Forbidden | 文件存在但 Nginx 无读权限 | `chmod` / 调整目录属主 |
| 200 且是图片 | URL 没问题，检查前端是否用了错误地址 | 少见，一般不用改代码 |

### 原因 1：Nginx 未托管 `/uploads/`（最常见）

若只配置了 `/api/` 反代，没有 `/uploads/`，图片请求会落到前端静态目录，自然找不到。

**方案 A — Nginx 直接读磁盘（性能更好）**

```nginx
location ^~ /uploads/ {
    alias /opt/ljl-blog/data/uploads/;   # 必须与 app.storage.root 下的 uploads 一致
    expires 30d;
}
```

注意 `alias` 路径：`app.storage.root=/opt/ljl-blog/data` 时，文件在 `/opt/ljl-blog/data/uploads/images/xxx.jpg`，alias 应指向 `/opt/ljl-blog/data/uploads/`。

**方案 B — 反代给 Spring Boot（更简单，推荐排查时用）**

```nginx
location ^~ /uploads/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
}
```

只需保证 Java 的 `app.storage.root` 正确，无需与 Nginx 对齐两份路径。

### 原因 2：Nginx 正则 location 抢走了 `.jpg` 请求

若配置里有类似：

```nginx
location ~* \.(jpg|jpeg|png|gif)$ {
    try_files $uri =404;
}
```

它会优先于普通 `location /uploads/`，导致去 `dist/` 目录找图片而 404。**务必使用 `location ^~ /uploads/`**（见 `docs/nginx-site.conf`）。

### 原因 3：Java 存盘路径与 Nginx 不一致

`app.storage.root: ./data` 是相对路径，目录取决于 jar **启动时的工作目录**。若文件写在 `/home/user/app/data/`，而 Nginx alias 指向 `/opt/ljl-blog/data/`，上传 API 仍返回成功，但 Nginx 读不到文件。

```bash
# 上传后在服务器上确认文件位置
find /opt /home -path "*/uploads/images/*.jpg" 2>/dev/null | tail -5
```

生产环境建议使用绝对路径：`app.storage.root: /opt/ljl-blog/data`。

### 是否需要改代码？

**通常不需要。** 上传逻辑和 URL 格式（`/uploads/images/xxx`）在本地已验证正确，问题是生产环境的静态资源路由。优先改 Nginx / `storage.root` 配置即可。
