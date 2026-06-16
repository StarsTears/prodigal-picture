# 图片清理日志增强 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enhance PictureCleanupScheduler with structured JSON logging, separate DB/COS transactions, and batched COS deletion.

**Architecture:** Modify PictureCleanupScheduler to split DB and COS deletion into independent transactions, add structured JSON logging to a dedicated log file, and add batch-splitting in CosManager for COS delete operations. A new logback-spring.xml provides the dedicated `picture-cleanup.log` appender with 30-day retention.

**Tech Stack:** Spring Boot scheduling, MyBatis-Plus, Tencent COS SDK, Logback, Lombok, Jackson

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `system/src/main/resources/logback-spring.xml` | **Create** | Logback config: console + prodigal-system + picture-cleanup appenders |
| `system/src/main/java/com/prodigal/system/job/PictureCleanupScheduler.java` | **Modify** | Split transactions, structured cleanup logging |
| `system/src/main/java/com/prodigal/system/manager/CosManager.java` | **Modify** | Add `deleteObjectsBatch` with 500-key splitting |

---

### Task 1: Create logback-spring.xml with picture-cleanup appender

**Files:**
- Create: `system/src/main/resources/logback-spring.xml`

- [ ] **Step 1: Create logback-spring.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 保持 Spring Boot 默认的 console 输出 -->
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>

    <!-- 业务日志文件 -->
    <appender name="BUSINESS_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/prodigal-system.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{requestId}] [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/prodigal-system-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>50MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>7</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 图片清理专用日志 -->
    <appender name="CLEANUP_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/picture-cleanup.log</file>
        <encoder>
            <pattern>%msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/picture-cleanup-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 清理日志 logger：additivity=false 确保只写清理文件不污染业务日志 -->
    <logger name="CLEANUP" level="INFO" additivity="false">
        <appender-ref ref="CLEANUP_FILE"/>
    </logger>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="BUSINESS_FILE"/>
    </root>
</configuration>
```

- [ ] **Step 2: Verify logback config compiles**

Run: `cd system && mvn validate`

- [ ] **Step 3: Commit**

```bash
git add system/src/main/resources/logback-spring.xml
git commit -m "feat: add logback config with picture-cleanup dedicated appender"
```

---

### Task 2: Add batch delete method to CosManager

**Files:**
- Modify: `system/src/main/java/com/prodigal/system/manager/CosManager.java`

- [ ] **Step 1: Add deleteObjectsBatch method**

Add the following method to `CosManager.java` after the existing `deleteObjects` method:

```java
private static final int COS_DELETE_BATCH_SIZE = 500;

/**
 * 分批删除 COS 对象，每批最多 500 个 key
 * @param keys 待删除的 key 列表
 * @return 删除成功的 key 数量
 */
