# Frontend (Vue 3 + Vite)

## 本地启动

```sh
npm install
npm run dev
```

- 默认地址：`http://localhost:5173`
- 开发代理：`/api -> http://localhost:8080`

## 构建

```sh
npm run build
```

## 关键页面冒烟清单（T003）

### 一、基础与鉴权

- [ ] 访问 `/login`，登录页正常渲染（邮箱、密码、验证码相关交互可见）
- [ ] 未登录直接访问 `/dashboard` 自动跳转到 `/login`
- [ ] 已登录访问 `/login` 自动跳转到 `/dashboard`
- [ ] 退出登录后再次访问任一受保护路由被拦截到 `/login`

### 二、学生端闭环

- [ ] `/courses` 课程列表正常加载，筛选与进入详情可用
- [ ] `/courses/:id` 课时学习、续学定位、学习路径入口可见
- [ ] `/practice` 题库列表、作答提交、结果反馈可用
- [ ] `/code-practice` 代码编辑、提交、结果与错误提示可见
- [ ] `/learning-path` 显示已学/待学/推荐下一步信息
- [ ] `/exams` 可创建模拟考试并提交，结果页显示得分
- [ ] `/reports` 学习报告页面可正常打开（图表容器无报错）
- [ ] `/notices` 与 `/profile` 页面可正常加载

### 三、教师端能力

- [ ] `/teacher` 教师工作台可访问
- [ ] `/teacher/questions` 题目管理页可新增/编辑/删除或查看
- [ ] `/teacher/exams` 考试任务发布页可正常使用
- [ ] `/teacher/stats` 统计页数据与导出按钮可用
- [ ] `/teacher/courses/:id` 课程详情（章节/课时管理）可用

### 四、管理员端治理

- [ ] `/admin/teacher-apply` 教师申请审核页可用
- [ ] `/admin/users` 用户管理页可分页、状态变更、角色调整
- [ ] `/admin/courses` 课程审核页可分页与审核操作

### 五、前端质量检查

- [ ] 所有页面在 1366x768 和 iPhone 12 尺寸下无明显布局错位
- [ ] 控制台无阻塞级报错（忽略浏览器扩展噪声）
- [ ] 主要按钮具备加载态/错误提示，不出现“点击无反馈”

## 建议执行顺序

1. 先跑“基础与鉴权”
2. 再按“学生 -> 教师 -> 管理员”顺序验证
3. 最后统一做移动端与控制台检查

## 最近更新（2026-02-12）

- 社区列表与帖子详情新增时间展示：
  - 帖子发布时间
  - 最后活跃时间
  - 回复发布时间
- 左侧导航栏视觉增强：
  - 渐变玻璃背景
  - 选中态高亮与位移动效
  - 菜单层级间距优化
- 社区相关页面：
  - `front/src/pages/Community.vue`
  - `front/src/pages/CommunityPostDetail.vue`
  - `front/src/layouts/AppLayout.vue`
