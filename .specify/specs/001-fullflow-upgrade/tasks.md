# Tasks: 全流程编程学习平台能力补齐

**Input**: `.specify/specs/001-fullflow-upgrade/spec.md`、`.specify/specs/001-fullflow-upgrade/plan.md`  
**Prerequisites**: plan.md, spec.md

## Format: `[ID] [P?] [Story] Description`

- `[P]` = 可并行（不同文件、低耦合）
- `[USx]` = 所属用户故事
- `[Done]` = 当前代码已实现
- `[Todo]` = 待开发
- `[Partial]` = 部分完成

## Phase 1: Baseline Audit & Hardening

- [x] T001 [Done] 盘点现有模块能力并产出基线文档 `PROJECT_OVERVIEW.md`
- [x] T002 [Done] 增加后端关键流程集成测试骨架 `back/src/test/java/...`
- [x] T003 [Done] 增加前端关键页面冒烟清单 `front/README.md` 或 `docs/`
- [x] T004 [Done] 增加 50 并发提交压测脚本与结果记录 `back/tests/perf/`
- [x] T005 [Done] 将敏感配置迁移到环境变量并更新说明 `back/src/main/resources/application.yml`

---

## Phase 2: User Story 1 - 学生“学-练-测”闭环 (P1)

**Goal**: 稳定实现学生端完整学习闭环并满足响应性能目标。  
**Independent Test**: 学生账号完成课程学习、题库提交、代码提交、考试交卷并查看结果。

- [x] T101 [Done] [US1] 课程列表/详情/选课能力 `back/.../CourseController.java`, `front/src/pages/Courses.vue`
- [x] T102 [Done] [US1] 学习记录与续学能力 `back/.../LearnController.java`, `front/src/pages/CourseDetail.vue`
- [x] T103 [Done] [US1] 题库提交/错题/收藏/统计 `back/.../QuestionController.java`, `front/src/pages/Practice.vue`
- [x] T104 [Done] [US1] 编程题提交与本地判题 `back/.../CodeProblemController.java`, `back/.../LocalJudgeClient.java`
- [x] T105 [Done] [US1] 模拟考试与任务考试 `back/.../ExamController.java`, `back/.../ExamTaskController.java`, `front/src/pages/Exams.vue`
- [x] T106 [Done] [US1] 补判题超时/编译失败/运行错误的统一错误码与前端展示增强 `back/.../CodeProblemServiceImpl.java`, `front/src/pages/CodePractice.vue`
- [x] T107 [Done] [US1] 增加“编辑器默认模板代码 + 输入用例引导提示” `front/src/pages/CodePractice.vue`
- [x] T108 [Done] [US1] 针对考试提交、判题提交增加回归测试骨架（Web层）`back/src/test/java/com/example/back/controller/`

---

## Phase 3: User Story 2 - 教师发布与统计能力 (P1)

**Goal**: 教师可完整发布内容并查看班级统计。  
**Independent Test**: 教师完成课程/题目/考试发布，并查看统计和导出结果。

- [x] T201 [Done] [US2] 教师课程/章节/课时管理 `back/.../TeacherController.java`, `front/src/pages/TeacherDashboard.vue`
- [x] T202 [Done] [US2] 教师题库/编程题管理 `back/.../TeacherServiceImpl.java`, `front/src/pages/TeacherQuestions.vue`
- [x] T203 [Done] [US2] 教师考试任务发布与删除 `back/.../TeacherExamTaskController.java`, `front/src/pages/TeacherExams.vue`
- [x] T204 [Done] [US2] 新增教师统计 API（学习时长、得分榜、通过率）`back/src/main/java/com/example/back/controller/TeacherController.java`
- [x] T205 [Done] [US2] 新增统计服务与 Mapper 查询 `back/src/main/java/com/example/back/service/impl/TeacherServiceImpl.java`, `back/src/main/java/com/example/back/mapper/TeacherStatsMapper.java`
- [x] T206 [Done] [US2] 新增导出 CSV 功能 `back/src/main/java/com/example/back/controller/TeacherController.java`
- [x] T207 [Done] [US2] 前端新增教师统计页与入口 `front/src/pages/TeacherStats.vue`, `front/src/router/index.js`

---

## Phase 4: User Story 3 - 管理员治理能力 (P2)

**Goal**: 管理员具备用户治理、课程审核、内容审核能力。  
**Independent Test**: 管理员完成账号状态变更、课程审核、违规内容处理并验证生效。

