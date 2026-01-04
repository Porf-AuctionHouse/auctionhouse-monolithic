package com.example.monoauction.notifications.model;

import com.example.monoauction.common.enums.NotificationType;
import com.example.monoauction.common.enums.RelatedEntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ah_notifications", indexes = {
        @Index(name = "idx_notification_user_read", columnList = "user_id, is_read"),
        @Index(name = "idx_notification_created", columnList = "created_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 50)
    private RelatedEntityType relatedEntityType;

    private Long relatedEntityId;

    @Column(nullable = false)
    private Boolean isRead = false;

    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
