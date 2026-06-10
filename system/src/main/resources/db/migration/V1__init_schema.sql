-- 基线 schema：user / space / space_user / picture / sys_dict
-- picture_{spaceId} 分表由 DynamicShardingManager 在运行时动态创建

CREATE TABLE IF NOT EXISTS `user`
(
    `id`           varchar(255)                           NOT NULL COMMENT 'id',
    `user_account` varchar(256)                           NOT NULL COMMENT '账号',
    `user_password` varchar(512)                           NOT NULL COMMENT '密码',
    `user_email`    varchar(256)                           NULL COMMENT '邮箱',
    `user_name`     varchar(256)                           NULL COMMENT '用户昵称',
    `user_avatar`   varchar(1024)                          NULL COMMENT '用户头像',
    `user_profile`  varchar(512)                           NULL COMMENT '用户简介',
    `user_role`     varchar(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin/administrator',
    `vip_number`    bigint                                 NULL COMMENT '会员编码',
    `invite_user`   varchar(255)                           NULL COMMENT '邀请用户ID',
    `share_code`    varchar(255)                           NULL COMMENT '分享码',
    `edit_time`     datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`     tinyint      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_account` (`user_account`),
    INDEX `idx_user_name` (`user_name`),
    INDEX `idx_vip_number` (`vip_number`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户';

-- 初始化超级管理员（账号: admin, 密码: administrator）
INSERT INTO `user` (`id`, `user_account`, `user_password`, `user_email`, `user_name`, `user_role`, `vip_number`, `share_code`)
VALUES ('1', 'admin', 'a8e43a560b3b73db965c699ccf5e7c3c', 'admin@prodigal.com', '超级管理员', 'administrator', '1', 'ADMIN001');

CREATE TABLE IF NOT EXISTS `space`
(
    `id`          varchar(255)                         NOT NULL COMMENT 'id',
    `space_name`  varchar(128)                         NULL COMMENT '空间名称',
    `space_type`  int        DEFAULT 0                 NOT NULL COMMENT '空间类型：0-私有 1-团队',
    `space_level` int        DEFAULT 0                 NULL COMMENT '空间级别：0-普通版 1-专业版 2-旗舰版',
    `max_size`    bigint     DEFAULT 0                 NULL COMMENT '空间图片的最大总大小',
    `max_count`   bigint     DEFAULT 0                 NULL COMMENT '空间图片的最大数量',
    `total_size`  bigint     DEFAULT 0                 NULL COMMENT '当前空间下图片的总大小',
    `total_count` bigint     DEFAULT 0                 NULL COMMENT '当前空间下的图片数量',
    `user_id`     varchar(255)                         NOT NULL COMMENT '创建用户 id',
    `create_time` datetime   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `edit_time`   datetime   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `update_time` datetime   DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint    DEFAULT 0                 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_space_name` (`space_name`),
    INDEX `idx_space_level` (`space_level`),
    INDEX `idx_space_type` (`space_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='空间';

CREATE TABLE IF NOT EXISTS `space_user`
(
    `id`          varchar(255)                           NOT NULL COMMENT 'id',
    `space_id`    varchar(255)                           NOT NULL COMMENT '空间 id',
    `user_id`     varchar(255)                           NOT NULL COMMENT '用户 id',
    `space_role`  varchar(128) DEFAULT 'viewer'          NULL COMMENT '空间角色：viewer/editor/admin',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_space_id_user_id` (`space_id`, `user_id`),
    INDEX `idx_space_id` (`space_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='空间用户关联';

CREATE TABLE IF NOT EXISTS `picture`
(
    `id`            varchar(255)                            NOT NULL COMMENT 'id',
    `name`          varchar(128)                           NOT NULL COMMENT '图片名称',
    `introduction`  varchar(512)                           NULL COMMENT '简介',
    `category`      varchar(64)                            NULL COMMENT '分类',
    `tags`          varchar(512)                           NULL COMMENT '标签（JSON 数组）',
    `origin_url`     varchar(512)                           NULL COMMENT '原图url',
    `url`           varchar(512)                           NOT NULL COMMENT '图片 url',
    `thumbnail_url`  varchar(512)                           NULL COMMENT '缩略图url',
    `source_url`     varchar(512)                           NULL COMMENT '源图片url',
    `pic_color`      varchar(16)                            NULL COMMENT '图片主色调',
    `pic_size`       bigint                                 NULL COMMENT '图片体积',
    `pic_width`      int                                    NULL COMMENT '图片宽度',
    `pic_height`     int                                    NULL COMMENT '图片高度',
    `pic_scale`      double                                 NULL COMMENT '图片宽高比例',
    `pic_format`     varchar(32)                            NULL COMMENT '图片格式',
    `user_id`        varchar(255)                           NOT NULL COMMENT '创建用户 id',
    `space_id`       varchar(255)                           NOT NULL DEFAULT '0' COMMENT '空间 id（0 表示公共空间）',
    `view_quantity`  bigint                                 NOT NULL DEFAULT 0 COMMENT '查看次数',
    `share_quantity` bigint                                NOT NULL DEFAULT 0 COMMENT '分享次数',
    `reviewer_id`    varchar(255)                           NULL COMMENT '审核人 ID',
    `review_status`  int          DEFAULT 0                 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    `review_message` varchar(512)                           NULL COMMENT '审核信息',
    `review_time`    datetime                               NULL COMMENT '审核时间',
    `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `edit_time`      datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`      tinyint      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_name` (`name`),
    INDEX `idx_introduction` (`introduction`),
    INDEX `idx_category` (`category`),
    INDEX `idx_tags` (`tags`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_review_status` (`review_status`),
    INDEX `idx_space_id` (`space_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='图片';

-- 列顺序：id, url, thumbnail_url, source_url, origin_url, name, introduction, category, tags, pic_color, pic_size, pic_width, pic_height, pic_scale, pic_format, user_id, space_id, view_quantity, share_quantity, reviewer_id, review_status, review_message, review_time, create_time, edit_time, update_time, is_delete
INSERT INTO `picture` (`id`, `url`, `thumbnail_url`, `source_url`, `origin_url`, `name`, `introduction`, `category`, `tags`, `pic_color`, `pic_size`, `pic_width`, `pic_height`, `pic_scale`, `pic_format`, `user_id`, `space_id`, `view_quantity`, `share_quantity`, `reviewer_id`, `review_status`, `review_message`, `review_time`, `create_time`, `edit_time`, `update_time`, `is_delete`)
VALUES ('1943574703299616770', '/picture/private/SUPER/2025-07-11_DWWBRXCK.PNG', '/picture/private/SUPER/2025-07-11_DWWBRXCK.webp', '/picture/private/SUPER/2025-07-11_DWWBRXCK_thumbnail.PNG', NULL, 'wallhaven-x19mjz', NULL, NULL, NULL, '0x404040', 29026, 1920, 1080, 1.78, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-07-11 07:34:54', '2025-07-11 15:34:54', '2025-07-11 15:34:54', '2026-06-07 08:51:22', 0),
       ('1943579531174088705', '/picture/private/SUPER/2025-07-11_S6Z9NWDQ.PNG', '/picture/private/SUPER/2025-07-11_S6Z9NWDQ.webp', '/picture/private/SUPER/2025-07-11_S6Z9NWDQ_thumbnail.PNG', NULL, 'VMI采购流程图', 'VMI采购流程图', NULL, '[]', '0xe0e0e0', 201506, 2463, 1237, 1.99, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-07-11 07:54:43', '2025-07-11 15:54:04', '2025-07-11 07:54:43', '2026-06-07 08:51:22', 0),
       ('1957635409958244353', '/picture/private/SUPER/2025-08-19_SGNPZFFS.PNG', '/picture/private/SUPER/2025-08-19_SGNPZFFS.webp', '/picture/private/SUPER/2025-08-19_SGNPZFFS_thumbnail.PNG', NULL, '屏幕截图 2025-08-19 104638', 'WMS-出库', NULL, '[]', '0xe0e0e0', 24186, 1161, 769, 1.51, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 10:47:24', '2025-08-19 10:47:07', '2025-08-19 10:47:24', '2026-06-07 08:51:22', 0),
       ('1957635826700095489', '/picture/private/SUPER/2025-08-19_HITUDPYD.PNG', '/picture/private/SUPER/2025-08-19_HITUDPYD.PNG', '/picture/private/SUPER/2025-08-19_HITUDPYD.PNG', NULL, 'logstash', 'logstash', '', '["学习"]', '0xe0e0e0', 17936, 1051, 676, 1.55, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 10:49:22', '2025-08-19 10:48:46', '2025-08-19 10:49:22', '2026-06-07 08:51:22', 0),
       ('1957646711531311105', '/picture/private/SUPER/2025-08-19_DBPL7W12.PNG', '/picture/private/SUPER/2025-08-19_DBPL7W12.webp', '/picture/private/SUPER/2025-08-19_DBPL7W12_thumbnail.PNG', 'https://files.codelife.cc/wallhaven/full/k7/wallhaven-k7qo3d.png', 'wallhaven-k7qo3d', 'wallpaper壁纸', '壁纸', '["背景"]', '0x202c43', 993356, 3840, 2160, 1.78, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 11:32:44', '2025-08-19 11:32:02', '2025-08-19 11:32:44', '2026-06-07 08:51:22', 0),
       ('1957653409608343554', '/picture/public/1943581046668398593/2025-08-19_MOVHOUZ0.PNG', '/picture/public/1943581046668398593/2025-08-19_MOVHOUZ0.webp', '/picture/public/1943581046668398593/2025-08-19_MOVHOUZ0_thumbnail.PNG', NULL, '屏幕截图 2025-07-18 125033', 'ELK', NULL, '["学习"]', '0xe0e0e0', 13284, 1285, 347, 3.7, 'webp', '0', '0', 0, 0, '1', 1, '审核通过', '2025-08-19 12:00:14', '2025-08-19 11:58:39', '2025-08-19 11:59:16', '2026-06-07 08:51:22', 0),
       ('1957653668375928833', '/picture/public/1943581046668398593/2025-08-19_I3E3OPXU.JPG', '/picture/public/1943581046668398593/2025-08-19_I3E3OPXU.webp', '/picture/public/1943581046668398593/2025-08-19_I3E3OPXU_thumbnail.JPG', NULL, '猫', '小猫', NULL, '["生活"]', '0x6181a1', 26948, 1080, 1080, 1, 'webp', '0', '0', 0, 0, '1', 1, '审核通过', '2025-08-19 12:00:20', '2025-08-19 11:59:40', '2025-08-19 11:59:55', '2026-06-07 08:51:22', 0),
       ('1957658309146812417', '/picture/private/SUPER/2025-08-19_ORFRPRZU.JPG', '/picture/private/SUPER/2025-08-19_ORFRPRZU.webp', '/picture/private/SUPER/2025-08-19_ORFRPRZU_thumbnail.JPG', 'https://img95.699pic.com/photo/60020/3870.jpg_wh860.jpg', '小猫1', NULL, NULL, NULL, '0xe0e0e0', 45778, 860, 573, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:18:07', '2025-08-19 12:18:07', '2025-08-19 12:18:07', '2026-06-07 08:51:22', 0),
       ('1957658328809709569', '/picture/private/SUPER/2025-08-19_WYQLJYJF.JPG', '/picture/private/SUPER/2025-08-19_WYQLJYJF.webp', '/picture/private/SUPER/2025-08-19_WYQLJYJF_thumbnail.JPG', 'https://img95.699pic.com/photo/32197/5628.jpg_wh860.jpg', '小猫2', NULL, NULL, NULL, '0x3c3621', 125784, 860, 573, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:18:12', '2025-08-19 12:18:11', '2025-08-19 12:18:11', '2026-06-07 08:51:22', 0),
       ('1957658345461096449', '/picture/private/SUPER/2025-08-19_GGWI8Y2N.JPG', '/picture/private/SUPER/2025-08-19_GGWI8Y2N.webp', '/picture/private/SUPER/2025-08-19_GGWI8Y2N_thumbnail.JPG', 'https://img95.699pic.com/photo/60073/6218.jpg_wh860.jpg', '小猫3', NULL, NULL, NULL, '0x73452f', 58060, 860, 573, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:18:16', '2025-08-19 12:18:15', '2025-08-19 12:18:15', '2026-06-07 08:51:22', 0),
       ('1957658366013186049', '/picture/private/SUPER/2025-08-19_AYXX6A30.JPG', '/picture/private/SUPER/2025-08-19_AYXX6A30.webp', '/picture/private/SUPER/2025-08-19_AYXX6A30_thumbnail.JPG', 'http://d.ifengimg.com/q100/img1.ugc.ifeng.com/newugc/20190419/17/wemedia/f0e512c77b0b5a6ad725c3cd3bbdca079f004372_size292_w1024_h683.jpg', '小猫4', NULL, NULL, NULL, '0xc0a080', 360844, 1024, 683, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:18:21', '2025-08-19 12:18:20', '2025-08-19 12:18:20', '2026-06-07 08:51:22', 0),
       ('1957658373202223105', '/picture/private/SUPER/2025-08-19_1Y3QB7NO.JPG', '/picture/private/SUPER/2025-08-19_1Y3QB7NO.webp', '/picture/private/SUPER/2025-08-19_1Y3QB7NO_thumbnail.JPG', 'https://img95.699pic.com/photo/60021/2533.jpg_wh860.jpg', '小猫5', NULL, NULL, NULL, '0xe0c2a4', 33332, 860, 573, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:18:22', '2025-08-19 12:18:22', '2025-08-19 12:18:22', '2026-06-07 08:51:22', 0),
       ('1957659686715629570', '/picture/private/SUPER/2025-08-19_L6QY2IT6.JPG', '/picture/private/SUPER/2025-08-19_L6QY2IT6.webp', '/picture/private/SUPER/2025-08-19_L6QY2IT6_thumbnail.JPG', 'https://pic.616pic.com/ys_bnew_img/00/07/31/UEizrA13I4.jpg', '小狗1', NULL, NULL, NULL, '0xe0e0e0', 82714, 800, 1084, 0.74, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:36', '2025-08-19 12:23:35', '2025-08-19 12:23:35', '2026-06-07 08:51:22', 0),
       ('1957659696475774978', '/picture/private/SUPER/2025-08-19_LGYAXYUB.JPG', '/picture/private/SUPER/2025-08-19_LGYAXYUB.webp', '/picture/private/SUPER/2025-08-19_LGYAXYUB_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_231045885120_2.jpg', '小狗2', NULL, NULL, NULL, '0xd6b898', 591678, 1024, 683, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:38', '2025-08-19 12:23:37', '2025-08-19 12:23:37', '2026-06-07 08:51:22', 0),
       ('1957659699160129538', '/picture/private/SUPER/2025-08-19_RS6R1VL9.JPEG', '/picture/private/SUPER/2025-08-19_RS6R1VL9.webp', '/picture/private/SUPER/2025-08-19_RS6R1VL9_thumbnail.JPEG', 'http://pic.qianye88.com/4kaijic5331ea4-e35d-3baf-8b09-c37769139446.jpeg', '小狗3', NULL, NULL, NULL, '0xc69177', 57264, 1275, 717, 1.78, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:39', '2025-08-19 12:23:38', '2025-08-19 12:23:38', '2026-06-07 08:51:22', 0),
       ('1957659710048542721', '/picture/private/SUPER/2025-08-19_5DODLHZR.JPG', '/picture/private/SUPER/2025-08-19_5DODLHZR.webp', '/picture/private/SUPER/2025-08-19_5DODLHZR_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_232653011123_2.jpg', '小狗4', NULL, NULL, NULL, '0x7a5e1f', 577730, 1024, 683, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:41', '2025-08-19 12:23:41', '2025-08-19 12:23:41', '2026-06-07 08:51:22', 0),
       ('1957659720207147010', '/picture/private/SUPER/2025-08-19_94J8AW1P.JPG', '/picture/private/SUPER/2025-08-19_94J8AW1P.webp', '/picture/private/SUPER/2025-08-19_94J8AW1P_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_233426065127_2.jpg', '小狗5', NULL, NULL, NULL, '0x4c4b34', 714300, 1024, 683, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:44', '2025-08-19 12:23:43', '2025-08-19 12:23:43', '2026-06-07 08:51:22', 0),
       ('1957659735075954690', '/picture/private/SUPER/2025-08-19_WJBAH1WV.JPG', '/picture/private/SUPER/2025-08-19_WJBAH1WV.webp', '/picture/private/SUPER/2025-08-19_WJBAH1WV_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_233220049127_2.jpg', '小狗6', NULL, NULL, NULL, '0xdfc0a1', 988422, 1024, 1024, 1, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:47', '2025-08-19 12:23:47', '2025-08-19 12:23:47', '2026-06-07 08:51:22', 0),
       ('1957659737500262402', '/picture/private/SUPER/2025-08-19_WDWRZEAV.JPG', '/picture/private/SUPER/2025-08-19_WDWRZEAV.webp', '/picture/private/SUPER/2025-08-19_WDWRZEAV_thumbnail.JPG', 'https://pic.nximg.cn/file/20230816/34894542_215319787101_2.jpg', '小狗7', NULL, NULL, NULL, '0x000', 35044, 1024, 683, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:48', '2025-08-19 12:23:47', '2025-08-19 12:23:47', '2026-06-07 08:51:22', 0),
       ('1957659740889260033', '/picture/private/SUPER/2025-08-19_XIDEV5XW.JPG', '/picture/private/SUPER/2025-08-19_XIDEV5XW.webp', '/picture/private/SUPER/2025-08-19_XIDEV5XW_thumbnail.JPG', 'https://pic.nximg.cn/file/20230922/12075709_143540091100_2.jpg', '小狗8', NULL, NULL, NULL, '0xe0c7af', 88880, 717, 1024, 0.7, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:48', '2025-08-19 12:23:48', '2025-08-19 12:23:48', '2026-06-07 08:51:22', 0),
       ('1957659750049619970', '/picture/private/SUPER/2025-08-19_DXXWHQV1.JPEG', '/picture/private/SUPER/2025-08-19_DXXWHQV1.webp', '/picture/private/SUPER/2025-08-19_DXXWHQV1_thumbnail.JPEG', 'https://gd-hbimg.huaban.com/2fad4daff679f117ba9b91d3f27f185687463ad310461b-hsd5j9_fw658', '小狗9', NULL, NULL, NULL, '0xc0a284', 60788, 658, 833, 0.79, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:51', '2025-08-19 12:23:50', '2025-08-19 12:23:50', '2026-06-07 08:51:22', 0),
       ('1957659760501825538', '/picture/private/SUPER/2025-08-19_057JQS9U.JPG', '/picture/private/SUPER/2025-08-19_057JQS9U.webp', '/picture/private/SUPER/2025-08-19_057JQS9U_thumbnail.JPG', 'https://pic.nximg.cn/file/20220825/33331825_232459001125_2.jpg', '小狗10', NULL, NULL, NULL, '0x4e4a3c', 563758, 1024, 683, 1.5, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2025-08-19 12:23:53', '2025-08-19 12:23:53', '2025-08-19 12:23:53', '2026-06-07 08:51:22', 0),
       ('2055537065797894145', '/picture/public/1970043291593551874/2026-05-16_OJVVR2SD.PNG', '/picture/public/1970043291593551874/2026-05-16_OJVVR2SD.webp', '/picture/public/1970043291593551874/2026-05-16_OJVVR2SD_thumbnail.PNG', NULL, '汤姆', '汤姆大笑', '素材', '["热门"]', '0x9f7fde', 20338, 690, 690, 1, 'webp', '1', '0', 0, 0, '1', 1, '通过', '2026-05-16 15:10:36', '2026-05-16 14:33:21', '2026-05-16 14:34:00', '2026-06-07 08:51:22', 0),
       ('2060926790423281666', '/picture/private/SUPER/2026-05-31_8AAW0L2K.JPG', '/picture/private/SUPER/2026-05-31_8AAW0L2K.webp', '/picture/private/SUPER/2026-05-31_8AAW0L2K_thumbnail.JPG', NULL, 'wallhaven', 'nice', '壁纸', '["热门","背景"]', '0x5e3f9d', 1210590, 3840, 2160, 1.78, 'webp', '1', '0', 0, 0, '1', 1, '管理员自动审核通过', '2026-05-31 11:31:17', '2026-05-31 11:30:11', '2026-05-31 11:31:17', '2026-06-07 08:51:22', 0);

CREATE TABLE IF NOT EXISTS sys_dict (
    id          BIGINT        NOT NULL COMMENT '主键',
    dict_type   VARCHAR(64)   NOT NULL COMMENT '字典类型',
    dict_key    VARCHAR(128)  NOT NULL COMMENT '字典键',
    dict_value  VARCHAR(256)  NOT NULL COMMENT '字典值',
    sort_order  INT           DEFAULT 0 COMMENT '排序',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_dict_type (dict_type)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用字典配置表';

-- 图片分类
INSERT INTO sys_dict (id, dict_type, dict_key, dict_value, sort_order)
VALUES (1, 'PIC_CATEGORY', 'avatar', '头像', 1),
       (2, 'PIC_CATEGORY', 'wallpaper', '壁纸', 2),
       (3, 'PIC_CATEGORY', 'meme', '表情包', 3),
       (4, 'PIC_CATEGORY', 'poster', '海报', 4),
       (5, 'PIC_CATEGORY', 'illustration', '插画', 5),
       (6, 'PIC_CATEGORY', 'photography', '摄影', 6),
       (7, 'PIC_CATEGORY', 'ecommerce', '电商', 7),
       (8, 'PIC_CATEGORY', 'material', '素材', 8);

-- 图片标签
INSERT INTO sys_dict (id, dict_type, dict_key, dict_value, sort_order)
VALUES (11, 'PIC_TAG', 'hot', '热门', 1),
       (12, 'PIC_TAG', 'creative', '创意', 2),
       (13, 'PIC_TAG', 'minimalist', '简约', 3),
       (14, 'PIC_TAG', 'hd', '高清', 4),
       (15, 'PIC_TAG', 'nature', '自然', 5),
       (16, 'PIC_TAG', 'people', '人物', 6),
       (17, 'PIC_TAG', 'animal', '动物', 7),
       (18, 'PIC_TAG', 'tech', '科技', 8),
       (19, 'PIC_TAG', 'food', '美食', 9),
       (20, 'PIC_TAG', 'travel', '旅行', 10),
       (21, 'PIC_TAG', 'festival', '节日', 11),
       (22, 'PIC_TAG', 'vintage', '复古', 12);
