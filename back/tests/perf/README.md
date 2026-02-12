# Performance Test Scripts

## 目标

验证代码评测核心链路在 50 并发下的性能目标：

- `p95 <= 3s`
- 成功率 `>= 99%`

## 脚本

- `k6-code-submit-50vus.js`: 学生登录后持续并发提交 `/api/code/submit`

## 依赖

1. 安装 k6
2. 后端服务已启动（默认 `http://localhost:8080`）
3. 数据库存在可登录学生账号与可提交编程题

## 运行命令

```bash
k6 run back/tests/perf/k6-code-submit-50vus.js
```

可选环境变量：

```bash
BASE_URL=http://localhost:8080
LOGIN_EMAIL=student@example.com
LOGIN_PASSWORD=123456
PROBLEM_ID=1
LANGUAGE_ID=71
SOURCE_CODE=print(1)
```

Windows CMD 示例：

```bat
set BASE_URL=http://localhost:8080
set LOGIN_EMAIL=student@example.com
set LOGIN_PASSWORD=123456
set PROBLEM_ID=1
set LANGUAGE_ID=71
set SOURCE_CODE=print(1)
k6 run back/tests/perf/k6-code-submit-50vus.js
```

## 结果记录

请将每次执行结果追加到 `back/tests/perf/results/`，建议文件名：

- `YYYY-MM-DD-code-submit-50vus.md`
