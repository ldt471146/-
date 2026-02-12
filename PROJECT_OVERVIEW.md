# 青少年编程平台项目扫描总览

## 1. 项目定位
- 前后端分离的青少年编程学习平台
- 后端：Spring Boot 3.5.10 + MyBatis Plus + MySQL + Redis + Spring Security + JWT
- 前端：Vue 3 + Vite + Element Plus + Axios + Vue Router

## 2. 仓库结构
- `back/`：后端服务
- `front/`：前端工程
- `online.sql`：数据库结构和示例数据（online 库）
- `AGENTS.md`：项目长期记忆与协作约定

## 3. 后端架构速览
- 入口：`back/src/main/java/com/example/back/BackApplication.java`
- 分层：`controller -> service -> mapper -> mysql`
- 统一返回：`ApiResponse`（`code/message/data`）
- 全局异常：`GlobalExceptionHandler`
- 鉴权：
  - `SecurityConfig` 放行 `/api/auth/**`、Swagger
  - 其余接口默认需要 JWT
  - `JwtAuthFilter` 从 `Authorization: Bearer <token>` 注入登录态
- 角色控制：
  - 教师接口：`@PreAuthorize("hasRole('TEACHER')")`
  - 管理员接口：`@PreAuthorize("hasRole('ADMIN')")`

## 4. 后端核心业务模块
- 认证与用户：注册、登录、邮箱验证码、当前用户、资料和密码修改
- 课程学习：课程列表、我的课程、课程详情、选课/退课、学习进度记录
- 题库系统：题目列表、提交判题、错题本、收藏、统计
- 编程题系统：题目详情、本地判题（C/C++/Python）、提交记录
- 考试系统：
  - 模拟考试：随机抽题、限时、交卷评分
  - 任务考试：教师发布考试任务，学生参与并查看提交记录
- 通知系统：通知列表、已读、全部已读、删除、未读计数
- 成长报告：总览统计、近7日学习与做题趋势
- AI 助手：`/api/assistant/chat`

## 5. 前端架构速览
- 入口：`front/src/main.js`
- 路由：`front/src/router/index.js`
  - 登录页：`/login`
  - 主框架：`AppLayout`
  - 学生端：Dashboard/Courses/Practice/CodePractice/Exams/Reports/Notices/Profile
  - 教师端：TeacherDashboard/TeacherQuestions/TeacherExams/TeacherCourseDetail
  - 管理端：AdminTeacherApply
- 网络层：`front/src/api/http.js`
  - 请求自动注入 token
  - 业务码非 0 统一抛错
  - 401 自动清 token 并跳转登录

## 6. 数据库与代码映射（online.sql）
### 6.1 已接入核心表
- 用户权限：`sys_user` `sys_role` `sys_user_role` `sys_teacher_apply`
- 通知：`sys_notice` `sys_notice_user`
- 课程学习：`edu_course` `edu_chapter` `edu_lesson` `edu_course_enroll` `edu_learn_record`
- 题库：`edu_question` `edu_question_option` `edu_question_record` `edu_wrong_question` `edu_question_favorite`
- 编程题：`edu_code_problem` `edu_code_testcase` `edu_code_submission`
- 考试：`edu_exam_task` `edu_exam_task_question` `edu_exam_submission`

### 6.2 SQL 中存在但当前未直接接入的表
- `edu_class`
- `edu_class_member`
- `edu_homework`
- `edu_homework_problem`
- `oj_problem`
- `oj_submission`

## 7. 运行与配置要点
- 后端配置：`back/src/main/resources/application.yml`
- 前端代理：`/api -> http://localhost:8080`（`front/vite.config.js`）
- 本地判题依赖：
  - Python：`python`
  - C：`gcc`
  - C++：`g++`
- Redis 用于邮箱验证码与模拟考试快照缓存（Redis不可用时模拟考试有本地内存降级）

## 8. 当前识别的主要风险
- `application.yml` 存在明文敏感信息（邮箱授权码、数据库密码、RabbitMQ、AI Key、JWT Key）
- `online.sql` 包含真实业务数据快照，不建议直接用于公开或生产环境
- JWT Key 偏短（虽有代码拉伸），建议改为高强度随机密钥并使用环境变量注入

## 9. 推荐下一步
1. 增加多环境配置（dev/test/prod）并迁移敏感项到环境变量
2. 为关键流程补自动化测试（认证、考试交卷、判题、教师审核）
3. 补充接口文档与数据库ER图，减少后续功能迭代沟通成本

