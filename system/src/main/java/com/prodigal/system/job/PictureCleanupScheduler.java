package com.prodigal.system.job;

import com.prodigal.system.manager.CosManager;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.service.PictureService;
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
