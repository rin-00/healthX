/**
 * 健康目标管理模块脚手架
 * 包含：实体类、DTO、Repository、Service、Controller
 */

/****************************
 * 实体类
 ****************************/

// 文件: src/main/java/com/health/health_demo/model/HealthGoal.java
package com.health.health_demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "health_goals")
public class HealthGoal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "goal_type", nullable = false)
    private String goalType;
    
    @Column(name = "target_value", nullable = false)
    private Double targetValue;
    
    @Column(name = "current_value", nullable = false)
    private Double currentValue;
    
    @Column
    private String unit;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 目标类型枚举
    public enum GoalType {
        WEIGHT_LOSS("减重"),
        WEIGHT_GAIN("增重"),
        STEPS("步数"),
        EXERCISE_FREQUENCY("运动频率"),
        SLEEP_DURATION("睡眠时长"),
        CALORIE_CONTROL("卡路里控制"),
        CUSTOM("自定义");
        
        private final String displayName;
        
        GoalType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // 构造函数
    public HealthGoal(User user, String goalType, Double targetValue, Double currentValue,
                      String unit, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.goalType = goalType;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.unit = unit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCompleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 更新进度
    public void updateProgress(Double newValue) {
        this.currentValue = newValue;
        this.updatedAt = LocalDateTime.now();
        // 检查是否达成目标
        checkCompletion();
    }
    
    // 检查目标是否完成
    private void checkCompletion() {
        if (GoalType.WEIGHT_LOSS.name().equals(goalType)) {
            // 减重目标：当前值 <= 目标值时完成
            this.isCompleted = currentValue <= targetValue;
        } else if (GoalType.WEIGHT_GAIN.name().equals(goalType)) {
            // 增重目标：当前值 >= 目标值时完成
            this.isCompleted = currentValue >= targetValue;
        } else {
            // 其他目标：当前值 >= 目标值时完成
            this.isCompleted = currentValue >= targetValue;
        }
    }
}

/****************************
 * DTO
 ****************************/

// 文件: src/main/java/com/health/health_demo/model/dto/HealthGoalDTO.java
package com.health.health_demo.model.dto;

import com.health.health_demo.model.HealthGoal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthGoalDTO {
    
    private Long id;
    private Long userId;
    private String goalType;
    private Double targetValue;
    private Double currentValue;
    private String unit;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 将实体转换为DTO
    public HealthGoalDTO(HealthGoal healthGoal) {
        this.id = healthGoal.getId();
        this.userId = healthGoal.getUser().getId();
        this.goalType = healthGoal.getGoalType();
        this.targetValue = healthGoal.getTargetValue();
        this.currentValue = healthGoal.getCurrentValue();
        this.unit = healthGoal.getUnit();
        this.startDate = healthGoal.getStartDate();
        this.endDate = healthGoal.getEndDate();
        this.isCompleted = healthGoal.getIsCompleted();
        this.createdAt = healthGoal.getCreatedAt();
        this.updatedAt = healthGoal.getUpdatedAt();
    }
}

/****************************
 * Repository
 ****************************/

// 文件: src/main/java/com/health/health_demo/repository/HealthGoalRepository.java
package com.health.health_demo.repository;

import com.health.health_demo.model.HealthGoal;
import com.health.health_demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HealthGoalRepository extends JpaRepository<HealthGoal, Long> {
    
    // 根据用户查询所有健康目标，按结束日期升序排序
    List<HealthGoal> findByUserOrderByEndDateAsc(User user);
    
    // 根据用户ID查询所有健康目标，按结束日期升序排序
    List<HealthGoal> findByUserIdOrderByEndDateAsc(Long userId);
    
    // 根据用户ID和目标类型查询健康目标，按结束日期升序排序
    List<HealthGoal> findByUserIdAndGoalTypeOrderByEndDateAsc(Long userId, String goalType);
    
    // 根据用户ID查询当前活跃的健康目标（未完成且未过期）
    List<HealthGoal> findByUserIdAndIsCompletedFalseAndEndDateGreaterThanEqualOrderByEndDateAsc(
            Long userId, LocalDate currentDate);
    
    // 根据用户ID和目标类型查询当前活跃的健康目标
    List<HealthGoal> findByUserIdAndGoalTypeAndIsCompletedFalseAndEndDateGreaterThanEqualOrderByEndDateAsc(
            Long userId, String goalType, LocalDate currentDate);
}

/****************************
 * Service
 ****************************/

// 文件: src/main/java/com/health/health_demo/service/HealthGoalService.java
package com.health.health_demo.service;

import com.health.health_demo.model.dto.HealthGoalDTO;

import java.util.List;

public interface HealthGoalService {
    
    /**
     * 创建健康目标
     * 
     * @param healthGoalDTO 健康目标DTO
     * @return 创建的健康目标DTO
     */
    HealthGoalDTO createGoal(HealthGoalDTO healthGoalDTO);
    
    /**
     * 根据ID获取健康目标
     * 
     * @param id 健康目标ID
     * @return 健康目标DTO
     */
    HealthGoalDTO getGoalById(Long id);
    
    /**
     * 获取用户的所有健康目标
     * 
     * @param userId 用户ID
     * @return 健康目标DTO列表
     */
    List<HealthGoalDTO> getUserGoals(Long userId);
    
    /**
     * 获取用户指定类型的健康目标
     * 
     * @param userId 用户ID
     * @param goalType 目标类型
     * @return 健康目标DTO列表
     */
    List<HealthGoalDTO> getUserGoalsByType(Long userId, String goalType);
    
    /**
     * 获取用户当前活跃的健康目标
     * 
     * @param userId 用户ID
     * @return 健康目标DTO列表
     */
    List<HealthGoalDTO> getUserActiveGoals(Long userId);
    
    /**
     * 更新健康目标进度
     * 
     * @param id 健康目标ID
     * @param newValue 新的当前值
     * @return 更新后的健康目标DTO
     */
    HealthGoalDTO updateGoalProgress(Long id, Double newValue);
    
    /**
     * 完成健康目标
     * 
     * @param id 健康目标ID
     * @return 更新后的健康目标DTO
     */
    HealthGoalDTO completeGoal(Long id);
    
    /**
     * 更新健康目标
     * 
     * @param id 健康目标ID
     * @param healthGoalDTO 健康目标DTO
     * @return 更新后的健康目标DTO
     */
    HealthGoalDTO updateGoal(Long id, HealthGoalDTO healthGoalDTO);
    
    /**
     * 删除健康目标
     * 
     * @param id 健康目标ID
     */
    void deleteGoal(Long id);
}

// 文件: src/main/java/com/health/health_demo/service/impl/HealthGoalServiceImpl.java
package com.health.health_demo.service.impl;

import com.health.health_demo.exception.ResourceNotFoundException;
import com.health.health_demo.model.HealthGoal;
import com.health.health_demo.model.User;
import com.health.health_demo.model.dto.HealthGoalDTO;
import com.health.health_demo.repository.HealthGoalRepository;
import com.health.health_demo.repository.UserRepository;
import com.health.health_demo.service.HealthGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthGoalServiceImpl implements HealthGoalService {
    
    private final HealthGoalRepository healthGoalRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public HealthGoalServiceImpl(HealthGoalRepository healthGoalRepository, UserRepository userRepository) {
        this.healthGoalRepository = healthGoalRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public HealthGoalDTO createGoal(HealthGoalDTO healthGoalDTO) {
        User user = userRepository.findById(healthGoalDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + healthGoalDTO.getUserId()));
        
        HealthGoal healthGoal = new HealthGoal(
                user,
                healthGoalDTO.getGoalType(),
                healthGoalDTO.getTargetValue(),
                healthGoalDTO.getCurrentValue(),
                healthGoalDTO.getUnit(),
                healthGoalDTO.getStartDate(),
                healthGoalDTO.getEndDate()
        );
        
        HealthGoal savedGoal = healthGoalRepository.save(healthGoal);
        return new HealthGoalDTO(savedGoal);
    }
    
    @Override
    public HealthGoalDTO getGoalById(Long id) {
        HealthGoal healthGoal = healthGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health goal not found with id: " + id));
        return new HealthGoalDTO(healthGoal);
    }
    
    @Override
    public List<HealthGoalDTO> getUserGoals(Long userId) {
        List<HealthGoal> goals = healthGoalRepository.findByUserIdOrderByEndDateAsc(userId);
        return goals.stream()
                .map(HealthGoalDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HealthGoalDTO> getUserGoalsByType(Long userId, String goalType) {
        List<HealthGoal> goals = healthGoalRepository.findByUserIdAndGoalTypeOrderByEndDateAsc(userId, goalType);
        return goals.stream()
                .map(HealthGoalDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HealthGoalDTO> getUserActiveGoals(Long userId) {
        List<HealthGoal> goals = healthGoalRepository
                .findByUserIdAndIsCompletedFalseAndEndDateGreaterThanEqualOrderByEndDateAsc(userId, LocalDate.now());
        return goals.stream()
                .map(HealthGoalDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public HealthGoalDTO updateGoalProgress(Long id, Double newValue) {
        HealthGoal healthGoal = healthGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health goal not found with id: " + id));
        
        healthGoal.updateProgress(newValue);
        HealthGoal updatedGoal = healthGoalRepository.save(healthGoal);
        return new HealthGoalDTO(updatedGoal);
    }
    
    @Override
    @Transactional
    public HealthGoalDTO completeGoal(Long id) {
        HealthGoal healthGoal = healthGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health goal not found with id: " + id));
        
        healthGoal.setIsCompleted(true);
        healthGoal.setUpdatedAt(LocalDateTime.now());
        HealthGoal updatedGoal = healthGoalRepository.save(healthGoal);
        return new HealthGoalDTO(updatedGoal);
    }
    
    @Override
    @Transactional
    public HealthGoalDTO updateGoal(Long id, HealthGoalDTO healthGoalDTO) {
        HealthGoal healthGoal = healthGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health goal not found with id: " + id));
        
        healthGoal.setGoalType(healthGoalDTO.getGoalType());
        healthGoal.setTargetValue(healthGoalDTO.getTargetValue());
        healthGoal.setUnit(healthGoalDTO.getUnit());
        healthGoal.setStartDate(healthGoalDTO.getStartDate());
        healthGoal.setEndDate(healthGoalDTO.getEndDate());
        healthGoal.setUpdatedAt(LocalDateTime.now());
        
        HealthGoal updatedGoal = healthGoalRepository.save(healthGoal);
        return new HealthGoalDTO(updatedGoal);
    }
    
    @Override
    @Transactional
    public void deleteGoal(Long id) {
        HealthGoal healthGoal = healthGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health goal not found with id: " + id));
        
        healthGoalRepository.delete(healthGoal);
    }
}

/****************************
 * Controller
 ****************************/

// 文件: src/main/java/com/health/health_demo/controller/HealthGoalController.java
package com.health.health_demo.controller;

import com.health.health_demo.dto.ApiResponse;
import com.health.health_demo.model.dto.HealthGoalDTO;
import com.health.health_demo.service.HealthGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 健康目标控制器
 */
@RestController
@RequestMapping("/api/health-goals")
@CrossOrigin(origins = "*")
public class HealthGoalController {
    
    private final HealthGoalService healthGoalService;
    
    @Autowired
    public HealthGoalController(HealthGoalService healthGoalService) {
        this.healthGoalService = healthGoalService;
    }
    
    /**
     * 创建健康目标
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HealthGoalDTO>> createGoal(@RequestBody HealthGoalDTO healthGoalDTO) {
        HealthGoalDTO createdGoal = healthGoalService.createGoal(healthGoalDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "健康目标创建成功", createdGoal));
    }
    
    /**
     * 获取指定ID的健康目标
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HealthGoalDTO>> getGoal(@PathVariable Long id) {
        HealthGoalDTO healthGoalDTO = healthGoalService.getGoalById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", healthGoalDTO));
    }
    
    /**
     * 获取用户的所有健康目标
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<HealthGoalDTO>>> getUserGoals(@PathVariable Long userId) {
        List<HealthGoalDTO> goals = healthGoalService.getUserGoals(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", goals));
    }
    
    /**
     * 获取用户指定类型的健康目标
     */
    @GetMapping("/user/{userId}/type")
    public ResponseEntity<ApiResponse<List<HealthGoalDTO>>> getUserGoalsByType(
            @PathVariable Long userId,
            @RequestParam String goalType) {
        List<HealthGoalDTO> goals = healthGoalService.getUserGoalsByType(userId, goalType);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", goals));
    }
    
    /**
     * 获取用户当前活跃的健康目标
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ApiResponse<List<HealthGoalDTO>>> getUserActiveGoals(@PathVariable Long userId) {
        List<HealthGoalDTO> goals = healthGoalService.getUserActiveGoals(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "获取成功", goals));
    }
    
    /**
     * 更新健康目标
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HealthGoalDTO>> updateGoal(
            @PathVariable Long id,
            @RequestBody HealthGoalDTO healthGoalDTO) {
        HealthGoalDTO updatedGoal = healthGoalService.updateGoal(id, healthGoalDTO);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "健康目标更新成功", updatedGoal));
    }
    
    /**
     * 更新健康目标进度
     */
    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<HealthGoalDTO>> updateGoalProgress(
            @PathVariable Long id,
            @RequestBody Map<String, Double> progressUpdate) {
        Double newValue = progressUpdate.get("currentValue");
        if (newValue == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "当前值不能为空", null));
        }
        
        HealthGoalDTO updatedGoal = healthGoalService.updateGoalProgress(id, newValue);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "进度更新成功", updatedGoal));
    }
    
    /**
     * 完成健康目标
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<HealthGoalDTO>> completeGoal(@PathVariable Long id) {
        HealthGoalDTO completedGoal = healthGoalService.completeGoal(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "目标已标记为完成", completedGoal));
    }
    
    /**
     * 删除健康目标
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long id) {
        healthGoalService.deleteGoal(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "健康目标删除成功", null));
    }
} 