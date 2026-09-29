package com.sims.repository;

import com.sims.entity.ApiStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * API统计数据访问接口
 */
@Repository
public interface ApiStatsRepository extends JpaRepository<ApiStats, Long> {
    
    /**
     * 根据端点查找统计信息
     * @param endpoint API端点
     * @return 统计信息
     */
    Optional<ApiStats> findByEndpoint(String endpoint);
    
    /**
     * 检查端点是否存在
     * @param endpoint API端点
     * @return 是否存在
     */
    boolean existsByEndpoint(String endpoint);
    
    /**
     * 增加API调用次数
     * @param endpoint API端点
     * @return 更新的记录数
     */
    @Modifying
    @Transactional
    @Query("UPDATE ApiStats a SET a.callCount = a.callCount + 1 WHERE a.endpoint = :endpoint")
    int incrementCallCount(@Param("endpoint") String endpoint);
    
    /**
     * 增加API调用次数（指定增量）
     * @param endpoint API端点
     * @param increment 增量
     * @return 更新的记录数
     */
    @Modifying
    @Transactional
    @Query("UPDATE ApiStats a SET a.callCount = a.callCount + :increment WHERE a.endpoint = :endpoint")
    int incrementCallCountBy(@Param("endpoint") String endpoint, @Param("increment") long increment);
    
    /**
     * 按调用次数降序查找统计信息
     * @return 统计信息列表
     */
    @Query("SELECT a FROM ApiStats a ORDER BY a.callCount DESC")
    List<ApiStats> findAllOrderByCallCountDesc();
    
    /**
     * 查找调用次数前N的API
     * @param limit 限制数量
     * @return 统计信息列表
     */
    @Query("SELECT a FROM ApiStats a ORDER BY a.callCount DESC")
    List<ApiStats> findTopApisByCallCount(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 根据端点模糊查询统计信息
     * @param endpoint 端点关键字
     * @return 统计信息列表
     */
    @Query("SELECT a FROM ApiStats a WHERE a.endpoint LIKE %:endpoint%")
    List<ApiStats> findByEndpointContaining(@Param("endpoint") String endpoint);
    
    /**
     * 查找调用次数大于指定值的API
     * @param minCallCount 最小调用次数
     * @return 统计信息列表
     */
    @Query("SELECT a FROM ApiStats a WHERE a.callCount >= :minCallCount")
    List<ApiStats> findByCallCountGreaterThanEqual(@Param("minCallCount") long minCallCount);
    
    /**
     * 查找在指定时间之后更新的统计信息
     * @param dateTime 时间点
     * @return 统计信息列表
     */
    @Query("SELECT a FROM ApiStats a WHERE a.lastUpdate >= :dateTime")
    List<ApiStats> findByLastUpdateAfter(@Param("dateTime") LocalDateTime dateTime);
    
    /**
     * 计算总调用次数
     * @return 总调用次数
     */
    @Query("SELECT SUM(a.callCount) FROM ApiStats a")
    Optional<Long> calculateTotalCallCount();
    
    /**
     * 计算平均调用次数
     * @return 平均调用次数
     */
    @Query("SELECT AVG(a.callCount) FROM ApiStats a")
    Optional<Double> calculateAverageCallCount();
    
    /**
     * 统计API端点数量
     * @return API端点数量
     */
    @Query("SELECT COUNT(a) FROM ApiStats a")
    long countApiEndpoints();
    
    /**
     * 查找最近更新的统计信息
     * @param limit 限制数量
     * @return 统计信息列表
     */
    @Query("SELECT a FROM ApiStats a ORDER BY a.lastUpdate DESC")
    List<ApiStats> findRecentlyUpdated(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 重置所有统计数据
     * @return 更新的记录数
     */
    @Modifying
    @Transactional
    @Query("UPDATE ApiStats a SET a.callCount = 0")
    int resetAllStats();
    
    /**
     * 删除调用次数为0的统计记录
     * @return 删除的记录数
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ApiStats a WHERE a.callCount = 0")
    int deleteZeroCallCountStats();
    
    /**
     * 查找指定时间范围内的统计信息
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息列表
     */
    @Query("SELECT a FROM ApiStats a WHERE a.lastUpdate BETWEEN :startTime AND :endTime")
    List<ApiStats> findByLastUpdateBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);
}