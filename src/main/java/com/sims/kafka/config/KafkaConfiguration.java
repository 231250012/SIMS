package com.sims.kafka.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.annotation.EnableRetry;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka配置类
 * 负责配置Kafka生产者、消费者和主题管理
 * 需求: 4.1, 4.2, 4.3
 */
@Configuration
@EnableKafka
@EnableRetry
@Profile("!local")
public class KafkaConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfiguration.class);

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:sims-grade-consumer-group}")
    private String consumerGroupId;

    @Value("${kafka.topic.grade-updates:grade-updates}")
    private String gradeUpdatesTopic;

    @Value("${kafka.topic.grade-updates-dlq:grade-updates-dlq}")
    private String gradeUpdatesDlqTopic;

    @Value("${kafka.topic.partitions:3}")
    private int topicPartitions;

    @Value("${kafka.topic.replication-factor:2}")
    private short topicReplicationFactor;

    // ==================== Producer Configuration ====================

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        logger.info("配置Kafka生产者，Bootstrap服务器: {}", bootstrapServers);
        
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // 可靠性配置
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // 性能配置
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        logger.info("Kafka模板创建成功");
        return template;
    }

    // ==================== Consumer Configuration ====================

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        logger.info("配置Kafka消费者，Bootstrap服务器: {}, 消费者组: {}", bootstrapServers, consumerGroupId);
        
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // 消费者配置
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        // JSON反序列化配置
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.sims.kafka.dto");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.sims.kafka.dto.GradeUpdateEvent");
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // 手动确认模式
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // 并发配置
        factory.setConcurrency(3);
        
        logger.info("Kafka监听器容器工厂创建成功");
        return factory;
    }

    // ==================== Admin Configuration ====================

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // ==================== Topic Configuration ====================

    @Bean
    public NewTopic gradeUpdatesTopic() {
        logger.info("创建主题: {}, partitions={}, replicationFactor={}",
                gradeUpdatesTopic, topicPartitions, topicReplicationFactor);
        return new NewTopic(gradeUpdatesTopic, topicPartitions, topicReplicationFactor);
    }

    @Bean
    public NewTopic gradeUpdatesDlqTopic() {
        logger.info("创建死信队列主题: {}, partitions=1, replicationFactor={}",
                gradeUpdatesDlqTopic, topicReplicationFactor);
        return new NewTopic(gradeUpdatesDlqTopic, 1, topicReplicationFactor);
    }
}