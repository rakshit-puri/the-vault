package com.rakshit.vault.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "documents")
public class VaultDocument {
    @Id
    private String id;

    @Indexed
    private String ownerUserId;

    @Indexed
    private String linkedAssetId;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private String bucket;
    private String objectKey;
    private String objectUrl;
    private String checksum;
    private DocumentStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
