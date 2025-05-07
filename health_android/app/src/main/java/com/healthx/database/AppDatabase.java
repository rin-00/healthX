package com.healthx.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.healthx.database.converter.DateTimeConverter;
import com.healthx.database.dao.DietDao;
import com.healthx.database.dao.ExerciseDao;
import com.healthx.database.dao.SleepRecordDao;
import com.healthx.database.dao.StepGoalDao;
import com.healthx.database.dao.StepRecordDao;
import com.healthx.database.dao.UserDao;
import com.healthx.database.dao.WeightRecordDao;
import com.healthx.model.Diet;
import com.healthx.model.Exercise;
import com.healthx.model.SleepRecord;
import com.healthx.model.StepGoal;
import com.healthx.model.StepRecord;
import com.healthx.model.User;
import com.healthx.model.WeightRecord;


@Database(entities = {User.class, Diet.class, Exercise.class, SleepRecord.class, WeightRecord.class, StepRecord.class, StepGoal.class}, version = 13, exportSchema = false)
@TypeConverters({DateTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "health_database";
    private static AppDatabase instance;
    
    // 定义从版本1到版本2的迁移
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 此处可以编写具体迁移逻辑
            // 由于版本1可能是新安装的应用，所以这里不需要太多操作
        }
    };
    
    // 定义从版本2到版本3的迁移
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 架构哈希值不匹配，需要重新创建表以匹配新的架构
            // 首先创建临时表
            database.execSQL("CREATE TABLE IF NOT EXISTS diets_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "foodName TEXT NOT NULL, " +
                    "calories REAL NOT NULL, " +
                    "protein REAL NOT NULL, " +
                    "carbs REAL NOT NULL, " +
                    "fat REAL NOT NULL, " +
                    "mealType TEXT NOT NULL, " +
                    "eatenAt TEXT NOT NULL, " +
                    "createdAt TEXT NOT NULL, " +
                    "remoteId INTEGER, " +
                    "syncStatus INTEGER NOT NULL DEFAULT 0)");
            
            // 尝试复制数据
            try {
                database.execSQL("INSERT INTO diets_new SELECT * FROM diets");
            } catch (Exception e) {
                // 如果复制失败，可能是因为表结构不兼容
                // 继续执行，接受数据丢失
            }
            
            // 删除旧表并重命名新表
            database.execSQL("DROP TABLE IF EXISTS diets");
            database.execSQL("ALTER TABLE diets_new RENAME TO diets");
        }
    };
    
    // 定义从版本3到版本4的迁移，添加Exercise表
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建运动记录表
            database.execSQL("CREATE TABLE IF NOT EXISTS exercises (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "exerciseName TEXT NOT NULL, " +
                    "duration INTEGER NOT NULL, " +
                    "caloriesBurned REAL NOT NULL, " +
                    "exerciseType TEXT, " +
                    "intensity TEXT, " +
                    "exercisedAt INTEGER NOT NULL, " +
                    "createdAt INTEGER NOT NULL, " +
                    "remoteId INTEGER, " +
                    "syncStatus INTEGER NOT NULL DEFAULT 0)");
        }
    };
    
    // 定义从版本4到版本5的迁移，修复表结构
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建正确的exercises临时表
            database.execSQL("CREATE TABLE IF NOT EXISTS exercises_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "exerciseName TEXT NOT NULL, " +
                    "duration INTEGER NOT NULL, " +
                    "caloriesBurned REAL NOT NULL, " +
                    "exerciseType TEXT, " +
                    "intensity TEXT, " +
                    "exercisedAt INTEGER NOT NULL, " +
                    "createdAt INTEGER NOT NULL, " +
                    "remoteId INTEGER, " +
                    "syncStatus INTEGER NOT NULL DEFAULT 0)");
            
            // 删除旧表（不转移数据，因为类型不兼容）
            database.execSQL("DROP TABLE IF EXISTS exercises");
            
            // 重命名新表
            database.execSQL("ALTER TABLE exercises_new RENAME TO exercises");
        }
    };
    
    // 添加版本5到版本6的迁移，添加水分记录相关表
    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建水分记录表（与版本7相比存在问题）
            database.execSQL("CREATE TABLE IF NOT EXISTS water_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "amount INTEGER NOT NULL, " +
                    "recordTime INTEGER NOT NULL, " +
                    "createdAt INTEGER NOT NULL, " +
                    "remoteId INTEGER, " +
                    "syncStatus INTEGER NOT NULL DEFAULT 0)");
            
            // 创建水分目标表
            database.execSQL("CREATE TABLE IF NOT EXISTS water_goals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "dailyGoal INTEGER NOT NULL, " +
                    "startDate INTEGER NOT NULL, " +
                    "endDate INTEGER, " +
                    "isActive INTEGER NOT NULL DEFAULT 1, " +
                    "createdAt INTEGER NOT NULL, " +
                    "updatedAt INTEGER NOT NULL, " +
                    "remoteId INTEGER, " +
                    "syncStatus INTEGER NOT NULL DEFAULT 0)");
        }
    };
    
    // 添加版本6到版本7的迁移，修复水分记录表结构
    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 删除旧的水分记录表（如果存在）
            database.execSQL("DROP TABLE IF EXISTS water_records");
            
            // 重新创建水分记录表，确保结构与实体类完全匹配
            database.execSQL("CREATE TABLE IF NOT EXISTS water_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "amount INTEGER NOT NULL, " +
                    "recordTime INTEGER, " +
                    "createdAt INTEGER, " +
                    "remoteId INTEGER, " +
                    "syncStatus INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE NO ACTION)");
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_water_records_userId ON water_records(userId)");
            
            // 同样检查并修复水分目标表
            if (tableExists(database, "water_goals")) {
                database.execSQL("DROP TABLE IF EXISTS water_goals");
            }
            
            database.execSQL("CREATE TABLE IF NOT EXISTS water_goals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "dailyGoal INTEGER NOT NULL, " +
                    "startDate INTEGER, " +
                    "endDate INTEGER, " +
                    "isActive INTEGER NOT NULL DEFAULT 1, " +
                    "createdAt INTEGER, " +
                    "updatedAt INTEGER, " +
                    "remoteId INTEGER, " +
                    "syncStatus INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE NO ACTION)");
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_water_goals_userId ON water_goals(userId)");
        }
        
        // 辅助方法，检查表是否存在
        private boolean tableExists(SupportSQLiteDatabase db, String tableName) {
            try {
                db.query("SELECT 1 FROM " + tableName + " LIMIT 1");
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };
    
    // 添加版本7到版本8的迁移，确保默认用户存在
    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 1. 检查users表是否存在
            if (!tableExists(database, "users")) {
                // 创建users表
                database.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "username TEXT, " +
                        "email TEXT, " +
                        "nickname TEXT, " +
                        "gender TEXT, " +
                        "age INTEGER, " +
                        "height REAL, " +
                        "weight REAL)");
            }
            
            // 2. 确认默认用户存在
            try {
                // 检查是否存在ID为1的用户
                androidx.sqlite.db.SupportSQLiteStatement stmt = 
                        database.compileStatement("SELECT COUNT(*) FROM users WHERE id = 1");
                long userCount = stmt.simpleQueryForLong();
                
                if (userCount == 0) {
                    // 插入默认用户
                    database.execSQL("INSERT OR IGNORE INTO users (id, username, nickname) " +
                          "VALUES (1, 'default_user', '默认用户')");
                }
            } catch (Exception e) {
                // 忽略异常，继续执行
            }
            
            // 3. 检查并添加drinkTypeId字段到water_records表
            try {
                // 检查water_records表中是否有drinkTypeId字段
                database.query("SELECT drinkTypeId FROM water_records LIMIT 1");
            } catch (Exception e) {
                try {
                    // 字段不存在，添加它
                    database.execSQL("ALTER TABLE water_records ADD COLUMN drinkTypeId INTEGER DEFAULT 1");
                    Log.d("AppDatabase", "添加drinkTypeId字段到water_records表");
                } catch (Exception ex) {
                    Log.e("AppDatabase", "添加drinkTypeId字段失败", ex);
                }
            }
        }
    };
    
    // 添加版本8到版本9的迁移，修复drinkTypeId字段类型
    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                // 创建临时表，包含所有字段，drinkTypeId为Long类型
                database.execSQL("CREATE TABLE IF NOT EXISTS water_records_temp (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "userId INTEGER NOT NULL, " +
                        "amount INTEGER NOT NULL, " +
                        "recordTime INTEGER, " +
                        "createdAt INTEGER, " +
                        "remoteId INTEGER, " +
                        "syncStatus INTEGER NOT NULL DEFAULT 0, " +
                        "drinkTypeId INTEGER DEFAULT 1, " +
                        "FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE NO ACTION)");
                
                // 复制数据
                database.execSQL("INSERT INTO water_records_temp SELECT id, userId, amount, recordTime, createdAt, remoteId, syncStatus, 1 FROM water_records");
                
                // 删除旧表
                database.execSQL("DROP TABLE water_records");
                
                // 重命名新表
                database.execSQL("ALTER TABLE water_records_temp RENAME TO water_records");
                
                // 创建索引
                database.execSQL("CREATE INDEX IF NOT EXISTS index_water_records_userId ON water_records(userId)");
                
                Log.d("AppDatabase", "已经修复water_records表中的drinkTypeId字段");
            } catch (Exception e) {
                Log.e("AppDatabase", "修复drinkTypeId字段失败", e);
            }
        }
    };
    
    // 添加版本9到版本10的迁移，添加睡眠记录表
    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                // 创建睡眠记录表
                database.execSQL("CREATE TABLE IF NOT EXISTS sleep_records (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "userId INTEGER NOT NULL, " +
                        "startTime INTEGER NOT NULL, " +
                        "endTime INTEGER NOT NULL, " +
                        "duration INTEGER, " +
                        "createdAt INTEGER NOT NULL, " +
                        "remoteId INTEGER, " +
                        "syncStatus INTEGER NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE NO ACTION)");
                
                // 创建索引
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sleep_records_userId ON sleep_records(userId)");
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sleep_records_startTime ON sleep_records(startTime)");
                
                Log.d("AppDatabase", "已创建sleep_records表");
            } catch (Exception e) {
                Log.e("AppDatabase", "创建sleep_records表失败", e);
            }
        }
    };
    
    // 添加版本10到版本11的迁移，移除water相关表
    private static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                // 删除water_records表（如果存在）
                if (tableExists(database, "water_records")) {
                    database.execSQL("DROP TABLE IF EXISTS water_records");
                    Log.d(TAG, "已删除water_records表");
                }
                
                // 删除water_goals表（如果存在）
                if (tableExists(database, "water_goals")) {
                    database.execSQL("DROP TABLE IF EXISTS water_goals");
                    Log.d(TAG, "已删除water_goals表");
                }
                
                // 注意：这里不需要做任何架构变更，因为相关实体已经从@Database注解中移除
                Log.d(TAG, "完成从版本10到版本11的迁移，移除了水分相关表");
            } catch (Exception e) {
                Log.e(TAG, "从版本10到版本11的迁移失败", e);
            }
        }
    };
    
    // 版本11到版本12的迁移，添加体重记录表
    private static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            try {
                // 创建体重记录表
                database.execSQL("CREATE TABLE IF NOT EXISTS weight_records (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "userId INTEGER NOT NULL, " +
                        "weight REAL NOT NULL, " +
                        "bmi REAL NOT NULL, " +
                        "bmiStatus TEXT, " +
                        "bodyFatPercentage REAL, " +
                        "measurementTime INTEGER NOT NULL, " +
                        "note TEXT, " +
                        "createdAt INTEGER NOT NULL, " +
                        "remoteId INTEGER, " +
                        "syncStatus INTEGER NOT NULL DEFAULT 0, " +
                        "FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE)");
                
                // 创建索引
                database.execSQL("CREATE INDEX IF NOT EXISTS index_weight_records_userId ON weight_records(userId)");
                database.execSQL("CREATE INDEX IF NOT EXISTS index_weight_records_measurementTime ON weight_records(measurementTime)");
                
                Log.d(TAG, "已创建weight_records表");
            } catch (Exception e) {
                Log.e(TAG, "创建weight_records表失败", e);
            }
        }
    };
    
    // 添加版本12到版本13的迁移，添加步数相关表
    private static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 创建步数记录表
            database.execSQL("CREATE TABLE IF NOT EXISTS step_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "step_count INTEGER NOT NULL, " +
                    "distance REAL, " +
                    "calories_burned REAL, " +
                    "record_date INTEGER NOT NULL, " +
                    "source TEXT, " +
                    "created_at INTEGER NOT NULL, " +
                    "updated_at INTEGER NOT NULL, " +
                    "remote_id INTEGER, " +
                    "sync_status INTEGER NOT NULL DEFAULT 0)");
            
            // 创建步数记录索引
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_step_records_user_id_record_date " +
                    "ON step_records (user_id, record_date)");
            
            // 创建步数目标表
            database.execSQL("CREATE TABLE IF NOT EXISTS step_goals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "target_steps INTEGER NOT NULL, " +
                    "start_date INTEGER NOT NULL, " +
                    "end_date INTEGER, " +
                    "is_active INTEGER NOT NULL DEFAULT 1, " +
                    "created_at INTEGER NOT NULL, " +
                    "updated_at INTEGER NOT NULL, " +
                    "remote_id INTEGER, " +
                    "sync_status INTEGER NOT NULL DEFAULT 0)");
            
            // 创建步数目标索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_step_goals_user_id " +
                    "ON step_goals (user_id)");
        }
    };
    
    // DAOs
    public abstract UserDao userDao();
    public abstract DietDao dietDao();
    public abstract ExerciseDao exerciseDao();
    public abstract SleepRecordDao sleepRecordDao();
    public abstract WeightRecordDao weightRecordDao();
    public abstract StepRecordDao stepRecordDao();
    public abstract StepGoalDao stepGoalDao();
    
    /**
     * 获取AppDatabase实例
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, 
                            MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                            MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)
                    .fallbackToDestructiveMigration() // 升级数据库时，如果没有提供Migration，直接删除重建
                    .addCallback(new MigrationCallback()) // 添加迁移回调
                    .build();
            
            Log.d(TAG, "数据库实例已创建");
        }
        return instance;
    }
    
    /**
     * 仅供开发环境使用！清除并重建数据库实例。
     * 这将删除所有数据。请在生产环境中谨慎使用。
     */
    public static synchronized void resetDatabase(Context context) {
        if (instance != null) {
            instance.close();
        }
        context.deleteDatabase(DATABASE_NAME);
        instance = null;
        // 下次调用getInstance时会创建新的数据库
    }
    
    // 辅助方法，检查表是否存在
    private static boolean tableExists(SupportSQLiteDatabase db, String tableName) {
        try {
            db.query("SELECT 1 FROM " + tableName + " LIMIT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 数据库迁移回调
    private static class MigrationCallback extends RoomDatabase.Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d(TAG, "数据库已创建");
        }
        
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Log.d(TAG, "数据库已打开");
        }
    }
} 