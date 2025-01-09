package com.prodigal.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prodigal.system.api.aliyunai.AliYunAiApi;
import com.prodigal.system.api.aliyunai.model.dto.CreateOutPaintingTaskDto;
import com.prodigal.system.api.aliyunai.model.vo.CreateOutPaintingTaskVO;
import com.prodigal.system.constant.CacheConstant;
import com.prodigal.system.constant.FilePathConstant;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.CosManager;
import com.prodigal.system.manager.FileManager;
import com.prodigal.system.manager.CacheManager;
import com.prodigal.system.manager.strategy.CacheContext;
import com.prodigal.system.manager.upload.FilePictureUpload;
import com.prodigal.system.manager.upload.PictureUploadTemplate;
import com.prodigal.system.manager.upload.UrlPictureUpload;
import com.prodigal.system.model.dto.file.UploadPictureResult;
import com.prodigal.system.model.dto.picture.*;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.Space;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.PictureReviewStatusEnum;
import com.prodigal.system.model.vo.PictureVO;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.mapper.PictureMapper;
import com.prodigal.system.service.SpaceService;
import com.prodigal.system.service.UserService;
import com.prodigal.system.utils.ColorSimilarUtils;
import com.prodigal.system.utils.CustomThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Lang
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-12 15:47:24
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Resource
    private FileManager fileManager;
    @Resource
    private UserService userService;
    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Resource
    private CosManager cosManager;
    @Resource
    private SpaceService spaceService;
    @Resource
    private CacheManager cacheManager;

    @Resource
    private AliYunAiApi aliYunAiApi;

    // 创建自定义线程池
    private ExecutorService threadPool = CustomThreadPool.createCustomThreadPool(10, 50, 120, TimeUnit.SECONDS);
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 图片校验(更新与修改)
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        //更新或修改时，图片id不能为空
        ThrowUtils.throwIf(ObjUtil.isEmpty(picture.getId()), ErrorCode.PARAMS_ERROR, "id 不能为空！");
        //限制图片地址长度、简介
        ThrowUtils.throwIf(StrUtil.isNotBlank(picture.getUrl()) && picture.getUrl().length() > 1024,
                ErrorCode.PARAMS_ERROR, "图片地址过长！");
        ThrowUtils.throwIf(StrUtil.isNotBlank(picture.getIntroduction()) && picture.getIntroduction().length() > 1000,
                ErrorCode.PARAMS_ERROR, "图片简介过长！");
    }

    /**
     * 上传图片
     *
     * @param inputSource      数据源
     * @param pictureUploadDto 图片请求参数
     * @param loginUser        登录用户
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadDto pictureUploadDto, User loginUser) {
        if (inputSource == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传图片数据源为空");
        }
        ThrowUtils.throwIf(loginUser == null, ErrorCode.USER_NOT_AUTHORIZED);

        //校验空间ID是否存在；存在→私有空间；不存在→公开空间
        Long spaceId = pictureUploadDto.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
//            //判断当前用户有无权限上传图片到该空间
//            if (!space.getUserId().equals(loginUser.getId())) {
//                throw new BusinessException(ErrorCode.USER_NOT_AUTHORIZED, "没有空间权限");
//            }
            //校验额度
            ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(), ErrorCode.OPERATION_ERROR, "空间条数不够");
            ThrowUtils.throwIf(space.getTotalSize() >= space.getMaxSize(), ErrorCode.OPERATION_ERROR, "空间额度不够");
        }

        //判断新增还是修改
        Long pictureID = null;
        if (pictureUploadDto != null) {
            pictureID = pictureUploadDto.getId();
        }
        //校验图片是否存在
        if (pictureID != null) {
            Picture picture = this.getById(pictureID);
            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
//            //验权、进本人和管理员可编辑
//            boolean isCan = picture.getUserId().equals(loginUser.getId());
//            ThrowUtils.throwIf(!isCan && !userService.isAdmin(loginUser), ErrorCode.USER_NOT_AUTHORIZED, "用户没有权限修改该图片");

            //校验空间是否一致
            if (spaceId == null) {
                if (picture.getSpaceId() != null) {
                    spaceId = picture.getSpaceId();
                }
            } else {
                //若携带了spaceId，则判断是否一致
                if (ObjUtil.notEqual(spaceId, picture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间ID不一致");
                }
            }
            //更新、删除COS的文件
            this.clearPictureFile(picture);
        }
        /*
            上传图片-根据用户id来划分目录
            管理员上传的到私有下
            用户上传的到公有
         */
        String uploadPrefix = String.format("%s/%s", FilePathConstant.PICTURE_PUBLIC_PREFIX, loginUser.getId());
        if (spaceId != null) {
            uploadPrefix = String.format("%s/%s", FilePathConstant.PICTURE_SPACE_PREFIX, loginUser.getId());
        }

        if (loginUser.getUserRole().contains(UserConstant.SUPER_ADMIN_ROLE)) {
            uploadPrefix = String.format("%s/%S", FilePathConstant.PICTURE_PRIVATE_PREFIX, "super");
        }
        //通过数据源判断使用的上传模板（file、url）
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }

        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPrefix);
        //构造需要存储的图片信息
        Picture picture = new Picture();
        picture.setOriginUrl(uploadPictureResult.getOriginUrl());
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setSourceUrl(uploadPictureResult.getSourceUrl());
        String picName = uploadPictureResult.getPicName();
        //抓取图片，如果已经写了图片名称，则使用用户填写的图片名称
        if (pictureUploadDto != null && StrUtil.isNotBlank(pictureUploadDto.getPicName())) {
            picName = pictureUploadDto.getPicName();
        }
        picture.setName(picName);
        //设置颜色的十六进制
        picture.setPicColor(uploadPictureResult.getPicColor());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
