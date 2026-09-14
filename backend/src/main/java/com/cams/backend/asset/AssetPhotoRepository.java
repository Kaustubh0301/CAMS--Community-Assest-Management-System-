package com.cams.backend.asset;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface AssetPhotoRepository extends JpaRepository<AssetPhoto, UUID> {
}
