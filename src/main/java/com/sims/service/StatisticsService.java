package com.sims.service;

import com.sims.aspect.ApiStatisticsAspect;
import com.sims.dto.ApiStatResponse;
import com.sims.dto.ScoreDistributionResponse;
import com.sims.dto.SystemOverviewResponse;
import com.sims.entity.ApiStats;
import com.sims.repository.ApiStatsRepository;
import com.sims.repository.ClassRepository;
import com.sims.repository.StudentRepository;
import com.sims.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统计服务
 * 提供API调用统计和系统概览功能
 */
@Service
@Transactional(readOnly = true)
public class StatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
    
    @Autowired
    private ApiStatsRepository apiStatsRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ApiStatisticsAspect apiStatisticsAspect;
    
    /**
     * 获取所有API统计信息
     * @return API统计列表
     */
    @Cacheable(value = "apiStats", key = "'all'")
    public List<ApiStatResponse> getAllApiStatistics() {
        try {
            List<ApiStats> apiStatsList = apiStatsRepository.findAllOrderByCallCountDesc();
            return convertToApiStatResponses(apiStatsList);
        } catch (Exception e) {
            logger.error("获取API统计信息失败", e);
            throw new RuntimeException("获取API统计信息失败", e);
        }
    }
    
    /**
     * 获取热门API统计信息
     * @param limit 限制数量
     * @return 热门API统计列表
     */
    @Cacheable(value = "apiStats", key = "'top:' + #limit")
    public List<ApiStatResponse> getTopApiStatistics(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<ApiStats> topApiStats = apiStatsRepository.findTopApisByCallCount(pageable);
            return convertToApiStatResponses(topApiStats);
        } catch (Exception e) {
            logger.error("获取热门API统计信息失败", e);
            throw new RuntimeException("获取热门API统计信息失败", e);
        }
    }
    
    /**
     * 获取最近更新的API统计信息
     * @param limit 限制数量
     * @return 最近更新的API统计列表
     */
    @Cacheable(value = "apiStats", key = "'recent:' + #limit")
    public List<ApiStatResponse> getRecentApiStatistics(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<ApiStats> recentApiStats = apiStatsRepository.findRecentlyUpdated(pageable);
            return convertToApiStatResponses(recentApiStats);
        } catch (Exception e) {
            logger.error("获取最近API统计信息失败", e);
            throw new RuntimeException("获取最近API统计信息失败", e);
        }
    }
    
    /**
     * 获取总API调用次数（数据库）
     * @return 总调用次数
     */
    public Long getTotalApiCalls() {
        try {
            return apiStatsRepository.calculateTotalCallCount().orElse(0L);
        } catch (Exception e) {
            logger.error("获取总API调用次数失败", e);
            return 0L;
        }
    }
    
    /**
     * 获取总API调用次数（Redis）
     * @return 总调用次数
     */
    public Long getTotalApiCallsFromRedis() {
        try {
            return apiStatisticsAspect.getTotalRedisCallCount();
        } catch (Exception e) {
            logger.error("获取Redis总API调用次数失败", e);
            return 0L;
        }
    }
    
    /**
     * 获取平均API调用次数
     * @return 平均调用次数
     */
    public Double getAverageApiCalls() {
        try {
            return apiStatsRepository.calculateAverageCallCount().orElse(0.0);
        } catch (Exception e) {
            logger.error("获取平均API调用次数失败", e);
            return 0.0;
        }
    }
    
    /**
     * 获取成绩分布统计
     * @return 成绩分布数据
     */
    public List<ScoreDistributionResponse> getScoreDistribution() {
        try {
            List<ScoreDistributionResponse> distribution = new ArrayList<>();
            
            // 定义成绩区间
            distribution.add(new ScoreDistributionResponse("优秀 (90-100)", 
                studentRepository.countByScoreBetween(new java.math.BigDecimal("90"), new java.math.BigDecimal("100"))));
            distribution.add(new ScoreDistributionResponse("良好 (80-89)", 
                studentRepository.countByScoreBetween(new java.math.BigDecimal("80"), new java.math.BigDecimal("89.99"))));
            distribution.add(new ScoreDistributionResponse("中等 (70-79)", 
                studentRepository.countByScoreBetween(new java.math.BigDecimal("70"), new java.math.BigDecimal("79.99"))));
            distribution.add(new ScoreDistributionResponse("及格 (60-69)", 
                studentRepository.countByScoreBetween(new java.math.BigDecimal("60"), new java.math.BigDecimal("69.99"))));
            distribution.add(new ScoreDistributionResponse("不及格 (<60)", 
                studentRepository.countByScoreLessThan(new java.math.BigDecimal("60"))));
            
            logger.debug("成绩分布统计获取成功");
            return distribution;
        } catch (Exception e) {
            logger.error("获取成绩分布失败", e);
            throw new RuntimeException("获取成绩分布失败", e);
        }
    }

    /**
     * 获取系统概览统计信息
     * @return 系统概览
     */
    public SystemOverviewResponse getSystemOverview() {
        try {
            SystemOverviewResponse overview = new SystemOverviewResponse();
            
            // 设置基础统计数据
            overview.setTotalApiCalls(getTotalApiCalls());
            overview.setTotalApiCallsRedis(getTotalApiCallsFromRedis());
            overview.setTotalStudents(studentRepository.count());
            overview.setTotalClasses(classRepository.count());
            overview.setTotalUsers(userRepository.count());
            overview.setTotalApiEndpoints(apiStatsRepository.countApiEndpoints());
            overview.setAverageApiCalls(getAverageApiCalls());
            overview.setLastUpdateTime(LocalDateTime.now());
            
            // 设置学生成绩相关统计
            overview.setAverageScore(studentRepository.calculateOverallAverageScore()
                    .map(java.math.BigDecimal::doubleValue).orElse(0.0));
            overview.setTopPerformingClass(getTopPerformingClass());
            
            // 设置热门API端点（前10个）
            overview.setTopApiEndpoints(getTopApiStatistics(10));
            
            logger.debug("系统概览统计信息获取成功");
            return overview;
        } catch (Exception e) {
            logger.error("获取系统概览失败", e);
            throw new RuntimeException("获取系统概览失败", e);
        }
    }
    
    /**
     * 将ApiStats实体转换为ApiStatResponse DTO
     * @param apiStatsList API统计实体列表
     * @return API统计响应DTO列表
     */
    private List<ApiStatResponse> convertToApiStatResponses(List<ApiStats> apiStatsList) {
        return apiStatsList.stream()
                .map(this::convertToApiStatResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 将ApiStats实体转换为ApiStatResponse DTO
     * @param apiStats API统计实体
     * @return API统计响应DTO
     */
    private ApiStatResponse convertToApiStatResponse(ApiStats apiStats) {
        ApiStatResponse response = new ApiStatResponse();
        response.setId(apiStats.getId());
        response.setEndpoint(apiStats.getEndpoint());
        response.setCallCount(apiStats.getCallCount());
        response.setLastUpdate(apiStats.getLastUpdate());
        
        // 获取Redis中的调用次数
        try {
            Long redisCount = apiStatisticsAspect.getRedisCallCount(apiStats.getEndpoint());
            response.setRedisCallCount(redisCount);
        } catch (Exception e) {
            logger.warn("获取Redis调用次数失败: " + apiStats.getEndpoint(), e);
            response.setRedisCallCount(0L);
        }
        
        return response;
    }
    
    /**
     * 获取表现最佳的班级
     * @return 班级名称
     */
    private String getTopPerformingClass() {
        try {
            // 这里可以根据平均成绩来确定最佳班级
            // 简化实现，返回一个示例值
            return "计算机科学1班";
        } catch (Exception e) {
            logger.warn("获取最佳班级失败", e);
            return "暂无数据";
        }
    }
}