public int deleteObjectsBatch(List<String> keys) {
    if (keys == null || keys.isEmpty()) {
        return 0;
    }
    String prefix = cosClientConfig.getHost() + "/";
    int totalDeleted = 0;

    for (int i = 0; i < keys.size(); i += COS_DELETE_BATCH_SIZE) {
        int end = Math.min(i + COS_DELETE_BATCH_SIZE, keys.size());
        List<String> batch = keys.subList(i, end);

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
        List<DeleteObjectsRequest.KeyVersion> keyList = new ArrayList<>();
        for (String key : batch) {
            String strippedKey = key.startsWith(prefix) ? key.substring(prefix.length()) : key;
            keyList.add(new DeleteObjectsRequest.KeyVersion(strippedKey));
        }
        deleteObjectsRequest.setKeys(keyList);

        try {
            DeleteObjectsResult result = cosClient.deleteObjects(deleteObjectsRequest);
            List<String> deletedKeys = result.getDeletedObjects().stream()
                    .map(DeleteObjectsResult.DeletedObject::getKey)
                    .collect(Collectors.toList());
            totalDeleted += deletedKeys.size();
            log.info("COS batch {} deleted {}/{} keys", i / COS_DELETE_BATCH_SIZE, deletedKeys.size(), batch.size());
            if (deletedKeys.size() < batch.size()) {
                log.warn("COS batch {} partially failed: attempted {}, deleted {}",
                        i / COS_DELETE_BATCH_SIZE, batch.size(), deletedKeys.size());
            }
        } catch (MultiObjectDeleteException mde) {
            List<DeleteObjectsResult.DeletedObject> deleted = mde.getDeletedObjects();
            List<MultiObjectDeleteException.DeleteError> errors = mde.getErrors();
            int deletedCount = deleted != null ? deleted.size() : 0;
            totalDeleted += deletedCount;
            log.error("COS batch {} failed keys: {}",
                    i / COS_DELETE_BATCH_SIZE,
                    errors != null ? errors.stream().map(MultiObjectDeleteException.DeleteError::getKey).collect(Collectors.toList()) : "[]");
        } catch (CosServiceException e) {
            log.error("COS batch {} service error: {}", i / COS_DELETE_BATCH_SIZE, e.getMessage());
        } catch (CosClientException e) {
            log.error("COS batch {} client error: {}", i / COS_DELETE_BATCH_SIZE, e.getMessage());
        }
    }
    return totalDeleted;
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd system && mvn compile -q`

- [ ] **Step 3: Commit**

```bash
git add system/src/main/java/com/prodigal/system/manager/CosManager.java
git commit -m "feat: add batch COS delete with 500-key splitting"
```

---

### Task 3: Refactor PictureCleanupScheduler with split transactions and structured logging

**Files:**
- Modify: `system/src/main/java/com/prodigal/system/job/PictureCleanupScheduler.java`

- [ ] **Step 1: Replace PictureCleanupScheduler with the new implementation**

```java
package com.prodigal.system.job;

import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.CosManager;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.service.SpaceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Lang
 * @description: 定期清理已删除图片的 DB 记录和 COS 文件，输出结构化清理日志
 **/
@Slf4j
@Component
@EnableScheduling
public class PictureCleanupScheduler {

    private static final Logger CLEANUP_LOG = LoggerFactory.getLogger("CLEANUP");

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupDeletedPictures() {
        long startTime = System.currentTimeMillis();
        Date oneWeekAgo = DateUtils.addWeeks(new Date(), -1);

        List<Picture> toDeletePictures = pictureService.selectDeletedPictures(oneWeekAgo);

        if (toDeletePictures.isEmpty()) {
            CLEANUP_LOG.info("{\"ts\":\"{}\",\"type\":\"CLEANUP_DONE\",\"totalDbDeleted\":0,\"totalCosDeleted\":0,\"totalCosFailed\":0,\"durationMs\":{}}",
                    new Date(), System.currentTimeMillis() - startTime);
            log.info("No deleted pictures to clean up.");
            return;
        }

        CLEANUP_LOG.info("{\"ts\":\"{}\",\"type\":\"CLEANUP_START\",\"totalPictures\":{}}",
                new Date(), toDeletePictures.size());
        log.info("Found {} pictures to delete.", toDeletePictures.size());

        int totalDbDeleted = 0;
        int totalCosDeleted = 0;
        int totalCosFailed = 0;

        Map<String, List<Picture>> pictureGroupBySpace = toDeletePictures.stream()
                .collect(Collectors.groupingBy(Picture::getSpaceId));

        for (Map.Entry<String, List<Picture>> entry : pictureGroupBySpace.entrySet()) {
            String spaceId = entry.getKey();
            List<Picture> pictures = entry.getValue();

            List<String> pictureIds = pictures.stream().map(Picture::getId).collect(Collectors.toList());
            long sizeSum = pictures.stream().mapToLong(Picture::getPicSize).sum();

            CLEANUP_LOG.info("{\"ts\":\"{}\",\"type\":\"SPACE_START\",\"spaceId\":\"{}\",\"pictureCount\":{},\"pictureIds\":{}}",
                    new Date(), spaceId, pictures.size(), pictureIds);

            // 1) DB 删除在独立事务中先提交，失败则跳过该空间
            try {
                transactionTemplate.execute(status -> {
                    int rows = pictureService.deletePicturesByPictureIdsAndSpaceId(pictureIds, spaceId);
                    if (rows != pictures.size()) {
                        throw new RuntimeException(String.format(
                                "DB delete mismatch: expected %d, actual %d", pictures.size(), rows));
                    }
                    return true;
                });
                totalDbDeleted += pictures.size();
                CLEANUP_LOG.info("{\"ts\":\"{}\",\"type\":\"DB_DELETED\",\"spaceId\":\"{}\",\"deletedRows\":{}}",
                        new Date(), spaceId, pictures.size());
            } catch (Exception e) {
                log.error("DB delete failed for spaceId={}: {}", spaceId, e.getMessage());
                CLEANUP_LOG.info("{\"ts\":\"{}\",\"type\":\"DB_DELETE_FAILED\",\"spaceId\":\"{}\",\"error\":\"{}\"}",
                        new Date(), spaceId, e.getMessage());
                continue;
            }

            // 2) COS 删除在事务外执行，失败只记录日志不阻塞后续空间
            List<String> cosKeys = new ArrayList<>();
            for (Picture picture : pictures) {
                if (picture.getUrl() != null) cosKeys.add(picture.getUrl());
                if (picture.getOriginUrl() != null) cosKeys.add(picture.getOriginUrl());
                if (picture.getThumbnailUrl() != null) cosKeys.add(picture.getThumbnailUrl());
            }

            if (!cosKeys.isEmpty()) {
                int deleted = cosManager.deleteObjectsBatch(cosKeys);
                int failed = cosKeys.size() - deleted;
                totalCosDeleted += deleted;
                totalCosFailed += failed;
                if (failed > 0) {
                    log.warn("COS cleanup: spaceId={}, deleted={}, failed={}", spaceId, deleted, failed);
                }
            }

            CLEANUP_LOG.info("{\"ts\":\"{}\",\"type\":\"SPACE_DONE\",\"spaceId\":\"{}\",\"dbDeleted\":{},\"cosDeleted\":{},\"cosFailed\":{}}",
                    new Date(), spaceId, pictures.size(), totalCosDeleted, totalCosFailed);
        }

        long durationMs = System.currentTimeMillis() - startTime;
        CLEANUP_LOG.info("{\"ts\":\"{}\",\"type\":\"CLEANUP_DONE\",\"totalDbDeleted\":{},\"totalCosDeleted\":{},\"totalCosFailed\":{},\"durationMs\":{}}",
                new Date(), totalDbDeleted, totalCosDeleted, totalCosFailed, durationMs);
        log.info("Cleanup done: dbDeleted={}, cosDeleted={}, cosFailed={}, durationMs={}",
                totalDbDeleted, totalCosDeleted, totalCosFailed, durationMs);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd system && mvn compile -q`

- [ ] **Step 3: Commit**

```bash
git add system/src/main/java/com/prodigal/system/job/PictureCleanupScheduler.java
git commit -m "feat: enhance cleanup scheduler with split transactions and structured JSON logging"
```

---

### Task 4: Verify full build and review final diff

- [ ] **Step 1: Run full compilation**

Run: `cd system && mvn compile`

Expected: BUILD SUCCESS

- [ ] **Step 2: Review final diff**

Run: `git diff HEAD~3..HEAD --stat`

Expected: 3 files changed (logback-spring.xml created, CosManager.java modified, PictureCleanupScheduler.java modified)

- [ ] **Step 3: Verify no regressions in existing tests**

Run: `cd system && mvn test`

Expected: All existing tests pass
