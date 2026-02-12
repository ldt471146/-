# Back Service Configuration

## 环境变量配置（T005）

后端敏感配置已迁移到环境变量，默认配置位于：

- `back/src/main/resources/application.yml`
- 示例变量清单：`back/.env.example`

## 启动前最少需要配置

1. 数据库：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`
2. JWT：`JWT_KEY`
3. 邮箱发送验证码：`MAIL_USERNAME`、`MAIL_PASSWORD`（163 邮箱授权码）
4. 邮件发件人：`MAIL_FROM`（可不填，默认等于 `MAIL_USERNAME`）

## 可选配置

- Redis：`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`
- RabbitMQ：`RABBITMQ_*`
- MinIO：`MINIO_*`
- 天气 API：`WEATHER_*`
- AI：`AI_*`

## Windows CMD 示例

```bat
set DB_URL=jdbc:mysql://localhost:3306/online?useUnicode=true^&characterEncoding=utf-8^&serverTimezone=Asia/Shanghai
set DB_USERNAME=root
set DB_PASSWORD=123456
set JWT_KEY=replace-with-strong-key
set MAIL_USERNAME=your_163_mail@163.com
set MAIL_PASSWORD=your_mail_auth_code
```
