-- 基线 schema：user / space / space_user / picture
-- picture_{spaceId} 分表由 DynamicShardingManager 在运行时动态创建

CREATE TABLE IF NOT EXISTS `user`
(
    `id`           bigint                                 NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount`  varchar(256)                           NOT NULL COMMENT '账号',
    `userPassword` varchar(512)                           NOT NULL COMMENT '密码',
    `userEmail`    varchar(256)                           NULL COMMENT '邮箱',
    `userName`     varchar(256)                           NULL COMMENT '用户昵称',
    `userAvatar`   varchar(1024)                          NULL COMMENT '用户头像',
    `userProfile`  varchar(512)                           NULL COMMENT '用户简介',
    `userRole`     varchar(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin/administrator',
    `vipNumber`    bigint                                 NULL COMMENT '会员编码',
    `inviteUser`   bigint                                 NULL COMMENT '邀请用户ID',
    `shareCode`    varchar(255)                           NULL COMMENT '分享码',
    `editTime`     datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `createTime`   datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `updateTime`   datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     tinyint      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userAccount` (`userAccount`),
    INDEX `idx_userName` (`userName`),
    INDEX `idx_vipNumber` (`vipNumber`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户';

-- 初始化超级管理员（账号: admin, 密码: administrator）
INSERT INTO `user` (`id`, `userAccount`, `userPassword`, `userEmail`, `userName`, `userRole`, `vipNumber`, `shareCode`)
VALUES (1, 'admin', 'a8e43a560b3b73db965c699ccf5e7c3c', 'admin@prodigal.com', '超级管理员', 'administrator', 1, 'ADMIN001');

CREATE TABLE IF NOT EXISTS `space`
(
    `id`         bigint                               NOT NULL AUTO_INCREMENT COMMENT 'id',
    `spaceName`  varchar(128)                         NULL COMMENT '空间名称',
    `spaceLevel` int        DEFAULT 0                 NULL COMMENT '空间级别：0-普通版 1-专业版 2-旗舰版',
    `maxSize`    bigint     DEFAULT 0                 NULL COMMENT '空间图片的最大总大小',
    `maxCount`   bigint     DEFAULT 0                 NULL COMMENT '空间图片的最大数量',
    `totalSize`  bigint     DEFAULT 0                 NULL COMMENT '当前空间下图片的总大小',
    `totalCount` bigint     DEFAULT 0                 NULL COMMENT '当前空间下的图片数量',
    `userId`     bigint                               NOT NULL COMMENT '创建用户 id',
    `createTime` datetime   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `editTime`   datetime   DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `updateTime` datetime   DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   tinyint    DEFAULT 0                 NOT NULL COMMENT '是否删除',
    `spaceType`  int        DEFAULT 0                 NOT NULL COMMENT '空间类型：0-私有 1-团队',
    PRIMARY KEY (`id`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_spaceName` (`spaceName`),
    INDEX `idx_spaceLevel` (`spaceLevel`),
    INDEX `idx_spaceType` (`spaceType`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='空间';

CREATE TABLE IF NOT EXISTS `space_user`
(
    `id`         bigint                                 NOT NULL AUTO_INCREMENT COMMENT 'id',
    `spaceId`    bigint                                 NOT NULL COMMENT '空间 id',
    `userId`     bigint                                 NOT NULL COMMENT '用户 id',
    `spaceRole`  varchar(128) DEFAULT 'viewer'          NULL COMMENT '空间角色：viewer/editor/admin',
    `createTime` datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `updateTime` datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spaceId_userId` (`spaceId`, `userId`),
    INDEX `idx_spaceId` (`spaceId`),
    INDEX `idx_userId` (`userId`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='空间用户关联';

CREATE TABLE IF NOT EXISTS `picture`
(
    `id`            bigint                                 NOT NULL AUTO_INCREMENT COMMENT 'id',
    `originUrl`     varchar(512)                           NULL COMMENT '原图url',
    `url`           varchar(512)                           NOT NULL COMMENT '图片 url',
    `thumbnailUrl`  varchar(512)                           NULL COMMENT '缩略图url',
    `sourceUrl`     varchar(512)                           NULL COMMENT '源图片url',
    `name`          varchar(128)                           NOT NULL COMMENT '图片名称',
    `introduction`  varchar(512)                           NULL COMMENT '简介',
    `category`      varchar(64)                            NULL COMMENT '分类',
    `tags`          varchar(512)                           NULL COMMENT '标签（JSON 数组）',
    `picColor`      varchar(16)                            NULL COMMENT '图片主色调',
    `picSize`       bigint                                 NULL COMMENT '图片体积',
    `picWidth`      int                                    NULL COMMENT '图片宽度',
    `picHeight`     int                                    NULL COMMENT '图片高度',
    `picScale`      double                                 NULL COMMENT '图片宽高比例',
    `picFormat`     varchar(32)                            NULL COMMENT '图片格式',
    `userId`        bigint                                 NOT NULL COMMENT '创建用户 id',
    `spaceId`       bigint(20) UNSIGNED ZEROFILL         NOT NULL DEFAULT 0 COMMENT '空间 id（0 表示公共空间）',
    `viewQuantity`  bigint(20) UNSIGNED ZEROFILL           NOT NULL DEFAULT 0 COMMENT '查看次数',
    `shareQuantity` bigint(20) UNSIGNED ZEROFILL           NOT NULL DEFAULT 0 COMMENT '分享次数',
    `reviewerId`    bigint                                 NULL COMMENT '审核人 ID',
    `reviewStatus`  int          DEFAULT 0                 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝',
    `reviewMessage` varchar(512)                           NULL COMMENT '审核信息',
    `reviewTime`    datetime                               NULL COMMENT '审核时间',
    `createTime`    datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `editTime`      datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    `updateTime`    datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`      tinyint      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_name` (`name`),
    INDEX `idx_introduction` (`introduction`),
    INDEX `idx_category` (`category`),
    INDEX `idx_tags` (`tags`),
    INDEX `idx_userId` (`userId`),
    INDEX `idx_reviewStatus` (`reviewStatus`),
    INDEX `idx_spaceId` (`spaceId`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='图片';
