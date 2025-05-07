/**
 * 健康提醒/任务模块脚手架
 * 包含：实体类、DTO、Repository、Service、Controller
 */

/****************************
 * 实体类
 ****************************/

// 文件: src/main/java/com/health/health_demo/model/HealthReminder.java
package com.health.health_demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "health_reminders")
public class HealthReminder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "reminder_type", nullable = false)
    private String reminderType;
    
    @Column(nullable = false)
    private String title;
    
    @Column
    private String description;
    
    @Column(name = "reminder_time", nullable = false)
    private LocalDateTime reminderTime;
    
    @Column(name = "is_repeating", nullable = false)
    private Boolean isRepeating = false;
    
    @Column(name = "repeat_pattern")
    private String repeatPattern;
    
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 提醒类型枚举
    public enum ReminderType {
        MEDICATION("服药提醒"),
        EXERCISE("运动提醒"),
        MEAL("进餐提醒"),
        MEASUREMENT("测量提醒"),
        APPOINTMENT("预约提醒"),
        CUSTOM("自定义提醒");
        
        private final String displayName;
        
        ReminderType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 重复模式枚举
    public enum RepeatPattern {
        DAILY("每天"),
        WEEKLY("每周"),
        MONTHLY("每月"),
        CUSTOM("自定义");
        
        private final String displayName;
        
        RepeatPattern(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 构造函数
    public HealthReminder(User user, String reminderType, String title, String description,
                         LocalDateTime reminderTime, Boolean isRepeating, String repeatPattern) {
        this.user = user;
        this.reminderType = reminderType;
        this.title = title;
        this.description = description;
        this.reminderTime = reminderTime;
        this.isRepeating = isRepeating;
        this.repeatPattern = repeatPattern;
        this.isCompleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 完成提醒
    public void complete() {
        this.isCompleted = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 生成下一次提醒（对于重复提醒）
    public HealthReminder generateNextReminder() {
        if (!this.isRepeating || this.repeatPattern == null) {
            return null;
        }
        
        HealthReminder nextReminder = new HealthReminder();
        nextReminder.setUser(this.user);
        nextReminder.setReminderType(this.reminderType);
        nextReminder.setTitle(this.title);
        nextReminder.setDescription(this.description);
        nextReminder.setIsRepeating(this.isRepeating);
        nextReminder.setRepeatPattern(this.repeatPattern);
        
        // 根据重复模式计算下一次提醒时间
        if (RepeatPattern.DAILY.name().equals(this.repeatPattern)) {
            nextReminder.setReminderTime(this.reminderTime.plusDays(1));
        } else if (RepeatPattern.WEEKLY.name().equals(this.repeatPattern)) {
            nextReminder.setReminderTime(this.reminderTime.plusWeeks(1));
        } else if (RepeatPattern.MONTHLY.name().equals(this.repeatPattern)) {
            nextReminder.setReminderTime(this.reminderTime.plusMonths(1));
        } else {
            // 自定义重复模式需要特殊处理
            return null;
        }
        
        nextReminder.setCreatedAt(LocalDateTime.now());
        nextReminder.setUpdatedAt(LocalDateTime.now());
        nextReminder.setIsCompleted(false);
        
        return nextReminder;
    }
}

/****************************
 * DTO
 ****************************/

// 文件: src/main/java/com/health/health_demo/model/dto/HealthReminderDTO.java
package com.health.health_demo.model.dto;

import com.health.health_demo.model.HealthReminder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthReminderDTO {
    
    private Long id;
    private Long userId;
    private String reminderType;
    private String title;
    private String description;
    private LocalDateTime reminderTime;
    private Boolean isRepeating;
    private String repeatPattern;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 将实体转换为DTO
    public HealthReminderDTO(HealthReminder healthReminder) {
        this.id = healthReminder.getId();
        this.userId = healthReminder.getUser().getId();
        this.reminderType = healthReminder.getReminderType();
        this.title = healthReminder.getTitle();
        this.description = healthReminder.getDescription();
        this.reminderTime = healthReminder.getReminderTime();
        this.isRepeating = healthReminder.getIsRepeating();
        this.repeatPattern = healthReminder.getRepeatPattern();
        this.isCompleted = healthReminder.getIsCompleted();
        this.createdAt = healthReminder.getCreatedAt();
        this.updatedAt = healthReminder.getUpdatedAt();
    }
}

/****************************
 * Repository
 ****************************/

// 文件: src/main/java/com/health/health_demo/repository/HealthReminderRepository.java
package com.health.health_demo.repository;

import com.health.health_demo.model.HealthReminder;
import com.health.health_demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HealthReminderRepository extends JpaRepository<HealthReminder, Long> {
    
    // 根据用户查询所有提醒，按提醒时间升序排序
    List<HealthReminder> findByUserOrderByReminderTimeAsc(User user);
    
    // 根据用户ID查询所有提醒，按提醒时间升序排序
    List<HealthReminder> findByUserIdOrderByReminderTimeAsc(Long userId);
    
    // 根据用户ID和提醒类型查询提醒，按提醒时间升序排序
    List<HealthReminder> findByUserIdAndReminderTypeOrderByReminderTimeAsc(Long userId, String reminderType);
    
    // 查询用户指定日期的提醒
    @Query("SELECT hr FROM HealthReminder hr WHERE hr.user.id = :userId AND DATE(hr.reminderTime) = :date ORDER BY hr.reminderTime ASC")
    List<HealthReminder> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    // 查询用户未来的提醒
    List<HealthReminder> findByUserIdAndReminderTimeGreaterThanEqualAndIsCompletedFalseOrderByReminderTimeAsc(
            Long userId, LocalDateTime now);
    
    // 查询用户过期且未完成的提醒
    List<HealthReminder> findByUserIdAndReminderTimeLessThanAndIsCompletedFalseOrderByReminderTimeAsc(
            Long userId, LocalDateTime now);
    
    // 查询用户将要到期的提醒（30分钟内）
    @Query("SELECT hr FROM HealthReminder hr WHERE hr.user.id = :userId " +
           "AND hr.reminderTime BETWEEN :now AND :plusMinutes " +
           "AND hr.isCompleted = false ORDER BY hr.reminderTime ASC")
    List<HealthReminder> findUpcomingReminders(
            @Param("userId") Long userId, 
            @Param("now") LocalDateTime now, 
            @Param("plusMinutes") LocalDateTime plusMinutes);
}

/****************************
 * Service
 ****************************/

// 文件: src/main/java/com/health/health_demo/service/HealthReminderService.java
package com.health.health_demo.service;

import com.health.health_demo.model.dto.HealthReminderDTO;

import java.time.LocalDate;
import java.util.List;

public interface HealthReminderService {
    
    /**
     * 创建健康提醒
     * 
     * @param healthReminderDTO 健康提醒DTO
     * @return 创建的健康提醒DTO
     */
    HealthReminderDTO createReminder(HealthReminderDTO healthReminderDTO);
    
    /**
     * 根据ID获取健康提醒
     * 
     * @param id 健康提醒ID
     * @return 健康提醒DTO
     */
    HealthReminderDTO getReminderById(Long id);
    
    /**
     * 获取用户的所有健康提醒
     * 
     * @param userId 用户ID
     * @return 健康提醒DTO列表
     */
    List<HealthReminderDTO> getUserReminders(Long userId);
    
    /**
     * 获取用户指定类型的健康提醒
     * 
     * @param userId 用户ID
     * @param reminderType 提醒类型
     * @return 健康提醒DTO列表
     */
    List<HealthReminderDTO> getUserRemindersByType(Long userId, String reminderType);
    
    /**
     * 获取用户指定日期的提醒
     * 
     * @param userId 用户ID
     * @param date 日期
     * @return 健康提醒DTO列表
     */
    List<HealthReminderDTO> getUserRemindersByDate(Long userId, LocalDate date);
    
    /**
     * 获取用户未来的提醒
     * 
     * @param userId 用户ID
     * @return 健康提醒DTO列表
     */
    List<HealthReminderDTO> getUserUpcomingReminders(Long userId);
    
    /**
     * 获取用户过期且未完成的提醒
     * 
     * @param userId 用户ID
     * @return 健康提醒DTO列表
     */
    List<HealthReminderDTO> getUserOverdueReminders(Long userId);
    
    /**
     * 完成健康提醒
     * 
     * @param id 健康提醒ID
     * @return 更新后的健康提醒DTO
     */
    HealthReminderDTO completeReminder(Long id);
    
    /**
     * 更新健康提醒
     * 
     * @param id 健康提醒ID
     * @param healthReminderDTO 健康提醒DTO
     * @return 更新后的健康提醒DTO
     */
    HealthReminderDTO updateReminder(Long id, HealthReminderDTO healthReminderDTO);
    
    /**
     * 删除健康提醒
     * 
     * @param id 健康提醒ID
     */
    void deleteReminder(Long id);
}

// 文件: src/main/java/com/health/health_demo/service/impl/HealthReminderServiceImpl.java
package com.health.health_demo.service.impl;

import com.health.health_demo.exception.ResourceNotFoundException;
import com.health.health_demo.model.HealthReminder;
import com.health.health_demo.model.User;
import com.health.health_demo.model.dto.HealthReminderDTO;
import com.health.health_demo.repository.HealthReminderRepository;
import com.health.health_demo.repository.UserRepository;
import com.health.health_demo.service.HealthReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthReminderServiceImpl implements HealthReminderService {
    
    private final HealthReminderRepository healthReminderRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public HealthReminderServiceImpl(HealthReminderRepository healthReminderRepository, UserRepository userRepository) {
        this.healthReminderRepository = healthReminderRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public HealthReminderDTO createReminder(HealthReminderDTO healthReminderDTO) {
        User user = userRepository.findById(healthReminderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + healthReminderDTO.getUserId()));
        
        HealthReminder healthReminder = new HealthReminder(
                user,
                healthReminderDTO.getReminderType(),
                healthReminderDTO.getTitle(),
                healthReminderDTO.getDescription(),
                healthReminderDTO.getReminderTime(),
                healthReminderDTO.getIsRepeating(),
                healthReminderDTO.getRepeatPattern()
        );
        
        HealthReminder savedReminder = healthReminderRepository.save(healthReminder);
        return new HealthReminderDTO(savedReminder);
    }
    
    @Override
    public HealthReminderDTO getReminderById(Long id) {
        HealthReminder healthReminder = healthReminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health reminder not found with id: " + id));
        return new HealthReminderDTO(healthReminder);
    }
    
    @Override
    public List<HealthReminderDTO> getUserReminders(Long userId) {
        List<HealthReminder> reminders = healthReminderRepository.findByUserIdOrderByReminderTimeAsc(userId);
        return reminders.stream()
                .map(HealthReminderDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HealthReminderDTO> getUserRemindersByType(Long userId, String reminderType) {
        List<HealthReminder> reminders = healthReminderRepository
                .findByUserIdAndReminderTypeOrderByReminderTimeAsc(userId, reminderType);
        return reminders.stream()
                .map(HealthReminderDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HealthReminderDTO> getUserRemindersByDate(Long userId, LocalDate date) {
        List<HealthReminder> reminders = healthReminderRepository.findByUserIdAndDate(userId, date);
        return reminders.stream()
                .map(HealthReminderDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HealthReminderDTO> getUserUpcomingReminders(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<HealthReminder> reminders = healthReminderRepository
                .findByUserIdAndReminderTimeGreaterThanEqualAndIsCompletedFalseOrderByReminderTimeAsc(userId, now);
        return reminders.stream()
                .map(HealthReminderDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HealthReminderDTO> getUserOverdueReminders(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<HealthReminder> reminders = healthReminderRepository
                .findByUserIdAndReminderTimeLessThanAndIsCompletedFalseOrderByReminderTimeAsc(userId, now);
        return reminders.stream()
                .map(HealthReminderDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public HealthReminderDTO completeReminder(Long id) {
        HealthReminder healthReminder = healthReminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health reminder not found with id: " + id));
        
        healthReminder.complete();
        HealthReminder updatedReminder = healthReminderRepository.save(healthReminder);
        
        // 如果是重复提醒，创建下一次提醒
        if (healthReminder.getIsRepeating() && healthReminder.getRepeatPattern() != null) {
            HealthReminder nextReminder = healthReminder.generateNextReminder();
            if (nextReminder != null) {
                healthReminderRepository.save(nextReminder);
            }
        }
        
        return new HealthReminderDTO(updatedReminder);
    }
    
    @Override
    @Transactional
    public HealthReminderDTO updateReminder(Long id, HealthReminderDTO healthReminderDTO) {
        HealthReminder healthReminder = healthReminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health reminder not found with id: " + id));
        
        healthReminder.setReminderType(healthReminderDTO.getReminderType());
        healthReminder.setTitle(healthReminderDTO.getTitle());
        healthReminder.setDescription(healthReminderDTO.getDescription());
        healthReminder.setReminderTime(healthReminderDTO.getReminderTime());
        healthReminder.setIsRepeating(healthReminderDTO.getIsRepeating());
        healthReminder.setRepeatPattern(healthReminderDTO.getRepeatPattern());
        healthReminder.setUpdatedAt(LocalDateTime.now());
        
        HealthReminder updatedReminder = healthReminderRepository.save(healthReminder);
        return new HealthReminderDTO(updatedReminder);
    }
    
    @Override
    @Transactional
    public void deleteReminder(Long id) {
        HealthReminder healthReminder = healthReminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health reminder not found with id: " + id));
        
        healthReminderRepository.delete(healthReminder);
    }
}

/****************************
 * Controller
 ****************************/

// 文件: src/main/java/com/health/health_demo/controller/HealthReminderController.java
package com.health.health_demo.controller;

import com.health.health_demo.dto.ApiResponse;
import com.health.health_demo.model.dto.HealthReminderDTO;
import com.health.health_demo.service.HealthReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 健康提醒控制器
 */
@RestController
@RequestMapping("/api/health-reminders")
@CrossOrigin(origins = "*")
public class HealthReminderController {
    
    private final HealthReminderService healthReminderService;
    
    @Autowired
    public HealthReminderController(HealthReminderService healthReminderService) {
        this.healthReminderService = healthReminderService;
    }
    
    /**
     * 创建健康提醒
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HealthReminderDTO>> createReminder(@RequestBody HealthReminderDTO healthReminderDTO) {
        HealthReminderDTO createdReminder = healthReminderService.createReminder(healthReminderDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "健康提醒创建成功", createdReminder));
    }
    
    /**
     * 获取指定ID的健康提醒
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HealthReminderDTO>> getReminder(@PathVariable Long id) {
        HealthReminderDTO healthReminderDTO = healthReminderService.getReminderById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", healthReminderDTO));
    }
    
    /**
     * 获取用户的所有健康提醒
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<HealthReminderDTO>>> getUserReminders(@PathVariable Long userId) {
        List<HealthReminderDTO> reminders = healthReminderService.getUserReminders(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", reminders));
    }
    
    /**
     * 获取用户指定类型的健康提醒
     */
    @GetMapping("/user/{userId}/type")
    public ResponseEntity<ApiResponse<List<HealthReminderDTO>>> getUserRemindersByType(
            @PathVariable Long userId,
            @RequestParam String reminderType) {
        List<HealthReminderDTO> reminders = healthReminderService.getUserRemindersByType(userId, reminderType);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", reminders));
    }
    
    /**
     * 获取用户指定日期的提醒
     */
    @GetMapping("/user/{userId}/date")
    public ResponseEntity<ApiResponse<List<HealthReminderDTO>>> getUserRemindersByDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<HealthReminderDTO> reminders = healthReminderService.getUserRemindersByDate(userId, date);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", reminders));
    }
    
    /**
     * 获取用户未来的提醒
     */
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<ApiResponse<List<HealthReminderDTO>>> getUserUpcomingReminders(@PathVariable Long userId) {
        List<HealthReminderDTO> reminders = healthReminderService.getUserUpcomingReminders(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", reminders));
    }
    
    /**
     * 获取用户过期且未完成的提醒
     */
    @GetMapping("/user/{userId}/overdue")
    public ResponseEntity<ApiResponse<List<HealthReminderDTO>>> getUserOverdueReminders(@PathVariable Long userId) {
        List<HealthReminderDTO> reminders = healthReminderService.getUserOverdueReminders(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", reminders));
    }
    
    /**
     * 完成健康提醒
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<HealthReminderDTO>> completeReminder(@PathVariable Long id) {
        HealthReminderDTO completedReminder = healthReminderService.completeReminder(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "提醒已标记为完成", completedReminder));
    }
    
    /**
     * 更新健康提醒
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HealthReminderDTO>> updateReminder(
            @PathVariable Long id,
            @RequestBody HealthReminderDTO healthReminderDTO) {
        HealthReminderDTO updatedReminder = healthReminderService.updateReminder(id, healthReminderDTO);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "健康提醒更新成功", updatedReminder));
    }
    
    /**
     * 删除健康提醒
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(@PathVariable Long id) {
        healthReminderService.deleteReminder(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "健康提醒删除成功", null));
    }
} 