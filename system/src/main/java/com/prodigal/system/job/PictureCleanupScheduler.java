package com.prodigal.system.job;

import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.CosManager;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.service.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Lang
 * @description: 定期删除图片数据
 **/
@Slf4j
@Component
@EnableScheduling
public class PictureCleanupScheduler {
    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;
    @Resource
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 每天凌晨2点执行清理任务(清除一周都未及时恢复的数据)
     * 原始 cron 表达式 0 0 2 * * ?：
     * 0：秒（0秒）
     * 0：分钟（0分钟）
     * 2：小时（2点）
     * *：日期（每天）
     * *：月份（每月）
     * ?：星期（不指定，因为日期已经指定了）
     */
//    @Scheduled(cron = "0 0 2 * * ?")

    @Scheduled(cron = " 0 0 2 * * ?")
    public void cleanupDeletedPictures() {
        // 获取一周前的时间
        Date oneWeekAgo = DateUtils.addWeeks(new Date(), -1);

        // 查询需要删除的图片记录
        List<Picture> toDeletePictures = pictureService.selectDeletedPictures(oneWeekAgo);

        if (!toDeletePictures.isEmpty()) {
            log.info("Found {} pictures to delete.", toDeletePictures.size());

            Map<Long, List<Picture>> pictureList = toDeletePictures.stream().collect(Collectors.groupingBy(Picture::getSpaceId));
            for (Map.Entry<Long, List<Picture>> entry : pictureList.entrySet()) {
                Long spaceId = entry.getKey();
                List<Picture> pictures = entry.getValue();

                List<Long> pictureIds = pictures.stream().map(Picture::getId).collect(Collectors.toList());
                //获取 COS key
                List<String> urls = pictures.stream().map(Picture::getUrl).collect(Collectors.toList());
                List<String> originUrls = pictures.stream().map(Picture::getOriginUrl).collect(Collectors.toList());
                List<String> thumbnailUrls = pictures.stream().map(Picture::getThumbnailUrl).collect(Collectors.toList());
                urls.addAll(originUrls);
                urls.addAll(thumbnailUrls);

                long sizeSum = pictures.stream().mapToLong(Picture::getPicSize).sum();

                transactionTemplate.execute(status -> {
                    int rows = pictureService.deletePicturesByPictureIdsAndSpaceId(pictureIds, spaceId);

                    log.error("Failed to delete picture: " + (pictures.size() - rows));
                    ThrowUtils.throwIf(rows != pictures.size(), ErrorCode.OPERATION_ERROR, "Failed to delete picture");

//                    if (spaceId != null && spaceId != 0) {
//                        boolean update = spaceService.lambdaUpdate()
//                                .eq(Space::getId, spaceId)
//                                .setSql("totalSize = totalSize -" + sizeSum)
//                                .setSql("totalCount = totalCount -"+pictures.size())
//                                .update();
//                        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, String.format("空间【%s】额度更新失败-size%s;count:%s",spaceId,sizeSum,pictures.size()));
//                    }
                    // 删除COS上的文件
                    cosManager.deleteObjects(urls);
                    return true;
                });
            }
        }
    }

}
