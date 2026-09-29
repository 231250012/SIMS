package com.sims.controller;

import com.sims.dto.ApiStatResponse;
import com.sims.dto.ScoreDistributionResponse;
import com.sims.dto.SystemOverviewResponse;
import com.sims.security.annotation.RequireRole;
import com.sims.entity.UserRole;
import com.sims.service.StatisticsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计信息控制器
 * 提供API调用统计和系统概览接口
 */
@RestController
@RequestMapping("/api/statistics")

public class StatisticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * 获取所有API统计信息
     * @return API统计列表
     */
    @GetMapping("/api-calls")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<List<ApiStatResponse>> getApiStatistics() {
        try {
            List<ApiStatResponse> statistics = statisticsService.getAllApiStatistics();
            logger.info("获取API统计信息成功，共 {} 条记录", statistics.size());
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("获取API统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取热门API统计信息
     * @param limit 限制数量，默认10
     * @return 热门API统计列表
     */
    @GetMapping("/api-calls/top")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<List<ApiStatResponse>> getTopApiStatistics(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                limit = 10; // 默认值和最大值限制
            }
            
            List<ApiStatResponse> topStatistics = statisticsService.getTopApiStatistics(limit);
            logger.info("获取热门API统计信息成功，返回前 {} 条记录", limit);
            return ResponseEntity.ok(topStatistics);
        } catch (Exception e) {
            logger.error("获取热门API统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取总调用次数
     * @return 总调用次数信息
     */
    @GetMapping("/api-calls/total")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Map<String, Object>> getTotalApiCalls() {
        try {
            Long dbTotal = statisticsService.getTotalApiCalls();
            Long redisTotal = statisticsService.getTotalApiCallsFromRedis();
            Double average = statisticsService.getAverageApiCalls();
            
            Map<String, Object> result = Map.of(
                    "databaseTotal", dbTotal,
                    "redisTotal", redisTotal,
                    "averagePerEndpoint", average,
                    "difference", Math.abs(dbTotal - redisTotal)
            );
            
            logger.info("获取总调用次数成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取总调用次数失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取成绩分布统计
     * @return 成绩分布数据
     */
    @GetMapping("/score-distribution")
    public ResponseEntity<List<ScoreDistributionResponse>> getScoreDistribution() {
        try {
            List<ScoreDistributionResponse> distribution = statisticsService.getScoreDistribution();
            logger.info("获取成绩分布统计成功，共 {} 个区间", distribution.size());
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            logger.error("获取成绩分布统计失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取系统概览统计信息
     * @return 系统概览
     */
    @GetMapping("/overview")
    @RequireRole(UserRole.ADMIN)

    public ResponseEntity<SystemOverviewResponse> getSystemOverview() {
        try {
            SystemOverviewResponse overview = statisticsService.getSystemOverview();
            logger.info("获取系统概览成功");
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            logger.error("获取系统概览失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}