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
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    weight DOUBLE NOT NULL,
    bmi DOUBLE,
    record_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

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

-- 喝水记录表
CREATE TABLE water_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount INT NOT NULL, -- 单位：毫升
    record_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 睡眠记录表
CREATE TABLE sleep_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration INT, -- 单位：分钟
    quality VARCHAR(20), -- 好、一般、差
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 步数记录表
CREATE TABLE step_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    steps INT NOT NULL,
    distance DOUBLE, -- 单位：公里
    calories_burned INT,
    record_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 健康计划表
CREATE TABLE health_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    plan_type VARCHAR(50) NOT NULL, -- 减肥、增肌、保持健康等
    start_date DATE NOT NULL,
    end_date DATE,
    target_weight DOUBLE,
    target_steps INT,
    target_water INT, -- 单位：毫升
    target_sleep INT, -- 单位：分钟
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 提醒设置表
CREATE TABLE reminders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    reminder_type VARCHAR(50) NOT NULL, -- 喝水、运动、吃药等
    reminder_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    repeat_days VARCHAR(20), -- 例如：1,2,3,4,5 表示周一到周五
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
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
CREATE INDEX idx_diet_records_user_id ON diet_records(user_id);
CREATE INDEX idx_exercise_records_user_id ON exercise_records(user_id);
CREATE INDEX idx_water_records_user_id ON water_records(user_id);
CREATE INDEX idx_sleep_records_user_id ON sleep_records(user_id);
CREATE INDEX idx_step_records_user_id ON step_records(user_id);
CREATE INDEX idx_health_plans_user_id ON health_plans(user_id);
CREATE INDEX idx_reminders_user_id ON reminders(user_id);
CREATE INDEX idx_health_metrics_user_id ON health_metrics(user_id); 