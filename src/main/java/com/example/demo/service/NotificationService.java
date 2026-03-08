package com.example.demo.service;

import com.example.demo.common.AppException;
import com.example.demo.entity.NotificationEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.model.NotificationResponse;
import com.example.demo.model.enum_object.ActionType;
import com.example.demo.model.enum_object.TargetNotiType;
import com.example.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void send(UserEntity actor, UserEntity receiver,
                     ActionType actionType, TargetNotiType targetType, String targetId) {
        // Không tự gửi cho chính mình
        if (actor.getId().equals(receiver.getId())) return;

        NotificationEntity notification = new NotificationEntity();
        notification.setUserMakeNotification(actor);
        notification.setUserTakeNotification(receiver);
        notification.setActionType(actionType);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }
    public Slice<NotificationResponse> getNotifications(String userId, Pageable pageable) {
        return notificationRepository
                .findByReceiverId(userId, pageable)
                .map(this::toResponse);
    }

    public void markAsRead(String notificationId, String userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new AppException(HttpStatus.NOT_FOUND, "NOTI_NF_001", "Notification not found");
        }
    }

    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private NotificationResponse toResponse(NotificationEntity entity) {
        UserEntity actor = entity.getUserMakeNotification();

        return NotificationResponse.builder()
                .id(entity.getId())
                .actionType(entity.getActionType())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .actor(NotificationResponse.ActorDto.builder()
                        .id(actor.getId())
                        .name(actor.getName())
                        .imageUrl(actor.getImageUrl())
                        .build())
                .build();
    }
}