//        picture.setSpaceId(spaceId);
        // 设置空间ID;将公共空间的空间ID设置为0
        picture.setSpaceId(spaceId !=null ? spaceId : 0L);
        //补充审核参数
        this.fillReviewParams(picture, loginUser);
        //如果pictureID 不为空则进行更新
        if (pictureID != null) {
            picture.setId(pictureID);
            picture.setEditTime(new Date());
        }
        //开启事物
        UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", pictureID) // 根据主键 id 查询
                .eq("spaceId", spaceId); // 附加 spaceId 条件
        Long finalSpaceId = spaceId;
        Long finalPictureID = pictureID;
        transactionTemplate.execute(status -> {
            boolean result= true;
            if (finalPictureID != null){
                result = this.update(picture,updateWrapper);
            }else{
               result = this.save(picture);
            }
//            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            if (finalSpaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize +" + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "空间数据更新失败");
            }
            return picture;
        });
        return PictureVO.objToVO(picture);
    }

    /**
     * 从必应网站抓取图片（默认必应）
     *
     * @param pictureUploadByBatchDto 图片批量上传请求参数
     * @param loginUser               登录用户
     */
    @Override
    public int uploadPictureByBatch(PictureUploadByBatchDto pictureUploadByBatchDto, User loginUser) {
        String searchText = pictureUploadByBatchDto.getSearchText();
        //图片名称前缀为空，则使用关键词
        String namePrefix = pictureUploadByBatchDto.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        //数量校验-每次最多20条
        Integer count = pictureUploadByBatchDto.getCount();
        ThrowUtils.throwIf(count > 20, ErrorCode.PARAMS_ERROR, "每次最多上传20张图片");
        // 偏移量
        Integer offset = pictureUploadByBatchDto.getOffset();
        if (offset == null) {
            offset = 0; // 默认偏移量为0
        }
        //抓取的地址 https://cn.bing.com/images/async?q=%s&mmasync=1
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1&first=%d", searchText, offset);
        String url = pictureUploadByBatchDto.getUrl();
//        if (StrUtil.isNotBlank(url)){
//            fetchUrl = String.format("");
//        }
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (Exception e) {
            log.error("图片批量上传-抓取图片地址失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片批量上传-获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片批量上传-获取图片元素失败");
        }
        //查询一次图片数量，然后去重
        int uploadCount = 0;
        Elements detailUrls = div.select("a.iusc");
        for (Element element : detailUrls) {
            String href = element.attr("href");
            // 截取URL参数部分
            String dUrl = null;
            try {
                dUrl = URLDecoder.decode(href, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("URL解码报错", e);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "URL解码报错哦");
            }
            String query = dUrl.substring(dUrl.indexOf('?') + 1);

            // 将URL参数解析为JSON对象
            Map<String, String> json = new HashMap<>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    json.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //处理图片url.防止出现转义错误
            String fileUrl = json.get("riu");
            if (StrUtil.isBlank(fileUrl)) {
                fileUrl = json.get("mediaurl");
            }
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            //以免数据重复，可以将其查出与数据的源url进行对比

            //上传图片
            PictureUploadDto pictureUploadDto = new PictureUploadDto();
            if (StrUtil.isNotBlank(namePrefix)) {
                pictureUploadDto.setPicName(namePrefix + (uploadCount + 1));
            }
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadDto, loginUser);
                log.info("图片批量上传-上传成功->id:{}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片批量上传-上传失败", e);
                continue;
            }
            //如果上传数量达到要求，则跳出循环
            if (uploadCount >= count) {
                break;
            }
        }

