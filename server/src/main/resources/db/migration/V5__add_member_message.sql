-- M1 站内消息：店长回复评论后给会员留一条未读消息，客户在小程序内看到红点。

CREATE TABLE `member_message` (
  `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id`   bigint unsigned NOT NULL COMMENT '接收会员ID',
  `type`        tinyint unsigned NOT NULL COMMENT '消息类型：1-评论回复',
  `title`       varchar(64)     NOT NULL COMMENT '消息标题',
  `content`     varchar(500)    NOT NULL COMMENT '消息正文',
  `biz_id`      bigint unsigned NOT NULL COMMENT '业务主键：type=1 时为评论ID',
  `is_read`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否已读：1-是，0-否',
  `read_time`   datetime        NULL COMMENT '读取时间',
  `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_biz_id` (`type`, `biz_id`),
  KEY `idx_member_id_is_read` (`member_id`, `is_read`),
  KEY `idx_member_id_create_time` (`member_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员站内消息表';
