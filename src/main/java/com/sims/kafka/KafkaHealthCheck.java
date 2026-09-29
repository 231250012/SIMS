package com.sims.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Kafka健康检查组件
 * 在应用启动时检查Kafka连接状态
 */
@Component
@Profile("!local")
public class KafkaHealthCheck {

    private static final Logger logger = LoggerFactory.getLogger(KafkaHealthCheck.class);

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @PostConstruct
    public void checkKafkaConnection() {
        logger.info("开始检查Kafka连接状态...");
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            // 设置超时时间为5秒
            ListTopicsResult topicsResult = adminClient.listTopics();
            Set<String> topics = topicsResult.names().get(5, TimeUnit.SECONDS);
            
            logger.info("Kafka连接成功! 发现 {} 个主题: {}", topics.size(), topics);
            
            // 检查我们需要的主题是否存在
            String gradeUpdatesTopic = "grade-updates";
            if (topics.contains(gradeUpdatesTopic)) {
                logger.info("主题 '{}' 已存在", gradeUpdatesTopic);
            } else {
                logger.warn("主题 '{}' 不存在，可能需要创建", gradeUpdatesTopic);
            }
            
        } catch (Exception e) {
            logger.error("Kafka连接失败: {}", e.getMessage(), e);
            logger.error("请检查:");
            logger.error("1. Kafka服务器是否在 localhost:9092 运行");
            logger.error("2. 网络连接是否正常");
            logger.error("3. Kafka配置是否正确");
        }
    }
    
    /**
     * 运行时检查Kafka连接状态
     */
    public boolean isKafkaAvailable() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            ListTopicsResult topicsResult = adminClient.listTopics();
            topicsResult.names().get(3, TimeUnit.SECONDS);
            
            return true;
            
        } catch (Exception e) {
            logger.debug("Kafka连接检查失败: {}", e.getMessage());
            return false;
        }
    }
}