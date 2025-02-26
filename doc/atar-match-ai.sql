CREATE TABLE `match_record` (
`id` bigint NOT NULL AUTO_INCREMENT,
`user_id` bigint NOT NULL DEFAULT '0' COMMENT '用户ID',
`person1_birthday` datetime NOT NULL COMMENT '第一个人的生日',
`person2_birthday` datetime NOT NULL COMMENT '第二个人的生日',
`person1_sign` varchar(20) NOT NULL COMMENT '第一个人的星座',
`person2_sign` varchar(20) NOT NULL COMMENT '第二个人的星座',
`match_score` int NOT NULL COMMENT '匹配分数',
`analysis` text COMMENT '分析报告',
`advantages` text COMMENT '优势',
`disadvantages` text COMMENT '劣势',
`suggestions` text COMMENT '建议',
`ai_model` varchar(50) NOT NULL COMMENT '使用的AI模型',
`created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
PRIMARY KEY (`id`),
KEY `idx_user_id` (`user_id`)
) COMMENT='星座匹配记录表';



-- 用户表
CREATE TABLE `user` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
`email` varchar(100) NOT NULL COMMENT '邮箱',
`password` varchar(255) NOT NULL COMMENT '密码（加密存储）',
`nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
`avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
`phone` varchar(20) DEFAULT NULL COMMENT '手机号（预留）',
`wechat_openid` varchar(100) DEFAULT NULL COMMENT '微信OpenID（预留）',
`wechat_unionid` varchar(100) DEFAULT NULL COMMENT '微信UnionID（预留）',
`qq_openid` varchar(100) DEFAULT NULL COMMENT 'QQ OpenID（预留）',
`last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
`last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
`status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`),
UNIQUE KEY `idx_email` (`email`),
UNIQUE KEY `idx_phone` (`phone`),
UNIQUE KEY `idx_wechat_openid` (`wechat_openid`),
UNIQUE KEY `idx_wechat_unionid` (`wechat_unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
