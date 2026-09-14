package com.cams.backend.notification;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
