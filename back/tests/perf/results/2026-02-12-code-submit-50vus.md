# Code Submit Perf Record (50 VUs)

- Date: 2026-02-12
- Script: `back/tests/perf/k6-code-submit-50vus.js`
- Env:
  - Backend: `http://localhost:8080`
  - DB: MySQL (local)
  - Redis: local

## Run Command

```bash
k6 run back/tests/perf/k6-code-submit-50vus.js
```

## Result Summary

- Status: NOT_EXECUTED_IN_AGENT_ENV
- Reason: 当前代理环境缺少 k6，未能执行实际压测
- Target:
  - p95 <= 3000ms
  - success rate >= 99%

## Raw Output

```
<pending local run output>
```
