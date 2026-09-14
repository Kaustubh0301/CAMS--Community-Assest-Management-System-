package com.cams.backend.asset;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface AssetRepository extends JpaRepository<Asset, UUID> {
}