- [x] T301 [Done] [US3] 教师申请审核流程 `back/.../AdminTeacherApplyController.java`, `front/src/pages/AdminTeacherApply.vue`
- [ ] T302 [Todo] [US3] 新增用户管理实体字段（禁言状态、封禁原因）`back/src/main/java/com/example/back/entity/SysUser.java`, `online.sql`
- [x] T303 [Done] [US3] 新增管理员用户管理接口（列表、禁用、启用、角色调整）`back/src/main/java/com/example/back/controller/AdminUserController.java`
- [x] T304 [Done] [US3] 新增课程审核接口（基于课程上/下架状态）`back/src/main/java/com/example/back/controller/AdminCourseController.java`
- [x] T305 [Done] [US3] 前端新增管理员用户管理页 `front/src/pages/AdminUsers.vue`
- [x] T306 [Done] [US3] 前端新增管理员课程审核页 `front/src/pages/AdminCourses.vue`

---

## Phase 5: User Story 4 - 学习路径推荐 (P2)

**Goal**: 根据知识点依赖为学生提供阶梯式路径推荐。  
**Independent Test**: 配置依赖后，学生完成前置节点可解锁后续节点并展示推荐下一步。

- [x] T401 [Done] [US4] 新增知识点实体与关系实体 `back/src/main/java/com/example/back/entity/EduKnowledgePoint.java`, `EduKnowledgeDependency.java`
- [x] T402 [Done] [US4] 新增用户知识点进度实体 `back/src/main/java/com/example/back/entity/EduKnowledgeProgress.java`
- [x] T403 [Done] [US4] 新增知识点维护接口（教师）`back/src/main/java/com/example/back/controller/TeacherKnowledgeController.java`
- [x] T404 [Done] [US4] 新增路径推荐服务（依赖解锁规则）`back/src/main/java/com/example/back/service/impl/LearningPathServiceImpl.java`
- [x] T405 [Done] [US4] 新增学生路径查询接口 `back/src/main/java/com/example/back/controller/LearningPathController.java`
- [x] T406 [Done] [US4] 前端学习路径页与课程页入口 `front/src/pages/LearningPath.vue`, `front/src/pages/CourseDetail.vue`

---

## Phase 6: User Story 5 - 社区互动与治理 (P3)

**Goal**: 上线轻量社区问答和审核治理闭环。  
**Independent Test**: 完成发帖、回复、最佳答案、审核删除、禁言完整链路。

- [ ] T501 [Todo] [US5] 新增帖子/回复/审核记录实体 `back/src/main/java/com/example/back/entity/CommunityPost.java`, `CommunityReply.java`, `CommunityModeration.java`
- [ ] T502 [Todo] [US5] 新增社区 Mapper 与基础 CRUD `back/src/main/java/com/example/back/mapper/CommunityPostMapper.java`
- [ ] T503 [Todo] [US5] 新增社区服务（发帖、回复、最佳答案）`back/src/main/java/com/example/back/service/impl/CommunityServiceImpl.java`
- [ ] T504 [Todo] [US5] 新增社区控制器（学生/教师）`back/src/main/java/com/example/back/controller/CommunityController.java`
- [ ] T505 [Todo] [US5] 新增社区审核控制器（管理员）`back/src/main/java/com/example/back/controller/AdminCommunityController.java`
- [ ] T506 [Todo] [US5] 新增社区页面（列表、详情、发帖）`front/src/pages/Community.vue`, `front/src/pages/CommunityPostDetail.vue`
- [ ] T507 [Todo] [US5] 前端新增管理员内容审核页 `front/src/pages/AdminCommunity.vue`

---

## Phase 7: Cross-Cutting - 可视化、体验、合规

- [ ] T601 [Todo] [US1] 报告页集成 ECharts 折线图/雷达图/饼图 `front/src/pages/Reports.vue`
- [ ] T602 [Todo] [US2] 教师统计页接入 ECharts 柱状图/饼图 `front/src/pages/TeacherStats.vue`
- [ ] T603 [Todo] [US1] 注册补“学生家长手机号验证”流程（后端校验 + 前端表单）`back/.../AuthServiceImpl.java`, `front/src/pages/Login.vue`
- [ ] T604 [Todo] [US3] 内容审核关键操作写审计日志 `back/src/main/java/com/example/back/service/...`
- [ ] T605 [Todo] [US1] 增加关键接口性能监控点（判题、考试提交）`back/src/main/java/com/example/back/service/...`

---

## Dependencies & Execution Order

### Recommended Order
1. Phase 1 基线与测试
2. Phase 2 学生闭环增强（P1）
3. Phase 3 教师统计与导出（P1）
4. Phase 4 管理员治理（P2）
5. Phase 5 学习路径推荐（P2）
6. Phase 6 社区模块（P3）
7. Phase 7 跨模块收尾

### Parallel Opportunities
- T204/T205/T206/T207 可并行推进
- T401/T402/T403 可并行推进
- T501/T502 与前端 T506 可并行推进（先约定接口）
- ECharts 任务 T601/T602 可并行推进

## MVP Suggestion

如果你要最快提交阶段成果，建议 MVP 截止线设为：
- 完成 Phase 1 + Phase 2 + Phase 3 + T601  
这样可覆盖“学生闭环 + 教师发布 + 基础可视化 + 性能验证”。
