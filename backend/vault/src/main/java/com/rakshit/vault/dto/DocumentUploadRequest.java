package com.rakshit.vault.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentUploadRequest(
        @NotBlank String ownerUserId,
        String linkedAssetId,
        @NotBlank String fileName,
        @NotBlank String contentType,
        @NotNull @Min(1) Long sizeBytes) {
}
