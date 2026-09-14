package com.cams.backend.maintenance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface MaintenanceHistoryRepository extends JpaRepository<MaintenanceHistory, UUID> {
}