/**
 int uploadCount = 0;
 Elements imgElementList = div.select("img.mimg");
 for (Element imgElement : imgElementList) {
 String fileUrl = imgElement.attr("src");
 if (StrUtil.isBlank(fileUrl)){
 log.info("图片批量上传-获取图片地址为空,已跳过:{}",fileUrl);
 continue;
 }
 //处理图片url.防止出现转义错误
 int questionMarkIndex = fileUrl.indexOf("?");
 if (questionMarkIndex > -1){
 fileUrl = fileUrl.substring(0,questionMarkIndex);
 }
 //上传图片
 PictureUploadDto pictureUploadDto = new PictureUploadDto();
 if (StrUtil.isNotBlank(namePrefix)){
 pictureUploadDto.setPicName(namePrefix + (uploadCount+1));
 }
 try {
 //                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadDto, loginUser);
 //                log.info("图片批量上传-上传成功->id:{}",pictureVO.getId());
 uploadCount++;
 }catch (Exception e){
 log.error("图片批量上传-上传失败",e);
 continue;
 }
 //如果上传数量达到要求，则跳出循环
 if (uploadCount >= count){
 break;
 }
 }
 */
        return uploadCount;
    }

    @Override
    public void editPicture(PictureEditDto pictureEditDto, User loginUser) {
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditDto, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditDto.getTags()));
        //设置编辑时间
        picture.setEditTime(new Date());
        //数据校验
        this.validPicture(picture);
        //判断图片是否存在
        Long id = pictureEditDto.getId();
        Long spaceId=pictureEditDto.getSpaceId()==null?0:pictureEditDto.getSpaceId();
        // 构造 QueryWrapper
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)         // 根据主键 id 查询
                .eq("spaceId", spaceId); // 附加 spaceId 条件

        // 执行查询
        Picture oldPicture = this.getOne(queryWrapper);

