package com.rakshit.vault.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Document(collection = "assets")
public class Asset {
    @Id
    private String id;

    @Indexed
    private String ownerUserId;

    @Indexed
    private AssetType assetType;

    private AssetSubType subType;
    private String title;
    private String description;
    @Builder.Default
    private Map<String, Object> data = new LinkedHashMap<>();

    @Builder.Default
    private int schemaVersion = 1;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public void setData(Map<String, Object> data) {
        this.data = data == null ? new LinkedHashMap<>() : new LinkedHashMap<>(data);
    }
}
