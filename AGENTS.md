# 项目长期记忆（请保持更新）

## 项目定位
- 青少年编程平台
- 前后端分离：后端 Spring Boot，前端 Vue + Vite

## 后端技术栈
- Spring Boot 3.5.10（Maven）
- MyBatis Plus
- MySQL
- Redis（无密码）
- RabbitMQ
- Spring Security + JWT
- Spring Mail（163 邮箱，使用授权码）
- SpringDoc

## 账号与安全
- 登录方式：邮箱登录
- 注册方式：邮箱 + 用户名 + 密码 + 邮箱验证码
- 默认角色：STUDENT
- JWT Key 在 `application.yml` 中

## 前端技术栈
- Vue 3 + Vite
- Element Plus
- Axios
- Vue Router

## 前端页面现状
- 登录/注册页（高科技风格）：
  - 邮箱登录
  - 注册字段顺序：邮箱 → 用户名 → 密码 → 确认密码 → 验证码
  - 输入失焦显示校验错误
- Dashboard：
  - 左侧导航可折叠
  - 右上角显示用户头像、用户名、退出登录

## 重要配置
- 163 SMTP：必须使用授权码
- Redis 无密码
- 前端 dev 代理 `/api -> http://localhost:8080`

## 已安装技能
- font-pairing-suggester
- frontend-design
