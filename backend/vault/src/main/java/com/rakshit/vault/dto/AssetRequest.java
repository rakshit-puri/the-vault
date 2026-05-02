package com.rakshit.vault.dto;

import com.rakshit.vault.model.AssetSubType;
import com.rakshit.vault.model.AssetType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

public record AssetRequest(
        @NotBlank String ownerUserId,
        @NotNull AssetType assetType,
        AssetSubType subType,
        @NotBlank String title,
        String description,
        Map<String, Object> data) {

    public Map<String, Object> safeData() {
        return data == null ? new LinkedHashMap<>() : data;
    }

    @AssertTrue(message = "subType must be valid for assetType")
    public boolean isSubTypeValidForAssetType() {
        return subType == null || assetType == null || subType.isValidFor(assetType);
    }
}
