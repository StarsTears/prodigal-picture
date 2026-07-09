package com.prodigal.system.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prodigal.system.config.MetricsConfig;
import com.prodigal.system.manager.CosManager;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.service.PictureService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Lang
 * @description: 定期清理已删除图片的 DB 记录和 COS 文件，输出结构化清理日志
 **/
@Slf4j
@Component
public class PictureCleanupScheduler {

    private static final Logger CLEANUP_LOG = LoggerFactory.getLogger("CLEANUP");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    private PictureService pictureService;

    @Resource
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private MeterRegistry meterRegistry;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupDeletedPictures() {
        long startTime = System.currentTimeMillis();
        Date oneWeekAgo = DateUtils.addWeeks(new Date(), -1);

        List<Picture> toDeletePictures = pictureService.selectDeletedPictures(oneWeekAgo);

        if (toDeletePictures.isEmpty()) {
            CLEANUP_LOG.info("{}", json(new LinkedHashMap<>() {{
                put("ts", Instant.now().toString());
                put("type", "CLEANUP_DONE");
                put("totalDbDeleted", 0);
                put("totalCosDeleted", 0);
                put("totalCosFailed", 0);
                put("durationMs", System.currentTimeMillis() - startTime);
            }}));
            log.info("No deleted pictures to clean up.");
            return;
        }

        CLEANUP_LOG.info("{}", json(Map.of(
                "ts", Instant.now().toString(),
                "type", "CLEANUP_START",
                "totalPictures", toDeletePictures.size()
        )));
        log.info("Found {} pictures to delete.", toDeletePictures.size());

        int totalDbDeleted = 0;
        int totalCosDeleted = 0;
        int totalCosFailed = 0;

        Map<String, List<Picture>> pictureGroupBySpace = toDeletePictures.stream()
                .filter(p -> p.getSpaceId() != null)
                .collect(Collectors.groupingBy(Picture::getSpaceId));

        for (Map.Entry<String, List<Picture>> entry : pictureGroupBySpace.entrySet()) {
            String spaceId = entry.getKey();
            List<Picture> pictures = entry.getValue();

            List<String> pictureIds = pictures.stream()
                    .filter(p -> p.getId() != null)
                    .map(Picture::getId)
                    .collect(Collectors.toList());

            CLEANUP_LOG.info("{}", json(Map.of(
                    "ts", Instant.now().toString(),
                    "type", "SPACE_START",
                    "spaceId", spaceId,
                    "pictureCount", pictures.size(),
                    "pictureIds", pictureIds
            )));

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
                CLEANUP_LOG.info("{}", json(Map.of(
                        "ts", Instant.now().toString(),
                        "type", "DB_DELETED",
                        "spaceId", spaceId,
                        "deletedRows", pictures.size()
                )));
            } catch (Exception e) {
                log.error("DB delete failed for spaceId={}", spaceId, e);
                CLEANUP_LOG.error("{}", json(Map.of(
                        "ts", Instant.now().toString(),
                        "type", "DB_DELETE_FAILED",
                        "spaceId", spaceId,
                        "error", e.getMessage()
                )));
                continue;
            }

            // 2) COS 删除在事务外执行，失败只记录日志不阻塞后续空间
            int spaceCosDeleted = 0;
            int spaceCosFailed = 0;

            List<String> cosKeys = new ArrayList<>();
            for (Picture picture : pictures) {
                if (picture.getUrl() != null) cosKeys.add(picture.getUrl());
                if (picture.getOriginUrl() != null) cosKeys.add(picture.getOriginUrl());
                if (picture.getThumbnailUrl() != null) cosKeys.add(picture.getThumbnailUrl());
            }

            if (!cosKeys.isEmpty()) {
                try {
                    int deleted = cosManager.deleteObjectsBatch(cosKeys);
                    spaceCosDeleted = deleted;
                    spaceCosFailed = cosKeys.size() - deleted;
                } catch (Exception e) {
                    spaceCosFailed = cosKeys.size();
                    log.error("COS deletion failed for spaceId={}", spaceId, e);
                    CLEANUP_LOG.error("{}", json(Map.of(
                            "ts", Instant.now().toString(),
                            "type", "COS_FAILED",
                            "spaceId", spaceId,
                            "error", e.getMessage()
                    )));
                }
                totalCosDeleted += spaceCosDeleted;
                totalCosFailed += spaceCosFailed;
                if (spaceCosFailed > 0) {
                    log.warn("COS cleanup: spaceId={}, deleted={}, failed={}", spaceId, spaceCosDeleted, spaceCosFailed);
                }
            }

            CLEANUP_LOG.info("{}", json(Map.of(
                    "ts", Instant.now().toString(),
                    "type", "SPACE_DONE",
                    "spaceId", spaceId,
                    "dbDeleted", pictures.size(),
                    "cosDeleted", spaceCosDeleted,
                    "cosFailed", spaceCosFailed
            )));
        }

        long durationMs = System.currentTimeMillis() - startTime;
        CLEANUP_LOG.info("{}", json(Map.of(
                "ts", Instant.now().toString(),
                "type", "CLEANUP_DONE",
                "totalDbDeleted", totalDbDeleted,
                "totalCosDeleted", totalCosDeleted,
                "totalCosFailed", totalCosFailed,
                "durationMs", durationMs
        )));
        log.info("Cleanup done: dbDeleted={}, cosDeleted={}, cosFailed={}, durationMs={}",
                totalDbDeleted, totalCosDeleted, totalCosFailed, durationMs);

        meterRegistry.gauge(MetricsConfig.METRIC_CLEANUP_DURATION, durationMs / 1000.0);
        Counter.builder(MetricsConfig.METRIC_CLEANUP_DELETED)
                .register(meterRegistry)
                .increment(totalDbDeleted);
    }

    private static String json(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return map.toString();
        }
    }
}
