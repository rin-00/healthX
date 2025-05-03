package com.healthx.database.converter;

import androidx.room.TypeConverter;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

public class DateTimeConverter {
    
    @TypeConverter
    public static LocalDateTime fromTimestamp(Long timestamp) {
        return timestamp == null ? null : 
               LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
    
    @TypeConverter
    public static Long dateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? null : 
               dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
} 