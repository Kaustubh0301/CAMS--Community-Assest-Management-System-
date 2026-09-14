package com.cams.backend.asset;

/** Maps the PostgreSQL enum type {@code asset_status}: operational state (D8). */
public enum AssetStatus {
    WORKING,
    BROKEN,
    UNDER_MAINTENANCE,
    DECOMMISSIONED
}
