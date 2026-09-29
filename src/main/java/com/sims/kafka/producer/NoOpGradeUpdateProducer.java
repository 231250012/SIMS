package com.sims.kafka.producer;

import com.sims.kafka.dto.GradeUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 本地无 Kafka 时的空实现，保证业务服务可正常注入。
 */
@Service
@Profile("local")
public class NoOpGradeUpdateProducer implements GradeUpdateProducer {

    private static final Logger logger = LoggerFactory.getLogger(NoOpGradeUpdateProducer.class);

    @Override
    public CompletableFuture<SendResult<String, Object>> publishGradeUpdate(GradeUpdateEvent event) {
        logger.debug("Skip Kafka publish (local profile): studentId={}, subject={}",
                event != null ? event.getStudentId() : null,
                event != null ? event.getSubject() : null);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void publishGradeUpdateAsync(GradeUpdateEvent event, GradeUpdateCallback callback) {
        logger.debug("Skip Kafka async publish (local profile)");
        if (callback != null) {
            callback.onSuccess(event, null);
        }
    }
}
