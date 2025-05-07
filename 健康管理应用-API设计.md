# 健康管理应用-API设计

## 1. API设计原则

### 1.1 RESTful设计规范
- 使用HTTP方法表示操作类型：GET、POST、PUT、DELETE
- 使用名词复数形式表示资源：/users, /exercises
- 使用嵌套资源表示从属关系：/users/{userId}/exercises
- 使用HTTP状态码表示请求结果
- 支持过滤、排序和分页

### 1.2 请求/响应格式
所有API请求和响应均使用JSON格式进行数据交换。

**请求头**:
```
Content-Type: application/json
Accept: application/json
Authorization: Bearer {token}  // 认证令牌
```

**标准响应格式**:
```json
{
  "code": 200,           // HTTP状态码
  "message": "操作成功",   // 操作结果描述
  "data": { ... }        // 响应数据
}
```

### 1.3 错误处理
错误响应遵循统一格式：
```json
{
  "code": 400,           // HTTP错误码
  "message": "错误描述",   // 用户友好的错误描述
  "errors": [            // 详细错误信息（可选）
    {
      "field": "username", 
      "error": "用户名不能为空"
    }
  ]
}
```

### 1.4 分页处理
分页请求参数：
- page: 页码，从0开始
- size: 每页记录数
- sort: 排序字段，格式为field,direction (例如: createdAt,desc)

分页响应格式：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [ ... ],      // 数据内容
    "pageable": {
      "pageNumber": 0,       // 当前页码
      "pageSize": 10,        // 每页大小
      "sort": { ... }        // 排序信息
    },
    "totalElements": 100,    // 总记录数
    "totalPages": 10         // 总页数
  }
}
```

### 1.5 日期时间格式
- 请求与响应中使用ISO-8601标准格式: `yyyy-MM-dd'T'HH:mm:ss.SSS`
- 日期格式: `yyyy-MM-dd`
- 时间格式: `HH:mm:ss`

## 2. 认证与安全

### 2.1 认证方式
采用JWT (JSON Web Token)进行用户认证

### 2.2 认证流程
1. 用户登录，服务器返回JWT令牌
2. 客户端在后续请求中使用Authorization头携带令牌
3. 服务器验证令牌有效性，执行授权检查

### 2.3 接口安全策略
- 登录接口限流：相同IP 1分钟内最多5次请求
- 敏感操作需要二次验证
- OAuth2支持第三方平台登录

## 3. 核心API接口

### 3.1 用户管理API

#### 3.1.1 用户注册
```
POST /api/auth/register
```

请求体:
```json
{
  "username": "user123",
  "password": "SecurePassword@123",
  "email": "user@example.com",
  "nickname": "健康达人"
}
```

#### 3.1.2 用户登录
```
POST /api/auth/login
```

请求体:
```json
{
  "username": "user123",
  "password": "SecurePassword@123"
}
```

响应体:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1001,
    "username": "user123",
    "expiresIn": 3600
  }
}
```

#### 3.1.3 获取用户信息
```
GET /api/users/{userId}
```

#### 3.1.4 更新用户信息
```
PUT /api/users/{userId}
```

请求体:
```json
{
  "nickname": "健康先锋",
  "gender": "男",
  "age": 28,
  "height": 175.5,
  "weight": 68.5
}
```

#### 3.1.5 修改密码
```
PUT /api/users/{userId}/password
```

请求体:
```json
{
  "oldPassword": "OldPassword@123",
  "newPassword": "NewPassword@123"
}
```

### 3.2 运动记录API

#### 3.2.1 添加运动记录
```
POST /api/exercises
```

请求体:
```json
{
  "userId": 1001,
  "exerciseName": "慢跑",
  "duration": 30,
  "caloriesBurned": 300.5,
  "exerciseType": "有氧运动",
  "intensity": "中等强度",
  "exercisedAt": "2023-05-20T18:30:00.000"
}
```

#### 3.2.2 获取运动记录
```
GET /api/exercises/{id}
```

#### 3.2.3 获取用户所有运动记录
```
GET /api/exercises/user/{userId}
```

#### 3.2.4 获取用户指定日期的运动记录
```
GET /api/exercises/user/{userId}/date?date=2023-05-20
```

