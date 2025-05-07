-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nickname VARCHAR(50),
    gender VARCHAR(10),
    age INT,
    height DOUBLE,
    weight DOUBLE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- 体重记录表
CREATE TABLE weight_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    weight DECIMAL(5,2) NOT NULL COMMENT '体重值(kg)',
    bmi DECIMAL(4,2) COMMENT 'BMI指数',
    bmi_status VARCHAR(20) COMMENT 'BMI状态评估(偏瘦/正常/超重/肥胖)',
    body_fat_percentage DECIMAL(5,2) COMMENT '体脂率(%)',
    measurement_time DATETIME NOT NULL COMMENT '测量时间',
    note VARCHAR(200) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remote_id BIGINT COMMENT '远程服务器ID',
    sync_status TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态：0-未同步，1-已同步，2-同步失败',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户体重记录表';

-- 体重目标表
CREATE TABLE weight_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_weight DECIMAL(5,2) NOT NULL COMMENT '目标体重(kg)',
    target_bmi DECIMAL(4,2) COMMENT '目标BMI',
    target_body_fat DECIMAL(5,2) COMMENT '目标体脂率(%)',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '目标达成日期',
    weekly_goal DECIMAL(3,2) COMMENT '每周目标变化(kg)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remote_id BIGINT COMMENT '远程服务器ID',
    sync_status TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态：0-未同步，1-已同步，2-同步失败',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户体重目标表';

-- 步数记录表
CREATE TABLE step_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    step_count INT NOT NULL COMMENT '步数',
    distance DECIMAL(8,2) COMMENT '行走距离(米)',
    calories_burned DECIMAL(8,2) COMMENT '消耗卡路里(kcal)',
    record_date DATE NOT NULL COMMENT '记录日期',
    source VARCHAR(50) COMMENT '数据来源(手机计步器/手动录入)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remote_id BIGINT COMMENT '远程服务器ID',
    sync_status TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态：0-未同步，1-已同步，2-同步失败',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_date (user_id, record_date) COMMENT '一个用户每天只能有一条步数记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户步数记录表';

-- 步数目标表
CREATE TABLE step_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_steps INT NOT NULL COMMENT '目标步数',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE COMMENT '结束日期(null表示长期目标)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remote_id BIGINT COMMENT '远程服务器ID',
    sync_status TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态：0-未同步，1-已同步，2-同步失败',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户步数目标表';

-- 步数统计表(按周/月汇总数据)
CREATE TABLE step_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    statistic_type VARCHAR(10) NOT NULL COMMENT '统计类型(WEEKLY/MONTHLY)',
    year INT NOT NULL COMMENT '年份',
    period INT NOT NULL COMMENT '周数或月份(1-53表示周数，1-12表示月份)',
    total_steps INT NOT NULL DEFAULT 0 COMMENT '总步数',
    avg_steps DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '平均每日步数',
    max_steps INT NOT NULL DEFAULT 0 COMMENT '最高步数',
    min_steps INT NOT NULL DEFAULT 0 COMMENT '最低步数(有记录的日期)',
    recorded_days INT NOT NULL DEFAULT 0 COMMENT '记录天数',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_period (user_id, statistic_type, year, period) COMMENT '用户每个统计周期只有一条记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户步数统计表';

-- 饮食记录表
CREATE TABLE diet_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    calories INT NOT NULL,
    protein DOUBLE,
    fat DOUBLE,
    carbohydrate DOUBLE,
    meal_type VARCHAR(20) NOT NULL, -- 早餐、午餐、晚餐、加餐
    record_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 运动记录表
CREATE TABLE exercise_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    exercise_name VARCHAR(100) NOT NULL,
    duration INT NOT NULL, -- 单位：分钟
    calories_burned INT,
    exercise_type VARCHAR(50), -- 有氧运动、力量训练等
    record_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 睡眠记录表
CREATE TABLE sleep_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL, -- 睡眠开始时间
    end_time DATETIME NOT NULL,   -- 睡眠结束时间
    duration INT,                 -- 睡眠时长（分钟）
    quality VARCHAR(20),          -- 睡眠质量：好、一般、差
    created_at DATETIME NOT NULL, -- 记录创建时间
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 健康指标表（用于存储BMI、体脂率等计算结果）
CREATE TABLE health_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    bmi DOUBLE,
    body_fat_percentage DOUBLE,
    basal_metabolic_rate INT, -- 基础代谢率
    record_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 食物表
CREATE TABLE food_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    calories INT NOT NULL,
    protein DOUBLE,
    fat DOUBLE,
    carbohydrate DOUBLE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- 添加食物表索引
CREATE INDEX idx_food_items_name ON food_items(name);

-- 添加索引以提高查询性能
CREATE INDEX idx_weight_records_user_id ON weight_records(user_id);
CREATE INDEX idx_weight_records_measurement_time ON weight_records(measurement_time);
CREATE INDEX idx_weight_goals_user_id ON weight_goals(user_id);
CREATE INDEX idx_diet_records_user_id ON diet_records(user_id);
CREATE INDEX idx_exercise_records_user_id ON exercise_records(user_id);
CREATE INDEX idx_sleep_records_user_id ON sleep_records(user_id);
CREATE INDEX idx_step_records_user_id ON step_records(user_id);
CREATE INDEX idx_step_records_record_date ON step_records(record_date);
CREATE INDEX idx_step_goals_user_id ON step_goals(user_id);
CREATE INDEX idx_step_statistics_user_id ON step_statistics(user_id);
CREATE INDEX idx_step_statistics_period ON step_statistics(statistic_type, year, period);
CREATE INDEX idx_health_metrics_user_id ON health_metrics(user_id);
