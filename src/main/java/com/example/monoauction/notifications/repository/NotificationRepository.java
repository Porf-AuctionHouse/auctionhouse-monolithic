package com.example.monoauction.notifications.repository;

import com.example.monoauction.common.enums.NotificationType;
import com.example.monoauction.notifications.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    Long countByUserIdAndIsReadFalse(Long userId);

    List<Notification> findByUserIdAndType(Long userId, NotificationType type);

}