#### 3.2.5 获取用户指定日期范围的运动记录
```
GET /api/exercises/user/{userId}/range?startDate=2023-05-01&endDate=2023-05-31
```

#### 3.2.6 获取用户指定类型的运动记录
```
GET /api/exercises/user/{userId}/type?exerciseType=有氧运动
```

#### 3.2.7 获取用户指定日期的消耗卡路里统计
```
GET /api/exercises/user/{userId}/calories?date=2023-05-20
```

#### 3.2.8 获取用户指定日期范围的每日消耗卡路里统计
```
GET /api/exercises/user/{userId}/calories/range?startDate=2023-05-01&endDate=2023-05-31
```

#### 3.2.9 更新运动记录
```
PUT /api/exercises/{id}
```

#### 3.2.10 删除运动记录
```
DELETE /api/exercises/{id}
```

### 3.3 饮食管理API

#### 3.3.1 添加饮食记录
```
POST /api/diets
```

请求体:
```json
{
  "userId": 1001,
  "foodName": "全麦面包",
  "mealType": "早餐",
  "calories": 150.0,
  "protein": 5.5,
  "fat": 2.1,
  "carbohydrate": 30.5,
  "eatenAt": "2023-05-20T08:30:00.000"
}
```

#### 3.3.2 获取饮食记录
```
GET /api/diets/{id}
```

#### 3.3.3 获取用户所有饮食记录
```
GET /api/diets/user/{userId}
```

#### 3.3.4 获取用户指定日期的饮食记录
```
GET /api/diets/user/{userId}/date?date=2023-05-20
```

#### 3.3.5 获取用户指定日期的摄入卡路里统计
```
GET /api/diets/user/{userId}/calories?date=2023-05-20
```

#### 3.3.6 更新饮食记录
```
PUT /api/diets/{id}
```

#### 3.3.7 删除饮食记录
```
DELETE /api/diets/{id}
```

### 3.4 睡眠记录API

#### 3.4.1 添加睡眠记录
```
POST /api/sleep-records
```

请求体:
```json
{
  "userId": 1001,
  "startTime": "2023-05-19T23:00:00.000",
  "endTime": "2023-05-20T07:30:00.000"
}
```

#### 3.4.2 获取睡眠记录
```
GET /api/sleep-records/{id}
```

#### 3.4.3 获取用户所有睡眠记录
```
GET /api/sleep-records/user/{userId}
```

#### 3.4.4 获取用户指定日期的睡眠记录
```
GET /api/sleep-records/user/{userId}/date?date=2023-05-20
```

#### 3.4.5 获取用户指定日期范围的睡眠统计
```
GET /api/sleep-records/user/{userId}/stats?startDate=2023-05-01&endDate=2023-05-31
```

#### 3.4.6 更新睡眠记录
```
PUT /api/sleep-records/{id}
```

#### 3.4.7 删除睡眠记录
```
DELETE /api/sleep-records/{id}
```

## 4. 待开发功能API

### 4.1 健康目标管理API

#### 4.1.1 创建健康目标
```
POST /api/health-goals
```

请求体:
```json
{
  "userId": 1001,
  "goalType": "WEIGHT_LOSS",
  "targetValue": 65.0,
  "currentValue": 70.0,
  "unit": "kg",
  "startDate": "2023-06-01",
  "endDate": "2023-08-31"
}
```

#### 4.1.2 获取健康目标
```
GET /api/health-goals/{id}
```

#### 4.1.3 获取用户所有健康目标
```
GET /api/health-goals/user/{userId}
```

#### 4.1.4 获取用户指定类型的健康目标
```
GET /api/health-goals/user/{userId}/type?goalType=WEIGHT_LOSS
```

#### 4.1.5 获取用户当前活跃的健康目标
```
GET /api/health-goals/user/{userId}/active
```

#### 4.1.6 更新健康目标进度
```
PATCH /api/health-goals/{id}/progress
```

请求体:
```json
{
  "currentValue": 67.5
}
```

#### 4.1.7 完成健康目标
```
PATCH /api/health-goals/{id}/complete
```

