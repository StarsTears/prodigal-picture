# 图片清理日志增强 — 设计说明

**日期**: 2026-06-16
**状态**: 已批准

## 背景

`PictureCleanupScheduler` 每天凌晨 2 点清理一周前软删除的图片（先删 DB 再删 COS）。
当前实现存在以下不足：

1. 日志只有 "Found N pictures to delete"，缺少每张图的 trace 信息
2. `log.error("Failed to delete picture: ...")` 无条件执行，成功时也打印
3. DB 删除与 COS 删除在同一事务中，COS 失败会回滚 DB
4. COS 批量删除未做分批，超过 SDK 1000 key 限制时会报错

## 目标

- 结构化记录每次清理的完整链路（图片 ID、COS key、成功/失败计数）
- 清理日志持久化到独立文件，按天滚动，保留 30 天
- DB 删除与 COS 删除事务分离，COS 失败不阻塞清理流程
- COS 批量删除按 500 key/批拆分

## 流程

```
@Scheduled(cron = "0 0 2 * * ?")
    ↓
查询 is_delete=1 且 edit_time < 一周前 的图片
    ↓
按 spaceId 分组
    ↓
对每组:
  ① 记录 SPACE_START 日志（spaceId, pictureIds）
  ② DB 物理删除 → 提交独立事务
  ③ 收集该组 COS key（url + originUrl + thumbnailUrl）
  ④ 按 500 个/批 → CosManager.deleteObjectsBatch
  ⑤ 每批记录 COS_DELETED / COS_FAILED
  ⑥ 记录 SPACE_DONE 汇总
    ↓
记录 CLEANUP_DONE 全局汇总（总数、总耗时）
```

## 事务边界

**改前**：DB 删除和 COS 删除在同一个 `transactionTemplate.execute()` 中，COS 失败 → DB 回滚

**改后**：DB 删除独立事务先提交；COS 删除在事务外执行。这样可以保证：
- DB 清理过的记录不会因为 COS 失败而残留
- COS 删除失败记录到日志，下次运行时对应的 DB 记录已不存在，COS 会成为孤儿文件（可接受，概率极低）

## 日志格式

文件：`logs/picture-cleanup.log`
格式：每行一个 JSON 对象
滚动：按天，保留 30 天

事件类型：

| type | 含义 | 关键字段 |
|------|------|----------|
| CLEANUP_START | 清理开始 | totalPictures |
| SPACE_START | 空间开始 | spaceId, pictureCount, pictureIds |
| DB_DELETED | DB 删除完成 | spaceId, deletedRows |
| DB_DELETE_FAILED | DB 删除失败 | spaceId, error |
| COS_DELETED | COS 批次成功 | spaceId, batchIndex, keys, successCount, failCount |
| COS_FAILED | COS 批次失败 | spaceId, keys, error |
| SPACE_DONE | 空间清理完成 | spaceId, dbDeleted, cosDeleted, cosFailed |
| CLEANUP_DONE | 清理结束 | totalDbDeleted, totalCosDeleted, totalCosFailed, durationMs |

## 改动清单

| 文件 | 改动 |
|------|------|
| `PictureCleanupScheduler.java` | 拆分 DB/COS 事务；各级结构化日志；异常不中断整体流程 |
| `CosManager.java` | 新增 `deleteObjectsBatch` 方法，按 500 key/批拆分；细化异常时 key 级别日志 |
| `src/main/resources/logback-spring.xml` | 新增 `picture-cleanup` FileAppender（如果不存在则新建） |
| `PictureService.java` / `PictureServiceImpl.java` | 可选：新增 `getDeletedPictureUrlKeys` 辅助方法 |

## 回滚

- 日志文件仅追加写入，不影响业务
- 定时任务流程变更可回退：恢复旧版 `PictureCleanupScheduler` 即可
- CosManager 新增方法不影响现有调用方
