package com.sims.repository;

import com.sims.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadOrderByCreateTimeDesc(Long userId, Boolean isRead, Pageable pageable);

    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    boolean existsByEventId(String eventId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllReadByUserId(@Param("userId") Long userId);
}
