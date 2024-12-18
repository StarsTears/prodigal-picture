package com.prodigal.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.prodigal.system.constant.CacheConstant;
import com.prodigal.system.constant.FilePathConstant;
import com.prodigal.system.constant.UserConstant;
import com.prodigal.system.exception.BusinessException;
import com.prodigal.system.exception.ErrorCode;
import com.prodigal.system.exception.ThrowUtils;
import com.prodigal.system.manager.FileManager;
import com.prodigal.system.manager.strategy.CacheContext;
import com.prodigal.system.manager.CacheManager;
import com.prodigal.system.manager.upload.FilePictureUpload;
import com.prodigal.system.manager.upload.PictureUploadTemplate;
import com.prodigal.system.manager.upload.UrlPictureUpload;
import com.prodigal.system.model.dto.file.UploadPictureResult;
import com.prodigal.system.model.dto.picture.PictureQueryDto;
import com.prodigal.system.model.dto.picture.PictureReviewDto;
import com.prodigal.system.model.dto.picture.PictureUploadByBatchDto;
import com.prodigal.system.model.dto.picture.PictureUploadDto;
import com.prodigal.system.model.entity.Picture;
import com.prodigal.system.model.entity.User;
import com.prodigal.system.model.enums.PictureReviewStatusEnum;
import com.prodigal.system.model.vo.PictureVO;
import com.prodigal.system.model.vo.UserVO;
import com.prodigal.system.service.PictureService;
import com.prodigal.system.mapper.PictureMapper;
import com.prodigal.system.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
                                                                .initialCapacity(1024)
                                                                .maximumSize(10000L)
                                                                // 缓存 5 分钟移除
                                                                .expireAfterWrite(5L, TimeUnit.MINUTES)
                                                                .build();
    @Resource
    private CacheManager cacheManager;

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
     * @param inputSource    数据源
     * @param pictureUploadDto 图片请求参数
     * @param loginUser        登录用户
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadDto pictureUploadDto, User loginUser) {
        if (inputSource == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"上传图片数据源为空");
        }
        ThrowUtils.throwIf(loginUser == null, ErrorCode.USER_NOT_AUTHORIZED);
        //判断新增还是修改
        Long pictureID = null;
        if (pictureUploadDto != null) {
            pictureID = pictureUploadDto.getId();
        }
        //校验图片是否存在
        if (pictureID != null) {
             Picture picture = this.getById(pictureID);
             ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR,"图片不存在");
             //验权
            boolean isCan = picture.getUserId().equals(loginUser.getId());
            ThrowUtils.throwIf(!isCan && !userService.isAdmin(loginUser), ErrorCode.USER_NOT_AUTHORIZED, "用户没有权限修改该图片");
        }
        /*
            上传图片-根据用户id来划分目录
            管理员上传的到私有下
            用户上传的到公有
         */
        String uploadPrefix = String.format("%s/%S", FilePathConstant.PICTURE_PUBLIC_PREFIX, loginUser.getId());
        if (loginUser.getUserRole().contains(UserConstant.SUPER_ADMIN_ROLE)) {
            uploadPrefix = String.format("%s/%S", FilePathConstant.PICTURE_PRIVATE_PREFIX, "super");
        }
        //通过数据源判断使用的上传模板（file、url）
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String){
            pictureUploadTemplate = urlPictureUpload;
        }

        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource,uploadPrefix);
        //构造需要存储的图片信息
        Picture picture = new Picture();
        picture.setOriginUrl(uploadPictureResult.getOriginUrl());
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setSourceUrl(uploadPictureResult.getSourceUrl());
        String picName = uploadPictureResult.getPicName();
        //抓取图片，如果已经写了图片名称，则使用用户填写的图片名称
        if (pictureUploadDto!=null && StrUtil.isNotBlank(pictureUploadDto.getPicName())){
            picName = pictureUploadDto.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //补充审核参数
        this.fillReviewParams(picture, loginUser);
        //如果pictureID 不为空则进行更新
        if (pictureID != null) {
            picture.setId(pictureID);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVO(picture);
    }

    /**
     * 从必应网站抓取图片（默认必应）
     * @param pictureUploadByBatchDto 图片批量上传请求参数
     * @param loginUser 登录用户
     */
    @Override
    public int uploadPictureByBatch(PictureUploadByBatchDto pictureUploadByBatchDto, User loginUser){
        String searchText = pictureUploadByBatchDto.getSearchText();
        //图片名称前缀为空，则使用关键词
        String namePrefix = pictureUploadByBatchDto.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)){
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
        Document document ;
        try{
            document = Jsoup.connect(fetchUrl).get();
        }catch (Exception e){
            log.error("图片批量上传-抓取图片地址失败",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"图片批量上传-获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"图片批量上传-获取图片元素失败");
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
                log.error("URL解码报错",e);
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"URL解码报错哦");
            }
            String query = dUrl.substring(dUrl.indexOf('?') + 1);

            // 将URL参数解析为JSON对象
            Map<String,String> json = new HashMap<>();
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
            String fileUrl= json.get("riu");
            if (StrUtil.isBlank(fileUrl)){
                fileUrl = json.get("mediaurl");
            }
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1){
                fileUrl = fileUrl.substring(0,questionMarkIndex);
            }
            //以免数据重复，可以将其查出与数据的源url进行对比

            //上传图片
            PictureUploadDto pictureUploadDto = new PictureUploadDto();
            if (StrUtil.isNotBlank(namePrefix)){
                pictureUploadDto.setPicName(namePrefix + (uploadCount+1));
            }
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadDto, loginUser);
                log.info("图片批量上传-上传成功->id:{}",pictureVO.getId());
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
        wrapper.eq(ObjUtil.isNotEmpty(pictureQueryDto.getId()), Picture::getId, pictureQueryDto.getId())
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
                .like(StrUtil.isNotBlank(pictureQueryDto.getReviewMessage()), Picture::getReviewMessage, pictureQueryDto.getReviewMessage());
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
     * @param pictureQueryDto
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVOPageCache(PictureQueryDto pictureQueryDto, HttpServletRequest request) {
        long current = pictureQueryDto.getCurrent();
        long size = pictureQueryDto.getPageSize();
        //先去查询缓存，未命中再去查询数据库
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();

        String queryCondition = JSONUtil.toJsonStr(pictureQueryDto);
        String hashKey = DigestUtils.md5Hex(queryCondition);
        String caffeineKey = String.format("%s%s", CacheConstant.PICTURE_PAGE_CAFFEINE_CACHE_KEY, hashKey);
        String cachedValue = LOCAL_CACHE.getIfPresent(caffeineKey);
        if (cachedValue!=null){
            //命中本地缓存，返回结果
            Page<PictureVO> page = JSONUtil.toBean(cachedValue, Page.class);
            return page;
        }

        String redisKey = String.format("%s%s", CacheConstant.PICTURE_PAGE_REDIS_CACHE_KEY, hashKey);
        cachedValue = opsForValue.get(redisKey);
        if (cachedValue!=null){
            Page<PictureVO> page = JSONUtil.toBean(cachedValue, Page.class);
            LOCAL_CACHE.put(caffeineKey, cachedValue);
            return page;
        }
        //操作数据库
        Page<Picture> picturePage = this.page(new Page<>(current, size), this.getQueryWrapper(pictureQueryDto));
        Page<PictureVO> pictureVOPage = this.getPictureVOPage(picturePage, request);
        //将数据存到缓存 5~10分钟 随机过期，防止雪崩
        String value = JSONUtil.toJsonStr(pictureVOPage);
        int expireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(redisKey, value,expireTime, TimeUnit.SECONDS);
        LOCAL_CACHE.put(caffeineKey, value);
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
        Picture oldPicture = this.getById(pictureReviewDto.getId());
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

        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 填充审核参数
     * @param picture 实体对象
     * @param loginUser 登录用户
     */
    @Override
    public void fillReviewParams(Picture picture,User loginUser){
        if (userService.isAdmin(loginUser)){
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动审核通过");
            picture.setReviewTime(new Date());
        }else{
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }
}




