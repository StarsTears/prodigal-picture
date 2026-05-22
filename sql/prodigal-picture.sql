/*
 Navicat Premium Data Transfer

 Source Server         : local-3306
 Source Server Type    : MySQL
 Source Server Version : 80042
 Source Host           : localhost:3306
 Source Schema         : prodigal-picture

 Target Server Type    : MySQL
 Target Server Version : 80042
 File Encoding         : 65001

 Date: 10/04/2026 22:03:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for picture
-- ----------------------------
DROP TABLE IF EXISTS `picture`;
CREATE TABLE `picture`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `originUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原图url',
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片 url',
  `thumbnailUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '缩略图url',
  `sourceUrl` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '源图片url',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片名称',
  `introduction` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简介',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类',
  `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签（JSON 数组）',
  `picColor` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片主色调',
  `picSize` bigint NULL DEFAULT NULL COMMENT '图片体积',
  `picWidth` int NULL DEFAULT NULL COMMENT '图片宽度',
  `picHeight` int NULL DEFAULT NULL COMMENT '图片高度',
  `picScale` double NULL DEFAULT NULL COMMENT '图片宽高比例',
  `picFormat` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片格式',
  `userId` bigint NOT NULL COMMENT '创建用户 id',
  `spaceId` bigint(20) UNSIGNED ZEROFILL NOT NULL DEFAULT 00000000000000000000 COMMENT '空间 id（为空表示公共空间）',
  `reviewerId` bigint NULL DEFAULT NULL COMMENT '审核人 ID',
  `reviewStatus` int NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
  `reviewMessage` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核信息',
  `reviewTime` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `viewQuantity` bigint(20) UNSIGNED ZEROFILL NOT NULL DEFAULT 00000000000000000000 COMMENT '查看次数',
  `shareQuantity` bigint(20) UNSIGNED ZEROFILL NOT NULL DEFAULT 00000000000000000000 COMMENT '分享次数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_introduction`(`introduction` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_tags`(`tags` ASC) USING BTREE,
  INDEX `idx_userId`(`userId` ASC) USING BTREE,
  INDEX `idx_reviewStatus`(`reviewStatus` ASC) USING BTREE,
  INDEX `idx_spaceId`(`spaceId` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1957659760501825538 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '图片' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of picture
-- ----------------------------
INSERT INTO `picture` VALUES (1943574703299616770, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-07-11_DWWBRXCK.PNG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-07-11_DWWBRXCK.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-07-11_DWWBRXCK_thumbnail.PNG', NULL, 'wallhaven-x19mjz', NULL, NULL, NULL, '0x404040', 29026, 1920, 1080, 1.78, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-07-11 07:34:54', '2025-07-11 15:34:54', '2025-07-11 15:34:54', '2025-07-11 15:34:54', 0, 00000000000000000005, 00000000000000000000);
INSERT INTO `picture` VALUES (1943579531174088705, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-07-11_S6Z9NWDQ.PNG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-07-11_S6Z9NWDQ.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-07-11_S6Z9NWDQ_thumbnail.PNG', NULL, 'VMI采购流程图', 'VMI采购流程图', NULL, '[]', '0xe0e0e0', 201506, 2463, 1237, 1.99, 'webp', 1943570823434137602, 01943578683371032578, 1943570823434137602, 1, '管理员自动审核通过', '2025-07-11 07:54:43', '2025-07-11 15:54:04', '2025-07-11 07:54:43', '2025-07-11 15:54:42', 0, 00000000000000000001, 00000000000000000000);
INSERT INTO `picture` VALUES (1957635409958244353, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_SGNPZFFS.PNG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_SGNPZFFS.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_SGNPZFFS_thumbnail.PNG', NULL, '屏幕截图 2025-08-19 104638', 'WMS-出库', NULL, '[]', '0xe0e0e0', 24186, 1161, 769, 1.51, 'webp', 1943570823434137602, 01943578683371032578, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 10:47:24', '2025-08-19 10:47:07', '2025-08-19 10:47:24', '2025-08-19 10:47:23', 0, 00000000000000000001, 00000000000000000000);
INSERT INTO `picture` VALUES (1957635826700095489, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_HITUDPYD.PNG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_HITUDPYD.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_HITUDPYD_thumbnail.PNG', NULL, 'logstash', 'logstash', '', '[\"学习\"]', '0xe0e0e0', 17936, 1051, 676, 1.55, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 10:49:22', '2025-08-19 10:48:46', '2025-08-19 10:49:22', '2025-08-19 10:49:21', 0, 00000000000000000010, 00000000000000000000);
INSERT INTO `picture` VALUES (1957646711531311105, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_DBPL7W12.PNG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_DBPL7W12.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_DBPL7W12_thumbnail.PNG', 'https://files.codelife.cc/wallhaven/full/k7/wallhaven-k7qo3d.png', 'wallhaven-k7qo3d', 'wallpaper壁纸', '壁纸', '[\"背景\"]', '0x202c43', 993356, 3840, 2160, 1.78, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 11:32:44', '2025-08-19 11:32:02', '2025-08-19 11:32:44', '2025-08-19 11:32:44', 0, 00000000000000000002, 00000000000000000000);
INSERT INTO `picture` VALUES (1957653409608343554, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/public/1943581046668398593/2025-08-19_MOVHOUZ0.PNG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/public/1943581046668398593/2025-08-19_MOVHOUZ0.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/public/1943581046668398593/2025-08-19_MOVHOUZ0_thumbnail.PNG', NULL, '屏幕截图 2025-07-18 125033', 'ELK', NULL, '[\"学习\"]', '0xe0e0e0', 13284, 1285, 347, 3.7, 'webp', 1943581046668398593, 00000000000000000000, 1943570823434137602, 1, '审核通过', '2025-08-19 12:00:14', '2025-08-19 11:58:39', '2025-08-19 11:59:16', '2025-08-19 12:00:14', 0, 00000000000000000001, 00000000000000000000);
INSERT INTO `picture` VALUES (1957653668375928833, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/public/1943581046668398593/2025-08-19_I3E3OPXU.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/public/1943581046668398593/2025-08-19_I3E3OPXU.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/public/1943581046668398593/2025-08-19_I3E3OPXU_thumbnail.JPG', NULL, '猫', '小猫', NULL, '[\"生活\"]', '0x6181a1', 26948, 1080, 1080, 1, 'webp', 1943581046668398593, 00000000000000000000, 1943570823434137602, 1, '审核通过', '2025-08-19 12:00:20', '2025-08-19 11:59:40', '2025-08-19 11:59:55', '2025-08-19 12:00:19', 0, 00000000000000000003, 00000000000000000000);
INSERT INTO `picture` VALUES (1957658309146812417, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_ORFRPRZU.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_ORFRPRZU.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_ORFRPRZU_thumbnail.JPG', 'https://img95.699pic.com/photo/60020/3870.jpg_wh860.jpg', '小猫1', NULL, NULL, NULL, '0xe0e0e0', 45778, 860, 573, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:18:07', '2025-08-19 12:18:07', '2025-08-19 12:18:07', '2025-08-19 12:18:07', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957658328809709569, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_WYQLJYJF.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_WYQLJYJF.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_WYQLJYJF_thumbnail.JPG', 'https://img95.699pic.com/photo/32197/5628.jpg_wh860.jpg', '小猫2', NULL, NULL, NULL, '0x3c3621', 125784, 860, 573, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:18:12', '2025-08-19 12:18:11', '2025-08-19 12:18:11', '2025-08-19 12:18:11', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957658345461096449, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_GGWI8Y2N.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_GGWI8Y2N.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_GGWI8Y2N_thumbnail.JPG', 'https://img95.699pic.com/photo/60073/6218.jpg_wh860.jpg', '小猫3', NULL, NULL, NULL, '0x73452f', 58060, 860, 573, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:18:16', '2025-08-19 12:18:15', '2025-08-19 12:18:15', '2025-08-19 12:18:15', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957658366013186049, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_AYXX6A30.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_AYXX6A30.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_AYXX6A30_thumbnail.JPG', 'http://d.ifengimg.com/q100/img1.ugc.ifeng.com/newugc/20190419/17/wemedia/f0e512c77b0b5a6ad725c3cd3bbdca079f004372_size292_w1024_h683.jpg', '小猫4', NULL, NULL, NULL, '0xc0a080', 360844, 1024, 683, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:18:21', '2025-08-19 12:18:20', '2025-08-19 12:18:20', '2025-08-19 12:18:20', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957658373202223105, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_1Y3QB7NO.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_1Y3QB7NO.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_1Y3QB7NO_thumbnail.JPG', 'https://img95.699pic.com/photo/60021/2533.jpg_wh860.jpg', '小猫5', NULL, NULL, NULL, '0xe0c2a4', 33332, 860, 573, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:18:22', '2025-08-19 12:18:22', '2025-08-19 12:18:22', '2025-08-19 12:18:22', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659686715629570, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_L6QY2IT6.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_L6QY2IT6.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_L6QY2IT6_thumbnail.JPG', 'https://pic.616pic.com/ys_bnew_img/00/07/31/UEizrA13I4.jpg', '小狗1', NULL, NULL, NULL, '0xe0e0e0', 82714, 800, 1084, 0.74, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:36', '2025-08-19 12:23:35', '2025-08-19 12:23:35', '2025-08-19 12:23:35', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659696475774978, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_LGYAXYUB.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_LGYAXYUB.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_LGYAXYUB_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_231045885120_2.jpg', '小狗2', NULL, NULL, NULL, '0xd6b898', 591678, 1024, 683, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:38', '2025-08-19 12:23:37', '2025-08-19 12:23:37', '2025-08-19 12:23:37', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659699160129538, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_RS6R1VL9.JPEG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_RS6R1VL9.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_RS6R1VL9_thumbnail.JPEG', 'http://pic.qianye88.com/4kaijic5331ea4-e35d-3baf-8b09-c37769139446.jpeg', '小狗3', NULL, NULL, NULL, '0xc69177', 57264, 1275, 717, 1.78, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:39', '2025-08-19 12:23:38', '2025-08-19 12:23:38', '2025-08-19 12:23:38', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659710048542721, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_5DODLHZR.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_5DODLHZR.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_5DODLHZR_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_232653011123_2.jpg', '小狗4', NULL, NULL, NULL, '0x7a5e1f', 577730, 1024, 683, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:41', '2025-08-19 12:23:41', '2025-08-19 12:23:41', '2025-08-19 12:23:41', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659720207147010, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_94J8AW1P.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_94J8AW1P.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_94J8AW1P_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_233426065127_2.jpg', '小狗5', NULL, NULL, NULL, '0x4c4b34', 714300, 1024, 683, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:44', '2025-08-19 12:23:43', '2025-08-19 12:23:43', '2025-08-19 12:23:43', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659735075954690, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_WJBAH1WV.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_WJBAH1WV.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_WJBAH1WV_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_233220049127_2.jpg', '小狗6', NULL, NULL, NULL, '0xdfc0a1', 988422, 1024, 1024, 1, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:47', '2025-08-19 12:23:47', '2025-08-19 12:23:47', '2025-08-19 12:23:47', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659737500262402, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_WDWRZEAV.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_WDWRZEAV.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_WDWRZEAV_thumbnail.JPG', 'https://pic.nximg.cn/file/20230816/34894542_215319787101_2.jpg', '小狗7', NULL, NULL, NULL, '0x000', 35044, 1024, 683, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:48', '2025-08-19 12:23:47', '2025-08-19 12:23:47', '2025-08-19 12:23:47', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659740889260033, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_XIDEV5XW.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_XIDEV5XW.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_XIDEV5XW_thumbnail.JPG', 'https://pic.nximg.cn/file/20230922/12075709_143540091100_2.jpg', '小狗8', NULL, NULL, NULL, '0xe0c7af', 88880, 717, 1024, 0.7, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:48', '2025-08-19 12:23:48', '2025-08-19 12:23:48', '2025-08-19 12:23:48', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659750049619970, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_DXXWHQV1.JPEG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_DXXWHQV1.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_DXXWHQV1_thumbnail.JPEG', 'https://gd-hbimg.huaban.com/2fad4daff679f117ba9b91d3f27f185687463ad310461b-hsd5j9_fw658', '小狗9', NULL, NULL, NULL, '0xc0a284', 60788, 658, 833, 0.79, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:51', '2025-08-19 12:23:50', '2025-08-19 12:23:50', '2025-08-19 12:23:50', 0, 00000000000000000000, 00000000000000000000);
INSERT INTO `picture` VALUES (1957659760501825538, 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_057JQS9U.JPG', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_057JQS9U.webp', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/picture/private/SUPER/2025-08-19_057JQS9U_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_232459001125_2.jpg', '小狗10', NULL, NULL, NULL, '0x4e4a3c', 563758, 1024, 683, 1.5, 'webp', 1943570823434137602, 00000000000000000000, 1943570823434137602, 1, '管理员自动审核通过', '2025-08-19 12:23:53', '2025-08-19 12:23:53', '2025-08-19 12:23:53', '2025-08-19 12:23:53', 0, 00000000000000000000, 00000000000000000000);

-- ----------------------------
-- Table structure for space
-- ----------------------------
DROP TABLE IF EXISTS `space`;
CREATE TABLE `space`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spaceName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '空间名称',
  `spaceLevel` int NULL DEFAULT 0 COMMENT '空间级别：0-普通版 1-专业版 2-旗舰版',
  `maxSize` bigint NULL DEFAULT 0 COMMENT '空间图片的最大总大小',
  `maxCount` bigint NULL DEFAULT 0 COMMENT '空间图片的最大数量',
  `totalSize` bigint NULL DEFAULT 0 COMMENT '当前空间下图片的总大小',
  `totalCount` bigint NULL DEFAULT 0 COMMENT '当前空间下的图片数量',
  `userId` bigint NOT NULL COMMENT '创建用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `spaceType` int NOT NULL DEFAULT 0 COMMENT '空间类型：0-私有 1-团队',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userId`(`userId` ASC) USING BTREE,
  INDEX `idx_spaceName`(`spaceName` ASC) USING BTREE,
  INDEX `idx_spaceLevel`(`spaceLevel` ASC) USING BTREE,
  INDEX `idx_spaceType`(`spaceType` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1957640919323353089 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '空间' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of space
-- ----------------------------
INSERT INTO `space` VALUES (1943578683371032578, 'Admin', 2, 10485760000, 10000, 225692, 2, 1943570823434137602, '2025-07-11 15:50:42', '2025-07-11 15:50:42', '2025-08-19 10:47:07', 0, 0);

-- ----------------------------
-- Table structure for space_user
-- ----------------------------
DROP TABLE IF EXISTS `space_user`;
CREATE TABLE `space_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spaceId` bigint NOT NULL COMMENT '空间 id',
  `userId` bigint NOT NULL COMMENT '用户 id',
  `spaceRole` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'viewer' COMMENT '空间角色：viewer/editor/admin',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_spaceId_userId`(`spaceId` ASC, `userId` ASC) USING BTREE,
  INDEX `idx_spaceId`(`spaceId` ASC) USING BTREE,
  INDEX `idx_userId`(`userId` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '空间用户关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of space_user
-- ----------------------------
INSERT INTO `space_user` VALUES (7, 1957640919323353089, 1943570823434137602, 'admin', '2025-08-19 11:09:01', '2025-08-19 11:09:01');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `userEmail` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `userAvatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像',
  `userProfile` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户简介',
  `userRole` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/administrator',
  `vipNumber` bigint NULL DEFAULT NULL COMMENT '会员编码',
  `inviteUser` bigint NULL DEFAULT NULL COMMENT '邀请用户ID',
  `shareCode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分享码',
  `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_userAccount`(`userAccount` ASC) USING BTREE,
  INDEX `idx_userName`(`userName` ASC) USING BTREE,
  INDEX `idx_vipNumber`(`vipNumber` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1970043291593551874 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1943570823434137602, 'admin', 'a8e43a560b3b73db965c699ccf5e7c3c', '198116203@qq.com', 'admin', NULL, NULL, 'administrator', 0, NULL, NULL, '2025-07-11 15:19:28', '2025-07-11 15:19:28', '2025-07-11 15:20:14', 0);
INSERT INTO `user` VALUES (1943581046668398593, 'test', '2f56b8d1c9c5bccff746c5af41bf0645', 'langprodigal@outlook.com', '测试', '', '', 'user', NULL, NULL, NULL, '2025-07-11 16:00:06', '2025-07-11 16:00:06', '2025-07-11 16:00:06', 0);
INSERT INTO `user` VALUES (1944630159585492993, 'ranlang@x-ri.com', '2f56b8d1c9c5bccff746c5af41bf0645', 'ranlang@x-ri.com', 'ranlang@x-ri.com', NULL, NULL, 'user', NULL, NULL, NULL, '2025-07-14 13:28:54', '2025-07-14 13:28:54', '2025-07-14 13:28:54', 0);
INSERT INTO `user` VALUES (1944632515823845377, '2812632023@qq.com', '2f56b8d1c9c5bccff746c5af41bf0645', '2812632023@qq.com', '2812632023@qq.com', NULL, NULL, 'user', NULL, NULL, NULL, '2025-07-14 13:38:15', '2025-07-14 13:38:15', '2025-07-14 13:38:15', 0);
INSERT INTO `user` VALUES (1970043291593551874, 'prodigal.lang@qq.com', '2f56b8d1c9c5bccff746c5af41bf0645', 'prodigal.lang@qq.com', 'prodigal.lang@qq.com', NULL, NULL, 'user', NULL, NULL, NULL, '2025-09-22 16:31:37', '2025-09-22 16:31:37', '2025-09-22 16:31:37', 0);

SET FOREIGN_KEY_CHECKS = 1;