#### 4.1.8 删除健康目标
```
DELETE /api/health-goals/{id}
```

### 4.2 健康提醒/任务API

#### 4.2.1 创建健康提醒
```
POST /api/health-reminders
```

请求体:
```json
{
  "userId": 1001,
  "reminderType": "MEDICATION",
  "title": "服用降压药",
  "description": "每日早晚各一次",
  "reminderTime": "2023-06-01T08:00:00.000",
  "isRepeating": true,
  "repeatPattern": "DAILY"
}
```

#### 4.2.2 获取健康提醒
```
GET /api/health-reminders/{id}
```

#### 4.2.3 获取用户所有健康提醒
```
GET /api/health-reminders/user/{userId}
```

#### 4.2.4 获取用户指定日期的提醒
```
GET /api/health-reminders/user/{userId}/date?date=2023-06-01
```

#### 4.2.5 获取用户指定类型的提醒
```
GET /api/health-reminders/user/{userId}/type?reminderType=MEDICATION
```

#### 4.2.6 完成提醒任务
```
PATCH /api/health-reminders/{id}/complete
```

#### 4.2.7 删除健康提醒
```
DELETE /api/health-reminders/{id}
```

### 4.3 健康报告/统计API

#### 4.3.1 生成用户日健康报告
```
GET /api/health-reports/user/{userId}/daily?date=2023-06-01
```

#### 4.3.2 生成用户周健康报告
```
GET /api/health-reports/user/{userId}/weekly?startDate=2023-06-01&endDate=2023-06-07
```

#### 4.3.3 生成用户月健康报告
```
GET /api/health-reports/user/{userId}/monthly?year=2023&month=6
```

#### 4.3.4 健康趋势分析
```
GET /api/health-reports/user/{userId}/trends?startDate=2023-06-01&endDate=2023-06-30&metricType=WEIGHT
```

#### 4.3.5 获取用户健康统计摘要
```
GET /api/health-reports/user/{userId}/summary
```

### 4.4 健康数据同步/集成API

#### 4.4.1 导入健康数据
```
POST /api/health-data/import
```
Content-Type: multipart/form-data

表单字段:
- userId: 1001
- dataSource: "FITBIT"
- dataFile: [二进制文件]

#### 4.4.2 导出健康数据
```
GET /api/health-data/export?userId=1001&startDate=2023-06-01&endDate=2023-06-30&format=JSON
```

#### 4.4.3 第三方平台授权
```
POST /api/health-data/authorize
```

请求体:
```json
{
  "userId": 1001,
  "platform": "FITBIT",
  "authCode": "xyz123"
}
```

#### 4.4.4 同步第三方平台数据
```
POST /api/health-data/sync
```

请求体:
```json
{
  "userId": 1001,
  "platform": "FITBIT",
  "dataTypes": ["ACTIVITY", "SLEEP", "HEART_RATE"],
  "startDate": "2023-06-01",
  "endDate": "2023-06-30"
}
```

## 5. API版本控制

### 5.1 版本控制策略
API采用URI路径方式进行版本控制，格式为：`/api/v{n}/resource`

例如：
- V1版本：`/api/v1/exercises`
- V2版本：`/api/v2/exercises`

### 5.2 版本兼容性
- 同一主版本内保持向后兼容
- 不兼容的变更必须提升主版本号
- 新增字段不影响向后兼容性
- 弃用字段保留至少一个完整版本周期

## 6. API文档

### 6.1 文档工具
API文档使用Swagger/OpenAPI规范生成，提供在线浏览和测试功能。

### 6.2 文档访问
- 开发环境：http://localhost:8080/swagger-ui.html
- 测试环境：https://test-api.healthmanager.com/swagger-ui.html
- 生产环境：https://api.healthmanager.com/api-docs

### 6.3 接口更新日志
| 版本  | 日期          | 变更说明                      |
|------|--------------|------------------------------|
| v1.0 | 2023-06-01   | 初始API版本，包含用户、运动、饮食、睡眠基础功能 |
| v1.1 | 2023-08-15   | 新增健康目标和提醒相关API       |
| v1.2 | 2023-10-20   | 新增社区互动和报告统计相关API    | 