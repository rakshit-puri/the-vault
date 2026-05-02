package com.rakshit.vault.dto;

import com.rakshit.vault.model.DocumentStatus;
import com.rakshit.vault.model.VaultDocument;
import java.time.Instant;

public record DocumentResponse(
        String id,
        String ownerUserId,
        String linkedAssetId,
        String fileName,
        String contentType,
        Long sizeBytes,
        String bucket,
        String objectKey,
        String objectUrl,
        String downloadUrl,
        String checksum,
        DocumentStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static DocumentResponse from(VaultDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getOwnerUserId(),
                document.getLinkedAssetId(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getBucket(),
                document.getObjectKey(),
                document.getObjectUrl(),
                null,
                document.getChecksum(),
                document.getStatus(),
                document.getCreatedAt(),
                document.getUpdatedAt());
    }

    public DocumentResponse withDownloadUrl(String downloadUrl) {
        return new DocumentResponse(
                id,
                ownerUserId,
                linkedAssetId,
                fileName,
                contentType,
                sizeBytes,
                bucket,
                objectKey,
                objectUrl,
                downloadUrl,
                checksum,
                status,
                createdAt,
                updatedAt);
    }
}
