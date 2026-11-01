-- M1 评论功能：卡产品评价、店长单条回复、屏蔽/删除均用 status 表达。

CREATE TABLE `card_comment` (
  `id`             bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id`     bigint unsigned NOT NULL COMMENT '卡产品ID',
  `product_name`   varchar(64)     NOT NULL COMMENT '卡名称快照',
  `member_id`      bigint unsigned NOT NULL COMMENT '会员ID',
  `order_no`       varchar(32)     NOT NULL COMMENT '来源订单号，一笔已支付订单只能评价一次',
  `content`        varchar(500)    NOT NULL COMMENT '评价内容',
  `images`         varchar(1024)   NOT NULL DEFAULT '' COMMENT '图片地址，最多4张，逗号分隔',
  `status`         tinyint unsigned NOT NULL DEFAULT 1 COMMENT '状态：1-显示，2-已屏蔽，3-已删除',
  `reply_content`  varchar(500)    NULL COMMENT '店长回复内容',
  `reply_staff_id` bigint unsigned NULL COMMENT '回复员工ID',
  `reply_time`     datetime        NULL COMMENT '回复时间',
  `create_time`    datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_product_id_status_create_time` (`product_id`, `status`, `create_time`),
  KEY `idx_member_id_create_time` (`member_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡产品评论表';
