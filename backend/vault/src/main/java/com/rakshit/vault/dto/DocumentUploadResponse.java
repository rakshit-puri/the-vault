package com.rakshit.vault.dto;

import java.time.Instant;

public record DocumentUploadResponse(
        DocumentResponse document,
        String uploadUrl,
        Instant expiresAt) {
}
