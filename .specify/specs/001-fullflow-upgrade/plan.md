# Implementation Plan: 全流程编程学习平台能力补齐

**Branch**: `001-fullflow-upgrade` | **Date**: 2026-02-12 | **Spec**: `.specify/specs/001-fullflow-upgrade/spec.md`  
**Input**: Feature specification from `.specify/specs/001-fullflow-upgrade/spec.md`

## Summary

在现有“课程 + 题库 + 编程判题 + 考试 + 基础权限”能力基础上，补齐总需求缺口：
1) 学习路径推荐  
2) 社区互动与治理  
3) 管理员完整治理能力  
4) 教师班级统计与导出  
5) ECharts 可视化升级  
并保持判题与核心流程稳定在性能目标内。

## Technical Context

**Language/Version**: Java 17, Spring Boot 3.5.10, Vue 3, Node 20+  
**Primary Dependencies**: Spring Security, MyBatis Plus, JWT, Redis, Element Plus, Axios, Vue Router  
**Storage**: MySQL 8, Redis  
**Testing**: Spring Boot Test（当前基础薄弱，需补集成测试与压测脚本）  
**Target Platform**: Web（前后端分离）  
**Project Type**: Web application (`back/` + `front/`)  
**Performance Goals**: 判题 p95 <= 3s；50 并发提交可用  
**Constraints**: 权限隔离严格；内容审核可追踪；数据一致性  
**Scale/Scope**: 学生/教师/管理员三角色，核心模块全覆盖

## Constitution Check

`.specify/memory/constitution.md` 当前仍为占位模板，暂无可执行硬性门禁。  
本计划以“先 MVP、后增强”的增量交付原则执行。

## Current State Mapping

### 已具备能力
- 认证鉴权、角色权限、课程学习、题库与编程题、考试、通知、报告（基础）

### 关键缺口
- 社区互动模块全缺
- 学习路径推荐全缺
- 管理员用户治理、课程审核缺
- 教师班级与导出缺
- ECharts 可视化缺
- 注册家长手机号验证缺

## Project Structure

### Documentation (this feature)

```text
.specify/specs/001-fullflow-upgrade/
├── plan.md
├── spec.md
└── tasks.md
```

### Source Code (repository root)

```text
back/
├── src/main/java/com/example/back/
│   ├── controller/
│   ├── service/
│   ├── service/impl/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   └── vo/
└── src/main/resources/

front/
└── src/
    ├── api/
    ├── pages/
    ├── layouts/
    ├── components/
    └── router/
```

**Structure Decision**: 采用现有单仓前后端分离结构，不拆分新服务，优先复用当前鉴权、Mapper、页面框架。

## Delivery Strategy

### Stage A (MVP hardening, P1)
- 巩固学生“学-练-测”闭环质量与性能
- 巩固教师内容发布与考试任务
- 补基础测试与压测脚本

### Stage B (Core gap closure, P2)
- 上线学习路径推荐
- 上线管理员用户治理与课程审核
- 上线教师班级统计与导出

### Stage C (Enhancement, P3)
- 上线社区互动与治理
- 升级 ECharts 可视化
- 优化青少年引导与易用性细节

## Risks & Mitigations

- 风险：社区与审核涉及新数据模型，改动面大  
  方案：先最小模型（帖子/回复/审核记录）再扩展
- 风险：判题与考试并发下性能波动  
  方案：补压测基线、增加提交限流与日志埋点
- 风险：权限遗漏导致越权  
  方案：控制器统一 `@PreAuthorize` + 集成测试覆盖

