package com.healthx.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.healthx.database.converter.DateTimeConverter;
import com.healthx.database.dao.DietDao;
import com.healthx.database.dao.ExerciseDao;
import com.healthx.model.Diet;
import com.healthx.model.Exercise;
import com.healthx.model.User;

@Database(entities = {User.class, Diet.class, Exercise.class}, version = 5, exportSchema = false)
@TypeConverters({DateTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "health_app_db";
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
    
    public abstract DietDao dietDao();
    public abstract ExerciseDao exerciseDao();
    
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME
            )
            // 应用所有迁移路径
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            // 如果找不到迁移路径，允许破坏性重建数据库（会清除数据）
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
} 