//        Picture oldPicture = this.getById(id);
        picture.setUserId(oldPicture.getUserId());
        //验证权限
        //图片应只能管理员/本人删除
        if (!loginUser.getId().equals(picture.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
        }
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //验权
//        checkPicturePermission(loginUser, picture);
        //补充审核参数
        this.fillReviewParams(picture, loginUser);

        UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)         // 根据主键 id 查询
                .eq("spaceId", spaceId); // 附加 spaceId 条件
        boolean result = this.update(picture,updateWrapper);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 批量修改图片的分类与标签（若数据量过大，可使用线程池+分批+并发进行优化- 数量要达到万条以上）
     *
     * @param pictureEditByBatchDto 修改参数接收类
     * @param loginUser             登录用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPictureByBatch(PictureEditByBatchDto pictureEditByBatchDto, User loginUser) {
        //获取所有参数
        List<Long> pictureIdList = pictureEditByBatchDto.getPictureIdList();
        Long spaceId = pictureEditByBatchDto.getSpaceId();
        String category = pictureEditByBatchDto.getCategory();
        List<String> tags = pictureEditByBatchDto.getTags();

        //1、校验参数
        ThrowUtils.throwIf(spaceId == null || pictureIdList.isEmpty(), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR);

        //2、空间权限校验
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        spaceService.checkSpacePermission(space, loginUser);

        //3、图片查询(仅需要的字段)
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if (pictureList.isEmpty()) {
            return;
        }

        //4、更新分类与标签
        pictureList.forEach(picture -> {
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });
        //批量重命名
        String nameRule = pictureEditByBatchDto.getNameRule();
        this.fillPictureWithNameRule(pictureList, nameRule);
        //5、批量修改
        boolean result = this.updateBatchById(pictureList);

        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "批量编辑图片失败");
    }

    /**
     * 自动填充图片名称规则 图片{序号}
     *
     * @param pictureList 图片列表
     * @param nameRule    名称规则
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (StrUtil.isBlank(nameRule) || pictureList.isEmpty()) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String name = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(name);
            }

        } catch (Exception e) {
            log.warn("图片批量修改-名称规则解析失败,使用默认规则", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片批量修改-名称规则解析失败,使用默认规则" + e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchEditPictureMetaData(PictureEditByBatchDto pictureEditByBatchDto, User loginUser) {
        Long spaceId = pictureEditByBatchDto.getSpaceId();
        String category = pictureEditByBatchDto.getCategory();
        List<String> tags = pictureEditByBatchDto.getTags();
        List<Long> pictureIdList = pictureEditByBatchDto.getPictureIdList();
        //参数校验
        //查询该空间下的图片
        //3、图片查询(仅需要的字段)
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if (pictureList.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "指定的图片不存在或不属于该空间");
        }
        //分批处理事物
        long batchSize = 100;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(); // 记录成功的条数
        AtomicInteger failureCount = new AtomicInteger(); // 记录失败的条数
        for (int i = 0; i < pictureList.size(); i += batchSize) {
            int start = i;
            int end = (int) Math.min(i + batchSize, pictureList.size());
            List<Picture> batchList = pictureList.subList(start, end);
            //异步批量处理数据
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    batchList.forEach(picture -> {
                        //修改分类与标签
                        if (StrUtil.isNotBlank(category)) {
                            picture.setCategory(category);
                        }
                        if (CollUtil.isNotEmpty(tags)) {
                            picture.setTags(JSONUtil.toJsonStr(tags));
                        }
                    });
                    boolean result = this.updateBatchById(batchList);
                    if (!result) {
                        failureCount.addAndGet(batchList.size()); // 增加失败条数
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "批量编辑图片失败");
                    } else {
                        successCount.addAndGet(batchList.size());
                    }
                } catch (Exception e) {
                    log.error("批量处理图片失败：{}", e.getMessage(), e);
                    failureCount.addAndGet(batchList.size());
                }
            }, threadPool);
            futures.add(future);
        }
        //等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("批量处理图片完成，成功：{}, 失败：{}", successCount.get(), failureCount.get());
    }

    /**
     * 分页查询参数封装
     *
     * @param pictureQueryDto 图片查询参数
     */
    @Override
    public LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryDto pictureQueryDto) {
        LambdaQueryWrapper<Picture> wrapper = new LambdaQueryWrapper<Picture>();
        if (pictureQueryDto == null) {
            return wrapper;
        }
        String sortOrder = pictureQueryDto.getSortOrder();
        String sortField = pictureQueryDto.getSortField() == null ? "" : pictureQueryDto.getSortField().trim();
        //关键字查询(名称，简介)
        if (StrUtil.isNotBlank(pictureQueryDto.getSearchText())) {
            wrapper.and(e -> e.like(Picture::getName, pictureQueryDto.getSearchText())
                    .or().like(Picture::getIntroduction, pictureQueryDto.getSearchText()));
        }
        wrapper.eq(true, Picture::getIsDelete, 0)
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getSpaceId()), Picture::getSpaceId, pictureQueryDto.getSpaceId())
//                .isNull(pictureQueryDto.isNullSpaceId(), Picture::getSpaceId) //公共图库的空间ID 为 0
                .eq(pictureQueryDto.isNullSpaceId(), Picture::getSpaceId,0L) //公共图库的空间ID 为 0
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getId()), Picture::getId, pictureQueryDto.getId())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getUserId()), Picture::getUserId, pictureQueryDto.getUserId())
                .like(StrUtil.isNotBlank(pictureQueryDto.getName()), Picture::getName, pictureQueryDto.getName())
                .like(StrUtil.isNotBlank(pictureQueryDto.getIntroduction()), Picture::getIntroduction, pictureQueryDto.getIntroduction())
                .like(StrUtil.isNotBlank(pictureQueryDto.getPicFormat()), Picture::getPicFormat, pictureQueryDto.getPicFormat())
                .eq(StrUtil.isNotBlank(pictureQueryDto.getCategory()), Picture::getCategory, pictureQueryDto.getCategory())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getPicHeight()), Picture::getPicHeight, pictureQueryDto.getPicHeight())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getPicWidth()), Picture::getPicWidth, pictureQueryDto.getPicWidth())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getPicSize()), Picture::getPicSize, pictureQueryDto.getPicSize())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getPicScale()), Picture::getPicScale, pictureQueryDto.getPicScale())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getReviewStatus()), Picture::getReviewStatus, pictureQueryDto.getReviewStatus())
                .eq(ObjUtil.isNotEmpty(pictureQueryDto.getReviewerId()), Picture::getReviewerId, pictureQueryDto.getReviewerId())
                .like(StrUtil.isNotBlank(pictureQueryDto.getReviewMessage()), Picture::getReviewMessage, pictureQueryDto.getReviewMessage())
                .ge(ObjUtil.isNotEmpty(pictureQueryDto.getStartEditTime()), Picture::getEditTime, pictureQueryDto.getStartEditTime())
                .lt(ObjUtil.isNotEmpty(pictureQueryDto.getEndEditTime()), Picture::getEditTime, pictureQueryDto.getEndEditTime());
        if (CollUtil.isNotEmpty(pictureQueryDto.getTags())) {
            // and (tags like "%"\tag1\"%" and tags like "%"\tag2\"%")
            for (String tag : pictureQueryDto.getTags()) {
                wrapper.like(Picture::getTags, "\"" + tag + "\"");
            }
        }
        switch (sortField) {
            case "name":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getName);
                break;
            case "category":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getCategory);
                break;
            case "picHeight":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicHeight);
                break;
            case "picWidth":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicWidth);
                break;
            case "picSize":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicSize);
                break;
            case "picScale":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicScale);
                break;
            case "picFormat":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getPicFormat);
                break;
            case "createTime":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getCreateTime);
            case "editTime":
                wrapper.orderBy(StrUtil.isNotEmpty(pictureQueryDto.getSortField()), sortOrder.equals("ascend"), Picture::getEditTime);
                break;
            default:
                break;
        }
        return wrapper;
    }

    /**
     * 将获取到的图片再按照 主色调筛选
     *
     * @param targetColor 目标主色调
     * @param pictureList 筛选主色调的数据
     * @return
     */
    @Override
    public List<Picture> getPicturePageWithColor(Color targetColor, List<Picture> pictureList) {
        //根据相似度降序排排列
//        List<Picture> sortedPictures = pictureList.stream().sorted(Comparator.comparingDouble(e -> {
//            //提取图片主色调
//            String hexColor = e.getPicColor();
//            //主色调为空的放到最后
//            if (StrUtil.isBlank(hexColor)) {
//                return Double.MAX_VALUE;
//            }
//            Color picColor = Color.decode(hexColor);
//            //越大越相似，故放在最前面
//            return -ColorSimilarUtils.calculateSimilarity(targetColor, picColor);
//        })).collect(Collectors.toList());
//
        //获取相似度大于0.8的数据
        List<Picture> sortedPictures = pictureList.stream()
                .filter(picture -> {
                    //提取图片主色调
                    String hexColor = picture.getPicColor();
                    //主色调为空的放到最后
                    if (StrUtil.isNotBlank(hexColor)) {
                        Color pictureColor = Color.decode(hexColor);
                        return ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor) > 0.8;// 假设相似度大于0.8为符合条件
                    }
                    return false;
                }).collect(Collectors.toList());

        return sortedPictures;
    }

    /**
     * 单张图片信息(VO封装)
     *
     * @param picture 实体对象
     * @param request 浏览器请求
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        //根据Picture信息 脱敏后返回给前端
        PictureVO pictureVO = PictureVO.objToVO(picture);
        //根据userID 获取UserVO
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            UserVO userVO = userService.getUserVO(userService.getById(userId));
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页图片查询(VO封装)
     *
     * @param picturePage 分页数据
     * @param request     浏览器请求
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        //转换VO
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVO).collect(Collectors.toList());
        //获取UserVO
        Set<Long> userIDSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIDUserListMap = userService.listByIds(userIDSet).stream().collect(Collectors.groupingBy(User::getId));
        pictureVOList.forEach(e -> {
            Long userId = e.getUserId();
            if (userIDUserListMap.containsKey(userId))
                e.setUser(userService.getUserVO(userIDUserListMap.get(userId).get(0)));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 使用本地缓存+redis缓存 实现查询优化
     *
     * @param pictureQueryDto
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVOPageCache(PictureQueryDto pictureQueryDto, HttpServletRequest request) {
        long current = pictureQueryDto.getCurrent();
        long size = pictureQueryDto.getPageSize();
        //先去查询缓存，未命中再去查询数据库
        String queryCondition = JSONUtil.toJsonStr(pictureQueryDto);
        String hashKey = DigestUtils.md5Hex(queryCondition);
        String caffeineKey = String.format("%s%s", CacheConstant.PICTURE_PAGE_CAFFEINE_CACHE_KEY, hashKey);

        CacheContext cacheContext = new CacheContext();
        cacheContext.setType(CacheConstant.CAFFEINE_TYPE);
        cacheContext.setKey(caffeineKey);

        Object cachedValue = cacheManager.getCacheValue(cacheContext);
        if (cachedValue != null) {
            //命中本地缓存，返回结果
            Page<PictureVO> page = JSONUtil.toBean(cachedValue.toString(), Page.class);
            return page;
        }

        String redisKey = String.format("%s%s", CacheConstant.PICTURE_PAGE_REDIS_CACHE_KEY, hashKey);
        cacheContext.setType(CacheConstant.REDIS_TYPE);
        cacheContext.setKey(redisKey);
        cachedValue = cacheManager.getCacheValue(cacheContext);
        if (cachedValue != null) {
            Page<PictureVO> page = JSONUtil.toBean(cachedValue.toString(), Page.class);

            cacheContext.setKey(caffeineKey);
            cacheContext.setValue(cachedValue.toString());
            cacheContext.setType(CacheConstant.CAFFEINE_TYPE);
            cacheManager.putCacheValue(cacheContext);
            return page;
        }
        //操作数据库
        Page<Picture> picturePage = this.page(new Page<>(current, size), this.getQueryWrapper(pictureQueryDto));
        Page<PictureVO> pictureVOPage = this.getPictureVOPage(picturePage, request);

        String value = JSONUtil.toJsonStr(pictureVOPage);

        cacheContext.setValue(value);
        //保存到本地缓存
        cacheContext.setType(CacheConstant.CAFFEINE_TYPE);
        cacheContext.setKey(caffeineKey);
        cacheManager.putCacheValue(cacheContext);

        //保存到redis缓存
        //将数据存到缓存 5~10分钟 随机过期，防止雪崩
        int expireTime = 300 + RandomUtil.randomInt(0, 300);
        cacheContext.setType(CacheConstant.REDIS_TYPE);
        cacheContext.setCacheExpireTime(expireTime);
        cacheContext.setCacheExpireTimeUnit(TimeUnit.SECONDS);
        cacheManager.putCacheValue(cacheContext);
        return pictureVOPage;
    }

    @Override
    public void doPictureReview(PictureReviewDto pictureReviewDto, User loginUser) {
        //1、校验数据能否审核
        Long id = pictureReviewDto.getId();
        Integer reviewStatus = pictureReviewDto.getReviewStatus();
        PictureReviewStatusEnum pictureReviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || pictureReviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(pictureReviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2、获取旧图片信息
        // 构造 QueryWrapper
        Long spaceId=pictureReviewDto.getSpaceId()==null?0:pictureReviewDto.getSpaceId();
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)         // 根据主键 id 查询
                .eq("spaceId", spaceId); // 附加 spaceId 条件

        Picture oldPicture = this.getOne(queryWrapper);

//        Picture oldPicture = this.getById(pictureReviewDto.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        if (reviewStatus.equals(oldPicture.getReviewStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复审核后");
        }
        //更新审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewDto, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewMessage(pictureReviewDto.getReviewMessage());
        updatePicture.setReviewTime(new Date());

        // 构造 UpdateWrapper
        UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id) // 指定主键条件，批量更新则使用 in 传递多条
                .eq("spaceId", spaceId);      // 补充条件 spaceId=xxx

        // 执行更新
        boolean result = this.update(updatePicture, updateWrapper);

//        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 填充审核参数
     *
     * @param picture   实体对象
     * @param loginUser 登录用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动审核通过");
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public void deletePicture(long pictureId,long spaceId, User loginUser) {
        //图片应只能管理员/本人删除
        //查询该图片是否存在
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", pictureId)         // 根据主键 id 查询
                .eq("spaceId", spaceId); // 附加 spaceId 条件

        Picture picture = this.getOne(queryWrapper);
//        Picture picture = this.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //验证权限
//        checkPicturePermission(loginUser, picture);
//        if (!loginUser.getId().equals(picture.getUserId()) && !userService.isAdmin(loginUser)) {
//            throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
//        }
        //删除图片开启事物、删除成功后释放额度
        transactionTemplate.execute(status -> {
            // 执行删除
            boolean result = this.remove(queryWrapper);

            //删除图片
//            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
//            spaceId = picture.getSpaceId();
            if (spaceId != 0) {
                boolean update = spaceService.lambdaUpdate().eq(Space::getId, spaceId)
                        .setSql("totalCount = totalCount -1")
                        .setSql("totalSize = totalSize -" + picture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return true;
        });

        //异步清理文件
        this.clearPictureFile(picture);
    }

    @Async //异步执行
    @Override
    public void clearPictureFile(Picture oldPicture) {
        String pictureUrl = oldPicture.getUrl();
        Long count = this.lambdaQuery().eq(Picture::getUrl, pictureUrl).count();
        //如果该图片被其他图片引用，则不删除文件
        if (count > 1) {
            return;
        }
        List<String> keys = new ArrayList<>();
        keys.add(pictureUrl);
        //原图
        String originUrl = oldPicture.getOriginUrl();
        if (StrUtil.isNotBlank(originUrl)) {
            keys.add(originUrl);
        }
        //缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            keys.add(thumbnailUrl);
        }

        cosManager.deleteObjects(keys);
    }

    @Override
    public void checkPicturePermission(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.USER_NOT_PERMISSION);
            }
        }
    }

    /**
     * AI 扩图
     * @param createPictureOutPaintingTaskDto 扩图任务请求参数
     * @param loginUser 当前登录用户
     * @return 扩图任务信息
     */
    @Override
    public CreateOutPaintingTaskVO createPictureOutPaintingTask(CreatePictureOutPaintingTaskDto createPictureOutPaintingTaskDto, User loginUser) {
        //1、获取图片信息
        Long pictureId = createPictureOutPaintingTaskDto.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId)).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        //2、权限校验
//        this.checkPicturePermission(loginUser, picture);
        //3、组装 ai 扩图 请求参数
        CreateOutPaintingTaskDto taskDto = new CreateOutPaintingTaskDto();
        CreateOutPaintingTaskDto.Input input = new CreateOutPaintingTaskDto.Input();
        input.setImageUrl(picture.getUrl());
        taskDto.setInput(input);
        BeanUtils.copyProperties(createPictureOutPaintingTaskDto, taskDto);
        //3、调用aliYunAi 扩图任务
        return aliYunAiApi.createOutPaintingTask(taskDto);
    }
}
