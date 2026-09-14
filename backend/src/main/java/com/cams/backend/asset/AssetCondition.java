package com.cams.backend.asset;

/**
 * Maps the PostgreSQL enum type {@code asset_condition}: physical state (D24),
 * independent of {@link AssetStatus}.
 */
public enum AssetCondition {
    GOOD,
    FAIR,
    POOR,
    CRITICAL
}
