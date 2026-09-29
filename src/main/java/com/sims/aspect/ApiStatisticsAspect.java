package com.sims.aspect;

import com.sims.entity.ApiStats;
import com.sims.repository.ApiStatsRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * API统计切面
 * 使用 AOP 将调用次数写入 Redis，并异步落库
 */
@Aspect
@Component
public class ApiStatisticsAspect {

    private static final Logger logger = LoggerFactory.getLogger(ApiStatisticsAspect.class);

    private static final String ENDPOINT_COUNT_PREFIX = "api:stats:endpoint:";
    private static final String TOTAL_COUNT_KEY = "api:stats:total";

    @Autowired
    private ApiStatsRepository apiStatsRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Pointcut("execution(public * com.sims.controller..*(..))")
    public void controllerMethods() {}

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void recordApiCall(JoinPoint joinPoint, Object result) {
        try {
            String endpoint = extractEndpoint(joinPoint);
            if (endpoint != null) {
                recordApiStatistics(endpoint);
            }
        } catch (Exception e) {
            logger.error("记录API统计信息时发生错误", e);
        }
    }

    private String extractEndpoint(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String method = request.getMethod();
                String uri = request.getRequestURI();

                String contextPath = request.getContextPath();
                if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
                    uri = uri.substring(contextPath.length());
                }

                return method + " " + uri;
            }
        } catch (Exception e) {
            logger.warn("提取API端点信息失败", e);
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }

    @Async("statisticsTaskExecutor")
    public void recordApiStatistics(String endpoint) {
        try {
            updateRedisCounter(endpoint);
            updateDatabaseStats(endpoint);
            logger.debug("成功记录API调用统计: {}", endpoint);
        } catch (Exception e) {
            logger.error("记录API统计信息失败: " + endpoint, e);
        }
    }

    private void updateRedisCounter(String endpoint) {
        try {
            Long count = stringRedisTemplate.opsForValue().increment(ENDPOINT_COUNT_PREFIX + endpoint);
            stringRedisTemplate.opsForValue().increment(TOTAL_COUNT_KEY);
            logger.debug("Redis计数器更新成功: {} = {}", endpoint, count);
        } catch (Exception e) {
            logger.error("更新Redis计数器失败: " + endpoint, e);
        }
    }

    private void updateDatabaseStats(String endpoint) {
        try {
            int updatedRows = apiStatsRepository.incrementCallCount(endpoint);
            if (updatedRows == 0) {
                createNewApiStats(endpoint);
            }
            logger.debug("数据库统计更新成功: {}", endpoint);
        } catch (Exception e) {
            logger.error("更新数据库统计失败: " + endpoint, e);
        }
    }

    private void createNewApiStats(String endpoint) {
        try {
            if (!apiStatsRepository.existsByEndpoint(endpoint)) {
                ApiStats newStats = new ApiStats(endpoint, 1L);
                apiStatsRepository.save(newStats);
                logger.debug("创建新的API统计记录: {}", endpoint);
            } else {
                apiStatsRepository.incrementCallCount(endpoint);
            }
        } catch (Exception e) {
            logger.error("创建API统计记录失败: " + endpoint, e);
        }
    }

    /**
     * 获取 Redis 中某端点的调用次数
     */
    public Long getMemoryCallCount(String endpoint) {
        return getRedisCallCount(endpoint);
    }

    public Long getRedisCallCount(String endpoint) {
        try {
            String value = stringRedisTemplate.opsForValue().get(ENDPOINT_COUNT_PREFIX + endpoint);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            logger.warn("读取Redis端点计数失败: {}", endpoint, e);
            return 0L;
        }
    }

    /**
     * 获取 Redis 中总调用次数
     */
    public Long getTotalMemoryCallCount() {
        return getTotalRedisCallCount();
    }

    public Long getTotalRedisCallCount() {
        try {
            String value = stringRedisTemplate.opsForValue().get(TOTAL_COUNT_KEY);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            logger.warn("读取Redis总计数失败", e);
            return 0L;
        }
    }

    /**
     * 清除 Redis 中的统计数据
     */
    public void clearMemoryStats() {
        clearRedisStats();
    }

    public void clearRedisStats() {
        try {
            var keys = stringRedisTemplate.keys(ENDPOINT_COUNT_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
            stringRedisTemplate.delete(TOTAL_COUNT_KEY);
            logger.info("Redis统计数据已清除");
        } catch (Exception e) {
            logger.error("清除Redis统计数据失败", e);
        }
    }
}
