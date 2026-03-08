package com.example.demo.repository;

import com.example.demo.entity.NotificationEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    @Query("""
        SELECT n FROM NotificationEntity n
        JOIN FETCH n.userMakeNotification
        WHERE n.userMakeNotification.id = :userId
        ORDER BY n.createdAt DESC
    """)
    Slice<NotificationEntity> findByReceiverId(@Param("userId") String userId, Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.id = :id AND n.userTakeNotification.id = :userId")
    int markAsRead(@Param("id") String id, @Param("userId") String userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.userTakeNotification.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") String userId);
}
