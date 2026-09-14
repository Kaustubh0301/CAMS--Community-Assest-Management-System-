package com.cams.backend.assetcategory;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface AssetCategoryRepository extends JpaRepository<AssetCategory, UUID> {
}
