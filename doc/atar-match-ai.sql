CREATE TABLE match_record (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              person1_birthday DATETIME NOT NULL COMMENT '第一个人的生日',
    person2_birthday DATETIME NOT NULL COMMENT '第二个人的生日',
    person1_sign VARCHAR(20) NOT NULL COMMENT '第一个人的星座',
    person2_sign VARCHAR(20) NOT NULL COMMENT '第二个人的星座',
    match_score INT NOT NULL COMMENT '匹配分数',
    analysis TEXT COMMENT '分析报告',
    advantages TEXT COMMENT '优势',
    disadvantages TEXT COMMENT '劣势',
    suggestions TEXT COMMENT '建议',
    ai_model VARCHAR(50) NOT NULL COMMENT '使用的AI模型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='星座匹配记录表';