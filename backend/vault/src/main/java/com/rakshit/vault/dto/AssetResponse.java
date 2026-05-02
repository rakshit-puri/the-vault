package com.rakshit.vault.dto;

import com.rakshit.vault.model.Asset;
import com.rakshit.vault.model.AssetSubType;
import com.rakshit.vault.model.AssetType;
import java.time.Instant;
import java.util.Map;

public record AssetResponse(
        String id,
        String ownerUserId,
        AssetType assetType,
        AssetSubType subType,
        String title,
        String description,
        Map<String, Object> data,
        int schemaVersion,
        Instant createdAt,
        Instant updatedAt) {

    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getOwnerUserId(),
                asset.getAssetType(),
                asset.getSubType(),
                asset.getTitle(),
                asset.getDescription(),
                asset.getData(),
                asset.getSchemaVersion(),
                asset.getCreatedAt(),
                asset.getUpdatedAt());
    }
}
