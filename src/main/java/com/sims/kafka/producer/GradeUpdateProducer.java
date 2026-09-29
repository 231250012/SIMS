package com.sims.kafka.producer;

import com.sims.kafka.dto.GradeUpdateEvent;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * 成绩更新生产者接口
 * 负责将成绩更新事件发布到Kafka主题
 * 需求: 1.1, 1.3, 1.4
 */
public interface GradeUpdateProducer {

    /**
     * 异步发布成绩更新事件
     * 
     * @param event 成绩更新事件
     * @return 发送结果的CompletableFuture
     */
    CompletableFuture<SendResult<String, Object>> publishGradeUpdate(GradeUpdateEvent event);

    /**
     * 同步发布成绩更新事件（带回调）
     * 
     * @param event 成绩更新事件
     * @param callback 发送完成后的回调函数
     */
    void publishGradeUpdateAsync(GradeUpdateEvent event, GradeUpdateCallback callback);

    /**
     * 发送回调接口
     */
    interface GradeUpdateCallback {
        /**
         * 发送成功回调
         * 
         * @param event 发送的事件
         * @param result 发送结果
         */
        void onSuccess(GradeUpdateEvent event, SendResult<String, Object> result);

        /**
         * 发送失败回调
         * 
         * @param event 发送的事件
         * @param exception 异常信息
         */
        void onFailure(GradeUpdateEvent event, Throwable exception);
    }
}